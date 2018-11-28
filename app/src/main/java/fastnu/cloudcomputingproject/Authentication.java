package fastnu.cloudcomputingproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    Button signup,login;
    EditText email,password;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_authentication );


        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        share=  new MysharedPrefrencess( getApplicationContext() );
        signup =findViewById( R.id.auth_signup );
        login=findViewById( R.id.authentication_loginBtn );
        email= findViewById( R.id.authentication_email );
        password=findViewById( R.id.authentication_password );

        fb= findViewById( R.id.login_button );
        fb.setReadPermissions( "email" );

        login.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email,Password;
                Email=email.getText().toString().trim();
                Password=password.getText().toString().trim();
                if(!Email.equals( null) &&!Password.equals( null ) ){
                   signInAppMethod( Email,Password );
                }
                else{
                    Toast.makeText( getApplicationContext(), "Please fill all the empty Fields", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

// Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Continue With Google");
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
                share.setName( profile.getName() );
                share.setProfilePic( profile.getProfilePictureUri( 150,150 ) );

               handleFacebookAccessToken(loginResult.getAccessToken(),profile);


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        } );

        signup.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( Authentication.this,SignUp.class ).putExtra( "login-method","app" ) );

            }
        } );

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

        mAuth = FirebaseAuth.getInstance();

        //This is to check if user already signed IN? if yes who's the provider?
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child( currentUser.getUid().toString() );
            databaseReference.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user newuser = new user(  );
                    newuser = dataSnapshot.getValue(user.class);
                    share.setUser( newuser );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (userInfo.getProviderId().equals( "facebook.com" )) {
                    Toast.makeText( this, "Facebook", Toast.LENGTH_SHORT ).show();
                    startActivity( new Intent( Authentication.this, Home.class ).putExtra( "login-method", "fb" ) );
                    finish();
                } else if (userInfo.getProviderId().equals( "google.com" )) {
                    Toast.makeText( this, "Google", Toast.LENGTH_SHORT ).show();
                    startActivity( new Intent( Authentication.this, Home.class ).putExtra( "login-method", "google" ) );
                    finish();
                }
                else
                {
                    Toast.makeText( this, "app method", Toast.LENGTH_SHORT ).show();
                    startActivity( new Intent( Authentication.this, Home.class ).putExtra( "login-method", "app" ) );
                    finish();
                }
            }

        }
        ////user signed iN staus check end here

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    25);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
           // handleSignInResult(task);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            boolean isNewUser =task.getResult().getAdditionalUserInfo().isNewUser();
                            FirebaseUser user = mAuth.getCurrentUser();
                            share.setName( acct.getDisplayName() );
                            share.setProfilePic( acct.getPhotoUrl() );
                            if(isNewUser) {
                                                                startActivity( new Intent( Authentication.this, SignUp.class )
                                        .putExtra( "login-method", "google" )
                                        .putExtra( "name",acct.getDisplayName() )
                                        .putExtra( "profile",acct.getPhotoUrl() .toString())//Uri
                                            );
                                finish();
                            }
                            else {
                                startActivity( new Intent( Authentication.this, Home.class ).putExtra( "login-method", "google" ) );
                                finish();
                            }
                        } else {
                            Toast.makeText( Authentication.this, "Authentication Error", Toast.LENGTH_SHORT ).show();
                        }

                        // ...
                    }
                });
    }


    private void handleFacebookAccessToken(AccessToken token, final Profile profile) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            boolean isNewUser =task.getResult().getAdditionalUserInfo().isNewUser();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if(isNewUser){

                                startActivity(  new Intent( Authentication.this,SignUp.class )
                                        .putExtra( "login-method","fb" )
                                        .putExtra( "name",profile.getName() )
                                        .putExtra( "profile",profile.getProfilePictureUri( 150,150 ).toString() )//Uri
                                );
                            }
                            else
                                startActivity(  new Intent( Authentication.this,Home.class ).putExtra( "login-method","fb" ) );
                            finish();


                        } else {
                            Toast.makeText( Authentication.this, "Authentication Error", Toast.LENGTH_SHORT ).show();
                        }

                        // ...
                    }
                });
    }

    public void signInAppMethod(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(  new Intent( Authentication.this,Home.class ).putExtra( "login-method","app" ) );
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText( Authentication.this, "Credentials MisMatched or User Not Exists", Toast.LENGTH_SHORT ).show();

                        }

                        // ...
                    }
                });
    }
}
