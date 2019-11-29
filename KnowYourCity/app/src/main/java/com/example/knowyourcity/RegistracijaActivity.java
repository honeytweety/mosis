package com.example.knowyourcity;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import id.zelory.compressor.Compressor;

public class RegistracijaActivity extends AppCompatActivity implements View.OnClickListener
{

    private FirebaseAuth mAuth;
    private Context context;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Uri image_uri;
    private File compressed_image_file;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseREF;
    //---------------------------------------------------------------------

    private String email;
    private String name;
    private String username;
    private String password;
    private ImageView imageView;
    private ProgressBar loadingProgress;

    //------------------------------------------------------------------------

    private Button reg, slika, mCaptureBtn;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE = 1000;

    static final int PERMISSION_READ_EXTERNAL_STORAGE= 1;
    static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registracija);

        context=this;

        try
        {
            mAuth = FirebaseAuth.getInstance();
        }
        catch(Exception e)
        {
            Toast.makeText(this, "Connection failed!", Toast.LENGTH_SHORT).show();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();

        if(bar!=null)
            bar.setDisplayHomeAsUpEnabled(true);

        //otvara Glavni Activity

        reg = (Button) findViewById(R.id.registracijaActivityOKBtn);
        reg.setOnClickListener(this);

        slika = (Button) findViewById(R.id.registracijaActivitySlikaBtn);
        slika.setOnClickListener(this);

        mCaptureBtn = (Button) findViewById(R.id.registracijaActivityCameraBtn);
        mCaptureBtn.setOnClickListener(this);

        imageView = findViewById(R.id.registracijaActivitySlikaIV);
        loadingProgress = findViewById(R.id.pregledPitanjaProgressBar);
        loadingProgress.setVisibility(View.INVISIBLE);

        authStateListener =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if(firebaseAuth.getCurrentUser() != null )
                {
                    onAuthSuccess(firebaseAuth.getCurrentUser());
                }
            }
        };

        mAuth.addAuthStateListener(authStateListener);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("ProfilePictures");
        databaseREF = FirebaseDatabase.getInstance().getReference();


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            slika.setEnabled(false);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    private void openCamera()
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nova slika");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Slikana kamericom");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    private void onAuthSuccess(@NonNull FirebaseUser currentUser)
    {
        mAuth.removeAuthStateListener(authStateListener);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       // if(task.isSuccessful())
                           // Toast.makeText(context,"Profile updated",Toast.LENGTH_SHORT).show();
                        //else
                           // Toast.makeText(context,"Fail",Toast.LENGTH_SHORT).show();
                        //reg.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                    }
                });

        String nesto  = "default";
        Korisnik korisnik = new Korisnik(name, username, nesto);
        korisnik.koo.longitude="0";
        korisnik.koo.latitude="0";
        korisnik.email = email;

        MojiPodaci.getInstance().addNewUser(korisnik);
        MojiPodaci.getInstance().setThisUser(korisnik);
        uploadPicture(image_uri, currentUser);

        openActivityMapa();
    }

    public void openActivityMapa()
    {
        Intent i = new Intent(this, MapaActivity.class);
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registracijaActivityOKBtn:
            {
                reg.setVisibility(View.INVISIBLE);
                EditText etEmail = (EditText)findViewById(R.id.registracijaActivityEmailET);
                 email = etEmail.getText().toString();
                EditText etName = (EditText)findViewById(R.id.registracijaActivityImeET);
                 name = etName.getText().toString();
                EditText etUser = (EditText)findViewById(R.id.registracijaActivityUsernameET);
                 username = etUser.getText().toString();
                EditText etPass = (EditText)findViewById(R.id.registracijaActivityPasswordET);
                 password = etPass.getText().toString();
                EditText etpassAgain = (EditText)findViewById(R.id.registracijaActivityPassword2ET);
                String passAgain = etpassAgain.getText().toString();

                if(email.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty() || image_uri == null || passAgain.isEmpty() || !password.equals(passAgain))
                {
                    if(!password.equals(passAgain))
                    {
                        showMess("Lozinke nisu iste!");
                    }
                    else if(image_uri == null)
                    {
                        showMess("Slika nije uneta!");
                    }
                    else
                        showMess("Nisu popunjena sva polja");
                    reg.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else
                {
                    mAuth.createUserWithEmailAndPassword(email, password); //kreira firebase usera
                }
                break;
            }
            case R.id.registracijaActivitySlikaBtn:
            {
               // Toast.makeText(context,"dugme za sliku", Toast.LENGTH_LONG).show();
                pickFromGallery();
                break;

            }
            case R.id.registracijaActivityCameraBtn:
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED )
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

        }
    }

    public void showMess(String poruka)
    {
        Snackbar.make(findViewById(R.id.constraint_layout_reg),poruka,Snackbar.LENGTH_LONG).show();
        //Toast.makeText(context, poruka, Toast.LENGTH_SHORT).show();
    }


    private void pickFromGallery()
    {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
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
       // else
       //   Toast.makeText(this, "onActivityResul je uso u else", Toast.LENGTH_SHORT).show();

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
                    Button slika = (Button) findViewById(R.id.registracijaActivitySlikaBtn);
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

    public void uploadPicture(Uri image_uri, FirebaseUser currentUser)
    {
        EditText etUser = (EditText) findViewById(R.id.registracijaActivityUsernameET);
        String user = etUser.getText().toString();

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
                                    databaseREF.child("users").child(username).child("profilePicID").setValue(uri.toString());
                                    MojiPodaci.getInstance().getThisUser().profilePicID=uri.toString();
                                }
                            });
                        }
                    });
        }


    }

    public String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

}
