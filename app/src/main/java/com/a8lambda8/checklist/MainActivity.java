package com.a8lambda8.checklist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView Checklist;
    TextView TV_SelList;
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

    Animation shake;

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

        shake = AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake);

        selectedList = SP.getString("selectedList","testList");
        Log.i(TAG,"sel list: "+selectedList);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = navigationView.getHeaderView(0);

        final CircleImageView h_usrPic = header.findViewById(R.id.usrPic);
        h_usrPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                h_usrPic.startAnimation(shake);
                LogIn();
            }
        });
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

                    dataRef.child("users").child(user.getUid()).child("email").setValue(encodeAsFirebaseKey(user.getEmail()));

                    dataRef.child("emails").child(encodeAsFirebaseKey(user.getEmail())).setValue(user.getUid());

                    Log.i(TAG,"getPath: "+dataRef.child("emails").child(encodeAsFirebaseKey(user.getEmail())).getParent().getParent().getKey()+"/"+dataRef.child("emails").child(encodeAsFirebaseKey(user.getEmail())).getParent().getKey()+"/"+dataRef.child("emails").child(encodeAsFirebaseKey(user.getEmail())).getKey());

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

                if(Objects.equals(dataSnapshot.getValue().toString(), "deleted")){

                    Toast.makeText(getBaseContext(), "Selected List \""+getListName(selectedList)+"\" was deleted by Owner",
                            Toast.LENGTH_SHORT).show();

                    dataRef.child("users").child(user.getUid()).child("lists").child(selectedList).removeValue();

                    resetList(1);


                    return;

                }


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
        }); //update list count

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

        TV_SelList = (TextView) findViewById(R.id.tv_selList);
        TV_SelList.setPaintFlags(TV_SelList.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

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

        Log.i(TAG,"Clicked item id for ContextMenu:"+v.getId()+"\n"+R.id.dotButton);

        MenuInflater m = getMenuInflater();
        //Log.i(TAG,""+v.getContext()+"\n"+v.getPaddingTop()+"\n"+v.getId());
        //Log.i(TAG,""+R.id.dotButton);
        if(v.getId()==R.id.dotButton+5){
            menu.setHeaderTitle(ItemList.getItem(SP.getInt("ContextMenuItemId",0)).getText()+":");
            m.inflate(R.menu.dotbutton_menu, menu);
        }
        if(v.getId()==R.id.dotButton-4){

            //Log.i(TAG,""+v+"\n"+v.getId());
            menu.setHeaderTitle(ListList.getItem(SP.getInt("ContextMenuListId",0)).getListName()+":");
            m.inflate(R.menu.list_dot_menu,menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final int idInListList = SP.getInt("ContextMenuListId",0);
        final list_ lst = ListList.getItem(idInListList);

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


                Log.i(TAG,"del List: "+lst.getListName()+
                        "\nid: "+getListID(lst.getListName())+
                        "\nrights: "+lst.getRights()+
                        "\nid in LstLst: "+idInListList);

                final AlertDialog.Builder delAlert = new AlertDialog.Builder(this);

                if(Objects.equals(lst.getRights(), "owner")){

                    delAlert.setTitle("Delete List");
                    delAlert.setMessage("Do you really want to DELETE the list \""+lst.getListName()+"\"?");

                    delAlert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dataRef.child("lists").child(lst.getListID()).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    dataRef.child("lists").child(selectedList).removeEventListener(ChecklistValEventListener);

                                    dataRef.child("lists").child(lst.getListID()).removeValue();
                                    dataRef.child("lists").child(lst.getListID()).setValue("deleted");


                                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                                        Log.i(TAG,"key:"+postSnapshot.getKey()+"\nlstId: "+lst.getListID());
                                        dataRef.child("users").child(postSnapshot.getKey()).child("lists").child(lst.getListID()).removeValue();

                                    }

                                    if(Objects.equals(selectedList, lst.getListID())){
                                        if(idInListList==1)resetList(2);
                                        else resetList(1);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });


                }else if(Objects.equals(lst.getRights(), "read")||Objects.equals(lst.getRights(), "write")){

                    delAlert.setTitle("Remove List");
                    delAlert.setMessage("Do you really want to Remove the list \""+lst.getListName()+"\" from your Lists?");

                    delAlert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dataRef.child("users").child(user.getUid()).child("lists").child(lst.getListID()).removeValue();
                            dataRef.child("lists").child(lst.getListID()).child("users").child(user.getUid()).removeValue();
                        }
                    });

                }
                delAlert.setNegativeButton("Cancel",null);
                delAlert.show();

                return true;
            case R.id.list_edit:

                final AlertDialog.Builder editAlert = new AlertDialog.Builder(this);

                final EditText ET_edit_lst = new EditText(this);
                ET_edit_lst.setText(lst.getListName());
                editAlert.setTitle("Edit "+lst.getListName())
                        .setView(ET_edit_lst)
                        .setMessage("New Title for \""+lst.getListName()+"\":")
                        .setNegativeButton("Cancel",null);

                if(Objects.equals(lst.getRights(), "owner")){

                    editAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dataRef.child("lists").child(lst.getListID()).child("name").setValue(ET_edit_lst.getText().toString());
                            dataRef.child("users").child(user.getUid()).child("lists").child(lst.getListID()).child("name").setValue(ET_edit_lst.getText().toString());

                        }
                    });

                }else if(Objects.equals(lst.getRights(), "read")||Objects.equals(lst.getRights(), "write")) {

                    editAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dataRef.child("users").child(user.getUid()).child("lists").child(lst.getListID()).child("name").setValue(ET_edit_lst.getText().toString());

                        }
                    });

                }

                editAlert.show();


                return true;
            case R.id.list_share:

                final AlertDialog.Builder shareAlert = new AlertDialog.Builder(this);

                shareAlert.setTitle("Share List");

                shareAlert.setMessage("Enter the Email address of the person you want to share your List:");

                View alertView = getLayoutInflater().inflate(R.layout.dialog_share_list,null);

                final EditText ET_Email = alertView.findViewById(R.id.etEmail);
                final Spinner SP_Rights = alertView.findViewById(R.id.spRights);

                List<String> items = new ArrayList<>();
                items.add("read");
                items.add("write");

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                SP_Rights.setAdapter(spinnerAdapter);

                shareAlert.setView(alertView);

                shareAlert.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dataRef.child("emails").child(encodeAsFirebaseKey(ET_Email.getText().toString())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue()!=null) {

                                    Log.i(TAG,""+SP_Rights.getSelectedItem());

                                    dataRef.child("lists").child(lst.getListID()).child("users").child((String) dataSnapshot.getValue()).setValue(SP_Rights.getSelectedItem());
                                    dataRef.child("users").child((String) dataSnapshot.getValue()).child("lists").child(lst.getListID()).child("name").setValue(lst.getListName());
                                    dataRef.child("users").child((String) dataSnapshot.getValue()).child("lists").child(lst.getListID()).child("rights").setValue(SP_Rights.getSelectedItem());
                                }else {
                                    Toast.makeText(getBaseContext(), "User not found in Database!", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ET_Email.getText();
                    }
                });

                shareAlert.setNegativeButton("Cancel",null);

                shareAlert.show();

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
            //LogIn();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void LogIn(){

        if(user!=null){

            Log.i(TAG, String.valueOf(user.getProviderId()));
            if (Objects.equals(String.valueOf(user.getProviders()), "[google.com]")){
                startActivity(new Intent(getBaseContext(), GoogleSignInActivity.class));
                return;
            }else if (Objects.equals(String.valueOf(user.getProviders()), "[password]")){
                startActivity(new Intent(getBaseContext(), EmailPasswordActivity.class));
                return;
            }

        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Select Login Method:\n");

        login_menu_list lst = new login_menu_list();
        lst.clear();

        lst.addItem(new login_menu_item("Google",getResources().getDrawable(R.drawable.googleg_standard_color_18)));
        lst.addItem(new login_menu_item("Email",getResources().getDrawable(android.R.drawable.ic_dialog_email)));

        ListView LV = new ListView(getBaseContext());

        login_menu_adapter ad = new login_menu_adapter(getBaseContext(),LV.getId(),lst.getAllItems());
        LV.setAdapter(ad);
        ad.notifyDataSetInvalidated();

        alert.setView(LV);
        final AlertDialog dialog = alert.show();

        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.i(TAG,"id of item:"+i);
                if (i==0){//Google

                    startActivity(new Intent(getBaseContext(), GoogleSignInActivity.class));

                }else if (i==1){ //Email

                    startActivity(new Intent(getBaseContext(), EmailPasswordActivity.class));

                }
                dialog.cancel();

            }
        });

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

        int id = item.getItemId();

        if (id==0) {


            selectedListLast = selectedList;
            selectedList = getListID(item.getTitle().toString());
            SPedit.putString("selectedList", selectedList);
            SPedit.apply();
            TV_SelList.setText(getListName(selectedList)+":");

            dataRef.child("lists").child(selectedListLast).removeEventListener(ChecklistValEventListener);
            dataRef.child("lists").child(selectedList).addValueEventListener(ChecklistValEventListener);


            Log.i(TAG,"Switeched To"+"\nTitle:  "+item.getTitle()+"\n"+getListName(selectedList));

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
        List<Integer> IntegerArray = new ArrayList<>();
        for (int anIntArray : intArray) {
            IntegerArray.add(anIntArray); // returns Integer value
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
                    if(ItemList.getItemCount()!=0&&edit) checked = ItemList.getItem(id).getChecked();

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
                int i = 0;
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

                    final int finalI = i;
                    item.getActionView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            view.startAnimation(shake);
                            SPedit.putInt("ContextMenuListId", finalI);
                            SPedit.apply();

                            navigationView.showContextMenuForChild(view);

                        }
                    });

                    //navigationView.setCheckedItem(item.getItemId());

                    ListList.addItem(new list_(""+postSnapshot.getKey(),""+postSnapshot.child("name").getValue(),""+postSnapshot.child("rights").getValue()));

                    i++;
                }

                TV_SelList.setText(getListName(selectedList)+":");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Nullable
    private String getListID(String item){

        for (int i =0;i<ListList.getItemCount();i++) {
            if (Objects.equals(ListList.getItem(i).getListName(), item)){
                //Log.i(TAG,"loop checker "+":"+ListList.getItem(i).getListName()+" == "+item);
                return ListList.getItem(i).getListID();

            }
        }
        return null;
    }

    private String getListName(String id) {
        for (int i =0;i<ListList.getItemCount();i++) {
            if (Objects.equals(ListList.getItem(i).getListID(), id)){
                //Log.i(TAG,"loop checker "+":"+ListList.getItem(i).getListName()+" == "+item);
                return ListList.getItem(i).getListName();

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
                //newList.put("users",new ArrayMap<>().put(user.getUid(),"owner"));

                Map newUserListEntry = new ArrayMap();
                newUserListEntry.put("name",input.getText().toString());
                newUserListEntry.put("rights","owner");

                //Log.i(TAG,"map: "+newList.toString());

                dataRef.child("users").child(""+user.getUid()).child("lists").child(""+listCount).setValue(newUserListEntry);
                dataRef.child("lists").child(""+listCount).setValue(newList);
                dataRef.child("lists").child(listCount+"/users").child(user.getUid()).setValue("owner");
                dataRef.child("lists").child("listCount").setValue(listCount+1);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private void resetList(int i){
        selectedListLast = selectedList;
        selectedList = ListList.getItem(i).getListID();
        Log.i(TAG,"id: "+ListList.getItem(i).getListID());
        SPedit.putString("selectedList", selectedList);
        SPedit.apply();
        TV_SelList.setText(getListName(selectedList)+":");

        dataRef.child("lists").child(selectedListLast).removeEventListener(ChecklistValEventListener);
        dataRef.child("lists").child(selectedList).addValueEventListener(ChecklistValEventListener);
    }

    String encodeAsFirebaseKey(String string) {
        string = string.replace("%", "%25");
        string = string.replace(".", "%2E");
        string = string.replace("#", "%23");
        string = string.replace("$", "%24");
        string = string.replace("/", "%2F");
        string = string.replace("[", "%5B");
        string = string.replace("]", "%5D");
        return string;
    };

    String decodeFromFirebaseKey(String string) {
        string = string.replace("%25", "%");
        string = string.replace("%2E", ".");
        string = string.replace("%23", "#");
        string = string.replace("%24", "$");
        string = string.replace("%2F", "/");
        string = string.replace("%5B", "[");
        string = string.replace("%5D", "]");
        return string;
    }

}
