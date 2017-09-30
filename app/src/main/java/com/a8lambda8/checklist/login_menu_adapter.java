package com.a8lambda8.checklist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jakob Wasle on 26.09.2017.
 */

public class login_menu_adapter extends ArrayAdapter<login_menu_item>{

    private List<login_menu_item> items;
    private Context context;


    public login_menu_adapter(@NonNull Context context, int resource, List<login_menu_item> items){
        super(context, resource, items);

        this.items = items;
        this.context = context;

    }


    @NonNull
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.login_menu_item, null);
        }

        login_menu_item item = items.get(position);

        ImageView IV = v.findViewById(R.id.login_option_icon);
        TextView TV = v.findViewById(R.id.login_option_text);

        IV.setImageDrawable(item.getLoginMethodeIcon());
        TV.setText(item.getLoginMethode());


        return v;
    }

}
