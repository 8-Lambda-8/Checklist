package com.a8lambda8.checklist;

import java.util.List;
import java.util.Vector;

/**
 * Created by Jakob Wasle on 26.09.2017.
 */

public class login_menu_list {

    private int itemcount = 0;
    private List<login_menu_item> itemlist;

    login_menu_list() {
        itemlist = new Vector<login_menu_item>();
    }

    int addItem(login_menu_item item) {
        itemlist.add(item);
        itemcount++;

        return itemcount;
    }

    login_menu_item getItem(int location) {
        return itemlist.get(location);
    }

    List<login_menu_item> getAllItems() {
        return itemlist;
    }

    int getItemCount() {
        return itemcount;
    }

    void clear(){
        itemlist.clear();
        itemcount = 0;
    }

}
