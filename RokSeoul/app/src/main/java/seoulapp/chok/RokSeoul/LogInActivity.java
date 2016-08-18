package seoulapp.chok.rokseoul;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SUGNUP = 0;

    //컴포넌트 생성 이메일, 패스워드, 로그인버튼, 회원가입링크
    EditText emailText;
    EditText passwordText;
    Button loginBtn;
    TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loginBtn = (Button) findViewById(R.id.btn_login);
        signupLink = (TextView) findViewById(R.id.link_signup);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);

        //로그인버튼
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        //회원가입링크
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입 화면으로 이동
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SUGNUP);
            }
        });
    }

        //로그인 동작
     public void login(){
         //로그 뿌림
         Log.d(TAG, "Login");

         //validation체크
         if(!validate()){
             onLoginFailed();
             return;
         }
         loginBtn.setEnabled(false);
/*이건 뭔지 모르겟당
         final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                 R.style.AppTheme_Dark_Dialog);
         progressDialog.setIndeterminate(true);
         progressDialog.setMessage("Authenticating...");
         progressDialog.show();
*/

         String email = emailText.getText().toString();
         String password = passwordText.getText().toString();


         new android.os.Handler().postDelayed(
                 new Runnable() {
                     public void run() {
                         // On complete call either onLoginSuccess or onLoginFailed
                         onLoginSuccess();
                         // onLoginFailed();
                         //progressDialog.dismiss();
                     }
                 }, 3000);
     }
//로그인 성공시
    public void onLoginSuccess(){
        loginBtn.setEnabled(true);
        finish();
    }
//로그인 실패
    public void onLoginFailed(){
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginBtn.setEnabled(true);
    }
    //validation체크
    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

}
