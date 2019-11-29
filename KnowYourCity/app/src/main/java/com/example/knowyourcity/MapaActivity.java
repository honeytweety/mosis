package com.example.knowyourcity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.ObservableList;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MapaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    MapView map = null;
    IMapController mapController = null;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    static final float RADIUS = 100;
    static final int KILOMETER = 1000;
    MyLocationNewOverlay myLocationOverlay;
    ItemizedIconOverlay myPlacesOverlay;
    ItemizedIconOverlay myFriendsOverlay;
    CirclePlottingOverlay circle;
    ImageView slikaProfila;
    SharedPreferences preferences;

    static int EDIT_PLACE = 2;
    DatabaseReference databaseREF;

    Context context;

    private boolean selCoorsEnabled = false;

    public static final int SELECT_COORDINATES = 2;

    private int state = 0;
    private boolean bshowInRadius; //false=show all, true = show in 1km

    LocationManager locationManager;
    LocationListener myLocListener;
    Location lastLocation;

    private String filter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context=this;

        filter=null;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hview = navigationView.getHeaderView(0);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setColorFilter(Color.WHITE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(context, PretragaPoStringuActivity.class);
                startActivity(i);

            }
        });

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView)
            {
                slikaProfila = (ImageView) hview.findViewById(R.id.navMyPictureImg);

                if(MojiPodaci.getInstance().getThisUser() != null)
                {
                    TextView ime = (TextView) hview.findViewById(R.id.navDisplayName);
                    ime.setText(MojiPodaci.getInstance().getThisUser().imeIprezime);
                    TextView poeni = (TextView) hview.findViewById(R.id.navDisplayScore);
                    poeni.setText(String.valueOf(MojiPodaci.getInstance().getThisUser().poeni));

                    if(MojiPodaci.getInstance().getThisUser().profiSlikaBit!=null)
                        slikaProfila.setImageBitmap(MojiPodaci.getInstance().getThisUser().profiSlikaBit);
                    else
                        getProfilePicture();
                }
                else //thisUser==null
                {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(firebaseUser == null) //no current user, error on login
                    {
                        MojiPodaci.getInstance().setThisUser(null);
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(context,PrijavaActivity.class);
                        startActivity(i);
                    }
                    else //has current user, set thisUser
                    {
                        MojiPodaci.getInstance().thisUsername=null;
                        MojiPodaci.getInstance().setUserListeners(firebaseUser.getDisplayName());

                        Korisnik test = MojiPodaci.getInstance().getThisUser();

                        if (MojiPodaci.getInstance().getThisUser() != null)
                        {
                            TextView ime = (TextView) hview.findViewById(R.id.navDisplayName);
                            ime.setText(MojiPodaci.getInstance().getThisUser().imeIprezime);
                            TextView poeni = (TextView) hview.findViewById(R.id.navDisplayScore);
                            poeni.setText(String.valueOf(MojiPodaci.getInstance().getThisUser().poeni));

                            if(MojiPodaci.getInstance().getThisUser().profiSlikaBit!=null)
                                slikaProfila.setImageBitmap(MojiPodaci.getInstance().getThisUser().profiSlikaBit);
                            else
                                getProfilePicture();
                        }
                        else //no current user, error on login
                        {
                            Toast.makeText(context,"Current user not found, please wait!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        databaseREF = FirebaseDatabase.getInstance().getReference();


        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) findViewById(R.id.map);

        try
        {
            locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        }
        catch (Exception e)
        {
            locationManager = null;
        }

        mapController = map.getController();
        //podesavanje pocetnih koordinata za mapu

        if(mapController != null)
        {
            mapController.setZoom(18.0);
            GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
            mapController.setCenter(startPoint);
        }

        //dozvola za pristupanje lokaciji korisnika
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else
        {
            setMyLocationOverlay();
            if(state == SELECT_COORDINATES)
                setOnMapClickOverlay();
        }



        myLocListener =  new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                lastLocation=location;
                if(bshowInRadius)
                    showMyPlaces();
                drawCircle(point);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if(locationManager!=null)
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, myLocListener);
            }
            catch (SecurityException e) {}

        }

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // String key = getString(R.string.preferences_share_location);
        boolean bshareLocation = preferences.getBoolean(getString(R.string.preferences_share_location),false);

        //  String show_key = getString(R.string.preferences_show_friends);
        boolean bshowFriends = preferences.getBoolean(getString(R.string.preferences_show_friends),false);

        Switch locationSwitch = (Switch) findViewById(R.id.share_location_switch);
        locationSwitch.setChecked(bshareLocation);

        Switch friendsSwitch = (Switch) findViewById(R.id.prikaziprijatelje_switch);
        friendsSwitch.setChecked(bshowFriends);

        Switch radiusSwitch = (Switch) findViewById(R.id.pretraga_radijus);
        bshowInRadius = false;

        if(bshareLocation)
        {
            Intent i = new Intent(context, LocationTrackService.class);
            ContextCompat.startForegroundService(context,i);
            Toast.makeText(context, "Tracking location is enabled.", Toast.LENGTH_SHORT).show();
        }

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(getString(R.string.preferences_share_location),isChecked);
                editor.apply();

                Intent i = new Intent(context, LocationTrackService.class);
                if(isChecked)
                {
                    ContextCompat.startForegroundService(context,i);
                    Toast.makeText(context, "Tracking location is now enabled.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    stopService(i);
                    Toast.makeText(context, "Tracking location is now disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(bshowFriends)
        {
            showMyFriends();
        }

        friendsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(getString(R.string.preferences_show_friends),isChecked);
                editor.apply();
                if(isChecked)
                    showMyFriends();
                else
                {
                    if(myFriendsOverlay !=  null)
                    {
                        map.getOverlays().remove(myFriendsOverlay);
                    }
                }
            }
        });

        radiusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                bshowInRadius = isChecked;
                showMyPlaces();
            }
        });


        showMyPlaces();

        MojiPodaci.getInstance().addOnChangeListener(new ObservableList.OnListChangedCallback() {
            @Override
            public void onChanged(ObservableList sender)
            {
                //Toast.makeText(context,"on changed friends", Toast.LENGTH_LONG).show();
                showMyFriends();
            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount)
            {
                // Toast.makeText(context,"on item range changed friends", Toast.LENGTH_LONG).show();
                showMyFriends();
            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount)
            {
                // Toast.makeText(context,"on item range inserted friends", Toast.LENGTH_LONG).show();
                showMyFriends();
            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount)
            {
                //  Toast.makeText(context,"on item range moved friends", Toast.LENGTH_LONG).show();
                showMyFriends();
            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount)
            {
                // Toast.makeText(context,"on item range removed friends", Toast.LENGTH_LONG).show();
                showMyFriends();

            }
        });

        map.setMultiTouchControls(true);

    }

    private void getProfilePicture()
    {
        Korisnik k = MojiPodaci.getInstance().getThisUser();
        if(k!=null)
        {
            String link = k.profilePicID;
            Picasso.get().load(link).fit().into(slikaProfila, new Callback() {
                @Override
                public void onSuccess()
                {
                    Korisnik korisnik = MojiPodaci.getInstance().getThisUser();
                    if(korisnik!=null)
                    {
                        Drawable drawable = slikaProfila.getDrawable();
                        if (drawable != null && drawable instanceof BitmapDrawable)
                        {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            korisnik.profiSlikaBit = bitmap;
                        }
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        }
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.spomeniciFil:
                filter = "Spomenik";
                break;
            case R.id.crkveFil:
                filter = "Crkva";
                break;
            case R.id.izlatistaFil:
                filter = "Izletište";
                break;
            case R.id.festivaliFil:
                filter = "Festival";
                break;
            case R.id.cesmeFil:
                filter = "Česma";
                break;
            case R.id.ostaloFil:
                filter = "Ostalo";
                break;
            case R.id.sveFill:
                filter = null;
                break;
        }
        showMyPlaces();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.pronadji_prijatelje)
        {
            Intent i = new Intent(this, PretragaPrijateljaActivity.class);
            startActivity(i);
        }
        else if (id == R.id.rang_lista)
        {
            Intent i = new Intent(this, RangListaActivity.class);
            startActivity(i);
        }
        else if (id == R.id.dodaj_novi_objekat)
        {
            state = SELECT_COORDINATES;
            selCoorsEnabled = true;
            setOnMapClickOverlay();
            Snackbar.make(findViewById(R.id.mapConstr),"Long click inside the blue circle to add new object at that location.", Snackbar.LENGTH_LONG).show();
            //Intent i = new Intent(this, DodajNoviObjekatActivity.class);
            //startActivity(i);
        }
        else if (id == R.id.izmeni_profil)
        {
            startActivity(new Intent(this,IzmeniProfilActivity.class));
        }
        else if (id == R.id.odjavi_se)
        {

            MojiPodaci.getInstance().setThisUser(null);
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this,PrijavaActivity.class);
            startActivity(i);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMyLocationOverlay()
    {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(false);
        map.getOverlays().add(this.myLocationOverlay);
        mapController = map.getController();
        if(mapController != null)
        {
            mapController.setZoom(18.0);
            myLocationOverlay.enableFollowLocation();

            GeoPoint location = LocationTrackService.getUserGeoPoint();

            if(location != null)
            {
                mapController.setCenter(location);
                drawCircle();
            }
            else  try
            {
                Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if(loc!=null)
                {
                    lastLocation=loc;
                    location = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    mapController.setCenter(location);
                    drawCircle(location);
                }

            }
            catch (SecurityException e) {          }


            MojiPodaci.getInstance().setMyListener(this::drawCircle);

        }
    }

    private void setOnMapClickOverlay()
    {
        MapEventsReceiver mReceive = new MapEventsReceiver()
        {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p)
            {
                //Toast.makeText(context,"Map clicked", Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p)
            {
                //Toast.makeText(context,"Map long-clicked", Toast.LENGTH_LONG).show();
                if(circle!=null) {
                    if (circle.isInside(p))
                    {
                        if(state == SELECT_COORDINATES && selCoorsEnabled)
                        {
                            if(locationManager!=null)
                                locationManager.removeUpdates(myLocListener);
                            String lon = Double.toString(p.getLongitude());
                            String lat = Double.toString(p.getLatitude());
                            Intent locationIntent = new Intent(context, DodajNoviObjekatActivity.class);
                            locationIntent.putExtra("lon", lon);
                            locationIntent.putExtra("lat", lat);
                            startActivity(locationIntent);
                            //  setResult(Activity.RESULT_OK, locationIntent);
                            finish();
                        }
                    }
                }
                return false;
            }
        };

        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);
        if(locationManager!=null)
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, myLocListener);
            }
            catch (SecurityException e) {}
        }
    }

    public void onResume()
    {
        super.onResume();
        map.onResume();
        MojiPodaci.getInstance().setMyListener(this::drawCircle);
        if(locationManager!=null)
        {
            try {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, myLocListener);
            }
            catch (SecurityException e) {}
        }

    }

    public void onPause()
    {
        super.onPause();
        map.onPause();
        MojiPodaci.getInstance().setMyListener(null);
        if(locationManager!=null)
            locationManager.removeUpdates(myLocListener);
        if(circle!=null)
            map.getOverlays().remove(circle.p);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_ACCESS_FINE_LOCATION:
            {
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, myLocListener);
                    setMyLocationOverlay();

                    if(state== SELECT_COORDINATES)
                        setOnMapClickOverlay();

                }
                return;
            }
        }
    }

    private void showMyPlaces()
    {
        if(myPlacesOverlay != null)
        {
            this.map.getOverlays().remove(myPlacesOverlay);
        }
        final ArrayList<OverlayItem> items = new ArrayList<>();
        Drawable marker = this.getResources().getDrawable(R.drawable.pin_objekat);

        GeoPoint center = null;
        if(bshowInRadius && lastLocation != null)
            center=new GeoPoint(lastLocation.getLatitude(),lastLocation.getLongitude());

        if(filter==null) {
            for (MojObjekat myPlace : MojiPodaci.getInstance().getMyPlaces()) {
                GeoPoint point = new GeoPoint(Double.parseDouble(myPlace.latitude), Double.parseDouble(myPlace.longitude));
                if(center == null || center.distanceToAsDouble(point) <= KILOMETER) {
                    OverlayItem item = new OverlayItem(myPlace.name, myPlace.key, point);
                    item.setMarker(marker);
                    items.add(item);
                }
            }
        }
        else {
            for (MojObjekat myPlace :  MojiPodaci.getInstance().getMyPlaces()) {
                if(myPlace.kategorija != null) {
                    if (myPlace.kategorija.compareToIgnoreCase(filter) == 0) {
                        GeoPoint point = new GeoPoint(Double.parseDouble(myPlace.latitude), Double.parseDouble(myPlace.longitude));
                        if(center == null || center.distanceToAsDouble(point) <= KILOMETER) {
                            OverlayItem item = new OverlayItem(myPlace.name, myPlace.key, point);
                            item.setMarker(marker);
                            items.add(item);
                        }
                    }
                }
            }
        }
        myPlacesOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
                {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item)
                    {
                        org.osmdroid.api.IGeoPoint point = item.getPoint();
                        if(circle!=null)
                        {
                            if(circle.isInside(point))
                            {
                                int placeIndex = MojiPodaci.getInstance().getPlaceIndex(item.getSnippet());
                                Intent intent = new Intent(MapaActivity.this, PregledObjektaActivity.class);
                                intent.putExtra("position", placeIndex);
                                startActivity(intent);
                                //startActivityForResult(intent, EDIT_PLACE);
                                return true;
                            }
                            else
                                Snackbar.make(findViewById(R.id.drawer_layout),item.getTitle(),Snackbar.LENGTH_SHORT).show();
                        }
                        else
                            Snackbar.make(findViewById(R.id.drawer_layout),item.getTitle(),Snackbar.LENGTH_SHORT).show();
                       // Toast.makeText(context,item.getTitle(), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item)
                    {
                        org.osmdroid.api.IGeoPoint point = item.getPoint();
                        if(circle!=null)
                        {
                            if (circle.isInside(point))
                            {
                                Intent intent = new Intent(MapaActivity.this, DodajNoviObjekatActivity.class);
                                intent.putExtra("position", index);
                                startActivityForResult(intent, EDIT_PLACE);
                                return true;
                            }
                        }
                        return false;
                    }

                } , getApplicationContext());
        this.map.getOverlays().add(myPlacesOverlay);
    }

    private void showMyFriends()
    {
        if(myFriendsOverlay != null)
        {
            this.map.getOverlays().remove(myFriendsOverlay);
        }
        final ArrayList<OverlayItem> items = new ArrayList<>();
        Drawable defaultMarker = this.getResources().getDrawable(R.drawable.osm_ic_follow_me);
        //  for (int i=0; i< MyPlacesData.getInstance().getMyPlaces().size();i++)
        for (Prijatelj prijatelj :  MojiPodaci.getInstance().getMyFriends())
        {
            if(prijatelj !=null &&  prijatelj.getKoo()!=null)
            {
                OverlayItem item = new OverlayItem(prijatelj.getUsername(), "Poeni: " + prijatelj.getPoeni(), new GeoPoint(Double.parseDouble(prijatelj.getKoo().latitude), Double.parseDouble(prijatelj.getKoo().longitude)));
                if(prijatelj.getProfiSlikaBit() !=null)
                {
                    Drawable d = new BitmapDrawable(getResources(),CircleImage.getRoundedCornerBitmap(prijatelj.getProfiSlikaBit(),
                            75));
                    item.setMarker(d);

                }
                else
                    item.setMarker(defaultMarker);
                items.add(item);
            }
        }
        myFriendsOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
                {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item)
                    {
                        // int b = index;
                        Intent intent = new Intent(MapaActivity.this, PregledPrijateljaActivity.class);
                        String s = item.getTitle();

                        intent.putExtra("username", s);
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item)
                    {
                        //Intent intent = new Intent(MapaActivity.this, DodajNoviObjekatActivity.class);
                        //intent.putExtra("position", index);
                        //startActivityForResult(intent, EDIT_PLACE);
                        return false;
                    }

                } , getApplicationContext());
        this.map.getOverlays().add(myFriendsOverlay);
        map.invalidate();
    }

    public void drawCircle()
    {
        GeoPoint location = LocationTrackService.getUserGeoPoint();
        drawCircle(location);

    }
    public void drawCircle(GeoPoint location)
    {
        if(circle!=null)
        {
            // map.getOverlays().remove(circle);
            map.getOverlays().remove(circle.p);
        }
        circle  = new CirclePlottingOverlay(RADIUS,location,map);
        //map.getOverlays().add(0,circle);
    }


}
