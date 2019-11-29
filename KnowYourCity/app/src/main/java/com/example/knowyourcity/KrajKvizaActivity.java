package com.example.knowyourcity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class KrajKvizaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kraj_kviza);

        int tacnih =0, netacnih = 0, poeni = 0, ukupno;
        try
        {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();

            if (positionBundle != null)
            {
                tacnih = positionBundle.getInt("tacnih", 0);
                netacnih = positionBundle.getInt("netacnih", 0);
                poeni = positionBundle.getInt("osvojeni", 0);

            }
        }
        catch (Exception e)
        {
            finish();
            return;
        }

        ukupno = MojiPodaci.getInstance().getThisUser().poeni;

        TextView tac = (TextView)findViewById(R.id.krajKvizaBrojTacnihTV);
        tac.setText(String.valueOf(tacnih));

        TextView net = (TextView)findViewById(R.id.krajKvizaBrojNetacnihTV);
        net.setText(String.valueOf(netacnih));

        TextView poe = (TextView)findViewById(R.id.krajKvizaOsvojeniPoeniTV);
        poe.setText(String.valueOf(poeni));

        TextView uk = (TextView)findViewById(R.id.krajKvizaUkupnoTV);
        uk.setText(String.valueOf(ukupno));

        findViewById(R.id.krajKvizaCloseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(KrajKvizaActivity.this, MapaActivity.class);
                startActivity(intent);
            }
        });

    }
}
