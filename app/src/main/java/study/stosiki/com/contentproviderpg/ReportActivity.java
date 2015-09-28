package study.stosiki.com.contentproviderpg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.*;

/**
 * Created by User on 09/08/2015.
 * Given a name of an event line represent in some way its events
 *
 */
public class ReportActivity extends AppCompatActivity
{
    private static final String TAG = ReportActivity.class.getSimpleName();

    private static final CharSequence TITLES[]={"Chart","Event List"};
    private static final int NUM_TABS =2;
    private static final int CHART_TAB = 0;
    private static final int PLAIN_LOG_TAB = 1;

    private static final int MESSAGE_NORMAL = 0;
    private static final int MESSAGE_ERROR = 1;
    private static final int MESSAGE_WARNING = 2;

    private ListView listView;

    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), TITLES, NUM_TABS);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true,
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chart_view_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // try to find out which tab is visible, because action is context dependent
        int currentTabIndex = pager.getCurrentItem();
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
/*
            case R.id.menu_item_save:
                switch (currentTabIndex) {
                    case CHART_TAB:
                        saveChartToFile();
                        break;
                    case PLAIN_LOG_TAB:
                        saveLogToCSV();
                        break;
                }
                break;
*/
            case R.id.menu_item_share:
                switch (currentTabIndex) {
                    case CHART_TAB:
                        startShareImageActivity(saveChartToFile());
                        break;
                    case PLAIN_LOG_TAB:
                        startShareLogActivity(saveLogToCSV());
                        break;
                }
        }
        return true;
    }

    private void startShareLogActivity(File csvFile) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(csvFile));
        startActivity(Intent.createChooser(share, "Share CSV file"));
    }

    private void startShareImageActivity(File imageFile) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private File saveLogToCSV() {
        Fragment listReportFragment = findListReportFragment();
        List<ListReportFragment.EventListEntry> events =
                ((ListReportFragment)listReportFragment).getEventList();
        StringBuilder sb = new StringBuilder("Line Title, Date, Data\n");
        for(ListReportFragment.EventListEntry entry : events) {
            sb.append(entry.toCsvString());
        }
        File outFile = new File(format("%s%s", getCsvDirectory(), getCsvFileName()));
        try {
            FileWriter fw = new FileWriter(outFile);
            fw.append(sb.toString());
            fw.close();
            showMessage(R.string.file_saved_success, MESSAGE_NORMAL);
        } catch (IOException ioe) {
            Log.d(TAG, ioe.getMessage());
            showMessage(R.string.error_writing_to_file, MESSAGE_ERROR);
        }
        return outFile;
    }

    private File saveChartToFile() {
//        Fragment chartReportFragment = adapter.getItem(0);
        Fragment chartReportFragment = findChartFragment();
        View fragmentView = chartReportFragment.getView();
        File outFile = new File(format("%s%s", getChartDirectory(), getChartFileName()));
        saveViewAsImage(fragmentView, outFile);
        showMessage(R.string.file_saved_success, MESSAGE_NORMAL);
        return outFile;
    }

    private void showMessage(int rId, int type) {
        final ViewGroup messageContainer = (ViewGroup)findViewById(R.id.message_view_container);
        TextView messageText = (TextView)messageContainer.findViewById(R.id.message_text);
        messageText.setText(rId);

        switch (type) {
            case MESSAGE_NORMAL:
                break;
            case MESSAGE_ERROR:
                messageText.setBackgroundColor(getResources().getColor(R.color.error_msg_bg));
                messageText.setTextColor(getResources().getColor(R.color.error_msg_text));
                break;
            case MESSAGE_WARNING:
                messageText.setBackgroundColor(getResources().getColor(R.color.warning_msg_bg));
                messageText.setTextColor(getResources().getColor(R.color.warning_msg_text));
                break;
        }

        messageContainer.setVisibility(View.VISIBLE);
        messageContainer.requestLayout();

        final int viewY = findCoords(R.id.message_view_container)[1];

        ObjectAnimator hideMessageAnimator = ObjectAnimator.ofFloat(
                messageContainer, View.Y, viewY, viewY + messageContainer.getHeight());
        hideMessageAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                restoreMessageContainerState();
            }

            private void restoreMessageContainerState() {
                messageContainer.setY(viewY);
                messageContainer.setVisibility(View.INVISIBLE);
            }
        });
        hideMessageAnimator.setStartDelay(2000);
        hideMessageAnimator.start();
    }

    //TODO: this is code duplication with MainActivity, factor it out
    private int[] findCoords(int viewId) {
        View view = findViewById(viewId);
        int[] coords = new int[2];
        if(view != null) {
            coords[0] = (int)(view.getX());
            coords[1] = (int)(view.getY());
        }
        return coords;
    }

    private Fragment findChartFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment f : fragments) {
            if(f instanceof ChartReportFragment) {
                return f;
            }
        }
        return null;
    }

    private Fragment findListReportFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment f : fragments) {
            if(f instanceof ListReportFragment) {
                return f;
            }
        }
        return null;
    }

    private String getChartFileName() {
        return "/" + valueOf(new Date().getTime()) + ".png";
    }

    private String getCsvFileName() {
        return "/" + valueOf(new Date().getTime()) + ".csv";
    }

    private void saveViewAsImage(View v, File outFile) {

        if(isExternalStorageWritable() == false) {
            showMessage(R.string.external_storage_error, MESSAGE_ERROR);
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
            fos = null;
        }
        catch (IOException e) {
            showMessage(R.string.error_writing_to_file, MESSAGE_ERROR);
            e.printStackTrace();
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    showMessage(R.string.error_writing_to_file, MESSAGE_ERROR);
                }
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public File getChartDirectory() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "EventLinesCharts");
        if (file.mkdirs() == false) {
            Log.e(TAG, "Directory was not created");
        }
        return file;
    }

    public File getCsvDirectory() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "EventLinesCharts");
        if (file.mkdirs() == false) {
            Log.e(TAG, "Directory was not created");
        }
        return file;
    }
}
