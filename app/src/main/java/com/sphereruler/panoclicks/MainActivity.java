package com.sphereruler.panoclicks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sphereruler.panoclicks.Adapter.HomeAdapter;
import com.sphereruler.panoclicks.Model.FileModel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton cameraButton;
    private static int CAMERA_REQUEST_CODE=1888;

    ArrayList<FileModel> fileList;
    HomeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=(RecyclerView)findViewById(R.id.home_recyclerView);
        cameraButton=(FloatingActionButton)findViewById(R.id.home_cameraButton);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),LinearLayoutManager.VERTICAL));

        fileList=new ArrayList<>();

        String path = Environment.getExternalStorageDirectory().toString()+"/PanoClicks";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: 0"+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            FileModel model = new FileModel(files[i].getName(),files[i].lastModified());
            fileList.add(model);
        }

        adapter=new HomeAdapter(fileList,getApplicationContext());

        recyclerView.setAdapter(adapter);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
    }

    public void openCamera(){
        Intent intent=new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
    }



}
