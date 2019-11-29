package com.example.knowyourcity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PrijavaActivity extends AppCompatActivity implements View.OnClickListener {

    private Button reg, prijava;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijava);

        MojiPodaci.getInstance();

        context = this;
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

     //otvara Registraciju Activity
        reg = (Button) findViewById(R.id.logInRegistrujSeBtn);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivityReg();
            }
        });

        //otvara Mapa Activity
        prijava = (Button) findViewById(R.id.logInPrijaviSeBtn);
        prijava.setOnClickListener(this);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null && firebaseUser.getDisplayName() != null && firebaseUser.getDisplayName().length() > 0)
        {
           // Toast.makeText(this,"firebase user:" + firebaseUser.getDisplayName() , Toast.LENGTH_LONG).show();
            MojiPodaci.getInstance().setUserListeners(firebaseUser.getDisplayName());
          //  MojiPodaci.getInstance().setThisUser(MojiPodaci.getInstance().getUser(firebaseUser.getDisplayName()));
            openActivityMapa();
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
            //    Toast.makeText(context,"authState" , Toast.LENGTH_SHORT).show();
                if(firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().getDisplayName() != null)
                    onLogin(firebaseAuth.getCurrentUser());
            }
        };
        mAuth.addAuthStateListener(authStateListener);

    }

    private void onLogin(@NonNull FirebaseUser currentUser)
    {
  //      Toast.makeText(context, "onLogin", Toast.LENGTH_SHORT).show();
        if(currentUser==null) {
         //   Toast.makeText(context, "error onLogin null user", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(currentUser.getDisplayName()==null) {
        //    Toast.makeText(context, "error onLogin null display name", Toast.LENGTH_SHORT).show();
            findUsernameForCurrentUser();
            return;
        }
        else if(currentUser.getDisplayName().length()==0) {
       //     Toast.makeText(context, "error onLogin empty display name", Toast.LENGTH_SHORT).show();
            findUsernameForCurrentUser();
            return;
        }
        boolean b=MojiPodaci.getInstance().setUserListeners(currentUser.getDisplayName());
        //MojiPodaci.getInstance().setThisUser(user);

        if(b)
           // Toast.makeText(context, "error onLogin", Toast.LENGTH_SHORT).show();
        //else
            openActivityMapa();
    }

    public void openActivityReg()
        {
            Intent i = new Intent(this, RegistracijaActivity.class);
            startActivity(i);
        }

    public void openActivityMapa()
    {
        if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            Intent i = new Intent(this, MapaActivity.class);
            startActivity(i);
        }
        else
            Toast.makeText(context, "invalid firebase user", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.logInPrijaviSeBtn:
            {
                EditText emailET = (EditText) findViewById(R.id.logInUsernameET);
                EditText passwordET = (EditText) findViewById(R.id.logInPasswordET);

                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();


                //user = MojiPodaci.getInstance().getUser(email);
                //if (user != null)
                if(email != null && email.length() > 0)
                {
                   mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                     //   Toast.makeText(context, "onComplete", Toast.LENGTH_SHORT).show();
                                        if(mAuth.getCurrentUser()!=null)
                                            onLogin(mAuth.getCurrentUser());
                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(context, "Invalid email", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_item)
        {
            Toast.makeText(this, "Kreacija Kike i Kaje", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void findUsernameForCurrentUser()
    {
        if(mAuth.getCurrentUser()==null)
            return;

        String mail=mAuth.getCurrentUser().getEmail();
        FirebaseDatabase.getInstance().getReference().child("users")
                .orderByChild("email").equalTo(mail).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren())
                            return;

                        String username = dataSnapshot.getChildren().iterator().next().getKey();

                        if(mAuth.getCurrentUser()==null)
                            return;

                        if (username != null && username.length() != 0) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                            mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                      //  Toast.makeText(context,"Profile updated",Toast.LENGTH_SHORT).show();
                                        onLogin(mAuth.getCurrentUser());
                                    }
                                  //  else
                                    //    Toast.makeText(context,"Failed to update profile",Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
