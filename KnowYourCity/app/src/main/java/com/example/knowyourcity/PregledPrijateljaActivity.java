package com.example.knowyourcity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.ObservableList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PregledPrijateljaActivity extends AppCompatActivity {

    String value;
    int index;
    Context context;
    Prijatelj prijatelj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_prijatelja);

        try
        {
            Intent listIntent = getIntent();

            value  = listIntent.getStringExtra("username");
            index = listIntent.getIntExtra("position", -1);

            ObservableList<Prijatelj> listaPrijatelja = MojiPodaci.getInstance().getMyFriends();

            if(index!=-1)
            {
                prijatelj  = listaPrijatelja.get(index);
                popuniFormu();
            }
            else if (value != null)
            {
                index = MojiPodaci.getInstance().getFriendIndex(value);
                if(index!=-1)
                {
                    prijatelj  = listaPrijatelja.get(index);
                    popuniFormu();
                }
                else
                {
                    finish();
                    return;
                }
            }
            else
            {
                finish();
                return;
            }
        }
        catch (Exception e)
        {
            finish();
            return;
        }

        Intent intent = getIntent();


        context=this;
        Button unfriendBtn = (Button) findViewById(R.id.PregledPrijateljaUnfriendBtn);
        unfriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(context)
                        .setTitle("Prijateljstvo")
                        .setMessage("Da li ste sigurni da zelite da uklonite korisnika " + value + " iz prijatelja?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Ukloni", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                MojiPodaci.getInstance().unfriendUser(value);
                                Intent intent = new Intent(context, MapaActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Odustani",null).show();
            }
        });

    }

    void popuniFormu()
    {

        if(prijatelj != null)
        {

            String username = prijatelj.getUsername();
            String poencic  =  String.valueOf(prijatelj.getPoeni());


            TextView user = (TextView) findViewById(R.id.PregledPrijateljaUsername);
            user.setText(username);

            TextView poe  = (TextView) findViewById(R.id.PregledPrijateljaPoeni);
            poe.setText(poencic);

            ImageView image = (ImageView) findViewById(R.id.PregledPrijateljaSlika);
            if(prijatelj.getProfiSlikaBit() != null)
            {
                image.setImageBitmap(prijatelj.getProfiSlikaBit());
            }
            else
            {
                String link = prijatelj.getProfilePicID();
                Picasso.get().load(link).fit().into(image, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        Drawable drawable = image.getDrawable();
                        if (drawable != null && drawable instanceof BitmapDrawable)
                        {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            prijatelj.setProfiSlikaBit(bitmap);
                            int i = MojiPodaci.getInstance().getFriendIndex(prijatelj.getUsername());
                            if(i>=0)
                                MojiPodaci.getInstance().getMyFriends().set(i,prijatelj);
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        }
    }

}
