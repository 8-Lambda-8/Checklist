package com.a8lambda8.checklist;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Jakob Wasle on 10.10.2017.
 */

public class ChecklistWidgetService extends RemoteViewsService{

    checklist_item_list listItemList;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);


        String widgetLstID = ChecklistWidgetConfigureActivity.loadPref(getBaseContext(), appWidgetId)[0];

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dataRef = database.getReference();

        ValueEventListener VEL = new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("ccc", "in service: itemcount:" + String.valueOf(dataSnapshot.child("itemCount").getValue()));

                listItemList = new checklist_item_list();

                long itemCount = (long) dataSnapshot.child("itemCount").getValue();

                for(int i = 0;i<itemCount;i++) {

                    checklist_item item = new checklist_item("", 0, false, false, 0);

                    if (dataSnapshot.child(String.valueOf(i)).child("text").getValue() != null)
                        item.setText((String) dataSnapshot.child(String.valueOf(i)).child("text").getValue());
                    if (dataSnapshot.child(String.valueOf(i)).child("checked").getValue() != null)
                        item.setChecked((Boolean) dataSnapshot.child(String.valueOf(i)).child("checked").getValue());
                    if (dataSnapshot.child(String.valueOf(i)).child("col").getValue() != null)
                        item.setCol((Long) dataSnapshot.child(String.valueOf(i)).child("col").getValue());
                    /*if (dataSnapshot.child(String.valueOf(i)).child("remSet").getValue() != null)
                        item.setReminderSet((Boolean) dataSnapshot.child(String.valueOf(i)).child("remSet").getValue());
                    if (dataSnapshot.child(String.valueOf(i)).child("remTime").getValue() != null)
                        item.setReminderTime((Long) dataSnapshot.child(String.valueOf(i)).child("remTime").getValue());*/

                    listItemList.addItem(item);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dataRef.child("lists").child(widgetLstID).removeEventListener(VEL);
        dataRef.child("lists").child(widgetLstID).addValueEventListener(VEL);

        Log.i("ccc","In Service");

        return (new ChecklistWidgetListProvider(this.getApplicationContext(), intent,  listItemList));
    }

}
