package seoulapp.chok.rokseoul;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import seoulapp.chok.rokseoul.drawingtool.DrawingActivity;
import seoulapp.chok.rokseoul.firebase.GoogleSignInActivity;
import seoulapp.chok.rokseoul.firebase.models.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private User user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ChildEventListener mChildeEventListener;
    public static int doodleCount;

    private TextView profile_userName, profile_email, profile_doodles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    Log.d("MainActivity", "datasnapshot.doodles: " + doodleCount);
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
        mChildeEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", "DB Error");
            }
        };
        mDatabase.addChildEventListener(mChildeEventListener);

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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            Toast.makeText(MainActivity.this, "카메라를 선택했습니다.", Toast.LENGTH_SHORT).show();

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

            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
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
        if(mChildeEventListener != null) {
            mDatabase.removeEventListener(mChildeEventListener);
        }
    }
}
