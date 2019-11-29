package com.example.knowyourcity;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import org.osmdroid.util.GeoPoint;

public class Korisnik
{
    public String imeIprezime;
    @Exclude
    public String korisnickoIme;
    public String email;
    public String profilePicID;
    public int poeni;
    @Exclude
    public MyCoordinates koo;
    @Exclude
    public Bitmap profiSlikaBit;




    public Korisnik() {}
    public Korisnik(String ime, String kor, String profilePicID, int poen)
    {
        this.imeIprezime = ime;
        this.korisnickoIme = kor;
        this.profilePicID = profilePicID;
        this.poeni =poen;
        koo=new MyCoordinates();

    }

    public Korisnik(String ime, String kor, String profilePicID)
    {
        this(ime,kor, profilePicID,0);
    }

    @Override
    public boolean equals(Object object)
    {
        boolean same = false;

        if(object != null && object instanceof Korisnik)
        {
            same = this.korisnickoIme.equals(((Korisnik) object).korisnickoIme);
        }

        return same;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }


}
