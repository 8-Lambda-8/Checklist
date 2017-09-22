package com.a8lambda8.checklist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView Checklist;
    checklist_item_list ItemList;
    checklist_item_adapter listAdapter;

    View header;

    SharedPreferences SP;
    SharedPreferences.Editor SPedit;



    long itemCount = 0;


    DatabaseReference dataRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    public static final String TAG = "xxx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.setCheckedItem(navItemId);

        header = navigationView.getHeaderView(0);

        final CircleImageView h_usrPic = header.findViewById(R.id.usrPic);
        final TextView h_usrName = header.findViewById(R.id.usrName);
        final TextView h_usrEmail = header.findViewById(R.id.usrEmail);



        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dataRef = database.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Picasso.with(getBaseContext()).load(user.getPhotoUrl()).into(h_usrPic);

                    h_usrName.setText(user.getDisplayName());
                    h_usrEmail.setText(user.getEmail());


                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    LogIn();
                }

            }
        };


        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SPedit = SP.edit();
        SPedit.apply();


        fab(this);

        initList();

        registerForContextMenu(Checklist);

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initList() {

        String selList = "testList";

        Checklist = (ListView) findViewById(R.id.checklist);
        ItemList = new checklist_item_list();
        listAdapter = new checklist_item_adapter(getApplicationContext(),R.id.checklist, ItemList.getAllItems());
        Checklist.setAdapter(listAdapter);

        dataRef.child("lists").child(selList).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemList.clear();


                itemCount = (long) dataSnapshot.child("itemCount").getValue();

                for(int i = 0;i<itemCount;i++){

                    checklist_item item = new checklist_item("",0,false,false,0);

                    //Log.i("xxx","index"+i+":\n"+dataSnapshot.child(String.valueOf(i)).getValue());

                    if(dataSnapshot.child(String.valueOf(i)).child("text").getValue()!=null)
                        item.setText((String) dataSnapshot.child(String.valueOf(i)).child("text").getValue());
                    if(dataSnapshot.child(String.valueOf(i)).child("checked").getValue()!=null)
                        item.setChecked((Boolean) dataSnapshot.child(String.valueOf(i)).child("checked").getValue());
                    if(dataSnapshot.child(String.valueOf(i)).child("col").getValue()!=null)
                        item.setCol((Long) dataSnapshot.child(String.valueOf(i)).child("col").getValue());
                    if(dataSnapshot.child(String.valueOf(i)).child("remSet").getValue()!=null)
                        item.setReminderSet((Boolean) dataSnapshot.child(String.valueOf(i)).child("remSet").getValue());
                    if(dataSnapshot.child(String.valueOf(i)).child("remTime").getValue()!=null)
                        item.setReminderTime((Long) dataSnapshot.child(String.valueOf(i)).child("remTime").getValue());

                    ItemList.addItem(item);

                }

                listAdapter.notifyDataSetInvalidated();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.dotbutton_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:
                //Log.i("xxx",SP.getInt("ContextMenuItemId",0) +" "+ String.valueOf(item.getItemId()));


                for(int i = SP.getInt("ContextMenuItemId",0);i<itemCount-1;i++){

                    checklist_item lstItem = ItemList.getItem(i+1);

                    Log.i(TAG,"for loop :"+i);

                    ItemSetter("testList",String.valueOf(i),lstItem);

                }

                dataRef.child("lists").child("testList").child(String.valueOf(itemCount-1)).removeValue();
                dataRef.child("lists").child("testList").child("itemCount").setValue(itemCount-1);

                return true;

            case R.id.item_changeImportance:
                //Log.i("xxx",SP.getInt("ContextMenuItemId",0) +" "+ String.valueOf(item.getItemId()));
                return true;

            case R.id.item_setReminder:

                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LogIn();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void LogIn(){

        startActivity(new Intent(this, ChooserActivity.class));

    }

    private void fab(final Context context){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setTitle("Add Task:");
                View alertView = getLayoutInflater().inflate(R.layout.dialog_additem,null);

                final EditText ET_Task = alertView.findViewById(R.id.etTask);
                final Spinner SP_Col = alertView.findViewById(R.id.spCol);

                int[] intArray = getResources().getIntArray(R.array.selColors);
                List<Integer> IntegerArray = new ArrayList<Integer>();

                for(int i = 0; i < intArray.length; i++) {
                    IntegerArray.add(Integer.valueOf(intArray[i])); // returns Integer value
                }

                SimpleColorArrayAdapter adapter = new SimpleColorArrayAdapter(alert.getContext(),R.id.spCol, IntegerArray);

                //Log.i(TAG,adapter.);

                SP_Col.setAdapter(adapter);
                adapter.notifyDataSetChanged();


                alert.setView(alertView);
                alert.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        checklist_item item = new checklist_item(ET_Task.getText().toString(),0,false,false,0);

                        ItemSetter("testList",String.valueOf(itemCount),item);

                        dataRef.child("lists").child("testList").child("itemCount").setValue(itemCount+1);

                    }
                });

                alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                alert.show();




                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } /*else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ItemSetter(String list, String itemNr, checklist_item item){

        dataRef.child("lists").child(list).child(itemNr).child("text")      .setValue(item.getText());
        dataRef.child("lists").child(list).child(itemNr).child("checked")   .setValue(item.getChecked());
        dataRef.child("lists").child(list).child(itemNr).child("col")       .setValue(item.getCol());

        dataRef.child("lists").child(list).child(itemNr).child("remSet")    .setValue(item.getReminderSet());
        dataRef.child("lists").child(list).child(itemNr).child("remTime")   .setValue(item.getReminderTime());

    }

}
