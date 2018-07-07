package com.example.android.gabmore;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */



public class ChatsFragment extends Fragment {

    private  RecyclerView mConvList;
    private View mMainView;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private  String mCurrent_UID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chats, container, false);
        Fresco.initialize(getContext());
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!= null) {
            mCurrent_UID = mAuth.getCurrentUser().getUid();
            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_UID);
            mConvDatabase.keepSynced(true);

            mConvDatabase.keepSynced(true);
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_UID);
            mMessageDatabase.keepSynced(true);
        }
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager lLManager = new LinearLayoutManager(getContext());
        lLManager.setReverseLayout(true);
        lLManager.setStackFromEnd(true);

        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(lLManager);


        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {

            Query conversationQuery = mConvDatabase.orderByChild("timestamp");

            FirebaseRecyclerAdapter<com.example.android.gabmore.Conv, ConvViewHolder> fra = new FirebaseRecyclerAdapter<com.example.android.gabmore.Conv, ConvViewHolder>(
                    com.example.android.gabmore.Conv.class,
                    R.layout.users_single_layout,
                    ConvViewHolder.class,
                    conversationQuery
            ) {
                @Override
                protected void populateViewHolder(final ConvViewHolder viewHolder, final com.example.android.gabmore.Conv model, int pos) {

                    final String list_user_id = getRef(pos).getKey();

                    Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String data = dataSnapshot.child("message").getValue().toString();
                            viewHolder.setMessage(data, model.isSeen());
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

                        }
                    });

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);
                            }
                            viewHolder.setName(userName);
                            viewHolder.setUserImage(userThumb);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);

                                    //viewHolder.setSeen(model.setSeen());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            };

            mConvList.setAdapter(fra);
        }
    }
    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ConvViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTextColor(Color.GREEN );
            }else {
                userStatusView.setTypeface(userStatusView.getTypeface(),Typeface.NORMAL);
            }

        }

        public void setName(String name){
            TextView userName = (TextView) mView.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setUserImage(String thumb_image){
            SimpleDraweeView userImage = (SimpleDraweeView) mView.findViewById(R.id.user_img_fresco);
            userImage.setImageURI(thumb_image);
        }

        public  void setUserOnline (String online_status){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_online_icon);

            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}