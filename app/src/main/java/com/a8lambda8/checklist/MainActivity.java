package com.a8lambda8.checklist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView Checklist;
    checklist_item_list ItemList;
    checklist_item_adapter listAdapter;

    View header;
    NavigationView navigationView;

    SharedPreferences SP;
    SharedPreferences.Editor SPedit;

    long itemCount = 0;
    String selectedList = "testList";
    String selectedListLast;
    long listCount;
    list_List ListList;

    DatabaseReference dataRef;
    ValueEventListener ChecklistValEventListener;

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

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SPedit = SP.edit();
        SPedit.apply();

        selectedList = SP.getString("selectedList","testList");
        Log.i(TAG,"sel list: "+selectedList);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = navigationView.getHeaderView(0);

        final CircleImageView h_usrPic = header.findViewById(R.id.usrPic);
        final TextView h_usrName = header.findViewById(R.id.usrName);
        final TextView h_usrEmail = header.findViewById(R.id.usrEmail);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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

                    initNav();
                    initList();

                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getEmail() +"  "+user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    LogIn();
                }

            }
        };

        ChecklistValEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemList.clear();

                itemCount = (long) dataSnapshot.child("itemCount").getValue();

                for(int i = 0;i<itemCount;i++){

                    checklist_item item = new checklist_item("",0,false,false,0);

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
        };

        fab();

        dataRef.child("lists").child("listCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listCount = (long) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        Checklist = (ListView) findViewById(R.id.checklist);
        ItemList = new checklist_item_list();
        listAdapter = new checklist_item_adapter(getApplicationContext(),R.id.checklist, ItemList.getAllItems());
        Checklist.setAdapter(listAdapter);

        dataRef.child("lists").child(selectedList).addValueEventListener(ChecklistValEventListener);

        registerForContextMenu(Checklist);
        registerForContextMenu(navigationView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater m = getMenuInflater();
        //Log.i(TAG,""+v.getContext()+"\n"+v.getPaddingTop()+"\n"+v.getId());
        //Log.i(TAG,""+R.id.dotButton);
        if(v.getId()==R.id.dotButton+4) m.inflate(R.menu.dotbutton_menu, menu);
        if(v.getId()==R.id.dotButton-4) m.inflate(R.menu.list_dot_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            ///items:
            case R.id.item_delete:

                for(int i = SP.getInt("ContextMenuItemId",0);i<itemCount-1;i++){

                    checklist_item lstItem = ItemList.getItem(i+1);

                    ItemSetter(selectedList,String.valueOf(i),lstItem);

                }

                dataRef.child("lists").child(selectedList).child(String.valueOf(itemCount-1)).removeValue();
                dataRef.child("lists").child(selectedList).child("itemCount").setValue(itemCount-1);

                return true;

            case R.id.item_edit:

                ItemEdit(true,SP.getInt("ContextMenuItemId",0));

                return true;

            case R.id.item_setReminder:

                return true;

            ////Lists:
            case R.id.list_delete:

                Log.i(TAG,"del List");

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

    private void fab(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ItemEdit(false,0);


                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Log.i(TAG,"groudId:"+item.getGroupId()+"\nItem id:"+item.getItemId()+"\nTitle:  "+item.getTitle());
        int id = item.getItemId();
        if (id==0) {

            selectedListLast = selectedList;
            selectedList = getListID(item);
            SPedit.putString("selectedList", selectedList);
            SPedit.apply();

            dataRef.child("lists").child(selectedListLast).removeEventListener(ChecklistValEventListener);
            dataRef.child("lists").child(selectedList).addValueEventListener(ChecklistValEventListener);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }else if (id==R.id.nav_addList){

            addList();

        }

        return true;
    }

    private void ItemSetter(String list, String itemNr, checklist_item item){

        dataRef.child("lists").child(list).child(itemNr).child("text")      .setValue(item.getText());
        dataRef.child("lists").child(list).child(itemNr).child("checked")   .setValue(item.getChecked());
        dataRef.child("lists").child(list).child(itemNr).child("col")       .setValue(item.getCol());

        dataRef.child("lists").child(list).child(itemNr).child("remSet")    .setValue(item.getReminderSet());
        dataRef.child("lists").child(list).child(itemNr).child("remTime")   .setValue(item.getReminderTime());

    }

    private void ItemEdit(final Boolean edit, final int id){

        Context context = this;

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle("Add Task:");

        View alertView = getLayoutInflater().inflate(R.layout.dialog_additem,null);

        final EditText ET_Task = alertView.findViewById(R.id.etTask);
        final Spinner SP_Col = alertView.findViewById(R.id.spCol);

        final int[] intArray = getResources().getIntArray(R.array.selColors);
        List<Integer> IntegerArray = new ArrayList<Integer>();

        for(int i = 0; i < intArray.length; i++) {
            IntegerArray.add(Integer.valueOf(intArray[i])); // returns Integer value
        }

        SimpleColorArrayAdapter adapter = new SimpleColorArrayAdapter(alert.getContext(),R.id.spCol, IntegerArray);

        SP_Col.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(edit){
            alert.setTitle("Edit Task:");
            ET_Task.setText(ItemList.getItem(id).getText());
            SP_Col.setSelection(IntegerArray.indexOf((int) (long) (ItemList.getItem(id).getCol())));
        }


        alert.setView(alertView);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if(!Objects.equals(ET_Task.getText().toString(), "")) {

                    Boolean checked = false;
                    if(ItemList.getItemCount()!=0) checked = ItemList.getItem(id).getChecked();

                    checklist_item item = new checklist_item(ET_Task.getText().toString(), intArray[SP_Col.getSelectedItemPosition()], checked, false, 0);

                    if(edit){
                        ItemSetter(selectedList,String.valueOf(id),item);
                    }else {

                        ItemSetter(selectedList, String.valueOf(itemCount), item);
                        dataRef.child("lists").child(selectedList).child("itemCount").setValue(itemCount + 1);
                    }

                }else Toast.makeText(getBaseContext(), "Task needs a name!", Toast.LENGTH_SHORT).show();

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();

    }

    private void initNav(){

        dataRef.child("users").child(user.getUid()).child("lists").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.i(TAG,"data snapshot:"+dataSnapshot);

                Menu m = navigationView.getMenu();
                SubMenu listMenuGroup = m.getItem(0).getSubMenu();
                listMenuGroup.clear();
                m.setGroupCheckable(0,true,true);
                ListList = new list_List();

                for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                    Button B = new Button(getBaseContext());
                    Drawable draw = getResources().getDrawable(R.drawable.ic_menu_moreoverflow_normal_holo_light);
                    B.setBackground(draw);
                    B.setWidth(B.getHeight());
                    B.setPadding(0,0,0,0);
                    B.setLayoutParams(new ViewGroup.LayoutParams(120,120));

                    MenuItem item = listMenuGroup.add(""+postSnapshot.child("name").getValue())
                            .setCheckable(true)
                            //.setChecked(Objects.equals(selectedList, postSnapshot.getKey()))
                            .setActionView(B);

                    item.getActionView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Animation shake = AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake);
                            view.startAnimation(shake);

                            navigationView.showContextMenuForChild(view);

                        }
                    });

                    //navigationView.setCheckedItem(item.getItemId());

                    ListList.addItem(new list_(""+postSnapshot.getKey(),""+postSnapshot.child("name").getValue(),""+postSnapshot.child("rights").getValue()));

                    if(Objects.equals(postSnapshot.getKey(), selectedList)){
                        //listMenuGroup.set
                        Log.i(TAG,"item id: "+item.getItemId()+"\nsel: "+selectedList+"   :"+item.getTitle());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    private String getListID(MenuItem item){
        for (int i =0;i<ListList.getItemCount();i++) {
            if (Objects.equals(ListList.getItem(i).getListName(), item.getTitle().toString())){
                //Log.i(TAG,"loop checker "+":"+ListList.getItem(i).getListName()+" == "+item);
                return ListList.getItem(i).getListID();

            }
        }
        return null;
    }

    private void addList(){

        Context context = this;

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle("Add List:");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


                Map newList = new ArrayMap();

                newList.put("itemCount",0);
                newList.put("name",input.getText().toString());
                newList.put("owner",user.getUid());

                Map newUserListEntry = new ArrayMap();
                newUserListEntry.put("name",input.getText().toString());
                newUserListEntry.put("rights","owner");

                //Log.i(TAG,"map: "+newList.toString());

                dataRef.child("users").child(""+user.getUid()).child("lists").child(""+listCount).setValue(newUserListEntry);
                dataRef.child("lists").child(""+listCount).setValue(newList);
                dataRef.child("lists").child("listCount").setValue(listCount+1);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }
}
