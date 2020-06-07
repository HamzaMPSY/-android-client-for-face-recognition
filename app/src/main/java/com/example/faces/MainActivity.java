package com.example.faces;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button CaptureImageFromCamera,add,predict;
    ImageView ImageViewHolder;
    Intent intent ;
    public  static final int RequestPermissionCode  = 1 ;
    Bitmap bitmap;
    String url ="" ;
    TextView editText ;
    EditText name,ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CaptureImageFromCamera = findViewById(R.id.btn1);
        ImageViewHolder = findViewById(R.id.img);
        add =  findViewById(R.id.btn2);
        predict = findViewById(R.id.btn3);
        editText = findViewById(R.id.txt2);
        name = findViewById(R.id.name);
        ip = findViewById(R.id.ip);

        EnableRuntimePermissionToAccessCamera();

        CaptureImageFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, 7);

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ip.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"Please Provide the IP address of the Host",Toast.LENGTH_LONG).show();
                }
                else if(name.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"Please Provide the name of the person in the picture",Toast.LENGTH_LONG).show();
                }
                else {
                    BitmapDrawable drawable = (BitmapDrawable) ImageViewHolder.getDrawable();
                    bitmap = drawable.getBitmap();
                    url = "http://"+ip.getText().toString()+":8000/add";
                    ImageUploadToServerFunction(name.getText().toString());
                }
            }
        });
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ip.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"Please Provide the IP address of the Host",Toast.LENGTH_LONG).show();
                }
                else{
                    BitmapDrawable drawable = (BitmapDrawable) ImageViewHolder.getDrawable();
                    bitmap = drawable.getBitmap();
                    url = "http://"+ip.getText().toString()+":8000/predict";
                    ImageUploadToServerFunction(null);
                }
            }
        });
    }

    // Star activity for result method to Set captured image on image view after click.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            // Adding captured image in bitmap.
            Glide.with(this).load(uri).into(ImageViewHolder);

        }
    }

    // Requesting runtime permission to access camera.
    public void EnableRuntimePermissionToAccessCamera(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA))
        {

            // Printing toast message after enabling runtime permission.
            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    // Upload captured image online on server function.
    public void ImageUploadToServerFunction(final String name){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        VolleyMultiPartRequest request = new VolleyMultiPartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    String txt = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    Log.d("debugg",txt);
                    editText.setText(txt);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("debugg",error.toString());
            }
        }){
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String,DataPart> params = new HashMap<>();
                Random t =  new Random();
                int a = t.nextInt(9999);
                params.put("img", new DataPart("image"+a+".jpg", getFileDataFromDrawable(Bitmap.createScaledBitmap(bitmap, 720, 1080, false)), "image/jpeg"));
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("name",name);
                return params;
            }
        };

        queue.add(request);
        queue.start();
    }

    public static byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        switch(RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}