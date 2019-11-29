package com.example.knowyourcity;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class MyCoordinates implements Serializable
{
    public String latitude;
    public String longitude;

    public MyCoordinates()
    {

    }

    @NonNull
    @Override
    public String toString() {
       return latitude + " " + longitude;
    }
}
