package study.stosiki.com.contentproviderpg.color_picker;

/**
 * Created by mike on 9/17/2015.
 */
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import java.util.ArrayList;

/**
 * Created by ferranribell on 19/08/15.
 */
public class ColorPickerAdapter extends BaseAdapter {

    private ArrayList<Integer> mColorPickerArrayList;
    private Context mContext;
    private int mBorderColor;
    private int mBorderColorSelected;
    private int mCircleSize;

    public ColorPickerAdapter(Context context, ArrayList<Integer> colorPickerArrayList) {
        mColorPickerArrayList = colorPickerArrayList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mColorPickerArrayList.size();
    }

    @Override
    public Integer getItem(int position) {
        return mColorPickerArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        int color = mColorPickerArrayList.get(position);
        ColorImageView imageView;
        if (convertView == null) {
            imageView = new ColorImageView(parent.getContext());
            imageView.setLayoutParams(new GridView.LayoutParams(mCircleSize, mCircleSize));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setTag("Color_" + color);
            imageView.setBackgroundColor(color);
            imageView.setBorderColor(mBorderColor);
            imageView.setBorderColorSelected(mBorderColorSelected);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ColorPickerView) parent).onClick(v, parent, position);
                }
            });


        } else {
            imageView = (ColorImageView) convertView;
        }
        return imageView;
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
    }

    public void setBorderColorSelected(int color) {
        mBorderColorSelected = color;
    }

    public void setColorArrayList(ArrayList<Integer> colorArrayList) {
        mColorPickerArrayList = colorArrayList;
    }

    public void setCircleSize(int circleSize) {
        mCircleSize = circleSize;
    }

    public int getCircleSize() {
        return mCircleSize;
    }
}