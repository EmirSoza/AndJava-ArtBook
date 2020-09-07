package com.emirhan.myartbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Addart extends AppCompatActivity {
    EditText artName;
    EditText artist;
    EditText year;
    Bitmap selectedImage;
    ImageView imageView;
    Button button;
    SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addart);
        artName = findViewById(R.id.artName);
        artist = findViewById(R.id.artistName);
        year = findViewById(R.id.artYear);
        imageView=findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        int idIndex = intent.getIntExtra("artId",1);

        if(info.matches("new")){
            //navigated this activity to add new data
            artName.setText("");
            artist.setText("");
            year.setText("");
            button.setVisibility(View.VISIBLE);
            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.selected);
            imageView.setImageBitmap(selectImage);

        }
        else{
            //Old clicked means we want to show the clicked information
            int artId = intent.getIntExtra("artId",1);
            button.setVisibility(View.INVISIBLE);
            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[]{String.valueOf(artId)});
                int artIx = cursor.getColumnIndex("art");
                int artistIx = cursor.getColumnIndex("artist");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    artName.setText(cursor.getString(artIx));
                    artist.setText(cursor.getString(artistIx));
                    year.setText(cursor.getString(yearIx));
                    byte[] imageByte = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte,0, imageByte.length);
                    imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void selectImage(View view){
        //If user granted to be reached to gallery or not
        //For permissions-there are levels like normal - dangerous
        //for example internet usage is normal so its enough to write only in manifest
        //but reading storage is dangerous so we have to ask user if he grants

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2&&resultCode==RESULT_OK&&data!=null){
            Uri imageData = data.getData();
            try {
                //Galeriden secim islemi 28 sdk oncesinde sorun cikariyor
                //O yuzden 28 oncesi ve sonrasi icin farkli sekilde sececegiz
                //Ayrica problem olmamasi icin try catch icinde yapiyoruz garanti olsun diye
                if(Build.VERSION.SDK_INT >= 28){
                    //Daha yeni bi method
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }
                else{
                    //Depreciated oldugu icin yeni versiyonda sikinti
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){
            String art = artName.getText().toString();
            String artistName = artist.getText().toString();
            String yearPub = year.getText().toString();

            Bitmap smallImage = makeSmaller(selectedImage,300);
            //SQLite da buyuk dosyalarla calismak sikinti oldugundan dolayi sectigimiz image i olusturdugumuz func ile kuculttuk
        // Sonra da bytearray haline getirdik
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream );
            byte[] byteArray = outputStream.toByteArray();

            try {
                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, art VARCHAR, artist VARCHAR, year VARCHAR, image BLOB )");
                //cannot assign the values directly
                //database.execSQL("INSERT INTO arts (art,artist,year,image) VALUES (art,artistName,yearPub,byteArray)");
                String sqlString = "INSERT INTO arts (art,artist,year,image) VALUES (?,?,?,?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                sqLiteStatement.bindString(1,art);
                sqLiteStatement.bindString(2,artistName);
                sqLiteStatement.bindString(3,yearPub);
                sqLiteStatement.bindBlob(4,byteArray);
                sqLiteStatement.execute();


            } catch (Exception e){}


            //Finish bu sayfayi kapatipm main activity e doner ancak onCreate cagirilmadigi icin Kaydettigimiz verileri goremeyiz
        // restart acinca ancak gorulebilir o yuzden bu aktiviteyi bitirirken mainactivity onCreate tekrar cagirilmali
        // bunu da intent in bir cesidiyle yapcaz
            //finish();
        Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


    }

    public Bitmap makeSmaller(Bitmap image, int maxSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float scale = (float)width/(float)height;
        if(scale >1){
            //horizontal image
            width= maxSize;
            height = (int)(width / scale);
        }
        else {
            //vertical image
            height= maxSize;
            width = (int )(height*scale);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

}