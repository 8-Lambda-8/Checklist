package com.a8lambda8.checklist;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link ChecklistWidget ChecklistWidget} AppWidget.
 */
public class ChecklistWidgetConfigureActivity extends Activity {


    DatabaseReference dataRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    list_List ListList;

    private static final String PREFS_NAME = "com.a8lambda8.checklist.ChecklistWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner mAppWidgetSpinner;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ChecklistWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetLstID = (String) ListList.getItem(mAppWidgetSpinner.getSelectedItemPosition()).getListID();
            savePref(context, mAppWidgetId, widgetLstID,user.getUid());

            Log.i("yyy","lst id for widget: "+ widgetLstID);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ChecklistWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public ChecklistWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void savePref(Context context, int appWidgetId, String lst,String usr) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId+"_lst", lst);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId+"_usr", usr);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String lstId = prefs.getString(PREF_PREFIX_KEY + appWidgetId+"_lst", null);
        String usr = prefs.getString(PREF_PREFIX_KEY + appWidgetId+"_usr", null);
        if (lstId != null&&usr !=null) {
            return lstId;
        } else {
            return "-";

        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.checklist_widget_configure);
        mAppWidgetSpinner = findViewById(R.id.appwidget_spinner);


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        dataRef = database.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i("yyy","User is signed in");
                } else {
                    // User is signed out
                    Log.i("yyy","User is signed out");
                    /*Log.d(TAG, "onAuthStateChanged: signed_out");
                    LogIn();*/
                }

            }
        };

        ListList = new list_List();

        dataRef.child("users").child(user.getUid()).child("lists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("yyy","in on data getter");


                List<String> items = new ArrayList<>();

                for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                    ListList.addItem(new list_(""+postSnapshot.getKey(),""+postSnapshot.child("name").getValue(),""+postSnapshot.child("rights").getValue()));

                    items.add(ListList.getItem(ListList.getItemCount()-1).getListName());

                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getBaseContext(),android.R.layout.simple_spinner_item, items);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mAppWidgetSpinner.setAdapter(spinnerAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        //mAppWidgetText.setText(loadTitlePref(ChecklistWidgetConfigureActivity.this, mAppWidgetId));
    }
}

