package study.stosiki.com.contentproviderpg;

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
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 09/08/2015.
 * Given a name of an event line represent in some way its events
 *
 */
public class ReportActivity extends AppCompatActivity
{
    private static final String TAG = ReportActivity.class.getSimpleName();

    private static final CharSequence TITLES[]={"Home","Events"};
    private static final int NUM_TABS =2;
    private static final int CHART_TAB = 0;
    private static final int PLAIN_LOG_TAB = 1;

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


        // Creating The ViewPagerAdapter and Passing Fragment Manager, TITLES fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), TITLES, NUM_TABS);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
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
            case R.id.menu_item_save:
                switch (currentTabIndex) {
                    case CHART_TAB:
                        saveChartToFile();
                        break;
                    case PLAIN_LOG_TAB:
                        saveLogToFile();
                        break;
                }
                break;
            case R.id.menu_item_share:
                switch (currentTabIndex) {
                    case CHART_TAB:
                        saveChartToFile();
                        startShareImageActivity();
                        break;
                    case PLAIN_LOG_TAB:
                        saveLogToFile();
                        startShareLogActivity();
                        break;
                }
        }
        return true;
    }

    private void startShareLogActivity() {
        //TODO: Implement
        Log.d(TAG, "startShareLogActivity is not yet implemented");
    }

    /**
     * saves the log to CSV file
     */
    private void saveLogToFile() {
        //TODO: Implement it
        Log.d(TAG, "saveLogToFile is not yet implemented");
    }

    private void startShareImageActivity() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(getChartDirectory().toString() + getChartFileName()));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void saveChartToFile() {
        Fragment chartReportFragment = adapter.getItem(0);
//                        getSupportFragmentManager().findFragmentById(R.id.chart_holder);
        View fragmentView = chartReportFragment.getView();
        // TODO: remove hardcoded filename
        File outFile = new File(getChartDirectory() + getChartFileName());
        saveViewAsImage(fragmentView, outFile);

    }

    private String getChartFileName() {
        return "/" + String.valueOf(new Date().getTime()) + ".png";
    }


    private void saveViewAsImage(View v, File outFile) {

        if(isExternalStorageWritable() == false) {
            //TODO: give some error indication
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
                }
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public File getChartDirectory() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "EventLinesCharts");
        if (file.mkdirs() == false) {
            Log.e(TAG, "Directory not created");
        }
        Log.d(TAG, file.toString());
        return file;
    }

}
