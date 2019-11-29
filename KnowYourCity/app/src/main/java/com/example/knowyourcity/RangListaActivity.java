package com.example.knowyourcity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RangListaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_lista);

        ListView gridView = findViewById(R.id.rang_lista_gv);

        ArrayList<Prijatelj> lista = new ArrayList<>(MojiPodaci.getInstance().getMyFriends());

        Korisnik me = MojiPodaci.getInstance().getThisUser();
        lista.add(new Prijatelj(me.korisnickoIme,me.koo,me.poeni,me.profilePicID));


        FriendsListAdapter fA = new FriendsListAdapter(this,R.layout.friend_in_grid_layout,lista);
        gridView.setAdapter(fA);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Prijatelj p = lista.get(position);
                Intent intent = new Intent(RangListaActivity.this, PregledPrijateljaActivity.class);
                intent.putExtra("username", p.getUsername());
                startActivity(intent);

            }
        });

    }
}
