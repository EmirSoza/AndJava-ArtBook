package com.emirhan.myartbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        nameArray = new ArrayList<String>(){};
        idArray = new ArrayList(){};
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArray);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Addart.class);
                intent.putExtra("artId", idArray.get(position));
                intent.putExtra("info","old");
                startActivity(intent);
            }
        });
        getData();
    }

    public void getData(){

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM arts", null);
            int nameIndex = cursor.getColumnIndex("art");
            int id = cursor.getColumnIndex("id");
            while (cursor.moveToNext()){
                nameArray.add(cursor.getString(nameIndex));
                idArray.add(cursor.getInt(id));
            }
            //Olaydan sonra listView i haberdar etmek icin
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e){e.printStackTrace();}


    }
    //First right click to res create directory
    //right click to directory(menu we called) new-menu resource file
    //name it then set id and title on xml
    //then create these functions-well override

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater- in order to use created xml files
        //inflater gets the items

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Here is for after selecting item what will happen
        //if the selected id == add_art_item navigate to second page
        if(item.getItemId() == R.id.add_art_item) {
            Intent intent = new Intent(MainActivity.this, Addart.class);
            //bu info amaci listeden mi yoksa menu iconundan mi tiklandi onu ayirt etmek icin
            //new ise menu den old ise listview dan tiklanmistir
            //ona gore addart sayfasinin gorunumunu yapacagiz
            intent.putExtra("info", "new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}