package seoulapp.chok.rokseoul.drawingtool.sensorset;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by choiseongsik on 2016. 9. 30..
 */

public class SensorSet2 implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mAzimuth = 0; // degree
    private float fAzimuth = 0;
    float[] orientation = new float[3];
    float[] rMat = new float[9];
    Context context;
    private int preAZ=0;
    private String preDI;
    private int count=0;

    public SensorSet2(Context context){
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if( sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            preAZ = mAzimuth;
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector( rMat, sensorEvent.values );
            // get the azimuth value (orientation[0]) in degree
            fAzimuth = (float) Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] );
            //mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
            mAzimuth = (int) (fAzimuth+360) %360;

            Log.d("sensorset", "degree : "+mAzimuth + " / preAZ : " + fAzimuth+ "/ 방위 :" + getDirectionFromDegrees(fAzimuth));
            count++;
            if(getDirectionFromDegrees(fAzimuth) != preDI && count>20){
                preDI = getDirectionFromDegrees(fAzimuth);
                Toast.makeText(context, "방위 : "+getDirectionFromDegrees(fAzimuth)
                        + " / degree : " + mAzimuth
                        , Toast.LENGTH_SHORT).show();
                count = 0;
            }
            if(count==50) {
                Toast.makeText(context, "센서값이 안정되었습니다. 저장하세요!", Toast.LENGTH_LONG).show();
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
    public Integer getSensorDegree() { return mAzimuth; }

    public void onResume() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }
}
