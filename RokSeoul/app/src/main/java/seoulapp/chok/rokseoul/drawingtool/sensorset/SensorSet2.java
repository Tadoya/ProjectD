package seoulapp.chok.rokseoul.drawingtool.sensorset;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import seoulapp.chok.rokseoul.R;
import seoulapp.chok.rokseoul.firebase.StorageSet;

/**
 * Created by choiseongsik on 2016. 9. 30..
 */

public class SensorSet2 implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mAzimuth = 0; // degree
    private int mPitch = 0;
    private int mRoll = 0;
    private float fAzimuth = 0;
    private float fPitch=0;
    private float fRoll=0;

    float[] orientation = new float[3];
    float[] rMat = new float[9];

    float[] inRotationMatrix = new float[16];
    float[] outRotationMatrix = new float[16];
    private Activity context;
    private int preAZ=0;
    private String preDI;
    private int count=0;
    private int unstableCount = 0;


    private StorageSet mStorageSet;
    private TextView drawing_degree;


    public SensorSet2(Activity acvitivy, StorageSet storageSet){
        this.context = acvitivy;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mStorageSet = storageSet;
        drawing_degree = (TextView) acvitivy.findViewById(R.id.drawing_degree);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if( sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            count++;
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(inRotationMatrix, sensorEvent.values);
            SensorManager.remapCoordinateSystem(inRotationMatrix, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, outRotationMatrix);
            SensorManager.getOrientation(outRotationMatrix, orientation);

            fAzimuth = (float) Math.toDegrees( orientation[0] );
            fPitch = (float) Math.toDegrees( orientation[1] );
            fRoll = (float) Math.toDegrees( orientation[2] );

            mAzimuth = (int) (fAzimuth+360) % 360;
            mPitch = (int) (fPitch+90) % 180;
            mRoll = (int) (fRoll+360) % 360;

            /*
            mAzimuth = (int) fAzimuth;
            mPitch = (int) fPitch;
            mRoll = (int) fRoll;
            */
            drawing_degree.setText("A : "+ mAzimuth+"("+getDirectionFromDegrees(fAzimuth)+")"
            +"\nP : "+mPitch +"\nR : " + mRoll);
            Log.d("sensorset", count + " / degree : "+mAzimuth + " / preAZ : " + fAzimuth+ "/ 방위 :" + getDirectionFromDegrees(fAzimuth));
            if(count%5 == 0 ){
                if(mStorageSet.getDownloadCheck() && unstableCount<=5) {
                    try {
                        mStorageSet.showDoodles(mAzimuth, mPitch, mRoll);
                    }catch (Exception e){
                        Log.d("sensorcheck", "burningtime"+e);
                    }
                }
            }
            if(count%20 == 0) {
                if(Math.abs(mAzimuth-preAZ) > 5 && !getDirectionFromDegrees(fAzimuth).equals(preDI)){
                    unstableCount++;
                    preAZ = mAzimuth;
                    preDI = getDirectionFromDegrees(fAzimuth);
                    if (unstableCount <= 5) {
                        Log.d("sensorcheck", unstableCount + " / 방위 : "+preDI+" / degree : "+mAzimuth);
                        count= 0;
                    } else if (unstableCount == 6) {
                        Toast.makeText(context, "센서 상태가 불안정합니다.\n주변에 자성물체가 있는지 확인하세요!", Toast.LENGTH_LONG).show();
                        unstableCount = 0;
                    }
                }
            }
            if (count == 80) {
                Toast.makeText(context, "방위 : " + preDI + " 센서값이 안정되었습니다.", Toast.LENGTH_LONG).show();
                unstableCount = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private String getDirectionFromDegrees(float degrees) {
        if(degrees >= -22.5 && degrees < 22.5) { return "N"; }
        if(degrees >= 22.5 && degrees < 67.5) { return "NE"; }
        if(degrees >= 67.5 && degrees < 112.5) { return "E"; }
        if(degrees >= 112.5 && degrees < 157.5) { return "SE"; }
        if(degrees >= 157.5 || degrees < -157.5) { return "S"; }
        if(degrees >= -157.5 && degrees < -112.5) { return "SW"; }
        if(degrees >= -112.5 && degrees < -67.5) { return "W"; }
        if(degrees >= -67.5 && degrees < -22.5) { return "NW"; }

        return null;
    }

    public String getSensorDirection(){ return getDirectionFromDegrees(fAzimuth); }
    public Integer getSensorAzimuth() { return mAzimuth; }
    public Integer getSensorPitch() { return  mPitch; }
    public Integer getSensorRoll() { return mRoll; }

    public void onResume() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }
}
