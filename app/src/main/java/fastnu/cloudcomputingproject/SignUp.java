package fastnu.cloudcomputingproject;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {
    EditText email,name,age,city,country,phone,password;
    CircleImageView pic;
    Button confirm;
    TextView passwordTextView,emailTextView;
    private FirebaseAuth mAuth;
    static final int REQUEST_IMAGE_CAPTURE = 6;
    static final int RESULT_GALLERY_PHOTO = 1;
    private AlertDialog.Builder mAttachImageDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    Uri dp=null;
    //FirebaseDatabase database ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign_up );

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        pic= findViewById( R.id.signup_dp );
        email= findViewById( R.id.signup_email );
        phone= findViewById( R.id.signup_phone );
        name= findViewById( R.id.signup_name );
        age= findViewById( R.id.signup_age );
        password=findViewById( R.id.signup_password );
        city= findViewById( R.id.signup_city );
        country= findViewById( R.id.signup_country );
        confirm= findViewById( R.id.signup_button );
        passwordTextView= findViewById( R.id.signup_passwordTextView );
        emailTextView= findViewById( R.id.signup_emailTextView );

        //if its a facebook or gmail user

        if(!getIntent().getStringExtra( "login-method" ).equals( "app" )){
            password.setVisibility( View.GONE );
            email.setVisibility( View.GONE );
            emailTextView.setVisibility( View.GONE );
            passwordTextView.setVisibility( View.GONE );

            name.setText( getIntent().getStringExtra( "name" ) );

                //Glide.with( getApplicationContext() )
                        //.load( getIntent().getStringExtra( "profile" ) )
                        //.into( pic );
               // dp=Uri.parse(getIntent().getStringExtra( "profile" ));
            new DownlaodImage().execute( getIntent().getStringExtra( "profile" ));
        }
//////////
        pic.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttachImageDialog = new AlertDialog.Builder(SignUp.this);
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


        confirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(getIntent().getStringExtra( "login-method" ).equals( "app" )){
                    if(dp!=null&&!name.getText().toString().equals( "" )
                            &&!age.getText().toString().equals( "" )
                            &&!city.getText().toString().equals( "" )
                            &&!country.getText().toString().equals( "" )
                            &&!phone.getText().toString().equals( "" )
                            &&!email.getText().toString().equals( "" )
                            &&!password.getText().toString().equals( "" ))
                        signUp();
                    else
                        Toast.makeText( SignUp.this, "Please fill all the Empty Fields", Toast.LENGTH_SHORT ).show();
                }
                else {
                    if(!name.getText().toString().equals( "" )
                            &&!age.getText().toString().equals( "" )
                            &&!city.getText().toString().equals( "" )
                            &&!country.getText().toString().equals( "" )
                            &&!phone.getText().toString().equals( "" ))
                        Saving();
                    else
                        Toast.makeText( SignUp.this, "Please fill all the Empty Fields", Toast.LENGTH_SHORT ).show();
                }

            }
        } );

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
            pic.setImageURI(dp);
        }
        else if (requestCode == RESULT_GALLERY_PHOTO && resultCode == RESULT_OK ) {
            Uri selectedimg= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap( getApplicationContext().getContentResolver(),selectedimg );
            } catch (IOException e) {
                e.printStackTrace();
            }
            pic.setImageURI(selectedimg);
            dp=selectedimg;
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void signUp(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                           Saving();
                            // Sign in success, update UI with the signed-in user's information

                          //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                        }

                        // ...
                    }
                });
    }


    public void Saving(){
        String Email;
        if(!getIntent().getStringExtra( "login-method" ).equals( "app" )) {
            Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        }
        else{
            Email=email.getText().toString();
        }
        user newUser = new user( name.getText().toString(),age.getText().toString(),city.getText().toString(),
                country.getText().toString(),phone.getText().toString(),dp==null?getIntent().getStringExtra( "profile" ):dp.toString(),Email,"0");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        databaseReference.child( "users" ).child(currentUser.getUid().toString()).setValue( newUser );
            if(dp!=null) {
                uploadImage( dp );
            }
            else{
                startActivity( new Intent( SignUp.this,Home.class ).putExtra( "login-method","app" ) );

                finish();
            }
    }
    public String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType( contentResolver.getType( uri ) );

    }

    public void uploadImage(final Uri uri){

            StorageReference imageRef = mStorageRef.child(mAuth.getCurrentUser().getUid().toString()+"."+getFileExtension( uri ));
            imageRef.putFile( uri )
                    .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child( mAuth.getCurrentUser().getUid().toString() );
                                    //mDatabaseRef.child( "profile" ).setValue( mAuth.getCurrentUser().getUid().toString()+"."
                                    //+getFileExtension( uri ));
                                    mDatabaseRef.child( "profile" ).setValue(uri.toString() );
                                    dp=null;
                                    startActivity( new Intent( SignUp.this,Home.class ).putExtra( "login-method","app" ) );

                                    finish();
                                }
                            } ).addOnFailureListener( new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText( SignUp.this, "Image Url Corrupted", Toast.LENGTH_SHORT ).show();
                                }
                            } );

                        }
                    } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText( SignUp.this, "Upload UnSuccessfull!", Toast.LENGTH_SHORT ).show();
                        }
                    } );



    }
    public class DownlaodImage extends AsyncTask<String,Void,Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String urldisaplay= strings[0];
            Bitmap bmp= null;

            try {
                InputStream in = new java.net.URL(urldisaplay).openStream();
                bmp= BitmapFactory.decodeStream( in );
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pic.setImageBitmap( bitmap );
            dp=getImageUri( getApplicationContext(),bitmap );
            //convert bitmap i nto uri
        }
    }

}
