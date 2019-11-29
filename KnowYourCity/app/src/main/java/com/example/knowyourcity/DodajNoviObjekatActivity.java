package com.example.knowyourcity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import id.zelory.compressor.Compressor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DodajNoviObjekatActivity extends AppCompatActivity implements View.OnClickListener {

    String lat ="";
    String lon = "";
    boolean editMode = false;
    int index;

    Context context;

    ImageView imageView;
    Button slikaBtn, cameraBtn, okBtn;
    Uri image_uri;
    private File compressed_image_file;

    private Spinner kategorijaSpinner;
    private ArrayAdapter<String> myAdapter;
    private String selecteditem;
    private List<String> listaKategorija;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseREF;
    //------------------------------------------------------------------
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE = 1000;
    static final int PERMISSION_READ_EXTERNAL_STORAGE= 1;
    static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_novi_objekat);

        context=this;

        try
        {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();

            if (positionBundle != null)
            {
                lat = positionBundle.getString("lat");
                lon = positionBundle.getString("lon");
                index = positionBundle.getInt("position", -1);
                if(index!=-1)
                {
                    editMode = true;
                }
                else
                    editMode=false;
            }
        }
        catch (Exception e)
        {
            editMode = false;
        }
        listaKategorija = new ArrayList<>();
        listaKategorija.add(0, "Spomenik");
        listaKategorija.add(1, "Crkva");
        listaKategorija.add(2,"Izletište");
        listaKategorija.add(3, "Festival");
        listaKategorija.add(4, "Česma");
        listaKategorija.add(5, "Ostalo");

        okBtn = (Button) findViewById(R.id.dodajObjekatActivityOKbtn);
        cameraBtn = (Button) findViewById(R.id.dodajObjekatActivityKameraBtn);
        slikaBtn = (Button) findViewById(R.id.dodajObjekatActivityUcitajSlikuBtn);
        imageView = (ImageView) findViewById(R.id.dodajObjekatActivitySlikaObjekta);

        kategorijaSpinner = (Spinner) findViewById(R.id.spinner);


        myAdapter  = new ArrayAdapter<String>(DodajNoviObjekatActivity.this,android.R.layout.simple_list_item_1, listaKategorija);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategorijaSpinner.setAdapter(myAdapter);
        kategorijaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selecteditem =  myAdapter.getItem(position);
               // Toast.makeText(DodajNoviObjekatActivity.this, position + " " + selecteditem + " " + id, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        okBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        slikaBtn.setOnClickListener(this);

        if(editMode)
        {
            EditText naziv = (EditText) findViewById(R.id.dodajObjekatActivityNazivET);
            EditText opis = (EditText) findViewById(R.id.dodajObjekatActivityOpisET);

            MojObjekat objekat = MojiPodaci.getInstance().getPlace(index);
            naziv.setText(objekat.name);
            opis.setText(objekat.description);

            String link   = objekat.objectPicID;
            Picasso.get().load(link).into(imageView);

            okBtn.setText(R.string.sacuvaj);

            if(objekat.kategorija!=null && !TextUtils.isEmpty(objekat.kategorija))
                kategorijaSpinner.setSelection(myAdapter.getPosition(objekat.kategorija));


            if(TextUtils.isEmpty(lat) || TextUtils.isEmpty(lon))
            {
                lat=objekat.latitude;
                lon=objekat.longitude;
            }


        }
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("ObjectPictures");
        databaseREF = FirebaseDatabase.getInstance().getReference();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            slikaBtn.setEnabled(false);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
        }


    }

    private void pickFromGallery()
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private void openCamera()
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nova slika");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Slikana kamericom");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_READ_EXTERNAL_STORAGE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Button slika = (Button) findViewById(R.id.dodajObjekatActivityUcitajSlikuBtn);
                    slika.setEnabled(true);
                }
                return;
            }
            case PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    openCamera();
                }
                else
                {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }


    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case R.id.dodajObjekatActivityKameraBtn:
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED )
                    {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else
                    { openCamera(); } }
                else
                { openCamera(); }
                break;
            }
            case R.id.dodajObjekatActivityUcitajSlikuBtn:
            {
                pickFromGallery();
                break;
            }
            case R.id.dodajObjekatActivityOKbtn:
            {
                String naziv = ((EditText) findViewById(R.id.dodajObjekatActivityNazivET)).getText().toString();
                String opis = ((EditText) findViewById(R.id.dodajObjekatActivityOpisET)).getText().toString();


                if(!editMode)
                {
                    if(image_uri==null) {
                        Toast.makeText(context, "Objekat mora da ima sliku!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    MojObjekat objekat = new MojObjekat(naziv, opis, lon, lat, selecteditem);
                    MojiPodaci.getInstance().addNewPlace(objekat);

                    MojiPodaci.getInstance().updatePoints(this.getResources().getInteger(R.integer.noviObjekat));

                    uploadPicture(image_uri, objekat);
                }
                else
                {
                    MojiPodaci.getInstance().updatePlace(index,naziv,opis,lon,lat, selecteditem);

                    if(image_uri!=null)
                    {
                        MojObjekat objekat = MojiPodaci.getInstance().getPlace(index);
                        if(objekat.objectPicID != null && objekat.objectPicID.length() > 0 && objekat.objectPicID.compareToIgnoreCase("default") !=0)
                        {
                            StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(objekat.objectPicID);
                            reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                  //  Toast.makeText(context,"File successfully deleted.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        uploadPicture(image_uri, objekat);
                    }
                }
                setResult(Activity.RESULT_OK);
                startActivity(new Intent(this, MapaActivity.class));
                //finish();
                break;

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode)
            {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    image_uri = data.getData();
                    imageView.setImageURI(image_uri);
                    compressImage();
                    break;
                case IMAGE_CAPTURE_CODE:
                    imageView.setImageURI(image_uri);
                    compressImage();
                    break;

            }
        else
        {
           // Toast.makeText(this, "onActivityResult je uso u else", Toast.LENGTH_SHORT).show();
        }
    }

    private void compressImage()
    {
        if(image_uri==null)
            return;

        File uncompressed = new File(MojiPodaci.getImageRealPath(getContentResolver(), image_uri, null));
        if(uncompressed!=null)
        {
            try
            {
                compressed_image_file = new Compressor(this).compressToFile(uncompressed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(this, MapaActivity.class);
        startActivity(i);
    }
    public String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public void uploadPicture(Uri image_uri, MojObjekat o)
    {
        EditText etObject = (EditText) findViewById(R.id.dodajObjekatActivityNazivET);
        String nameObject = etObject.getText().toString();

        if(nameObject==null || nameObject.length()==0)
        {
            if (!editMode)
            {
                Toast.makeText(context, "Unesite naziv objekta i pokusajte ponovo", Toast.LENGTH_LONG).show();
                return;
            }
            else
                nameObject = o.name;
        }

        StorageReference ref = storageReference.child(nameObject);
        String konekcija = System.currentTimeMillis() +"." + getExtension(image_uri);
        StorageReference pomocniRef = ref.child(konekcija);

        if (compressed_image_file != null) {
            Uri help = Uri.parse(compressed_image_file.toURI().toString());

            pomocniRef.putFile(help)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pomocniRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                @Override
                                public void onSuccess(Uri uri) {
                                    if (o.key != null && o.key.length() > 0) {
                                        databaseREF.child("my-places").child(o.key).child("objectPicID").setValue(uri.toString());
                                    }
                                }
                            });
                        }
                    });
        }
    }


}
