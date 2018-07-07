package com.example.android.gabmore;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private SimpleDraweeView mProfileImage;
    private TextView mName,mStatus,mFriendsCount;
    private Button mSendRequest,mDeclineRequest;

    private DatabaseReference mdb_ref;

    private ProgressDialog mprogressdialog;

    private DatabaseReference mFriendRequestDb;

    private DatabaseReference mFriendDb;
    private FirebaseUser mcurrent_user;

    private DatabaseReference mNotificationDb;

    private DatabaseReference mRootRef;

    private String mCurrent_state;
    public String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Fresco.initialize(this);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mFriendsCount = (TextView) findViewById(R.id.totalfriend);
        mName = (TextView) findViewById(R.id.displayname);
        mStatus = (TextView) findViewById(R.id.userstatus);
        mProfileImage = (SimpleDraweeView) findViewById(R.id.profileimage_fresco);
        mSendRequest = (Button) findViewById(R.id.sendrequest);
        mDeclineRequest = (Button)findViewById(R.id.declinerequest);

        mCurrent_state = "not_friends";

        mDeclineRequest.setVisibility(View.INVISIBLE);
        mDeclineRequest.setEnabled(false);


        mprogressdialog = new ProgressDialog(this);
        mprogressdialog.setTitle("Loading User Data");
        mprogressdialog.setMessage("PLease wait while we load the user data");
        mprogressdialog.setCanceledOnTouchOutside(false);
        mprogressdialog.show();

        mNotificationDb = FirebaseDatabase.getInstance().getReference().child("notifications");
        mNotificationDb.keepSynced(true);
        mFriendRequestDb =FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendRequestDb.keepSynced(true);
        mcurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mFriendDb = FirebaseDatabase.getInstance().getReference().child("Friends");

        mdb_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mdb_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(display_name);
                mStatus.setText(status);

                mProfileImage.setImageURI(image);

                //--------------------------FRIENDS LIST / REQUEST FEATURE------------------------
                mFriendRequestDb.child(mcurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("Received")){

                                mCurrent_state = "req_received";
                                mSendRequest.setText("ACCEPT FRIEND REQUEST");
                                mDeclineRequest.setVisibility(View.VISIBLE);
                                mDeclineRequest.setEnabled(true);


                            }else if (req_type.equals("Sent")){
                                mCurrent_state = "req_sent";
                                mSendRequest.setText("CANCEL FRIEND REQUEST");

                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);
                            }

                            mprogressdialog.dismiss();

                        }else {

                            mFriendDb.child(mcurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Long size = dataSnapshot.getChildrenCount();
                                    mFriendsCount.setText(size.toString()+" Friends");
                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "friends";
                                        mSendRequest.setText("UNFRIEND "+display_name);

                                        mDeclineRequest.setVisibility(View.INVISIBLE);
                                        mDeclineRequest.setEnabled(false);

                                    }
                                    mprogressdialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mprogressdialog.dismiss();
                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mprogressdialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendRequest.setEnabled(true);

                //-------------------NOT FRIEND STATE--------------------------
                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationID = newNotificationref.getKey();

                    //--------------------- NOTIFICATION ----------------------
                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mcurrent_user.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+ mcurrent_user.getUid()+ "/" + user_id + "/request_type", "Sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mcurrent_user.getUid() + "/request_type","Received");
                    requestMap.put("notifications/" + user_id  + "/" + newNotificationID, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                          if(databaseError != null){
                              Toast.makeText(ProfileActivity.this,"Error in Sending Request",Toast.LENGTH_SHORT).show();
                          }
                          mSendRequest.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mSendRequest.setText("CANCEL FRIEND REQUEST");
                        }
                    });
                }

                //------------------- CANCEL FRIEND STATE --------------------------
                if(mCurrent_state.equals("req_sent")){

                    mFriendRequestDb.child(mcurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDb.child(user_id).child(mcurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mCurrent_state = "not_friends";
                                    mSendRequest.setText("SEND FRIEND REQUEST");

                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                    mDeclineRequest.setEnabled(false);

                                }
                            });
                        }
                    });
                    mSendRequest.setEnabled(true);
                }

                //-------------------------REQUEST RECEIVED STATE----------------------
                if(mCurrent_state.equals("req_received")){


                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+ mcurrent_user.getUid()+ "/" + user_id + "/date", ServerValue.TIMESTAMP);
                    friendsMap.put("Friends/" + user_id + "/" + mcurrent_user.getUid() + "/date",ServerValue.TIMESTAMP);

                    friendsMap.put("Friend_req/" + mcurrent_user.getUid()  + "/" + user_id,null);
                    friendsMap.put("Friend_req/" + user_id  + "/" + mcurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){


                                mCurrent_state = "friends";
                                mSendRequest.setText("UNFRIEND");

                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);

                            }else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    mSendRequest.setEnabled(true);
                }

                //--------------------------------UNFRIEND--------------------------------
                if(mCurrent_state.equals("friends")){

                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/" + mcurrent_user.getUid() + "/" + user_id,null);
                    unFriendMap.put("Friends/" + user_id + "/" + mcurrent_user.getUid(),null);

                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mSendRequest.setText("SEND FRIEND REQUEST");

                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);

                            }else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mSendRequest.setEnabled(true);
                        }
                    });
                }


            }
        });

        mDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestDb.child(mcurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendRequestDb.child(user_id).child(mcurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mCurrent_state = "not_friends";
                                mSendRequest.setText("SEND FRIEND REQUEST");

                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);

                            }
                        });
                    }
                });
                mSendRequest.setEnabled(true);            }
        });

    }
}
