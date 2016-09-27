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

    private Context context;

    private String currentUser;

    private Listener listener;

    private int lastPosition = -1;

    private static final int VIEW_TYPE_INCOMING_MESSAGE = 0;
    private static final int VIEW_TYPE_OUTGOING_MESSAGE = 1;


    public CustomRecyclerViewAdapter(Class modelClass, int modelLayout, Class viewHolderClass, DatabaseReference ref, Context context, String currentUser, Listener listener) {
        super(modelClass, modelLayout, viewHolderClass, ref);

        this.context = context;
        this.currentUser = currentUser;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        Message currentMessage = (Message) getItem(position);

        if (!currentMessage.getName().equals(currentUser)) {
            return VIEW_TYPE_INCOMING_MESSAGE;
        } else {
            return VIEW_TYPE_OUTGOING_MESSAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_INCOMING_MESSAGE:
                ViewGroup incomingMessageView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.message_item_incoming, parent, false);
                MessageViewHolder incomingMessageViewHolder = new MessageViewHolder(incomingMessageView);
                return incomingMessageViewHolder;

            case VIEW_TYPE_OUTGOING_MESSAGE:
                ViewGroup outgoingMessageView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.message_item_outgoing, parent, false);
                MessageViewHolder outgoingMessageViewHolder = new MessageViewHolder(outgoingMessageView);
                return outgoingMessageViewHolder;

            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    protected void populateViewHolder(RecyclerView.ViewHolder baseViewHolder, Object messageObject, int position) {
        MessageViewHolder viewHolder = (MessageViewHolder) baseViewHolder;

        Message currentMessage = (Message) messageObject;

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

        public void clearAnimation() {
            wrapperLayout.clearAnimation();
        }

    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce);
            animation.setDuration(2500);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Prevent animations from going while user is scrolling
    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        ((MessageViewHolder) holder).clearAnimation();
    }


    public interface Listener {
        void initialDataLoaded();
    }
}
