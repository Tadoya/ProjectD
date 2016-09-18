package seoulapp.chok.rokseoul;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class IntroActivity extends AppCompatActivity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        handler  = new Handler();
        handler.postDelayed(mrun, 3000);
    }
    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        handler.removeCallbacks(mrun);
    }
}