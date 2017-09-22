package com.a8lambda8.checklist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by jwasl on 22.09.2017.
 */

public class SimpleColorArrayAdapter extends ArrayAdapter<Integer> {
    private List<Integer> colors;
    private Context context;

    public SimpleColorArrayAdapter(Context context,int Resource, List<Integer> colors) {
        super(context, Resource, colors);
        this.colors = colors;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    private View getImageForPosition(int position) {

        Log.i("xxx",""+position);

        Bitmap bmp = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(colors.get(position));

        c.drawRect(0,0,200,150,p);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bmp);
        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return imageView;
    }
}