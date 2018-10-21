package fastnu.cloudcomputingproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class Authentication extends AppCompatActivity {
    LoginButton fb;
    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    MysharedPrefrencess share;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    int RC_SIGN_IN=123;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_authentication );
        mAuth = FirebaseAuth.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        share=  new MysharedPrefrencess( getApplicationContext() );
        fb= findViewById( R.id.login_button );
        fb.setReadPermissions( "email" );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult( mGoogleSignInClient.getSignInIntent(),RC_SIGN_IN );
            }
        } );

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };



        profileTracker= new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
              //  nextActivity(newProfile);
            }
        };

        fb.registerCallback( callbackManager, new FacebookCallback <LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Profile profile =Profile.getCurrentProfile();
                share.setName( profile.getFirstName()+" "+profile.getLastName() );
                share.setProfilePic( profile.getProfilePictureUri( 150,150 ) );
               handleFacebookAccessToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        } );

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
           final GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener( Authentication.this, new OnCompleteListener <AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task <AuthResult> task) {
                             if(task.isSuccessful()){
                                 FirebaseUser user= mAuth.getCurrentUser();
                                 share.setName( account.getDisplayName() );
                                 share.setProfilePic( account.getPhotoUrl() );
                                 startActivity(  new Intent( Authentication.this,Home.class ).putExtra( "login-method","google" ) );
                                 finish();
                             }
                        }
                    } );
            // Signed in successfully, show authenticated UI.


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());

        }
    }
    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(  new Intent( Authentication.this,Home.class ).putExtra( "login-method","fb" ) );
                            finish();

                        } else {
                            Toast.makeText( Authentication.this, "Authentication Error", Toast.LENGTH_SHORT ).show();
                        }

                        // ...
                    }
                });
    }

}
