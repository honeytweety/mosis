package com.example.knowyourcity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;


@IgnoreExtraProperties
public class MojObjekat
{
    public String name;
    public String description;
    public String longitude;
    public String latitude;
    public String objectPicID;
    public String kategorija;

    @Exclude
    public List<Pitanje> listaPitanja;

    @Exclude
    public  String key;

    public MojObjekat() {}

    public MojObjekat(String name, String desc, String lon, String lat)
    {
        this.name = name;
        this.description = desc;
        longitude=lon;
        latitude=lat;
        this.objectPicID = "default";
        this.kategorija = "nema kategorije";
    }

    public MojObjekat(String name, String desc, String lon, String lat,String k)
    {
        this.name = name;
        this.description = desc;
        longitude=lon;
        latitude=lat;
        this.objectPicID = "default";
        this.kategorija = k;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
