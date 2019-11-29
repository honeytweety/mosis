package com.example.knowyourcity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import id.zelory.compressor.Compressor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class IzmeniProfilActivity extends AppCompatActivity implements View.OnClickListener {

    EditText imeET;
    EditText emailET;
    EditText passET;
    EditText userET;
    ImageView picture;

    Button slikajBtn;
    Button galerijaBtn;
    Button sacuvajBtn;
    Button cancelBtn;
    Button deleteAccBtn;

    boolean bslika;
    boolean bpass;
    boolean bemail;

    Context context;

    Uri image_uri;
    private File compressed_image_file;

    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE = 1000;
    static final int PERMISSION_READ_EXTERNAL_STORAGE= 1;
    static final int GALLERY_REQUEST_CODE = 1;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseREF;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_izmeni_profil);

        Korisnik me = MojiPodaci.getInstance().getThisUser();
        if(me==null)
        {
            finish();
            return;
        }

        context=this;

        imeET = findViewById(R.id.izmeniProfilActivityImeET);
        emailET = findViewById(R.id.izmeniProfilActivityEmailET);
        passET = findViewById(R.id.izmeniProfilActivityPasswordET);
        userET = findViewById(R.id.izmeniProfilActivityUsernameET);
        picture = findViewById(R.id.izmeniProfilActivitySlikaIV);
        slikajBtn = findViewById(R.id.izmeniProfilActivityCameraBtn);
        galerijaBtn = findViewById(R.id.izmeniProfilActivitySlikaBtn);
        sacuvajBtn = findViewById(R.id.izmeniProfilActivityOKBtn);
        cancelBtn = findViewById(R.id.izmeniProfilActivityCancelBtn);
        deleteAccBtn = findViewById(R.id.izmeniProfilDeleteAcc);

        bslika = false;
        bpass = false;
        bemail=false;

        imeET.setText(me.imeIprezime);
        emailET.setText(me.email);
        userET.setText(me.korisnickoIme);

        if(me.profiSlikaBit !=null)
            picture.setImageBitmap(me.profiSlikaBit);
        else if(me.profilePicID!=null && me.profilePicID.length()>0 && me.profilePicID.compareToIgnoreCase("defautl")!=0)
        {
            Picasso.get().load(me.profilePicID).fit().into(picture, new Callback() {
                @Override
                public void onSuccess()
                {
                    Korisnik korisnik = MojiPodaci.getInstance().getThisUser();
                    if(korisnik!=null)
                    {
                        Drawable drawable = picture.getDrawable();
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

        cancelBtn.setOnClickListener(this::onClick);
        galerijaBtn.setOnClickListener(this::onClick);
        sacuvajBtn.setOnClickListener(this::onClick);
        slikajBtn.setOnClickListener(this::onClick);
        deleteAccBtn.setOnClickListener(this::onClick);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            galerijaBtn.setEnabled(false);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
        }

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("ProfilePictures");
        databaseREF = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.izmeniProfilActivityCameraBtn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
                break;
            }
            case R.id.izmeniProfilActivitySlikaBtn:
            {
                pickFromGallery();
                break;
            }

            case R.id.izmeniProfilActivityOKBtn:
                sacuvajSve();
                break;

            case R.id.izmeniProfilActivityCancelBtn:
                finish();
                break;

            case R.id.izmeniProfilDeleteAcc:
            {
                new AlertDialog.Builder(context)
                        .setTitle("Upozorenje")
                        .setMessage("Da li ste sigurni da zelite da OBRISETE NALOG?")
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setPositiveButton("Obrisi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                MojiPodaci.getInstance().deleteAcc();
                                Intent intent = new Intent(context, PrijavaActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Odustani",null).show();
            }

        }

    }

    private void sacuvajSve()
    {
        Korisnik user = MojiPodaci.getInstance().getThisUser();
        String ime = imeET.getText().toString();

        if(ime != null && ime.length() > 0)
        {
            databaseREF.child("users").child(user.korisnickoIme).child("imeIprezime").setValue(ime);
        }
        if(bslika)
        {
            if(user.profilePicID != null && user.profilePicID.length() > 0 && user.profilePicID.compareToIgnoreCase("default") !=0)
            {
                StorageReference reference= FirebaseStorage.getInstance().getReferenceFromUrl(user.profilePicID);
                reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"File successfully deleted.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            uploadPicture(image_uri);
        }

        String email = emailET.getText().toString();
        String pass = passET.getText().toString();

        if(email!=null && email.length()> 0 && email.compareToIgnoreCase(user.email) != 0)
            bemail = true;

        if(pass != null && pass.length() >5)
            bpass = true;

        if(bemail || bpass)
        {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser != null)
            {
                if(bemail)
                {
                    firebaseUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(context,"Email changed",Toast.LENGTH_LONG).show();
                                databaseREF.child("users").child(user.korisnickoIme).child("email").setValue(email);
                            }
                        }
                    });
                }
                if(bpass)
                {
                    firebaseUser.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context,"Password changed",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
           // startActivity(new Intent(context, PrijavaActivity.class));
        }

       // else
        //{
            setResult(Activity.RESULT_OK);
            startActivity(new Intent(context, MapaActivity.class));
        //}
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

    private void pickFromGallery()
    {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
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
                    Button slika = (Button) findViewById(R.id.izmeniProfilActivitySlikaBtn);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode)
            {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    image_uri = data.getData();
                    picture.setImageURI(image_uri);
                    compressImage();
                    bslika=true;
                    break;
                case IMAGE_CAPTURE_CODE:
                    picture.setImageURI(image_uri);
                    compressImage();
                    bslika=true;
                    break;

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

    public String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public void uploadPicture(Uri image_uri)
    {
        String user = MojiPodaci.getInstance().thisUsername;

        StorageReference ref = storageReference.child(user);
        String konekcija = System.currentTimeMillis() +"." + getExtension(image_uri);
        StorageReference pomocniRef = ref.child(konekcija);

        if (compressed_image_file != null)
        {
            Uri help =  Uri.parse(compressed_image_file.toURI().toString());

            pomocniRef.putFile(help)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pomocniRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseREF.child("users").child(MojiPodaci.getInstance().thisUsername)
                                            .child("profilePicID").setValue(uri.toString());
                                 //   MojiPodaci.getInstance().getThisUser().profilePicID=uri.toString();
                                ////trebalo bi to sam
                                }
                            });
                        }
                    });
        }
    }
}
