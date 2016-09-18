package seoulapp.chok.rokseoul.drawingtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import seoulapp.chok.rokseoul.firebase.StorageSet;
import seoulapp.chok.rokseoul.R;

/**
 * This is demo code to accompany the Mobiletuts+ tutorial series:
 * - Android SDK: Create a Drawing App
 *
 * Sue Smith
 * August 2013
 * Modified by Tadoya
 *
 */
public class DrawingActivity extends AppCompatActivity implements OnClickListener, SensorEventListener {

    //custom drawing view
    private DrawingView drawView;
    //buttons
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, loadBtn;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    public static int RESULT_LOAD_IMAGE = 1;

    private ImageView imageView;
    //private SurfaceView surfaceView;

    private FrameLayout camera_view;

    private LocationManager locationManager;
    // GPS 프로바이더 사용가능여부
    private Boolean isGPSEnabled;
    // 네트워크 프로바이더 사용가능여부
    private Boolean isNetworkEnabled;

    /** Sensor 메니저 **/
    private SensorManager sm;
    private Sensor s;

    public String OrientationData = "";
    public String GPSData = "";

    public float Roll;
    public float Pitch;
    public float Yaw;

    public double lat;
    public double lng;

    private Context mContext;


    private StorageSet storageSet;
    private String placeName = "royalpalace";
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);


        mAuth = FirebaseAuth.getInstance();
        storageSet = new StorageSet(this);

        //get drawing view
        drawView = (DrawingView)findViewById(R.id.drawing);

        //get the palette and first color button
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //draw button
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //set initial size
        drawView.setBrushSize(smallBrush);

        //erase button
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //load button
        loadBtn = (ImageButton)findViewById(R.id.load_btn);
        loadBtn.setOnClickListener(this);

        imageView = (ImageView)findViewById(R.id.imageView);
        //surfaceView = (SurfaceView)findViewById(R.id.surfaceView);

        camera_view = (FrameLayout)findViewById(R.id.camera_view);

        mContext = this;


        OriantationTool(); /** 방향 높이 각도 센서 설정 및 리스너 시작 **/
        //GPSTool(); /** GPS 위치 센서 설정 및 리스너 시작 **/


    }

    private void cameraOn(){
        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera_view.removeView(mCameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraOn();
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    //user clicked paint
    public void paintClicked(View view){
        //use chosen color

        //set erase false
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());

        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }


    @Override
    public void onClick(View view){

        if(view.getId()==R.id.draw_btn){
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            //show and wait for user interaction
            brushDialog.show();
        }
        else if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        }
        else if(view.getId()==R.id.new_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    drawView.startNew();
                    dialog.dismiss();
                    imageView.setImageResource(android.R.color.transparent);
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }
        else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to firebase?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);

                    //메모리그림을 변환
                    Bitmap screenshot = Bitmap.createBitmap(drawView.getDrawingCache());

                    //firebase에 저장
                    storageSet.uploadFromMemory(screenshot, placeName, mAuth.getCurrentUser().getUid());

                    drawView.destroyDrawingCache();
                    drawView.startNew();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }
        else if(view.getId()==R.id.load_btn){
            Log.d("SAVE", "clicked load_btn");
            AlertDialog.Builder loadDialog = new AlertDialog.Builder(this);
            loadDialog.setTitle("Load drawing");
            loadDialog.setMessage("Load drawing to device Gallery?");
            loadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent tmpl = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(tmpl, RESULT_LOAD_IMAGE);
                }
            });
            loadDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            loadDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Here we need to check if the activity that was triggers was the Image Gallery.
        // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            imageView.setImageBitmap(bitmap);
            //drawView.setImageBitmap(bitmap);
            // Do something with the bitmap


            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
    }

    private void GPSTool() {
        Toast toast = Toast.makeText(this, "GPSTool 진입", Toast.LENGTH_SHORT);
        toast.show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); /** 시스템 서비스에 LocationManager 인스턴스를 받아와서 모든 설정을 함 **/

        LocationListener locationListener = new LocationListener() {

            /**
             * 디바이스 좌표가 바뀔때 마다 호출되는 메소드
             **/
            public void onLocationChanged(Location location) {
                double mlat = location.getLatitude();
                double mlng = location.getLongitude();

                //Toast toast = Toast.makeText(mContext, "위도: " + mlat + "경도: " + mlng, Toast.LENGTH_SHORT);
                //toast.show();

                GPSData = "위도: " + mlat + "경도: " + mlng;
                lat = mlat;
                lng = mlng;

                Log.e("SAVE", "위도: " + lat + "경도: " + lng);
//                Toast toast = Toast.makeText(mContext,  "위도: " + mlat + "경도: " + mlng, Toast.LENGTH_SHORT);
//                toast.show();
            }

            /* 디바이스 GPS 좌표가 바뀔 때마다 호출 */
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //  logView.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                //   logView.setText("onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                //   logView.setText("onProviderDisabled");
            }
        };


        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled) {
            Log.e("SAVE", "isGPSEnabled: TRUE");

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void OriantationTool()
    {
        // 센서객체를 얻어오기 위해서는 센서메니저를 통해서만 가능하다
        sm = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        s = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION); // 방향센서

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 센서값이 변경되었을 때 호출되는 콜백 메서드
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // 방향센서값이 변경된거라면
            OrientationData = "방향센서값 \n\n"
                    +"\n횡 방향: "+event.values[0] /** 방향  190 200 210 **/
                    +"\n열 방향 : "+event.values[1] /** 위아래  80 88 100 **/
                    +"\n전방 회전 방향 : "+event.values[2]; /** 방향 **/
            Roll = event.values[2];
            Yaw = event.values[0];
            Pitch = event.values[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
