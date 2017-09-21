package com.a8lambda8.checklist;

import android.util.Log;

import java.util.List;
import java.util.Vector;

/**
 * Created by Jakob Wasle on 19.09.2017.
 */

public class checklist_item_list {

    private int itemcount = 0;
    private List<checklist_item> itemlist;

    checklist_item_list() {
        itemlist = new Vector<checklist_item>();
    }

    int addItem(checklist_item item) {
        itemlist.add(item);
        itemcount++;
        
        return itemcount;
    }

    checklist_item getItem(int location) {
        return itemlist.get(location);
    }

    List<checklist_item> getAllItems() {
        return itemlist;
    }

    int getItemCount() {
        return itemcount;
    }

    void clear(){
        itemlist.clear();
        itemcount = 0;
    }

    void remove(int id){
        itemlist.remove(id);
        itemcount --;

        //Log.i("xxx", "del id: "+id+" "+itemlist.get(id).getId());

        String str = "all ids: ";
        for(int i=0;i<itemcount;i++){
            //str=(str+" "+itemlist.get(i).);
        }

    }

}
