package com.example.knowyourcity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DodajPitanjeActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private String key;
    private Button okButton;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje);

        context=this;

        Intent intent = getIntent();

        try
        {
            Intent listIntent = getIntent();
            key = intent.getStringExtra("key");

            if (key == null)
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

        okButton = (Button) findViewById(R.id.btndodajPitanjeSacuvaj);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btndodajPitanjeSacuvaj:
            {
                String text, tac, netac, netac2;
                EditText tekst = (EditText)findViewById(R.id.dodajPitanjeActivityPitanjeET);
                text = tekst.getText().toString();

                EditText tacan = (EditText)findViewById(R.id.dodajPitanjeActivityTacanOdgovorET);
                tac = tacan.getText().toString();

                EditText netacan = (EditText)findViewById(R.id.dodajPitanjeActivityNetacanET);
                netac = netacan.getText().toString();

                EditText netacan2 = (EditText)findViewById(R.id.dodajPitanjeActivityNetacan2ET);
                netac2 = netacan2.getText().toString();

                if(text.isEmpty() || tac.isEmpty() || netac.isEmpty() || netac2.isEmpty())
                {
                    Toast.makeText(this, "Nisu popunjena sva polja", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Korisnik kor = MojiPodaci.getInstance().getThisUser();
                    Pitanje q = new Pitanje(text,tac,netac,netac2);
                    MojiPodaci.getInstance().addQuestion(q,key);

                    kor.poeni += this.getResources().getInteger(R.integer.novoPitanje);
                    MojiPodaci.getInstance().updateUserPoints(kor.korisnickoIme,kor.poeni);

                }

                setResult(Activity.RESULT_OK);
                Intent intent = new Intent(DodajPitanjeActivity.this, PregledObjektaActivity.class);
                intent.putExtra("position", MojiPodaci.getInstance().getPlaceIndex(key));
                startActivity(intent);

                break;
            }
        }
    }


}
