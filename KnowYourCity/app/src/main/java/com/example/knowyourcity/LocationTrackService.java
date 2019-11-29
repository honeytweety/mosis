package com.example.knowyourcity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class LocationTrackService extends Service
{

    public static final String CHANNEL_ID = "LocationTrackingServiceChannel";
    private static Location userLocation;
    private static final int INTERVAL = 10000;
    private static final int DISTANCE = 0;
    private static final int NOTIFY_DISTANCE = 100;
    private static final int NOTIFICATION_ID = 98765;
    public static boolean isRunning = false;
    private LocationManager locationManager = null;
    private Context context;
    LocationListener GPSListener;
    LocationListener NetworkListener;
    Thread serviceThread;
    NotificationManagerCompat notificationManager;

    private List<String> nearbyFriends;

    public LocationTrackService() {

        isRunning = true;
        GPSListener = new LocationListener(LocationManager.GPS_PROVIDER);
        NetworkListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

        context=this;
        nearbyFriends=new ArrayList<>();

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        try
        {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        catch (Exception e)
        {
            locationManager = null;
        }

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String input;
        if (intent!=null)
            input = intent.getStringExtra("inputExtra");
        else input = "Tracking your location";
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MapaActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Location service")
                .setContentText(input)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID,notification);

        //do stuff on background thread
        serviceThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    Looper.prepare();
                    try
                    {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, NetworkListener);
                    }
                    catch(SecurityException e) {   }
                    catch (Exception e) {  }

                    try
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, GPSListener);
                    }
                    catch(SecurityException e) {  }
                    catch (Exception e) {  }

                    Looper.loop();
                }
                catch (Exception e)
                {     }
            }
        });
        serviceThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        isRunning = false;

        if(locationManager != null)
        {
            locationManager.removeUpdates(GPSListener);
            locationManager.removeUpdates(NetworkListener);
        }

        if(notificationManager!=null)
            notificationManager.cancelAll();
        nearbyFriends.clear();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "Location tracking service channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    private class LocationListener implements android.location.LocationListener
    {
        @Override
        public void onLocationChanged(Location location)
        {
            userLocation = location;
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());

            MojiPodaci.getInstance().updateUserPosition(longitude,latitude);

            List<Prijatelj> friends = MojiPodaci.getInstance().getMyFriends();

            notificationManager = NotificationManagerCompat.from(context);
            for (Prijatelj friend:friends ) {
                if(friend.getKoo() != null)
                {
                    GeoPoint fpt = new GeoPoint(Double.parseDouble(friend.getKoo().latitude),
                            Double.parseDouble(friend.getKoo().longitude));

                    if(point.distanceToAsDouble(fpt) < NOTIFY_DISTANCE)
                    {
                        if(!nearbyFriends.contains(friend.getUsername())) {
                            Intent notificationIntent = new Intent(context, MapaActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setContentTitle("Friend nearby")
                                    .setContentText(friend.getUsername() + " is near you.")
                                    .setSmallIcon(R.drawable.logo)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .build();

                            nearbyFriends.add(friend.getUsername());
                            notificationManager.notify(friend.getUsername(), NOTIFICATION_ID, notification);
                        }
                    }
                    else if(nearbyFriends.contains(friend.getUsername()))
                    {
                        notificationManager.cancel(friend.getUsername(), NOTIFICATION_ID);
                        nearbyFriends.remove(friend.getUsername());
                    }
                }
            }

        }

        public LocationListener(String provider)
        {
            userLocation = new Location(provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }
    }

    public static Location getUserLocation() {
        return userLocation;
    }

    public static GeoPoint getUserGeoPoint() {
        Location location = getUserLocation();
        if(location!=null)
        {
            double lat = location.getLatitude();
            double lng = location.getLongitude() ;
            return new GeoPoint(lat, lng);
        }
        else return null;
    }

}


