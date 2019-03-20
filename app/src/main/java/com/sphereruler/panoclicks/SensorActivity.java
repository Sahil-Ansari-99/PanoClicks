package com.sphereruler.panoclicks;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends Activity implements SensorEventListener {
    TextView polarAngle, azimuthAngle, pitchAngle,zAngle;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    float azimuth, roll, pitch, polar;
    final  int MAX_SAMPLE_SIZE=10;
    List<Float>[] rollingAverage = new List[3];
    float finalAzimuth,finalPitch,finalRoll;
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
        zAngle=findViewById(R.id.zAngle);

        rollingAverage[0] = new ArrayList<Float>();
        rollingAverage[1] = new ArrayList<Float>();
        rollingAverage[2] = new ArrayList<Float>();

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

            boolean success = SensorManager.getRotationMatrix(R, I, mGravityFiltered, mGeomagneticFiltered);
            if (success) {
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


//                azimuth= (float) Math.toDegrees(azimuth);
//                pitch= (float) Math.toDegrees(pitch);
//                roll= (float) Math.toDegrees(roll);

                polar = (float) Math.acos(Math.cos(pitch)*Math.cos(roll));


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
            zAngle.setText(""+Math.round(Math.toDegrees(polar)));

        }


    }
}



