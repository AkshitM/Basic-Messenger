package com.quickenloans.techconference.basicmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityMain extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, CustomRecyclerViewAdapter.Listener {

    private static final String TAG = "MainActivity";

    public static final String REFERENCE_DATABASE_NAME = "messages";

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 255;

    public static final String ANONYMOUS = "anonymous";
    private String currentUserName;
    private String currentUserPhotoUrl;

    private Button sendMessageButton;
    private EditText messageEditText;

    private RecyclerView messageRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private ProgressBar progressBar;

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DatabaseReference firebaseDatabaseReference;

    private CustomRecyclerViewAdapter firebaseRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default username is anonymous.
        currentUserName = ANONYMOUS;

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, ActivityLogin.class));
            finish();
            return;
        } else {
            currentUserName = firebaseUser.getDisplayName();
            if (firebaseUser.getPhotoUrl() != null) {
                currentUserPhotoUrl = firebaseUser.getPhotoUrl().toString();
            }
        }

        // Initialize ProgressBar and RecyclerView.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        messageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);

        linearLayoutManager = new LinearLayoutManager(this);

        // start populating data at bottom of layout
        linearLayoutManager.setStackFromEnd(true);

        // New child entries
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseRecyclerViewAdapter = new CustomRecyclerViewAdapter(Message.class, R.layout.message_item_incoming, CustomRecyclerViewAdapter.MessageViewHolder.class, firebaseDatabaseReference.child(REFERENCE_DATABASE_NAME), this, currentUserName, this);

        firebaseRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int totalMessageCount = firebaseRecyclerViewAdapter.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (shouldScrollToBottomOfList(positionStart, totalMessageCount, lastVisiblePosition)) {
                    messageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.setAdapter(firebaseRecyclerViewAdapter);


        messageEditText = (EditText) findViewById(R.id.messageEditText);
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});


        messageEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendMessageButton.setEnabled(true);
                } else {
                    sendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sendMessageButton = (Button) findViewById(R.id.sendButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new
                        Message(messageEditText.getText().toString(),
                        currentUserName,
                        currentUserPhotoUrl);
                firebaseDatabaseReference.child(REFERENCE_DATABASE_NAME)
                        .push().setValue(message);
                messageEditText.setText("");
            }
        });
    }

    private boolean shouldScrollToBottomOfList(int positionStart, int totalMessageCount, int lastVisiblePosition) {
        return lastVisiblePosition == -1 ||
                (positionStart >= (totalMessageCount - 1) &&
                        lastVisiblePosition == (positionStart - 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                firebaseAuth.signOut();
                currentUserName = ANONYMOUS;
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, ActivityLogin.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void initialDataLoaded() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }
}
