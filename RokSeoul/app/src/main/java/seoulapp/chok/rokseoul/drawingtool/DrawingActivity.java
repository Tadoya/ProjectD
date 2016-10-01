package seoulapp.chok.rokseoul.drawingtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import seoulapp.chok.rokseoul.MainActivity;
import seoulapp.chok.rokseoul.drawingtool.cameraset.Camera2Preview;
import seoulapp.chok.rokseoul.drawingtool.cameraset.Camera_SurfaceTextureListener;
import seoulapp.chok.rokseoul.drawingtool.sensorset.SensorSet;
import seoulapp.chok.rokseoul.drawingtool.sensorset.SensorSet2;
import seoulapp.chok.rokseoul.drawingtool.view.AutoFitTextureView;
import seoulapp.chok.rokseoul.drawingtool.view.DrawingView;
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
public class DrawingActivity extends AppCompatActivity implements OnClickListener {

    static String TAG = "DrawingAcitivty";
    //custom drawing view
    private DrawingView drawView;
    //buttons
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, loadBtn;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;

    //Camera
    //private TextureView textureView;
    private AutoFitTextureView textureView;

    //Camera1preview
    private Camera_SurfaceTextureListener cameraListener;

    //Camera2preview
    private Camera2Preview camera2Preview;
    public static final int REQUEST_CAMERA = 1;


    //drawingView
    public static int RESULT_LOAD_IMAGE = 1;

    //저장된 이미지 불러올 뷰
    private ImageView imageView;

    //센서
    //private SensorSet sensorSet;
    private SensorSet2 sensorSet2;



    /**
     * firebase
     */
    private StorageSet storageSet;
    private String placeName = "royalpalace";
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drawing);


        mAuth = FirebaseAuth.getInstance();
        storageSet = new StorageSet(this);

        //get drawing view
        drawView = (DrawingView) findViewById(R.id.drawing);

        //get the palette and first color button
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //draw button
        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //set initial size
        drawView.setBrushSize(smallBrush);

        //erase button
        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //load button
        loadBtn = (ImageButton) findViewById(R.id.load_btn);
        loadBtn.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //API21이상
            //textureView = (TextureView) findViewById(R.id.textureView);
            textureView = (AutoFitTextureView) findViewById(R.id.textureView);
            camera2Preview = new Camera2Preview(this, textureView);
        } else {
            //API 19
            cameraListener = new Camera_SurfaceTextureListener(this);
        }

        //sensorSet = new SensorSet(this);
        sensorSet2 = new SensorSet2(this);
    }




    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camera2Preview.onPause();
            Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
        } else {
            Log.d("SDK", "SDK version 21- : " + Build.VERSION.SDK_INT);
        }
        //sensorSet.onPause();
        sensorSet2.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camera2Preview.onResume();
            Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
        } else {
            //textureView = (TextureView) findViewById(R.id.textureView);
            textureView = (AutoFitTextureView) findViewById(R.id.textureView);
            textureView.setSurfaceTextureListener(cameraListener);
            Log.d("SDK", "SDK version 21- : " + Build.VERSION.SDK_INT);
        }
        //sensorSet.onResume();
        sensorSet2.onResume();
    }

    /**
     * API 21+에서 카메라 사용을 승인했을 때 다시 카메라뷰를 띄우기위해
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            //textureView = (TextureView) findViewById(R.id.textureView);
                            textureView = (AutoFitTextureView) findViewById(R.id.textureView);
                            camera2Preview = new Camera2Preview(this, textureView);
                            camera2Preview.openCamera(textureView.getWidth(), textureView.getHeight());
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    //user clicked paint
    public void paintClicked(View view) {
        //use chosen color

        //set erase false
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());

        if (view != currPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }


    //그리기 도구 버튼들
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.draw_btn) {
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
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
        } else if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //size buttons
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        } else if (view.getId() == R.id.new_btn) {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                    imageView.setImageResource(android.R.color.transparent);
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
            //save drawing
            if(!MainActivity.placeName.isEmpty()) placeName = MainActivity.placeName;
            Log.d("QRcode", "Draw-placeName : " +placeName);

            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Place : " + placeName
                    +"\nDirection : "+sensorSet2.getSensorDirection()
                    + " / Degree:"+ sensorSet2.getSensorDegree()
                    +"\nSave drawing to firebase?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);

                    //메모리그림을 변환
                    Bitmap screenshot = Bitmap.createBitmap(drawView.getDrawingCache());
                    //firebase에 저장
                    storageSet.uploadFromMemory(screenshot, placeName, mAuth.getCurrentUser().getUid()
                            , sensorSet2.getSensorDirection(), sensorSet2.getSensorDegree());

                    drawView.destroyDrawingCache();
                    drawView.startNew();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        } else if (view.getId() == R.id.load_btn) {
            Log.d("SAVE", "clicked load_btn");
            AlertDialog.Builder loadDialog = new AlertDialog.Builder(this);
            loadDialog.setTitle("Load drawing");
            loadDialog.setMessage("Load drawing to device Gallery?");
            loadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent tmpl = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(tmpl, RESULT_LOAD_IMAGE);
                }
            });
            loadDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            loadDialog.show();
        }
    }

    //갤러리 불러오기 이후 동작
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
            String[] filePath = {MediaStore.Images.Media.DATA};
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
}
