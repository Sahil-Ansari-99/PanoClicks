package com.sphereruler.panoclicks;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {
    private Camera camera;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera=getCameraInstance();
        cameraPreview=new CameraPreview(this, camera);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.camera_preview);
        frameLayout.addView(cameraPreview);
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
