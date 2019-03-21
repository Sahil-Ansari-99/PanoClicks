package com.sphereruler.panoclicks;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sphereruler.panoclicks.Model.Angles;
import com.sphereruler.panoclicks.Model.RootObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class CameraActivity extends Activity implements SensorEventListener {
    private Camera camera;
    private CameraPreview cameraPreview;
    private FloatingActionButton cameraButton;
    SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    View myCircleView;
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
        myCircleView=findViewById(R.id.myCircleView);
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
                saveImage(bytes);
                camera.startPreview();
            }
        };

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    camera.takePicture(null, null, pictureCallback);
                    counter++;
                    if (counter < anglesList.size()) {
                        currAzimuth = (float) anglesList.get(counter).getAzimuth();
                        currPolar = (float) anglesList.get(counter).getPolar();
                    }
                    Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        String path = Environment.getExternalStorageDirectory().toString()+"/PanoClicks";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: 0"+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }

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
//        resumeCamera();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

//    public void resumeCamera(){
//        camera=getCameraInstance();
//        camera.setDisplayOrientation(90);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        releaseCamera();
    }

    public void releaseCamera(){
        if(camera!=null){
            camera.release();
            camera=null;
        }
    }

    float[] mGravity;
    float[] mGeomagnetic;

    float[] mGravityFiltered;
    float[] mGeomagneticFiltered;

    public float[] lowPassFilter(float input[],float output[])
    {float alpha=0.025f;
        if(output==null)
            return input;

        for(int i=1;i<input.length;i++)
            output[i]=alpha*input[i] + (1-alpha)*(output[i]);

        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            mGeomagneticFiltered=lowPassFilter(mGeomagnetic,mGeomagneticFiltered);
            mGravityFiltered=lowPassFilter(mGravity,mGravityFiltered);

            final Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    saveImage(bytes);
                    camera.startPreview();
                }
            };

            boolean successMatrix = SensorManager.getRotationMatrix(R, I, mGravityFiltered, mGeomagneticFiltered);
            if (successMatrix) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];

//                rollingAverage[0] = roll(rollingAverage[0], azimuth);
//                rollingAverage[1] = roll(rollingAverage[1], pitch);
//                rollingAverage[2] = roll(rollingAverage[2], roll);
//
//                finalAzimuth = averageList(rollingAverage[0]);
//                finalPitch = averageList(rollingAverage[1]);
//                finalRoll = averageList(rollingAverage[2]);

                polar= (float) Math.acos(Math.cos(pitch)*Math.cos(roll));
                polar=Math.round(Math.toDegrees(polar));

                azimuth= (float) Math.toDegrees(azimuth);
                pitch= (float) Math.toDegrees(pitch);
                roll= (float) Math.toDegrees(roll);

                compareAngle(azimuth,polar);

            }
        }
    }

    public void compareAngle(float azimuth,float polar)
    {

        float UpperLimitPolar = currPolar+5;
        float LowerLimitPolar = currPolar-5;

        float UpperLimitAzimuth = currAzimuth+7;
        float LowerLimitAzimuth = currAzimuth-7;

        GradientDrawable myGrad = (GradientDrawable) myCircleView.getBackground();
        if (polar >= LowerLimitPolar && polar <= UpperLimitPolar) {
            if (azimuth >= LowerLimitAzimuth && azimuth <= UpperLimitAzimuth) {
                myGrad.setStroke(2, Color.GREEN);
                Toast.makeText(getApplicationContext(), String.valueOf(polar) + " + " + String.valueOf(azimuth), Toast.LENGTH_SHORT).show();
                upArrow.setVisibility(View.INVISIBLE);
                downArrow.setVisibility(View.INVISIBLE);
                leftArrow.setVisibility(View.INVISIBLE);
                rightArrow.setVisibility(View.INVISIBLE);

//                        camera.takePicture(null, null, pictureCallback);
//                        counter++;
//                        if(counter<anglesList.size()){
//                            currAzimuth= (float) anglesList.get(counter).getAzimuth();
//                            currPolar= (float) anglesList.get(counter).getPolar();
//                        }
//                        Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_LONG).show();

            }
            else if(azimuth>=UpperLimitAzimuth)
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

            if (azimuth >= LowerLimitAzimuth && azimuth <= UpperLimitAzimuth) {
                leftArrow.setVisibility(View.INVISIBLE);
                rightArrow.setVisibility(View.INVISIBLE);
            }
            else if (azimuth <= LowerLimitAzimuth) {
                leftArrow.setVisibility(View.INVISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
            }
            else {
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.INVISIBLE);
            }


            myGrad.setStroke(2, Color.RED);
            Toast.makeText(getApplicationContext(), String.valueOf(polar) + " + " + String.valueOf(azimuth), Toast.LENGTH_SHORT).show();
        }

    }


    public void saveImage(final byte[] imageArray){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap pic = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = "JPEG_" + timeStamp + "_";
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
//                                            Toast.makeText(getApplicationContext(), "Image saved in" + storageDir.toString(), Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }

            }
        };

        Thread thread=new Thread(runnable);
        thread.start();
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
