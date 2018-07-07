package com.example.android.gabmore;



import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.gabmore.Friends;
import com.example.android.gabmore.ProfileActivity;
import com.example.android.gabmore.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mCheck;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_User_ID;

    private View mMainView;
    private boolean checker;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mFriendList = (RecyclerView) mMainView.findViewById(R.id.req_list);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!= null) {
            mCurrent_User_ID = mAuth.getCurrentUser().getUid();
            mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(mCurrent_User_ID);
            mFriendDatabase.keepSynced(true);
        }

        mCheck = FirebaseDatabase.getInstance().getReference().child("friend_requests");


        mUserDatabase  = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {

            FirebaseRecyclerAdapter<Friends, RequestsFragment.RequestViewHolder> reqRecyclerAdapter =
                    new FirebaseRecyclerAdapter<Friends, RequestsFragment.RequestViewHolder>(
                            Friends.class,
                            R.layout.users_single_layout,
                            RequestsFragment.RequestViewHolder.class,
                            mFriendDatabase
                    ) {
                        @Override
                        protected void populateViewHolder(final RequestsFragment.RequestViewHolder viewHolder, Friends model, int position) {

                            // viewHolder.setDate(model.getDate());
                            final String list_user_id = getRef(position).getKey();

                            mFriendDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(list_user_id)){

                                        mFriendDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                try {
                                                    final String userStatus = dataSnapshot.child("request_type").getValue().toString();
                                                    viewHolder.setStatus(userStatus);
                                                }catch (NullPointerException e){

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) { }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String userName = dataSnapshot.child("name").getValue().toString();

                                    String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                    viewHolder.setName(userName);
                                    viewHolder.setImage(userThumb);
                                    //viewHolder.setDate();


                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    };

            mFriendList.setAdapter(reqRecyclerAdapter);
        }else {
            //Snackbar.make(mMainView, "No Requests Pending", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }

        public void setDate(Long date){

            SimpleDateFormat sfd = new SimpleDateFormat("EEE, d MMM yyyy");
            String date_toString = sfd.format(date);

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_status);
            userStatusView.setText(date_toString);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_name);
            userNameView.setText(name);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void setStatus(String Status){
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_status);


            if(Status.equals("Received")) {
                userStatusView.setText("Sent You a Friend Request");
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                userStatusView.setTextColor(Color.GREEN);
            }
            else{
                userStatusView.setText("Request Pending");
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                userStatusView.setTextColor(Color.RED);
                userStatusView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }

        }

        public void setImage(String thumb_image){
            SimpleDraweeView UserImage = (SimpleDraweeView) mView.findViewById(R.id.user_img_fresco);
            UserImage.setImageURI(thumb_image);
        }

    }

}
