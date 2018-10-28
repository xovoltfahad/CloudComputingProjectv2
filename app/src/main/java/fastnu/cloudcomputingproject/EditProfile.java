package fastnu.cloudcomputingproject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


public class EditProfile extends Fragment {
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private AlertDialog.Builder mAttachImageDialog;
    private static final int CAPTURE_IMAGE_REQUEST = 3332;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int RESULT_GALLERY_PHOTO = 1;
    ImageView profilePic;
    Button saveBtutton;
    EditText name,age,city,country;
    private File mCameraPhotoFile;
    Uri imageUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_edit_profile, container, false );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );

        name= view.findViewById( R.id.editProfile_name );
        age= view.findViewById( R.id.editProfile_age );
        city= view.findViewById( R.id.editProfile_city );
        country= view.findViewById( R.id.editProfile_country );
        saveBtutton= view.findViewById( R.id.buttonSave );
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        profilePic= view.findViewById( R.id.editProfileImage );
        profilePic.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttachImageDialog = new AlertDialog.Builder(getActivity());
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

        saveBtutton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri!=null) {
                    uploadImage( imageUri );
                }
                else{
                    Toast.makeText( getActivity(), "Upload Image First", Toast.LENGTH_SHORT ).show();
                }
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("users").child( FirebaseAuth.getInstance().getCurrentUser().getUid().toString() );
                if(!name.getText().toString().equals( null ))
                databaseReference.child( "name" ).setValue( name.getText().toString() );
                if(!age.getText().toString().equals( null ))
                databaseReference.child( "age" ).setValue( age.getText().toString() );
                if(!city.getText().toString().equals( null ))
                databaseReference.child( "city" ).setValue( city.getText().toString() );
                if(!country.getText().toString().equals( null ))
                databaseReference.child( "country" ).setValue( country.getText().toString() );

            }
        } );
    }

    private void galleryPhoto(){
        Intent gallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RESULT_GALLERY_PHOTO);
    }

    private void CameraPictureIntent() {
        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras= data.getExtras();
            Bitmap bitmap =(Bitmap) (extras != null ? extras.get("data") : null);

            profilePic.setImageBitmap(bitmap);
        }
        else if (requestCode == RESULT_GALLERY_PHOTO && resultCode == RESULT_OK ) {
            Uri selectedimg= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap( getApplicationContext().getContentResolver(),selectedimg );
            } catch (IOException e) {
                e.printStackTrace();
            }
            profilePic.setImageURI(selectedimg);
            imageUri=selectedimg;
        }

    }
    public static File getTempFile(Context context) {
        String tempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/TripDiary/" + "temp" + ".jpg";
        File tempFile = new File(tempFilePath);
        if(!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        return tempFile;
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        if(path!=null)
        return Uri.parse(path);
        else
            return null;
    }

    public String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType( contentResolver.getType( uri ) );

    }

    public void uploadImage(Uri uri){

        StorageReference imageRef = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension( uri ));
        imageRef.putFile( uri )
                .addOnSuccessListener( new OnSuccessListener <UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String id = mDatabaseRef.push().getKey();
               String url =  taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
            mDatabaseRef.child( id ).setValue( "firstImage"+url );
            }
        } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                } );


    }


}
