package com.a8lambda8.checklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by Jakob Wasle on 19.09.2017.
 */

public class checklist_item_adapter extends ArrayAdapter<checklist_item> {

    private List<checklist_item> items;
    private Context context;

    SharedPreferences SP;
    SharedPreferences.Editor SPedit;

    DatabaseReference dataRef;


    public checklist_item_adapter(@NonNull Context context, int resource, List<checklist_item> items) {
        super(context, resource, items);

        this.items = items;
        this.context = context;

    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.checklist_item, null);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dataRef = database.getReference();

        SP = PreferenceManager.getDefaultSharedPreferences(context);
        SPedit = SP.edit();
        SPedit.apply();


        final checklist_item item = items.get(position);

        Button dotButton = v.findViewById(R.id.dotButton);
        final CheckBox checkbox = v.findViewById(R.id.checkBox);
        ImageView colView = v.findViewById(R.id.ivCol);


        dotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                view.startAnimation(shake);

                SPedit.putInt("ContextMenuItemId",position);
                SPedit.apply();
                parent.showContextMenuForChild(view);

            }
        });

        checkbox.setText(item.getText());
        checkbox.setChecked(item.getChecked());

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataRef.child("lists").child("testList").child(String.valueOf(position)).child("checked").setValue(checkbox.isChecked());
            }
        });

        colView.setBackgroundColor(((int) item.getCol()));

        //Log.i("xxx",""+Color.BLUE+" "+Color.GREEN+" "+Color.RED);

        return v;

    }



}
