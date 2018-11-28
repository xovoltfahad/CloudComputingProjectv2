package fastnu.cloudcomputingproject;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;



import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.facebook.FacebookSdk;

import com.facebook.login.LoginManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {



    GoogleApiClient mGoogleApiClient;
    MysharedPrefrencess sharedPref;
    FirebaseAuth mAuth;
    ImageView imageView;
    EditText descriptionText;
    Button postButton;
    private AlertDialog.Builder mAttachImageDialog;
    static final int REQUEST_IMAGE_CAPTURE = 6;
    static final int RESULT_GALLERY_PHOTO = 1;
    Uri dp=null;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 8;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        FacebookSdk.sdkInitialize( getApplicationContext() );
        setContentView( R.layout.activity_home );
        sharedPref= new MysharedPrefrencess( getApplicationContext() );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageView= findViewById( R.id.postpic );
        descriptionText=findViewById( R.id.description_text );
        postButton=findViewById( R.id.postBtn );





            FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference( "users" ).child( currentUser.getUid().toString() );
            databaseReference.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user newuser = new user();
                    newuser = dataSnapshot.getValue( user.class );
                    sharedPref.setUser( newuser );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }


        imageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttachImageDialog = new AlertDialog.Builder(Home.this);
                mAttachImageDialog.setItems(new CharSequence[]{"Camera", "Galley"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                CameraPictureIntent();
                                break;
                            case 1:
                                galleryPhoto();
                                break;
                        }
                    }
                });
                mAttachImageDialog.show();
            }
        } );

        postButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(dp!=null)
                uploadImage( dp );
            else
                Toast.makeText( Home.this, "Please capture or Select Image First", Toast.LENGTH_SHORT ).show();
            }
        } );

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );
        getLocationPermission();
        getDeviceLocation();


    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();

                        } else {
                            Log.d("Location Error:", "Current location is null. Using defaults.");
                            Log.e("Location Error:", "Exception: %s", task.getException());

                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }


    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            String[] permissions={"android.permission.ACCESS_FINE_LOCATION"};
            requestPermissions(
                    permissions,
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if ( permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                }
            }
        }

    }

    private void galleryPhoto(){
        Intent gallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_GALLERY_PHOTO);
    }

    private void CameraPictureIntent() {
        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras= data.getExtras();
            Bitmap bitmap =(Bitmap) (extras != null ? extras.get("data") : null);

            dp=getImageUri( getApplicationContext(),bitmap );
            imageView.setImageURI(dp);
        }
        else if (requestCode == RESULT_GALLERY_PHOTO && resultCode == RESULT_OK ) {
            Uri selectedimg= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap( getApplicationContext().getContentResolver(),selectedimg );
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageURI(selectedimg);
            dp=selectedimg;
        }
        getDeviceLocation();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            getSupportFragmentManager().beginTransaction().replace( R.id.mainlayout,new Profile() ).commit();
        }

        else if(id== R.id.nav_diary)
        {
            startActivity( new Intent( this,DiaryTabs.class ) );
        }
        else if(id== R.id.nav_logout)
        {
            String Method=getIntent().getStringExtra( "login-method" );
                if(Method.equals(  "fb")){
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    sharedPref.removeAll();
                    startActivity( new Intent( Home.this,Authentication.class ) );
                    finish();
                }
                else if (Method.equals(  "google")) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder( com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient googleSignInClient= GoogleSignIn.getClient( this,gso );
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut().addOnCompleteListener( this, new OnCompleteListener <Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sharedPref.removeAll();
                            startActivity( new Intent( Home.this,Authentication.class ) );
                            finish();
                        }
                    } );

                }
                else if(Method.equals( "app" )){
                    FirebaseAuth.getInstance().signOut();
                    sharedPref.removeAll();
                    startActivity( new Intent( Home.this,Authentication.class ) );
                    finish();

                    }
            }
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType( contentResolver.getType( uri ) );

    }

    public void uploadImage(final Uri uri){
        Geocoder geocoder;

        geocoder = new Geocoder(this, Locale.getDefault());



        try {
            addresses = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);
            // address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            //state = addresses.get(0).getAdminArea();
           // country = addresses.get(0).getCountryName();

        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageReference imageRef = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension( uri ));
        imageRef.putFile( uri )
                .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mDatabaseRef = FirebaseDatabase.getInstance().getReference("posts").child( mAuth.getCurrentUser().getUid() )
                                        .child(sharedPref.getUser().getPostCount() );
                                //mDatabaseRef.child( "profile" ).setValue( mAuth.getCurrentUser().getUid().toString()+"."
                                //+getFileExtension( uri ));

                                mDatabaseRef.child( "picUrl" ).setValue(uri.toString() );
                                mDatabaseRef.child( "username" ).setValue( sharedPref.getUser().getName() );
                                mDatabaseRef.child( "description" ).setValue( descriptionText.getText().toString() );
                                mDatabaseRef.child( "city" ).setValue(addresses.get(0).getLocality());
                                mDatabaseRef.child( "country" ).setValue( addresses.get(0).getCountryName() );
                                int count= Integer.parseInt( sharedPref.getUser().getPostCount() );
                                count=count+1;
                                sharedPref.getUser().setPostCount( String.valueOf( count ) );
                                mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child( mAuth.getCurrentUser().getUid());
                                mDatabaseRef.child( "postCount" ).setValue( String.valueOf( count ) );
                                dp=null;


                                finish();
                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText( Home.this, "Image Url Corrupted", Toast.LENGTH_SHORT ).show();
                            }
                        } );

                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( Home.this, "Upload UnSuccessfull!", Toast.LENGTH_SHORT ).show();
                    }
                } );

    }



}
