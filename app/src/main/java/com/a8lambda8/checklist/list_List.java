package com.a8lambda8.checklist;

import java.util.List;
import java.util.Vector;

/**
 * Created by jwasl on 23.09.2017.
 */

public class list_List {

    private int itemcount = 0;
    private List<list_> itemlist;

    list_List() {
        itemlist = new Vector<list_>();
    }

    int addItem(list_ item) {
        itemlist.add(item);
        itemcount++;

        return itemcount;
    }

    list_ getItem(int location) {
        return itemlist.get(location);
    }

    List<list_> getAllItems() {
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
