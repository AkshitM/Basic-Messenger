package com.quickenloans.techconference.basicmessenger;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomRecyclerViewAdapter extends FirebaseRecyclerAdapter {

    Context context;

    String currentUser;

    DatabaseReference ref;

    Listener listener;

    private int lastPosition = -1;

    @Override
    public int getItemViewType(int position) {

        Message message = (Message) getItem(position);

        if (!message.getName().equals(currentUser)) {
            return 0;
        } else {
            return 1;
        }
    }

    int idDefaultLayout;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 0:
                ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_incoming, parent, false);
                MessageViewHolder constructor = new MessageViewHolder(view);
                return constructor;

            case 1:
                ViewGroup view2 = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_outgoing, parent, false);
                MessageViewHolder constructor2 = new MessageViewHolder(view2);
                return constructor2;

            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    public CustomRecyclerViewAdapter(Class modelClass, int modelLayout, Class viewHolderClass, DatabaseReference ref, Context context, String currentUser, Listener listener) {
        super(modelClass, modelLayout, viewHolderClass, ref);

        this.ref = ref;
        this.context = context;
        this.currentUser = currentUser;
        this.idDefaultLayout = modelLayout;

        this.listener = listener;


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void populateViewHolder(RecyclerView.ViewHolder baseViewHolder, Object model, int position) {
        MessageViewHolder viewHolder = (MessageViewHolder) baseViewHolder;

        Message currentMessage = (Message) model;

        viewHolder.messageTextView.setText(currentMessage.getText());
        viewHolder.messengerTextView.setText(currentMessage.getName());
        if (currentMessage.getPhotoUrl() == null) {
            viewHolder.messengerImageView
                    .setImageDrawable(ContextCompat
                            .getDrawable(context,
                                    R.mipmap.default_image_circle));
        } else {
            Glide.with(context)
                    .load(currentMessage.getPhotoUrl())
                    .into(viewHolder.messengerImageView);
        }



        listener.initialDataLoaded();

        setAnimation(viewHolder.wrapperLayout, position);
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout wrapperLayout;
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            wrapperLayout = (LinearLayout) itemView.findViewById(R.id.wrapperLayout);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }

        public void clearAnimation()
        {
            wrapperLayout.clearAnimation();
        }

    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
//            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce);
            animation.setDuration(3000);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder)
    {
        ((MessageViewHolder)holder).clearAnimation();
    }


    public interface Listener {
        void initialDataLoaded();
    }
}
