package seoulapp.chok.rokseoul.firebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import seoulapp.chok.rokseoul.MainActivity;


/**
 * Created by choiseongsik on 2016. 9. 13..
 */

public class StorageSet {

    private static final String TAG = "StorageSet";
    private Activity activity;

    private FirebaseAuth mAuth;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener mValueEventListener;

    private int doodleCount;
    // Create a storage reference from our app
    private StorageReference storageRef = storage.getReferenceFromUrl("gs://rokseoul-16bb2.appspot.com/");

    private ProgressDialog mProgressDialog;

    private Uri mDownloadUrl = null;



    public StorageSet(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        //앱 시작 후(해당 엑티비티 내에서 DB변경이 있을 경우 실시간 변경
        mValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    doodleCount = Integer.parseInt(dataSnapshot.child("doodles").getValue().toString());
                    Log.d("MainActivity", "datasnapshot.doodles(realtime-drawing): " + doodleCount);
                }catch (Exception e){
                    doodleCount = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", "DB Error");
            }
        };
        mDatabase.addValueEventListener(mValueEventListener);
    }

    public void onStop(){
        if(mValueEventListener != null) {
            mDatabase.removeEventListener(mValueEventListener);
        }
    }
    public void uploadFromMemory(Bitmap bitmap, String placeName
            , final String userID, String direction, int degree) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // [START get_child_ref]
        String nowTime = "" + new Date().getTime();
        final StorageReference doodleRef = storageRef.child(placeName).child(direction).child(userID).child(nowTime);
        // [END get_child_ref]

        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .setCustomMetadata("userID", userID)
                .setCustomMetadata("time", nowTime)
                .setCustomMetadata("place", placeName)
                .setCustomMetadata("direction", direction)
                .setCustomMetadata("degree", ""+degree)
                .build();

        // [START_EXCLUDE]
        showProgressDialog();
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

                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                mDatabase.child("doodles").setValue(doodleCount+1);
                Log.d("MainActivity","doodleCount(storage): "+doodleCount+1);
                //업로드한 그림 다운로드 경로..
                mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                // [START_EXCLUDE]
                hideProgressDialog();
                Toast.makeText(activity, "Upload success!",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]

            }
        });
    }

    // [END upload_from_uri]

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage("Loading...");
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
