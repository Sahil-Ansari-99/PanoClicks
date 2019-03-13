package com.sphereruler.panoclicks;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {
    private Camera camera;
    private CameraPreview cameraPreview;
    private FloatingActionButton cameraButton;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera=getCameraInstance();
        camera.setDisplayOrientation(90);
        cameraPreview=new CameraPreview(this, camera);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.camera_preview);
        frameLayout.addView(cameraPreview);

        cameraButton=(FloatingActionButton)findViewById(R.id.button_capture);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
}
