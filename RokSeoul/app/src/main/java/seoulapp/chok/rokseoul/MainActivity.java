package seoulapp.chok.rokseoul;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import seoulapp.chok.rokseoul.drawingtool.DrawingActivity;
import seoulapp.chok.rokseoul.firebase.GoogleSignInActivity;
import seoulapp.chok.rokseoul.firebase.models.User;
import seoulapp.chok.rokseoul.maps.MapsActivity;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private User user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ValueEventListener mValueEventListener;
    public static int doodleCount;
    private FragmentManager fm;

    private TextView profile_userName, profile_email, profile_doodles;
    private SupportMapFragment mSupportMapFragment;
    public static String placeName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setContentView(R.layout.activity_main_second);
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.setCaptureActivity(QRAcvitivy.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.initiateScan();
                */

                startActivity(new Intent(getApplicationContext(), DrawingActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,  R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);
        profile_userName = (TextView) v.findViewById(R.id.profile_userName);
        profile_email = (TextView) v.findViewById(R.id.profile_email);
        profile_doodles = (TextView) v.findViewById(R.id.profile_doodles);

        //DB경로
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        //앱 시작시 DB한번 읽기
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                //첫 로그인 시 "doodles"가 비었을 때를 대비한 예외처리
                try {
                    doodleCount = Integer.parseInt(dataSnapshot.child("doodles").getValue().toString());
                    Log.d("MainActivity", "datasnapshot.doodles(Onetime): " + doodleCount);
                }catch (Exception e){
                    doodleCount = 0;
                }
                profile_doodles.setText(""+doodleCount);
                profile_userName.setText(user.username);
                profile_email.setText(user.email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", "DB Error");
            }
        });
        //앱 시작 후(해당 엑티비티 내에서 DB변경이 있을 경우 실시간 변경
        mValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    doodleCount = Integer.parseInt(dataSnapshot.child("doodles").getValue().toString());
                    Log.d("MainActivity", "datasnapshot.doodles(realtime): " + doodleCount);
                }catch (Exception e){
                    doodleCount = 0;
                }
                profile_doodles.setText(""+doodleCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", "DB Error");
            }
        };
        mDatabase.addValueEventListener(mValueEventListener);


        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MapsActivity()).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_camera) {

            /**
             * create an instance of the IntentIntegrator class we imported,
             * and then call on the initiateScan() method to start scanning
             */
            if(placeName.isEmpty()) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.setCaptureActivity(QRAcvitivy.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.initiateScan();
            }

        } else if (id == R.id.nav_gallery) {

            Toast.makeText(MainActivity.this, "갤러리를 선택했습니다.", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_slideshow) {

            Toast.makeText(MainActivity.this, "슬라이드쇼를 선택했습니다.", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.account_settings) {

            //Toast.makeText(MainActivity.this, "로그인을 선택했습니다.", Toast.LENGTH_SHORT).show();
            GoogleSignInActivity.stayLogin = false;
            Intent intent = new Intent(getApplicationContext(),GoogleSignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);


        } else if (id == R.id.log_out) {

            Toast.makeText(MainActivity.this, "로그아웃을 선택했습니다.", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.my_doodle) {

            //Toast.makeText(MainActivity.this, "내 낙서를 선택했습니다.", Toast.LENGTH_SHORT).show();
            //fragmentManager.beginTransaction().replace()
            //startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            fm.beginTransaction().replace(R.id.content_frame, new MapsActivity()).commit();
            /*
              FragmentManager fragmentManager = activity.getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
             Fragment newFragment = new FragmentType1();
             fragmentTransaction.replace(R.id.frameTitle, casinodetailFragment, "fragmentTag");
             //fragmentTransaction.addToBackStack(null);
             fragmentTransaction.commit();
             */
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //로그인 안되어있으면 앱 종료
        profile_doodles.setText(""+doodleCount);
        if(mAuth.getCurrentUser() == null){
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mValueEventListener != null) {
            mDatabase.removeEventListener(mValueEventListener);
        }
    }

    /**
     * The results of the scan will be returned and we'll be able to retrieve it in the onActivityResult() method
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * Parse the result into an instance of the IntentResult class we imported
         */
        IntentResult scanningIntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        /**
         * Check the scanningIntentResult object is it null or not, to proceed only if we have a valid result
         */
        if (scanningIntentResult != null) {
            /**
             * Retrieve the content and the format of the scan as strings value.
             */
            String scanContent = scanningIntentResult.getContents();
            String scanFormat = scanningIntentResult.getFormatName();
            /**
             * if condition after getting scanContent and scanFormat,
             * checking on them if there are consisting real data or not.
             */
            if(scanContent != null && scanFormat != null) {
                /**
                 * Now our program has the format and content of the scanned data,
                 * so you can do whatever you want with it.
                 */
                //contentTextView.setText("Content : " + scanContent);
                //formatTextView.setText("Format : " + scanFormat);
                Toast.makeText(getApplicationContext(), "Contents : "+scanContent
                        +"\nFormat : "+ scanFormat, Toast.LENGTH_LONG).show();
                placeName = scanContent;
                Intent intent = new Intent(getApplicationContext(), DrawingActivity.class);
                startActivity(intent);
                Log.d("QRcode", "Main-placeName : " +placeName);
            } else {
                Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT).show();
            }
        } else {
            /**
             * If scan data is not received
             * (for example, if the user cancels the scan by pressing the back button),
             * we can simply output a message.
             */
            Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT).show();
        }
    }
}
