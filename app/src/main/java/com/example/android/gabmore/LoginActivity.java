package com.example.android.gabmore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout  mEmail,mPass;
    private Button mLoginAcc;

    private Toolbar mToolbar;

    private ProgressDialog mloginProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserDb.keepSynced(true);

        mloginProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (TextInputLayout) findViewById(R.id.login_Email);
        mPass = (TextInputLayout) findViewById(R.id.login_Pass);
        mLoginAcc = (Button) findViewById(R.id.login_acc);

        mLoginAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Email = mEmail.getEditText().getText().toString();
                String Password = mPass.getEditText().getText().toString();

                if(!TextUtils.isEmpty(Email) || !TextUtils.isEmpty(Password)){

                    mloginProgress.setTitle("Logging In");
                    mloginProgress.setMessage("Please Wait while we check your Credentials");
                    mloginProgress.setCanceledOnTouchOutside(false);
                    mloginProgress.show();

                    loginUser(Email,Password);
                }

            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    mloginProgress.dismiss();

                    String currentUser_Uid = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDb.child(currentUser_Uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, com.example.android.gabmore.MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                }else {
                    mloginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Incorrect Email or Password",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}