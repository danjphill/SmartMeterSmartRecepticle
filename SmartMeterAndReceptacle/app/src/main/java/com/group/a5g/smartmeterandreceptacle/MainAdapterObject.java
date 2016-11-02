package com.group.a5g.smartmeterandreceptacle;

/**
 * Created by Daniel Phillips on 10/25/2016.
 */
//Object for Main Menu List View
public class MainAdapterObject {
    private String Title;
    private String SubTitle;
    private int Icon;

    MainAdapterObject(String Title, String SubTitle, int Icon){
        this.Title = Title;
        this.SubTitle = SubTitle;
        this.Icon = Icon;
    }

    public int getIcon() {
        return Icon;
    }

    public String getSubTitle() {
        return SubTitle;
    }

    public String getTitle() {
        return Title;
    }
}
