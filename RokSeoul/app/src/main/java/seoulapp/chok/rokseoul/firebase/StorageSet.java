package seoulapp.chok.rokseoul.firebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import seoulapp.chok.rokseoul.R;
import seoulapp.chok.rokseoul.firebase.models.DownloadURLs;


/**
 * Created by choiseongsik on 2016. 9. 13..
 */

public class StorageSet {

    private static final String TAG = "StorageSet";
    private Activity activity;

    //Auth
    private FirebaseAuth mAuth;

    //DB
    private DatabaseReference mDatabase;
    private DatabaseReference mPlaceDB;
    private DatabaseReference mPlaceDoodles;
    private DatabaseReference mTotalDoodles;

    private ValueEventListener mPlaceValueEventListener;

    // Create a storage reference from our app
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReferenceFromUrl("gs://rokseoul-16bb2.appspot.com/");

    private ProgressDialog mProgressDialog;

    private String placeName;
    private Uri mDownloadUrl = null;


    private ArrayList<DownloadURLs> urls;
    private boolean dataBaseCheck;  // DB다 받아왔는지 체크
    private boolean downloadCheck;  // 다운로드 완료 됐는지 체크


    private final String doodles = "doodles";

    private FrameLayout imageViewFrame;
    private ArrayList<ImageView> imageList;

    public StorageSet(final Activity activity, final String placeName) {
        this.activity = activity;
        this.placeName = placeName;

        imageViewFrame = (FrameLayout) activity.findViewById(R.id.imageViewFrame);
        mAuth = FirebaseAuth.getInstance();
        dataBaseCheck = false;
        downloadCheck = false;
        urls = new ArrayList<DownloadURLs>();
        imageList = new ArrayList<ImageView>();

        //업로드를 위한 레퍼런스
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mPlaceDB = FirebaseDatabase.getInstance().getReference().child("QRPlace").child(placeName).child("downloadURL");
        mPlaceDoodles = FirebaseDatabase.getInstance().getReference().child("QRPlace").child(placeName).child(doodles);
        mTotalDoodles = FirebaseDatabase.getInstance().getReference().child("totaldoodles");
        //앱 시작 후(해당 엑티비티 내에서 DB변경이 있을 경우 실시간 변경
        mPlaceValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    //int urlsCount = (int) dataSnapshot.getChildrenCount();
                    //imageViews = new ImageView[urlsCount];
                    //urls = new DownloadURLs[urlsCount];
                    int i = 0;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        urls.add(snapshot.getValue(DownloadURLs.class));

                        Log.d("Download", i +" object URL url : " + urls.get(i++).getUrl());
                    }

                }catch (Exception e){
                    Log.d("Download", "downloadurl's key : 에러" + e);
                }
                try {
                    if (!urls.isEmpty()) {
                        dataBaseCheck = true;
                        downloadToMemoryAll();
                        downloadCheck = true;
                    }
                }catch (Exception e){
                    dataBaseCheck = false;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Download", "mPlaceListener error"+databaseError.toString());
                Toast.makeText(activity, "DB Error", Toast.LENGTH_SHORT).show();
            }
        };
        mPlaceDB.addListenerForSingleValueEvent(mPlaceValueEventListener);
    }
    public void onStop(){
        dataBaseCheck = false;
        downloadCheck = false;
        if(mPlaceValueEventListener != null) mPlaceDB.removeEventListener(mPlaceValueEventListener);
    }
    public void uploadFromMemory(Bitmap bitmap, final String userID
             , final String direction, final int azimuth, final int pitch, final int roll) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // [START get_child_ref]
        final String nowTime = "" + new Date().getTime();
        final StorageReference doodleRef = storageRef.child(placeName).child(direction).child(userID).child(nowTime);
        // [END get_child_ref]

        // Create file metadata including the content type
        final StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .setCustomMetadata("userID", userID)
                .setCustomMetadata("time", nowTime)
                .setCustomMetadata("place", placeName)
                .setCustomMetadata("direction", direction)
                .setCustomMetadata("azimuth", ""+azimuth)
                .build();

        // [START_EXCLUDE]
        showProgressDialog("Uploading...");
        // [END_EXCLUDE]

        UploadTask uploadTask = doodleRef.putBytes(data, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.w(TAG, "uploadFromUri:onFailure", exception);

                mDownloadUrl = null;
                // [START_EXCLUDE]
                hideProgressDialog();
                Toast.makeText(activity, "Error : upload failed",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded
                Log.d(TAG, "uploadFromUri:onSuccess");

                //업로드한 그림 다운로드 경로..
                //mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                mDownloadUrl = taskSnapshot.getDownloadUrl();

                //downloadURL 하위 푸시키 얻기
                String urlKey = mDatabase.child("downloadURL").push().getKey();


                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                // users / uID /
                // 해당유저DB에 낙서 수 +1 , download URL
                DownloadURLs dUrl = new DownloadURLs(direction, mDownloadUrl.toString(), azimuth, pitch, roll);
                mDatabase.child("downloadURL").child(placeName).child(urlKey).setValue(dUrl);
                mDatabase.child(doodles).runTransaction(new Transaction.Handler() {
                    int myDoodles;
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        try {
                            myDoodles = Integer.parseInt(mutableData.getValue().toString());
                            Log.d(TAG, "myDoodles transaction try : "+mutableData);
                        }catch (Exception e){
                            Log.d(TAG, "myDoodles transaction e:"+ e);
                        }
                        //if(placeDoodles == 0) return Transaction.success(mutableData);
                        mutableData.setValue(myDoodles+1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        Log.d(TAG, placeName+"-Transaction:onComplete myDoodles : " + dataSnapshot.getValue());
                    }
                });

                //장소에 낙서 수 + 1
                mPlaceDB.child(urlKey).setValue(dUrl);
                mPlaceDoodles.runTransaction(new Transaction.Handler() {
                    int placeDoodles;
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        try {
                            placeDoodles = Integer.parseInt(mutableData.getValue().toString());
                            Log.d(TAG, "placeDoodles transaction try : "+mutableData);
                        }catch (Exception e){
                            Log.d(TAG, "placeDoodles transaction e:"+ e);
                        }
                        //if(placeDoodles == 0) return Transaction.success(mutableData);
                        mutableData.setValue(placeDoodles+1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        Log.d(TAG, placeName+"-Transaction:onComplete placeDoodles : " + dataSnapshot.getValue());
                    }
                });

                //TotalDoodles + 1
                mTotalDoodles.runTransaction(new Transaction.Handler() {
                    int totalDoodles;
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        try {
                            totalDoodles = Integer.parseInt(mutableData.getValue().toString());
                        }catch (Exception e){
                            Log.d(TAG, "totalDoodles transaction e:"+ e);
                        }

                        //if(totalDoodles == 0) return Transaction.success(mutableData);
                        Log.d(TAG, "totalDoodles transaction try : "+mutableData);
                        mutableData.setValue(totalDoodles+1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        // Transaction completed
                        Log.d(TAG, "totaldoodle Transaction:onComplete totalDoodles : " + dataSnapshot.getValue());
                    }
                });
                urls.add(dUrl);
                downloadToMemory(dUrl.getUrl());
                try {
                    showDoodles(azimuth, pitch, roll);
                }catch (Exception e){
                    Log.d("sensorcheck", "burningtime"+e);
                }
                // [START_EXCLUDE]
                hideProgressDialog();
                Toast.makeText(activity, "Upload success!",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]

            }
        });
    }

    // [END upload_from_uri]

    private void downloadToMemoryAll(){
        // [START_EXCLUDE]
        showProgressDialog("Downloading...");
        // [END_EXCLUDE]

        for(DownloadURLs url : urls) {
            downloadToMemory(url.getUrl());
        }
        hideProgressDialog();
    }

    private void downloadToMemory(String url){
        final long ONE_MEGABYTE = 1024 * 1024;
        final ImageView imageView = new ImageView(activity);
        StorageReference islandRef = storage.getReferenceFromUrl(url);
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed

                // First decode with inJustDecodeBounds=true to check dimensions
                /*
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                Log.d("Download", "bitmap : " + bitmap.getByteCount());
                Log.d("Download", "다운로드받기성공");
                imageView.setImageBitmap(bitmap);
                */
                Bitmap bitmap = decodeSampledBitmapFromBytes(bytes,imageViewFrame.getMeasuredWidth(), imageViewFrame.getMeasuredHeight());
                Log.d("Download", "bitmap : " + bitmap.getByteCount());
                Log.d("Download", "다운로드받기성공");
                imageView.setImageBitmap(bitmap);
                imageList.add(imageView);
                imageViewFrame.addView(imageView);
                imageView.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d("Download", "다운로드받기 실패");
                    downloadCheck = false;
                }
            });
        downloadCheck = true;
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    private Bitmap decodeSampledBitmapFromBytes(byte[] bytes, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }


    public void showDoodles(int azimuth, int pitch, int roll){
        // [START_EXCLUDE]
        showProgressDialog("Wait...");
        int i = 0;
        // [END_EXCLUDE]
        /**
         * 수정필요
         */
        for (DownloadURLs url : urls) {
            if (       (Math.abs(url.getAzimuth() - azimuth) <= 5 || Math.abs(url.getAzimuth() - azimuth) >= 355)
                    && (Math.abs(url.getPitch() - pitch) <= 5) || (Math.abs(url.getPitch() - pitch) >= 175)
                    //&& (Math.abs(url.getRoll() - roll) <= 20 || Math.abs(url.getRoll() - roll) >= 340)
                    && imageList.get(i).getVisibility() != View.VISIBLE) {
                imageList.get(i).setVisibility(View.VISIBLE);
            }
            else if(   ((Math.abs(url.getAzimuth() - azimuth) > 10)
                    || (Math.abs(url.getPitch() - pitch) > 10)
                    //|| (Math.abs(url.getRoll() - roll) > 30))
                    )&& imageList.get(i).getVisibility() == View.VISIBLE) {

                imageList.get(i).setVisibility(View.GONE);
            }
            Log.d("sensorset","urlNumber : " + i);
                i++;
        }
        hideProgressDialog();
    }

    public boolean getDatabaseCheck(){
        return dataBaseCheck;
    }
    public void setDatabaseCheck(boolean check) { dataBaseCheck = check; }

    public boolean getDownloadCheck(){
        return downloadCheck;
    }
    public void setDownloadCheck(boolean check){
        downloadCheck = check;
    }

    private void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
