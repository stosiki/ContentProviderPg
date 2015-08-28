package study.stosiki.com.contentproviderpg;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by User on 27/08/2015.
 */
public class ChartReportActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_activity);

        TimeSeriesChartDemo01View mView = new TimeSeriesChartDemo01View(this);
        setContentView(mView);

    }
}
