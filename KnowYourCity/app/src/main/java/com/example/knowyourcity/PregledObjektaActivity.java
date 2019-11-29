package com.example.knowyourcity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PregledObjektaActivity extends AppCompatActivity implements View.OnClickListener{

    int index;
    MojObjekat objekat;
    ImageView slika;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_objekta);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        key = null;

        try
        {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();

            if (positionBundle != null)
            {
                index = positionBundle.getInt("position", -1);
                key = positionBundle.getString("key");
                if(index != -1)
                {
                    objekat = MojiPodaci.getInstance().getPlace(index);
                }
                else if(key != null)
                {
                   index = MojiPodaci.getInstance().getPlaceIndex(key);
                    if(index != -1)
                    {
                        objekat = MojiPodaci.getInstance().getPlace(index);
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
        }
        catch (Exception e)
        {
            finish();
            return;
        }

        Button kviz = (Button) findViewById(R.id.pregledObjektaActivityPlay);
        Button izmeni = (Button) findViewById(R.id.pregledObjektaActivityIzmeni);
        slika = (ImageView) findViewById(R.id.pregledObjektaActivitySlikaObjekta);
        kviz.setOnClickListener(this);
        izmeni.setOnClickListener(this);

        String link   = objekat.objectPicID;
        Picasso.get().load(link).into(slika);

        TextView naziv = (TextView) findViewById(R.id.pregledObjektaActivityNaziv);
        TextView opis = (TextView) findViewById(R.id.pregledObjektaActivityOpis);

        naziv.setText(objekat.name);
        opis.setText(objekat.description);

        TextView kategorija = (TextView)  findViewById(R.id.pregledObjektaActivityKategorija);
        kategorija.setText(objekat.kategorija);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.fab:
            {
                Intent intent = new Intent(PregledObjektaActivity.this, DodajPitanjeActivity.class);
                intent.putExtra("key", objekat.key);
                startActivity(intent);
                break;
            }
            case R.id.pregledObjektaActivityPlay:
            {
               // Toast.makeText(this,"Pocni kviz", Toast.LENGTH_LONG).show();

                if(objekat.listaPitanja != null && objekat.listaPitanja.size()>0)
                {
                    Intent intent = new Intent(PregledObjektaActivity.this, PregledPitanjaActivity.class);
                    intent.putExtra("position",index);
                    startActivity(intent);
                }
                else
                    Snackbar.make(findViewById(R.id.cooridnator_layout_pregled_objekta),"Objekat nema ni jedno pitanje!",Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(this,"Objekat nema ni jedno pitanje!", Toast.LENGTH_LONG).show();


                break;
            }
            case R.id.pregledObjektaActivityIzmeni:
            {
                Intent intent = new Intent(PregledObjektaActivity.this, DodajNoviObjekatActivity.class);
                intent.putExtra("position", index);
                startActivity(intent);

            }

        }

    }
}
