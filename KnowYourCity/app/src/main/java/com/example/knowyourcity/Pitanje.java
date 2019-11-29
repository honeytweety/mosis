package com.example.knowyourcity;

import com.google.firebase.database.Exclude;

public class Pitanje
{
    private String text;
    private String tacanOdg;
    private String netacanOdg;
    private String netacan2Odg;

    @Exclude
    private String key;

    public Pitanje()
    {
        this.text = "empty";
        this.tacanOdg = "empty";
        this.netacanOdg = "empty";
        this.netacan2Odg = "empty";
    }

    public Pitanje(String text, String tac, String netac, String netac2)
    {
        this.text = text;
        this.tacanOdg = tac;
        this.netacanOdg = netac;
        this.netacan2Odg = netac2;
    }

    public String getText() {
        return text;
    }

    public void setText(String t) {
        this.text = t;
    }

    public void setTacanOdg(String t) {
        this.tacanOdg = t;
    }

    public String getNetacanOdg() {
        return netacanOdg;
    }

    public void setNetacanOdg(String t) {
        this.netacanOdg = t;
    }

    public String getNetacan2Odg() {
        return netacan2Odg;
    }

    public void setNetacan2Odg(String t) {
        this.netacan2Odg = t;
    }

    public String getKey() {
        return key;
    }

    public String getTacanOdg() {
        return tacanOdg;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
