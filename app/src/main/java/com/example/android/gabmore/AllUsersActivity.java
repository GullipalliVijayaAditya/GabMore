package com.example.android.gabmore;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.android.gabmore.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUsersList;

    private DatabaseReference allDatabaseUserReference;
    private FirebaseAuth mAuth;
    private String mCurrent_UID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        Fresco.initialize(this);

        mToolbar=(Toolbar)findViewById(R.id.allusers_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_UID = mAuth.getCurrentUser().getUid();

        allDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUserReference.keepSynced(true);

        allUsersList = (RecyclerView)findViewById(R.id.users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<com.example.android.gabmore.UsersData,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UsersData, UsersViewHolder>(UsersData.class, R.layout.users_single_layout, UsersViewHolder.class, allDatabaseUserReference) {

            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, UsersData users, int position) {

                final String user_uid = getRef(position).getKey();

                if(user_uid.equals(mCurrent_UID)) {
                    viewHolder.mView.setVisibility(View.GONE);
                    viewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                }
                    viewHolder.setName(users.getName());
                    viewHolder.setStatus(users.getStatus());
                    viewHolder.setImage(getApplicationContext(), users.getImage());


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(AllUsersActivity.this, com.example.android.gabmore.ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_uid);
                        startActivity(profileIntent);


                    }
                });
            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView UserNameView = (TextView) mView.findViewById(R.id.user_name);
            UserNameView.setText(name);
        }

        public void setStatus(String status){
            TextView UserNameView = (TextView) mView.findViewById(R.id.user_status);
            UserNameView.setText(status);
        }

        public void setImage(final Context ctx, final String thumb_image){
            final SimpleDraweeView UserImage = (SimpleDraweeView) mView.findViewById(R.id.user_img_fresco);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(UserImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(UserImage);
                }
            });

        }
}
}