package com.example.knowyourcity;

import android.graphics.Bitmap;

import org.osmdroid.util.GeoPoint;

import java.util.Comparator;

public class Prijatelj implements Comparable<Prijatelj> {
    private String username;
    private MyCoordinates koo;
    private int poeni;
    //slika
    private String profilePicID;
    private Bitmap profiSlikaBit;

    public Prijatelj() {

    }

    public Prijatelj(String user, MyCoordinates g, int p, String picID) {
        username = user;
        koo = g;
        poeni = p;
        profilePicID = picID;
    }

    public int compareTo(Prijatelj p) {
        //return username.compareTo(p.username);
        return p.poeni - poeni;
    }

    public Bitmap getProfiSlikaBit() {
        return profiSlikaBit;
    }

    public void setProfiSlikaBit(Bitmap profiSlikaBit) {
        this.profiSlikaBit = profiSlikaBit;
    }


    public int getPoeni() {
        return poeni;
    }

    public MyCoordinates getKoo() {
        return koo;
    }

    public void setKoo(MyCoordinates koo) {
        this.koo = koo;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicID() {
        return profilePicID;
    }

    public void setProfilePicID(String profilePicID) {
        this.profilePicID = profilePicID;
    }

    public void setPoeni(int poeni) {
        this.poeni = poeni;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object object) {
        boolean same = false;

        if (object != null && object instanceof Prijatelj) {
            same = this.username.equals(((Prijatelj) object).username);
        }

        return same;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


}