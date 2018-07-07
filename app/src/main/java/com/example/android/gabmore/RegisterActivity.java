package com.example.android.gabmore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mName, mEmail,mPass;
    private Button mCreateAcc;

    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseReference;

    String TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mName = (TextInputLayout)findViewById(R.id.reg_Name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_Email);
        mPass = (TextInputLayout) findViewById(R.id.reg_Pass);
        mCreateAcc = (Button) findViewById(R.id.reg_create_acc);

        mCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name = mName.getEditText().getText().toString();
                String Email = mEmail.getEditText().getText().toString();
                String Password = mPass.getEditText().getText().toString();

                if(!TextUtils.isEmpty(Name) || !TextUtils.isEmpty(Email) || !TextUtils.isEmpty(Password)){

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your Account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(Name,Email,Password);

                }

            }
        });
    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            mDatabaseReference.keepSynced(true);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("device_token",deviceToken);
                            userMap.put("name",display_name);
                            userMap.put("status","Hi there I'm using Wassup");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");

                            mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });

                        }else{

                            mRegProgress.hide();

                            Toast.makeText(RegisterActivity.this, "Cannot SIGN UP",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }
}