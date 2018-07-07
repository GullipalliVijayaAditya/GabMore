package com.example.android.gabmore;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.gabmore.Messages;
import com.example.android.gabmore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private View v;

    public  MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        //public TextView displayName;
        //public CircleImageView profileImage;

        private final TextView mTimeField;
        private final TextView mTextField;
        private final FrameLayout mLeftArrow;
        private final FrameLayout mRightArrow;
        private final RelativeLayout mMessageContainer;
        private final LinearLayout mMessage;
        private final int mWhite;
        private final int mGray;
        private final ImageView mImageField;

        public MessageViewHolder(View view){
            super(view);

            //displayName = (TextView) view.findViewById(R.id.display_name_layout);
            //messageText = (TextView) view.findViewById(R.id.message);
            //profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);

            mTimeField = (TextView) itemView.findViewById(R.id.time_text);
            mTextField = (TextView) itemView.findViewById(R.id.message_text);
            mLeftArrow = (FrameLayout) itemView.findViewById(R.id.left_arrow);
            mRightArrow = (FrameLayout) itemView.findViewById(R.id.right_arrow);
            mMessageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
            mMessage = (LinearLayout) itemView.findViewById(R.id.message);
            mWhite = ContextCompat.getColor(itemView.getContext(), R.color.white);
            mGray = ContextCompat.getColor(itemView.getContext(), R.color.gray);
            mImageField = (ImageView)itemView.findViewById(R.id.added_image);
        }
    }

    @Override
    public  void onBindViewHolder(MessageViewHolder viewHolder, int i){

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();

        SimpleDateFormat sfd = new SimpleDateFormat("h:mm a");
        String sent_time = sfd.format(c.getTime());
        final int color;

        if(from_user.equals(current_user_id)){

            color = viewHolder.mGray;
            viewHolder.mLeftArrow.setVisibility(View.GONE);
            viewHolder.mRightArrow.setVisibility(View.VISIBLE);
            viewHolder.mMessageContainer.setGravity(Gravity.END);
            //viewHolder.mRightArrow.setForegroundGravity(Gravity.TOP | Gravity.RIGHT);

          /*  LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.RIGHT;

            //viewHolder.messageText.setGravity(Gravity.RIGHT | Gravity.END);
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.messageText.setLayoutParams(params);*/

        }else{
            color = viewHolder.mWhite;
            viewHolder.mLeftArrow.setVisibility(View.VISIBLE);
            viewHolder.mRightArrow.setVisibility(View.GONE);
            viewHolder.mMessageContainer.setGravity(Gravity.START);

            /*viewHolder.messageText.setGravity(Gravity.LEFT | Gravity.START);
            viewHolder.messageText.setTextColor(Color.BLACK);*/

        }
        //viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);

        ((GradientDrawable) viewHolder.mMessage.getBackground()).setColor(color);
        ((RotateDrawable) viewHolder.mLeftArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
        ((RotateDrawable) viewHolder.mRightArrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);

        if(message_type.equals("text")){
            viewHolder.mTextField.setText(c.getMessage());
            viewHolder.mImageField.setVisibility(View.GONE);

        }else {
            viewHolder.mTextField.setVisibility(View.INVISIBLE);
            viewHolder.mImageField.setVisibility(View.VISIBLE);
            Picasso.with(viewHolder.mImageField.getContext()).load(c.getMessage()).into(viewHolder.mImageField);

        }
        viewHolder.mTimeField.setText(sent_time);
    }
    @Override
    public int getItemCount(){
        return mMessageList.size();
    }
}
