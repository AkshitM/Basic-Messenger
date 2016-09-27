package com.quickenloans.techconference.basicmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class ActivityLogin extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "LogInActivity";

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    private SignInButton googleSignInButton;

    private GoogleApiClient googleApiClient;

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private CallbackManager facebookLoginCallbackManager;

    private ProgressBar progressBar;

    private RelativeLayout wrapperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.login_layout);

        getSupportActionBar().hide();

        wrapperLayout = (RelativeLayout) findViewById(R.id.wrapper);
        progressBar = (ProgressBar) findViewById(R.id.loginProgressBar);

        googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button_google);
        googleSignInButton.setOnClickListener(this /* OnClickListener */);


        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        // Configure Facebook Sign In


        facebookLoginCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.sign_in_button_facebook);
        assert facebookLoginButton != null;
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.setOnClickListener(this /* OnClickListener */);

        facebookLoginButton.registerCallback(facebookLoginCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                authenticateFacebookTokenWithFirebase(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                wrapperLayout.setVisibility(LinearLayout.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                wrapperLayout.setVisibility(LinearLayout.VISIBLE);
            }
        });


        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);

                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        firebaseAuth = FirebaseAuth.getInstance();


        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        animation.setDuration(1500);
        wrapperLayout.setAnimation(animation);



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_google:
                authenticateWithGoogle();
                break;
            case R.id.sign_in_button_facebook:
                wrapperLayout.setVisibility(LinearLayout.INVISIBLE);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                break;
        }
    }

    private void authenticateWithGoogle() {
        wrapperLayout.setVisibility(LinearLayout.INVISIBLE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                authenticateGoogleAccountWithFirebase(account);
            } else {
                Log.e(TAG, "Google Sign In failed.");
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                wrapperLayout.setVisibility(LinearLayout.VISIBLE);
                Toast.makeText(ActivityLogin.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            facebookLoginCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void authenticateGoogleAccountWithFirebase(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(ActivityLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                            finish();
                        }
                    }
                });
    }

    private void authenticateFacebookTokenWithFirebase(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ActivityLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                            finish();
                        }
                    }
                });
    }





    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthStateListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
        }
    }

}
