package com.sphereruler.panoclicks;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    TextView testAzimuth;
    View myRectangleView;
    float azimuth, roll, pitch;
    float finalAzimuth,finalRoll,finalPitch;
    List<Float>[] rollingAverage = new List[3];
    private static final int MAX_SAMPLE_SIZE = 10;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        camera=getCameraInstance();
        camera.setDisplayOrientation(90);
        cameraPreview=new CameraPreview(this, camera);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.camera_preview);
        frameLayout.addView(cameraPreview);
        testAzimuth=findViewById(R.id.testAzimuth);
        myRectangleView=findViewById(R.id.myRectangleView);
        cameraButton=(FloatingActionButton)findViewById(R.id.button_capture);

        rollingAverage[0] = new ArrayList<Float>();
        rollingAverage[1] = new ArrayList<Float>();
        rollingAverage[2] = new ArrayList<Float>();

        final Camera.PictureCallback pictureCallback=new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(bytes);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, pictureCallback);
                Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_LONG).show();
            }
        });

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

                azimuth= (float) Math.toDegrees(azimuth);
                pitch= (float) Math.toDegrees(pitch);
                roll= (float) Math.toDegrees(roll);

                rollingAverage[0] = roll(rollingAverage[0], azimuth);
                rollingAverage[1] = roll(rollingAverage[1], pitch);
                rollingAverage[2] = roll(rollingAverage[2], roll);

                finalAzimuth = averageList(rollingAverage[0]);
                finalPitch = averageList(rollingAverage[1]);
                finalRoll = averageList(rollingAverage[2]);

                if(finalAzimuth>-170 && finalAzimuth<-160) {
                    GradientDrawable myGrad = (GradientDrawable) myRectangleView.getBackground();
                    myGrad.setStroke(2, Color.GREEN);
                    Toast.makeText(getApplicationContext(), String.valueOf(finalAzimuth), Toast.LENGTH_SHORT).show();
                }
                else{
                    GradientDrawable myGrad = (GradientDrawable) myRectangleView.getBackground();
                    myGrad.setStroke(2, Color.RED);
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
