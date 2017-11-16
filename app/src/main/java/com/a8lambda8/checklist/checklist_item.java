package com.a8lambda8.checklist;

import android.graphics.Color;

/**
 * Created by Jakob Wasle on 18.09.2017.
 */

public class checklist_item {

    //private int id = 0;
    private String text = null;
    private long col = Color.WHITE;
    private Boolean checked = null;
    private Boolean reminderSet = null;
    private long reminderTime = 0;


    public checklist_item(String text, long col, Boolean checked, Boolean reminderSet, long reminderTime){

        this.text = text;
        this.col = col;
        this.checked = checked;
        this.reminderSet = reminderSet;
        this.reminderTime = reminderTime;

    }

    /*public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }*/

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCol() {
        return col;
    }

    public void setCol(Long col) {
        this.col = col;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    /*public Boolean getReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(Boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }*/
}
