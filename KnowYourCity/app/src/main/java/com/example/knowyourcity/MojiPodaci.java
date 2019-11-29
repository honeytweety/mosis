package com.example.knowyourcity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class MojiPodaci
{
    final private ArrayList<MojObjekat> myPlaces;
    final private ArrayList<String> myFriendsId;
    final private ObservableList<Prijatelj> myFriends;
    private Korisnik thisUser;
    public String thisUsername;
    private MyCoordinates koordinate=null;

    private MyListener listener;


    private HashMap<String, Integer> myPlacesKeyIndexMapping;
    private HashMap<String, Integer> friendsKeyIndexMapping;
    private HashMap<Integer,Boolean> friendExistMapping;

    private DatabaseReference database;
    private static final String FIREBASE_CHILD_PLACES = "my-places";
    private static final String FIREBASE_CHILD_USERS = "users";
    private static final String FIREBASE_CHILD_QUESTIONS = "questions";
    public static final String FIREBASE_CHILD_FRIENDS = "friendships";
    public static final String FIREBASE_CHILD_LOCATIONS = "user_locations";

    public static final int MAX_BYTES = 1048576; //1MB

    public int getFriendIndex(String username)
    {
        if(friendsKeyIndexMapping.containsKey(username))
            return friendsKeyIndexMapping.get(username);
        else return -1;
    }

    private MojiPodaci()
    {
        myPlaces = new ArrayList<>();
        myFriendsId=new ArrayList<>();
        myFriends = new ObservableArrayList<>();
        thisUser = null;
        myPlacesKeyIndexMapping = new HashMap<>();
        friendsKeyIndexMapping=new HashMap<>();
        friendExistMapping=new HashMap<>();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        database = FirebaseDatabase.getInstance().getReference();

        //objekti
        database.child(FIREBASE_CHILD_PLACES).addChildEventListener(childEventListenerObjekat);

        setupFriends();

    }

    public boolean setUserListeners(String username)
    {
        if(username == null || username.length()==0 )
            return false;

        thisUsername = username;
        database.child(FIREBASE_CHILD_USERS).orderByKey().equalTo(username).limitToFirst(1)
                .addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(! dataSnapshot.hasChild(thisUsername))
                    return;

                DataSnapshot snapshot = dataSnapshot.child(thisUsername);

                Korisnik k = snapshot.getValue(Korisnik.class);
                if(k!=null)
                {
                    if(thisUser == null)
                    {
                        k.korisnickoIme = thisUsername;
                        setThisUser(k);

                        if (koordinate != null)
                            thisUser.koo = koordinate;

                        if(k.profilePicID!=null && k.profilePicID.length()>0 && k.profilePicID.compareToIgnoreCase("default") != 0)
                        try {
                            FirebaseStorage.getInstance().getReferenceFromUrl(k.profilePicID).getBytes(MAX_BYTES)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            thisUser.profiSlikaBit = bmp;
                                        }
                                    });
                        }
                        catch (Exception e)
                        {
                            Log.e("pic ref",e.getMessage());
                        }
                    }
                    else
                    {
                        thisUser.poeni=k.poeni;
                        thisUser.imeIprezime=k.imeIprezime;
                        thisUser.email=k.email;

                        if(thisUser.profilePicID != null && k.profilePicID != null
                                && thisUser.profilePicID.compareToIgnoreCase("default")!=0
                                && k.profilePicID.compareToIgnoreCase("default")!=0)
                        {
                            thisUser.profiSlikaBit=null;
                            thisUser.profilePicID = k.profilePicID;
                            FirebaseStorage.getInstance().getReferenceFromUrl(k.profilePicID).getBytes(MAX_BYTES)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            thisUser.profiSlikaBit = bmp;
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        database.child(FIREBASE_CHILD_LOCATIONS).orderByKey().equalTo(username).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(! dataSnapshot.hasChild(thisUsername))
                            return;

                        DataSnapshot snapshot = dataSnapshot.child(thisUsername);
                        MyCoordinates coo = snapshot.getValue(MyCoordinates.class);
                        if(thisUser !=null)
                            thisUser.koo=coo;
                        else
                            koordinate =coo;


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return true;
    }

    ChildEventListener childEventListenerObjekat = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {
            String key = dataSnapshot.getKey();
            MojObjekat objekat = dataSnapshot.getValue(MojObjekat.class);
            objekat.key=key;
            if(!myPlacesKeyIndexMapping.containsKey(objekat.key) || myPlaces.get(myPlacesKeyIndexMapping.get(objekat.key))==null )
            {
                myPlaces.add(objekat);
                myPlacesKeyIndexMapping.put(key, myPlaces.size() - 1);
            }

            database.child(FIREBASE_CHILD_QUESTIONS).orderByKey().equalTo(key).limitToFirst(1)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(! dataSnapshot.hasChild(key))
                                return;

                            DataSnapshot snapshot = dataSnapshot.child(key);

                            MojObjekat obj;
                            if(myPlaces!=null && myPlacesKeyIndexMapping != null && myPlacesKeyIndexMapping.containsKey(key))
                                obj = myPlaces.get(myPlacesKeyIndexMapping.get(key));
                            else return;

                            obj.listaPitanja=new ArrayList<>();

                            for (DataSnapshot child :snapshot.getChildren())
                            {
                                Pitanje p = child.getValue(Pitanje.class);
                                p.setKey(child.getKey());
                                obj.listaPitanja.add(p);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {
            String key = dataSnapshot.getKey();
            MojObjekat objekat = dataSnapshot.getValue(MojObjekat.class);
            objekat.key=key;
            if(myPlacesKeyIndexMapping.containsKey(key))
                myPlaces.set(myPlacesKeyIndexMapping.get(key),objekat);
            else
            {
                myPlaces.add(objekat);
                myPlacesKeyIndexMapping.put(key,myPlaces.size()-1);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {
            String key = dataSnapshot.getKey();

            MojObjekat objekat = myPlaces.get(myPlacesKeyIndexMapping.get(key));
            myPlaces.remove(objekat);
            recreateObjectKeyIndexMapping();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListenerLocations = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String key = dataSnapshot.getKey();
            MyCoordinates coordinates = dataSnapshot.getValue(MyCoordinates.class);

            if(friendsKeyIndexMapping.containsKey(key) && friendExistMapping.get(friendsKeyIndexMapping.get(key))
                    &&  myFriends.get(friendsKeyIndexMapping.get(key))!=null)
            {
                Prijatelj p = myFriends.get(friendsKeyIndexMapping.get(key));
                p.setKoo(coordinates);

                myFriends.set(friendsKeyIndexMapping.get(key),p);
            }


        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            String key = dataSnapshot.getKey();
            MyCoordinates coordinates = dataSnapshot.getValue(MyCoordinates.class);

            if(friendsKeyIndexMapping.containsKey(key) && friendExistMapping.get(friendsKeyIndexMapping.get(key))
                    && myFriends.get(friendsKeyIndexMapping.get(key))!=null)
            {
                Prijatelj p = myFriends.get(friendsKeyIndexMapping.get(key));
                p.setKoo(coordinates);

                myFriends.set(friendsKeyIndexMapping.get(key),p);
            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListenerPrijatelj = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {
            String key = dataSnapshot.getKey();
            if(!friendsKeyIndexMapping.containsKey(key))
            {
                int i= myFriendsId.size();
                myFriendsId.add(key);
                myFriends.add(i,null);
                friendsKeyIndexMapping.put(key,i);
                friendExistMapping.put(myFriendsId.size()-1,false);
                database.child(FIREBASE_CHILD_USERS).orderByKey().equalTo(key).limitToFirst(1).addChildEventListener(childEventListenerKorisnik);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {
            String key = dataSnapshot.getKey();

            if(friendsKeyIndexMapping!=null && friendsKeyIndexMapping.containsKey(key))
            {   Prijatelj p = myFriends.get(friendsKeyIndexMapping.get(key));
                myFriends.remove(p);
                myFriendsId.remove(key);
                database.child(FIREBASE_CHILD_LOCATIONS).orderByKey().equalTo(key).removeEventListener(childEventListenerLocations);
            }
            recreateFriendsKeyIndexMapping();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListenerKorisnik = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {

            String user = dataSnapshot.getKey();

            if(friendsKeyIndexMapping.containsKey(user))
            {
                Integer index = friendsKeyIndexMapping.get(user);
                Korisnik kor = dataSnapshot.getValue(Korisnik.class);
                kor.korisnickoIme=user;
                Prijatelj prijatelj;
                if(friendExistMapping.get(index))
                {
                    prijatelj= myFriends.get(index);
                    prijatelj.setPoeni(kor.poeni);
                    prijatelj.setProfilePicID(kor.profilePicID);
                    myFriends.set(index,prijatelj);
                }
                else
                {
                    prijatelj = new Prijatelj(kor.korisnickoIme,null,kor.poeni,kor.profilePicID);
                    myFriends.set(index,prijatelj);
                    friendExistMapping.put(index,true);
                    database.child(FIREBASE_CHILD_LOCATIONS).orderByKey().equalTo(user).limitToFirst(1).addChildEventListener(childEventListenerLocations);
                }
                FirebaseStorage.getInstance().getReferenceFromUrl(kor.profilePicID).getBytes(MAX_BYTES)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp  = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                prijatelj.setProfiSlikaBit(bmp);
                                myFriends.set(index,prijatelj);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Log.e("getPicture",e.getMessage());
                            }
                        });
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {

            String user = dataSnapshot.getKey();

            if(friendsKeyIndexMapping.containsKey(user))
            {
                Integer index = friendsKeyIndexMapping.get(user);
                Korisnik kor = dataSnapshot.getValue(Korisnik.class);
                Prijatelj prijatelj;
                if(friendExistMapping.get(index))
                {
                    prijatelj= myFriends.get(index);
                    prijatelj.setPoeni(kor.poeni);
                    if(prijatelj.getProfilePicID().compareToIgnoreCase(kor.profilePicID)==0)
                    {
                        myFriends.set(index,prijatelj);
                        return;
                    }
                    else
                    {
                        prijatelj.setProfilePicID(kor.profilePicID);
                        myFriends.set(index,prijatelj);
                        FirebaseStorage.getInstance().getReferenceFromUrl(kor.profilePicID).getBytes(1024 * MAX_BYTES)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        prijatelj.setProfiSlikaBit(bmp);
                                        myFriends.set(index, prijatelj);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("getPicture", e.getMessage());
                                    }
                                });

                    }

                }
                else {
                    prijatelj = new Prijatelj(kor.korisnickoIme, null, kor.poeni, kor.profilePicID);
                    myFriends.set(index, prijatelj);
                    friendExistMapping.put(index, true);

                    FirebaseStorage.getInstance().getReferenceFromUrl(kor.profilePicID).getBytes(1024 * MAX_BYTES)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    prijatelj.setProfiSlikaBit(bmp);
                                    myFriends.set(index, prijatelj);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("getPicture", e.getMessage());
                                }
                            });
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {
            String user = dataSnapshot.getKey();

            if(friendsKeyIndexMapping.containsKey(user))
            {
                Prijatelj prijatelj = myFriends.get(friendsKeyIndexMapping.get(user));
                myFriends.remove(prijatelj);
                myFriendsId.remove(prijatelj.getUsername());
                recreateFriendsKeyIndexMapping();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void addOnChangeListener(ObservableList.OnListChangedCallback callback)
    {
        myFriends.addOnListChangedCallback(callback);
    }

    public void unfriendUser(String username)
    {
        if(thisUsername != null && username != null && thisUsername.length() >0 && username.length() > 0 )
        {
            database.child(FIREBASE_CHILD_FRIENDS).child(thisUsername).child(username).removeValue();
            database.child(FIREBASE_CHILD_FRIENDS).child(username).child(thisUsername).removeValue();
        }
    }


    public void addFriend(String friend)
    {
        if(thisUser!=null)
        {
            database.child(FIREBASE_CHILD_FRIENDS).child(thisUser.korisnickoIme).child(friend).setValue(true);
            database.child(FIREBASE_CHILD_FRIENDS).child(friend).child(thisUser.korisnickoIme).setValue(true);
        }
    }
    private void setupFriends()
    {
        if(thisUser!=null)
        {
            database.child(FIREBASE_CHILD_FRIENDS).child(thisUser.korisnickoIme).addChildEventListener(childEventListenerPrijatelj);
            database.child(FIREBASE_CHILD_LOCATIONS).addChildEventListener(childEventListenerLocations);
        }
    }

    public ObservableList<Prijatelj> getMyFriends()
    {
        return myFriends;
    }

    public void addQuestion(Pitanje q, String key)
    {
        MojObjekat obj;
        if(myPlaces!=null && myPlacesKeyIndexMapping != null && myPlacesKeyIndexMapping.containsKey(key))
            obj = myPlaces.get(myPlacesKeyIndexMapping.get(key));
        else return;

        String questionKey = database.push().getKey();
        database.child(FIREBASE_CHILD_QUESTIONS).child(key).child(questionKey).setValue(q);

    }

    public void deleteAcc()
    {

        if(thisUser.profilePicID != null && thisUser.profilePicID.length()>0 && thisUser.profilePicID.compareToIgnoreCase("default")!=0)
            FirebaseStorage.getInstance().getReferenceFromUrl(thisUser.profilePicID).delete();

        for (String id : myFriendsId)
        {
            database.child(FIREBASE_CHILD_FRIENDS).child(id).child(thisUsername).removeValue();
        }
        database.child(FIREBASE_CHILD_FRIENDS).child(thisUsername).removeValue();
        database.child(FIREBASE_CHILD_USERS).child(thisUsername).removeValue();
        database.child(FIREBASE_CHILD_LOCATIONS).child(thisUsername).removeValue();

        myFriendsId.clear();
        myFriends.clear();
        friendsKeyIndexMapping.clear();
        friendExistMapping.clear();

        thisUser=null;
        thisUsername=null;

        FirebaseAuth.getInstance().getCurrentUser().delete();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            FirebaseAuth.getInstance().signOut();

    }


    private static class SingletonHolder
    {
        public static final MojiPodaci instance = new MojiPodaci();
    }

    public static MojiPodaci getInstance()
    {
        return  SingletonHolder.instance;
    }

    public ArrayList<MojObjekat> getMyPlaces()
    {
        return myPlaces;
    }

    public void addNewPlace(MojObjekat place)
    {
        String key = database.push().getKey();
        myPlaces.add(place);
        myPlacesKeyIndexMapping.put(key, myPlaces.size() -1);
        database.child(FIREBASE_CHILD_PLACES).child(key).setValue(place);
        place.key = key;
    }

    public void addNewUser(Korisnik k)
    {
        String key = k.korisnickoIme;
        database.child(FIREBASE_CHILD_USERS).child(key).setValue(k);
        database.child(FIREBASE_CHILD_LOCATIONS).child(key).setValue(k.koo);

        //novo
        setUserListeners(k.korisnickoIme);
    }


    public MojObjekat getPlace(int index)
    {
        return myPlaces.get(index);
    }

    public int getPlaceIndex(String key)
    {
        if(myPlacesKeyIndexMapping!=null && myPlacesKeyIndexMapping.containsKey(key))
            return myPlacesKeyIndexMapping.get(key);
        else return -1;
    }

    public void updatePlace(int index, String nme, String desc, String lng, String lat, String k)
    {
        MojObjekat myPlace = myPlaces.get(index);
        myPlace.name = nme;
        myPlace.description = desc;
        myPlace.latitude = lat;
        myPlace.longitude = lng;
        myPlace.kategorija = k;
        database.child(FIREBASE_CHILD_PLACES).child(myPlace.key).setValue(myPlace);
    }

    public Korisnik getThisUser()
    {
        return thisUser;
    }
    public void setThisUser(Korisnik korisnik)
    {
        if(korisnik!= null && (thisUser==null || thisUsername==null || thisUsername.compareTo(korisnik.korisnickoIme)!=0))
        {
            if (thisUser != null) {
                myFriends.clear();
                myFriendsId.clear();
                friendsKeyIndexMapping.clear();
                friendExistMapping.clear();
            }
            thisUser = korisnik;
            setupFriends();
        }
        else  if (korisnik==null)  //logout
        {
//            database.child(FIREBASE_CHILD_FRIENDS).child(thisUsername).removeEventListener(childEventListenerPrijatelj);
//            database.child(FIREBASE_CHILD_LOCATIONS).removeEventListener(childEventListenerLocations);
            thisUser=null;
            thisUsername=null;

            myFriends.clear();
            myFriendsId.clear();
            friendsKeyIndexMapping.clear();
            friendExistMapping.clear();
        }
    }

    /**
     *
     * @param username
     * @param points
     * @deprecated use {@link #updatePoints(int)} instead, send points to add
     */
    @Deprecated // use updatePoints instead
    public void updateUserPoints(String username, int points)
    {
        database.child(FIREBASE_CHILD_USERS).child(username).child("poeni").setValue(points);
    }
    public void updatePoints(int addPoints)
    {
        thisUser.poeni+=addPoints;
        database.child(FIREBASE_CHILD_USERS).child(thisUsername).child("poeni").setValue(thisUser.poeni);
    }

    public void updateUserPosition(String lon,String lat)
    {
        if(lon == null || lat == null || lon.length() == 0 || lat.length() == 0 )
            return;
        MyCoordinates coordinates = new MyCoordinates();
        coordinates.latitude=lat;
        coordinates.longitude=lon;
        thisUser.koo=coordinates;
        database.child(FIREBASE_CHILD_LOCATIONS).child(thisUsername).setValue(coordinates);

        if(listener!=null)
            listener.onUserMoved();

    }


    private void recreateObjectKeyIndexMapping()
    {
        myPlacesKeyIndexMapping.clear();
        for(int i =0;i<myPlaces.size();i++)
        {
            myPlacesKeyIndexMapping.put(myPlaces.get(i).key, i);
        }
    }

    private void recreateFriendsKeyIndexMapping()
    {
        friendsKeyIndexMapping.clear();
        friendExistMapping.clear();
        for (int i=0; i<myFriendsId.size();i++)
        {
            friendsKeyIndexMapping.put(myFriendsId.get(i),i);
            if(myFriends.contains(new Prijatelj(myFriendsId.get(i),null,0,"")))
                friendExistMapping.put(i,true);
            else
                friendExistMapping.put(i,false);
        }


    }

    public static String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;
                Log.d("UriTest: ", "uri: " + uri);
                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                }


                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
    }

    public interface MyListener
    {
        void onUserMoved();
    }

    public void setMyListener(MyListener lis)
    {
        listener=lis;
    }


}
