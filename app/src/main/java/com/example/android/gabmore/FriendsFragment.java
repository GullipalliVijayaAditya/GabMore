package com.example.android.gabmore;


import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
public class FriendsFragment extends Fragment {


    private RecyclerView mFriendList;

    private DatabaseReference mFriendDb;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_User_ID;

    private View mMainView;
    private Paint p = new Paint();

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView =  inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!= null) {
            mCurrent_User_ID = mAuth.getCurrentUser().getUid();
            mFriendDb = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_User_ID);
        }
        mUserDatabase  = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!= null) {

        final FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendDb
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();


                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setName(userName);
                        viewHolder.setImage(userThumb);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence option[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //Click Event for Items
                                        if(which == 0){
                                            Intent profileIntent = new Intent(getContext(), com.example.android.gabmore.ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);

                                        }
                                        if(which == 1){

                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });
                                builder.show();
                            }
                        });

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        };
        mFriendList.setAdapter(friendsRecyclerAdapter);




        initSwipe();ItemTouchHelper.SimpleCallback simpleCallback = new
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                        final int position = viewHolder.getAdapterPosition(); //get position which is swipe
                        String x_list_user_id = friendsRecyclerAdapter.getRef(position).getKey();



                        if (direction == ItemTouchHelper.LEFT) {

                            Intent profileIntent = new Intent(getContext(), com.example.android.gabmore.ProfileActivity.class);
                            profileIntent.putExtra("user_id",x_list_user_id);
                            startActivity(profileIntent);
                        }
                        if(direction == ItemTouchHelper.RIGHT){

                            mUserDatabase.child(x_list_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final String contact = dataSnapshot.child("contact").getValue().toString();
                                    Intent call_Intent = new Intent(Intent.ACTION_CALL);
                                    call_Intent.setData(Uri.parse("tel:"+contact));
                                    if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                                    {
                                        return;
                                    }
                                    startActivity(call_Intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

                        Bitmap icon;
                        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                            View itemView = viewHolder.itemView;
                            float height = (float) itemView.getBottom() - (float) itemView.getTop();
                            float width = height / 3;

                            if(dX > 0){
                                p.setColor(Color.parseColor("#388E3C"));
                                RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                                c.drawRoundRect(background,90,90,p);
                                icon = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                                RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                                c.drawBitmap(icon,null,icon_dest,p);
                            } else {
                                p.setColor(Color.parseColor("#D32F2F"));
                                RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                                c.drawRoundRect(background,90,90,p);
                                icon = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                                RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                                c.drawBitmap(icon,null,icon_dest,p);
                            }
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mFriendList);

    }}

private void initSwipe(){


}
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        SimpleDraweeView UserImage;
        public FriendsViewHolder(View itemView){
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
        public void setImage(String thumb_image){
            UserImage = (SimpleDraweeView) mView.findViewById(R.id.user_img_fresco);
            UserImage.setImageURI(thumb_image);        }

        public void setUserOnline (String online_status){
            ImageView  userOnlineView= (ImageView) mView.findViewById(R.id.user_online_icon);

            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}