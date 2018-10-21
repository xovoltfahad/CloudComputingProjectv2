package fastnu.cloudcomputingproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {
    EditText email,phonenNo,userName,age,Userpassword;
    CircleImageView pic;
    Button confirm;
    private FirebaseAuth mAuth;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int RESULT_GALLERY_PHOTO = 1;
    private AlertDialog.Builder mAttachImageDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign_up );

        mAuth = FirebaseAuth.getInstance();
        pic= findViewById( R.id.signup_profileImage );

        email= findViewById( R.id.signup_email );
        phonenNo= findViewById( R.id.signup_phone );
        userName= findViewById( R.id.signup_name );
        age= findViewById( R.id.signup_age );
        confirm= findViewById( R.id.signup_button );
        Userpassword=findViewById( R.id.signup_password );


        pic.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttachImageDialog = new AlertDialog.Builder(getApplicationContext());
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
        }

    }

    public void signUp(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), Userpassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            startActivity( new Intent( SignUp.this,Home.class ).putExtra( "login-method","app" ) );
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                          //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                        }

                        // ...
                    }
                });
    }
}
