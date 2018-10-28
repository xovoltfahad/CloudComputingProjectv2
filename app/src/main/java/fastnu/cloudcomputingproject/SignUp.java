package fastnu.cloudcomputingproject;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {
    EditText email,name,age,city,country,phone,password;
    CircleImageView pic;
    Button confirm;
    TextView passwordTextView,emailTextView;
    private FirebaseAuth mAuth;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int RESULT_GALLERY_PHOTO = 1;
    private AlertDialog.Builder mAttachImageDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    Uri dp;
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

        if(!getIntent().getStringExtra( "login-method" ).equals( "app" )){
            password.setVisibility( View.GONE );
            email.setVisibility( View.GONE );
            emailTextView.setVisibility( View.GONE );
            passwordTextView.setVisibility( View.GONE );

            name.setText( getIntent().getStringExtra( "name" ) );
            Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra( "profile" ))
                    .into(pic);


        }

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
                signUp();
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

            pic.setImageBitmap(bitmap);
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

    public void signUp(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String Email;
                            if(!getIntent().getStringExtra( "login-method" ).equals( "app" )) {
                                Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                if(dp==null)
                                    dp=Uri.parse(  getIntent().getStringExtra( "profile" ));
                            }
                            else{
                                Email=email.getText().toString();
                            }
                            user newUser = new user( name.getText().toString(),age.getText().toString(),city.getText().toString(),
                                    country.getText().toString(),phone.getText().toString(),"uri",Email);

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            databaseReference.child( "users" ).child(currentUser.getUid().toString()).setValue( newUser );
                            uploadImage(dp  );
                            // Sign in success, update UI with the signed-in user's information

                          //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                        }

                        // ...
                    }
                });
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
                       mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child( mAuth.getCurrentUser().getUid().toString() );
                        mDatabaseRef.child( "profile" ).setValue( mAuth.getCurrentUser().getUid().toString()+"."
                        +getFileExtension( uri ));
                        dp=null;
                        startActivity( new Intent( SignUp.this,Home.class ).putExtra( "login-method","app" ) );

                        finish();
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                } );


    }

}
