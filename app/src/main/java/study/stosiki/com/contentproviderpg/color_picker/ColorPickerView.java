package study.stosiki.com.contentproviderpg.color_picker;

/**
 * Created by mike on 9/17/2015.
 */


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import study.stosiki.com.contentproviderpg.R;

import java.util.ArrayList;

/**
 * Created by ferranribell on 19/08/15.
 */
public class ColorPickerView extends GridView {
    /**
     * The M context.
     */
    private final Context mContext;
    /**
     * The M listener.
     */
    private ColorPickerViewListener mListener;
    /**
     * The M color picker adapter.
     */
    private ColorPickerAdapter mColorPickerAdapter;

    /**
     * Instantiates a new Color picker view.
     *
     * @param context the context
     */
    public ColorPickerView(Context context) {
        super(context);
        mContext = context;
        mColorPickerAdapter = new ColorPickerAdapter(mContext, new ArrayList<Integer>());
    }

    /**
     * Instantiates a new Color picker view.
     *
     * @param context             the context
     * @param borderColor         the border color
     * @param borderColorSelected the border color selected
     */
    public ColorPickerView(Context context, int borderColor, int borderColorSelected) {
        super(context);
        mContext = context;
        mColorPickerAdapter = new ColorPickerAdapter(mContext, new ArrayList<Integer>());
        setBorderColor(borderColor);
        setBorderColorSelected(borderColorSelected);
    }

    /**
     * Instantiates a new Color picker view.
     *
     * @param context             the context
     * @param borderColor         the border color
     * @param borderColorSelected the border color selected
     * @param colorArrayList      the color array list
     */
    public ColorPickerView(Context context, int borderColor, int borderColorSelected, ArrayList<Integer> colorArrayList) {
        super(context);
        mContext = context;
        mColorPickerAdapter = new ColorPickerAdapter(mContext, colorArrayList);
        setBorderColor(borderColor);
        setBorderColorSelected(borderColorSelected);
    }

    /**
     * Instantiates a new Color picker view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mColorPickerAdapter = new ColorPickerAdapter(mContext, new ArrayList<Integer>());
        setBorderColor(getResources().getColor(R.color.border));
        setBorderColorSelected(getResources().getColor(R.color.border_selected));
        setCircleSize(getResources().getDimensionPixelSize(R.dimen.circle_size));

        ArrayList<Integer> colorArrayList = new ArrayList<>();
        colorArrayList.add(getResources().getColor(R.color.red_500));
        colorArrayList.add(getResources().getColor(R.color.blue_500));
        colorArrayList.add(getResources().getColor(R.color.brown_500));
        colorArrayList.add(getResources().getColor(R.color.lime_500));
        colorArrayList.add(getResources().getColor(R.color.orange_500));
        colorArrayList.add(getResources().getColor(R.color.teal_500));
        setColorsList(colorArrayList);
    }

    /**
     * Set listener.
     *
     * @param l the l
     */
    public void setListener(ColorPickerViewListener l) {
        mListener = l;
    }

    /**
     * Reset circles.
     *
     * @param parent the parent
     */
    private void resetCircles(ViewGroup parent) {
        int total = getAdapter().getCount();
        for (int i = 0; i < total; i++) {
            int color = (int) getAdapter().getItem(i);
            ImageView imageView = (ImageView) parent.findViewWithTag("Color_" + color);
            imageView.setSelected(false);
        }
    }

    /**
     * On click.
     *
     * @param view            the view
     * @param colorPickerView the color picker view
     * @param position        the position
     */
    public void onClick(View view, ViewGroup colorPickerView, int position) {
        boolean imageStatus = view.isSelected();
        resetCircles(colorPickerView);
        view.setSelected(!imageStatus);
        if (mListener != null) {
            mListener.onColorPickerClick(position);
        }
    }

    /**
     * Sets colors list.
     *
     * @param colorArrayList the color array list
     */
    public void setColorsList(ArrayList<Integer> colorArrayList) {
        mColorPickerAdapter.setColorArrayList(colorArrayList);
        setAdapter(mColorPickerAdapter);
    }

    /**
     * Sets border color.
     *
     * @param color the color
     */
    public void setBorderColor(int color) {
        mColorPickerAdapter.setBorderColor(color);
    }

    /**
     * Sets border color selected.
     *
     * @param color the color
     */
    public void setBorderColorSelected(int color) {
        mColorPickerAdapter.setBorderColorSelected(color);
    }

    public void setCircleSize(int circleSize) {
        mColorPickerAdapter.setCircleSize(circleSize);
    }

    public int getColor(int colorPosition) {
        return mColorPickerAdapter.getItem(colorPosition);
    }
}