package com.sphereruler.panoclicks;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
    TextView polarAngle, azimuthAngle, pitchAngle;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    Sensor angle;
    float azimuth, roll, pitch;
//    float mAzimuth,mPitch,mRoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        setContentView(R.layout.activity_sensor);

        polarAngle = (TextView) findViewById(R.id.polarAngle);
        azimuthAngle = (TextView) findViewById(R.id.azimuthAngle);
        pitchAngle = (TextView) findViewById(R.id.pitchAngle);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
                // orientation contains: azimut, pitch and roll
            }
//        if (event.sensor.getType()==Sensor.TYPE_ORIENTATION) {
//
//            mAzimuth = event.values[0];
//             mPitch = event.values[1];
//             mRoll = event.values[2];
//
//        }
//        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
//        {
//            Log.d("Test", "Inside");
//
//            float[] mRotationMatrix=new float[9];
//            float[] orientationVals=new float[3];
//            float[] test=new float[9];
//            // Convert the rotation-vector to a 4x4 matrix.
//            SensorManager.getRotationMatrixFromVector(mRotationMatrix,
//                    event.values);
//            SensorManager
//                    .remapCoordinateSystem(mRotationMatrix,
//                            SensorManager.AXIS_X, SensorManager.AXIS_Z,
//                            test);
//            SensorManager.getOrientation(test, orientationVals);
//
//            // Optionally convert the result from radians to degrees
//            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
//            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
//            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);
//
//            polarAngle.setText(""+orientationVals[2]);
//            azimuthAngle.setText(""+orientationVals[0]);
//            pitchAngle.setText(""+orientationVals[1]);
//
//        }
            polarAngle.setText("" + Math.round(Math.toDegrees(roll)));
            azimuthAngle.setText("" + Math.round(Math.toDegrees(azimuth)));
            pitchAngle.setText("" + Math.round(Math.toDegrees(pitch)));

        }


    }
}



