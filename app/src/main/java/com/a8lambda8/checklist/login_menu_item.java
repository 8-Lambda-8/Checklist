package com.a8lambda8.checklist;

import android.graphics.drawable.Drawable;

/**
 * Created by Jakob Wasle on 26.09.2017.
 */

public class login_menu_item {

    private String LoginMethode;
    private Drawable LoginMethodeIcon;


    public login_menu_item(String loginMethode, Drawable loginMethodeIcon) {
        LoginMethode = loginMethode;
        LoginMethodeIcon = loginMethodeIcon;
    }

    public String getLoginMethode() {
        return LoginMethode;
    }

    public void setLoginMethode(String loginMethode) {
        LoginMethode = loginMethode;
    }

    public Drawable getLoginMethodeIcon() {
        return LoginMethodeIcon;
    }

    public void setLoginMethodeIcon(Drawable loginMethodeIcon) {
        LoginMethodeIcon = loginMethodeIcon;
    }

}
