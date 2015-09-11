package study.stosiki.com.contentproviderpg;

/**
 * Created by mike on 9/5/2015.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence titles[]; // This will Store the titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private int numbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    private ChartReportFragment chartReportFragment;
    private ListReportFragment listReportFragment;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int numTabs) {
        super(fm);

        this.titles = titles;
        this.numbOfTabs = numTabs;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                if(chartReportFragment == null) {
                    chartReportFragment = new ChartReportFragment();
                }
                return chartReportFragment;
            case 1:
                if(listReportFragment == null) {
                    listReportFragment = new ListReportFragment();
                }
                return listReportFragment;
            default:
                throw new IllegalArgumentException("Tab index out of bounds");
        }
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return numbOfTabs;
    }
}