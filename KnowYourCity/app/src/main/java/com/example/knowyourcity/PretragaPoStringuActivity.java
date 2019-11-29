package com.example.knowyourcity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PretragaPoStringuActivity extends AppCompatActivity {

    SearchView mySearchView;
    ListView myList;
    ArrayAdapter<MojObjekat> adapter;
    ArrayList<MojObjekat> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pretraga_po_stringu);

        mySearchView = (SearchView) findViewById(R.id.serachView);
        myList = (ListView) findViewById(R.id.list_view_pretragaPoStringu);


        list = new ArrayList<>(MojiPodaci.getInstance().getMyPlaces());


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        myList.setAdapter(adapter);
        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                MojObjekat ob = adapter.getItem(position);
                if(ob!=null) {
                    Intent intent = new Intent(PretragaPoStringuActivity.this, PregledObjektaActivity.class);
                    intent.putExtra("key", ob.key);
                    startActivity(intent);
                }
            }
        });
    }


}
