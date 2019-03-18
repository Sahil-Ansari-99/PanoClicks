package com.sphereruler.panoclicks;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sphereruler.panoclicks.Model.Angles;
import com.sphereruler.panoclicks.Model.RootObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class CameraActivity extends Activity implements SensorEventListener {
    private Camera camera;
    private CameraPreview cameraPreview;
    private FloatingActionButton cameraButton;
    SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    View myRectangleView;
    RootObject rootObject;
    float azimuth, roll, pitch, polar;
    float currAzimuth, currPolar;
    float finalAzimuth,finalRoll,finalPitch;
    View upArrow,downArrow,leftArrow,rightArrow;
    List<Float>[] rollingAverage = new List[3];
    List<Angles> anglesList;
    int counter=0;
    String folderTimeStamp;
    private static final int MAX_SAMPLE_SIZE = 20;

    @Override
    protected void onStart() {
        super.onStart();
        String data=loadJSONData();
        loadModelData(data);
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        folderTimeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        camera=getCameraInstance();
        camera.setDisplayOrientation(90);
        cameraPreview=new CameraPreview(this, camera);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.camera_preview);
        frameLayout.addView(cameraPreview);
        myRectangleView=findViewById(R.id.myRectangleView);
        cameraButton=(FloatingActionButton)findViewById(R.id.button_capture);

        Camera.Parameters params= camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);

        rollingAverage[0] = new ArrayList<Float>();
        rollingAverage[1] = new ArrayList<Float>();
        rollingAverage[2] = new ArrayList<Float>();


        upArrow=findViewById(R.id.camera_arrow_up);
        downArrow=findViewById(R.id.camera_arrow_down);
        leftArrow=findViewById(R.id.camera_arrow_left);
        rightArrow=findViewById(R.id.camera_arrow_right);


        String data=loadJSONData();
        loadModelData(data);
        anglesList=rootObject.getAngles();

        currAzimuth= (float) anglesList.get(0).getAzimuth();
        currPolar= (float) anglesList.get(0).getPolar();

        final Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
//                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//                if (pictureFile == null){
//                    Log.d(TAG, "Error creating media file, check storage permissions");
//                    return;
//                }

//                Bitmap pictureMap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                Matrix pictureMatrix=new Matrix();
//                pictureMatrix.postRotate(90);
//                Bitmap rotatedPicture=Bitmap.createBitmap(pictureMap, 0, 0, pictureMap.getWidth(), pictureMap.getHeight(), pictureMatrix, true);
//
//                ByteArrayOutputStream bos=new ByteArrayOutputStream();
//                rotatedPicture.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                byte[] savedBytes=bos.toByteArray();

//                try {
//                    FileOutputStream fos = new FileOutputStream(pictureFile);
//                    fos.write(bytes);
//                    fos.close();
//                } catch (FileNotFoundException e) {
//                    Log.d(TAG, "File not found: " + e.getMessage());
//                } catch (IOException e) {
//                    Log.d(TAG, "Error accessing file: " + e.getMessage());
//                }
//
//                try {
//                    ExifInterface exifInterface = new ExifInterface(pictureFile.getPath());
//                    String imgISO=exifInterface.getAttribute(ExifInterface.TAG_ISO);
//                    Log.d("ISO", imgISO);
//                }catch (IOException e){
//                    e.printStackTrace();
//                }

                try {
                    Bitmap pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = "JPEG_" + timeStamp + "_";
//                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File storageDir = new File(Environment.getExternalStorageDirectory() +
                            File.separator + "PanoClicks");
                    boolean success = true;

                    if(storageDir.isDirectory() && storageDir.exists()){
                        Log.e("Test file", "File exists");
                    }else{
                        success=storageDir.mkdirs();
                    }

                    if (success) {
                        File storageFolder = new File(storageDir + File.separator + folderTimeStamp);
                        boolean folderCreated=true;

                        if(storageFolder.isDirectory() && storageFolder.exists()){
                            Log.e("Test Directory", "Directory exists");
                        }else{
                            folderCreated=storageFolder.mkdirs();
                        }
                        
                        if(folderCreated) {
                            try {
                                File image = File.createTempFile(fileName, ".jpg", storageFolder);
                                FileOutputStream fos = new FileOutputStream(image);
                                pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.close();
                                Toast.makeText(getApplicationContext(), "Image saved in" + storageDir.toString(), Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                camera.startPreview();
            }
        };

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, pictureCallback);
                counter++;
                if(counter<anglesList.size()){
                    currAzimuth= (float) anglesList.get(counter).getAzimuth();
                    currPolar= (float) anglesList.get(counter).getPolar();
                }
                Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_LONG).show();

            }
        });

    }

    public String loadJSONData(){
        String json;
        try{
            AssetManager assetManager=getAssets();
            InputStream stream=assetManager.open("angles.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            json = new String(buffer, "UTF-8");
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
//        Log.e("Test", json);
        return json;
    }

    public void loadModelData(String s){
        Gson gson=new Gson();
//        Log.e("Start","Success");
        rootObject=gson.fromJson(s, RootObject.class);
//        Log.e("Test",rootObject.getCategories().getBundesliga().get(1).getTitle());
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PanoClicks");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];

                rollingAverage[0] = roll(rollingAverage[0], azimuth);
                rollingAverage[1] = roll(rollingAverage[1], pitch);
                rollingAverage[2] = roll(rollingAverage[2], roll);

                finalAzimuth = averageList(rollingAverage[0]);
                finalPitch = averageList(rollingAverage[1]);
                finalRoll = averageList(rollingAverage[2]);

                polar= (float) Math.acos(Math.cos(finalPitch)*Math.cos(finalRoll));
                polar=Math.round(Math.toDegrees(polar));

                finalAzimuth= (float) Math.toDegrees(finalAzimuth);
                finalPitch= (float) Math.toDegrees(finalPitch);
                finalRoll= (float) Math.toDegrees(finalRoll);

                float UpperLimitPolar = currPolar+5;
                float LowerLimitPolar = currPolar-5;

                float UpperLimitAzimuth = currAzimuth+5;
                float LowerLimitAzimuth = currAzimuth-5;

                GradientDrawable myGrad = (GradientDrawable) myRectangleView.getBackground();
                if (polar >= LowerLimitPolar && polar <= UpperLimitPolar) {
                    if (finalAzimuth >= LowerLimitAzimuth && finalAzimuth <= UpperLimitAzimuth) {
                        myGrad.setStroke(2, Color.GREEN);
                        Toast.makeText(getApplicationContext(), String.valueOf(polar) + " + " + String.valueOf(finalAzimuth), Toast.LENGTH_SHORT).show();
                        upArrow.setVisibility(View.INVISIBLE);
                        downArrow.setVisibility(View.INVISIBLE);
                        leftArrow.setVisibility(View.INVISIBLE);
                        rightArrow.setVisibility(View.INVISIBLE);
                    }
                    else if(finalAzimuth>=UpperLimitAzimuth)
                    {
                        leftArrow.setVisibility(View.VISIBLE);
                        rightArrow.setVisibility(View.INVISIBLE);
                        myGrad.setStroke(2, Color.RED);
                    }
                    else
                    {
                        leftArrow.setVisibility(View.INVISIBLE);
                        rightArrow.setVisibility(View.VISIBLE);
                        myGrad.setStroke(2, Color.RED);
                    }
                }
                else {

                    if (polar >= UpperLimitPolar) {
                        upArrow.setVisibility(View.INVISIBLE);
                        downArrow.setVisibility(View.VISIBLE);
                    }
                    else {
                        downArrow.setVisibility(View.INVISIBLE);
                        upArrow.setVisibility(View.VISIBLE);
                    }

                    if (finalAzimuth >= LowerLimitAzimuth && finalAzimuth <= UpperLimitAzimuth) {
                        leftArrow.setVisibility(View.INVISIBLE);
                        rightArrow.setVisibility(View.INVISIBLE);
                    }
                    else if (finalAzimuth <= LowerLimitAzimuth) {
                        leftArrow.setVisibility(View.INVISIBLE);
                        rightArrow.setVisibility(View.VISIBLE);
                    }
                    else {
                        leftArrow.setVisibility(View.VISIBLE);
                        rightArrow.setVisibility(View.INVISIBLE);
                    }


                    myGrad.setStroke(2, Color.RED);
                    Toast.makeText(getApplicationContext(), String.valueOf(polar) + " + " + String.valueOf(finalAzimuth), Toast.LENGTH_SHORT).show();
                }


            }
        }
    }

    public List<Float> roll(List<Float> list, float newMember){
        if(list.size() == MAX_SAMPLE_SIZE){
            list.remove(0);
        }
        list.add(newMember);
        return list;
    }

    public float averageList(List<Float> tallyUp){

        float total=0;
        for(float item : tallyUp ){
            total+=item;
        }
        total = total/tallyUp.size();

        return total;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
