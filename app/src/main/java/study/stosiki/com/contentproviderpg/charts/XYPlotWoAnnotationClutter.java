package study.stosiki.com.contentproviderpg.charts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.View;

import org.afree.chart.annotations.XYAnnotation;
import org.afree.chart.axis.ValueAxis;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYItemRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.graphics.geom.RectShape;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mike on 9/20/2015.
 */
public class XYPlotWoAnnotationClutter extends XYPlot {
    private ArrayList<MyXYTextAnnotation> skipList;
    RectShape previousBoundingBox;
    public XYPlotWoAnnotationClutter(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis,
                                     XYItemRenderer renderer) {
        super(dataset, domainAxis, rangeAxis, renderer);
        skipList = new ArrayList<>();
    }

    @Override
    public void drawAnnotations(Canvas canvas, RectShape dataArea, PlotRenderingInfo info) {

        // first pass is to sort out annotations whose bounding boxes overlap
        //TODO: not sure it's the best way to solve it because of massive allocations
        Iterator iterator = getAnnotations().iterator();
        Bitmap b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        while (iterator.hasNext()) {
            MyXYTextAnnotation annotation = (MyXYTextAnnotation) iterator.next();
            annotation.draw(c, this, dataArea, getDomainAxis(), getRangeAxis(), 0, info);
            if(previousBoundingBox == null) {
                previousBoundingBox = annotation.getBoundingBox();
            }
            if(previousBoundingBox != annotation.getBoundingBox() && previousBoundingBox.intersects(annotation.getBoundingBox())) {
                skipList.add(annotation);
            }
            previousBoundingBox = annotation.getBoundingBox();
        }

        iterator = getAnnotations().iterator();
        while(iterator.hasNext()) {
            MyXYTextAnnotation annotation = (MyXYTextAnnotation) iterator.next();
            if(skipList.contains(annotation) == false) {
                annotation.draw(canvas, this, dataArea, getDomainAxis(), getRangeAxis(), 0, info);
            }
        }
        skipList.clear();
    }
}
