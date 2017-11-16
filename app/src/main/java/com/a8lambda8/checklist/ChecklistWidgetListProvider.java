package com.a8lambda8.checklist;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by Jakob Wasle on 10.10.2017.
 */

public class ChecklistWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {
    private checklist_item_list listItemList = new checklist_item_list();
    private Context context = null;
    private int appWidgetId;

    public ChecklistWidgetListProvider(Context context, Intent intent, checklist_item_list listItemList) {
        this.context = context;
        this.listItemList = listItemList;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return listItemList.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {

        /*final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.checklist_item);
        checklist_item listItem = listItemList.getItem(position);
        remoteView.setTextViewText(R.id.checkBox, listItem.getText());
        remoteView.setViewVisibility(R.id.dotButton,0);*/

        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.checklist_widget);
        checklist_item listItem = listItemList.getItem(position);

        remoteView.setTextViewText(R.id.tv_checklist_widget_name, listItem.getText());

        int resid = android.R.drawable.checkbox_off_background;
        if(listItem.getChecked()) resid = android.R.drawable.checkbox_on_background;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            remoteView.setImageViewResource(R.id.tv_checklist_widget_iv,resid);
        }

        Log.i("ccc","in \"getViewAt\": lst Item:"+listItem.getText());

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }
}
