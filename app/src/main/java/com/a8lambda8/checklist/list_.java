package com.a8lambda8.checklist;

/**
 * Created by jwasl on 23.09.2017.
 */

public class list_ {

    private String listID;
    private String listName;
    private String rights;

    public list_(String listID, String listName, String rights) {
        this.listID = listID;
        this.listName = listName;
        this.rights = rights;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }
}
