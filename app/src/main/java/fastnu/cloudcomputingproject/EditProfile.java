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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    MysharedPrefrencess sharedPref;
    private File mCameraPhotoFile;
    user currentUser;
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


        sharedPref=new MysharedPrefrencess( getApplicationContext() );
        currentUser=sharedPref.getUser();
        name= view.findViewById( R.id.editProfile_name );
        age= view.findViewById( R.id.editProfile_age );
        city= view.findViewById( R.id.editProfile_city );
        country= view.findViewById( R.id.editProfile_country );
        saveBtutton= view.findViewById( R.id.buttonSave );

        name.setText( currentUser.getName() );
        age.setText( currentUser.getAge() );
        city.setText(currentUser.getCity());
        country.setText( currentUser.getCountry() );

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        profilePic= view.findViewById( R.id.editProfileImage );
        Glide.with(getContext())
                .load(currentUser.getProfile())
                .into(profilePic);

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
                if(!name.getText().toString().equals( "" ))
                databaseReference.child( "name" ).setValue( name.getText().toString() );
                else
                    databaseReference.child( "name" ).setValue( currentUser.getName() );
                if(!age.getText().toString().equals( "" ))
                databaseReference.child( "age" ).setValue( age.getText().toString() );
                else
                    databaseReference.child( "age" ).setValue( currentUser.getAge() );
                if(!city.getText().toString().equals( "" ))
                databaseReference.child( "city" ).setValue( city.getText().toString() );
                else
                    databaseReference.child( "city" ).setValue( currentUser.getCity() );
                if(!country.getText().toString().equals( "" ))
                databaseReference.child( "country" ).setValue( country.getText().toString() );
                else
                    databaseReference.child( "country" ).setValue( currentUser.getCountry() );


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
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bundle extras= data.getExtras();
            Bitmap bitmap =(Bitmap) (extras != null ? extras.get("data") : null);

            imageUri=getImageUri( getActivity(),bitmap );
            profilePic.setImageURI(imageUri);
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }





    public String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType( contentResolver.getType( uri ) );

    }


    public void uploadImage(final Uri uri){

        StorageReference imageRef = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension( uri ));
        imageRef.putFile( uri )
                .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child( FirebaseAuth.getInstance().getCurrentUser().getUid().toString() );
                                //mDatabaseRef.child( "profile" ).setValue( mAuth.getCurrentUser().getUid().toString()+"."
                                //+getFileExtension( uri ));
                                mDatabaseRef.child( "profile" ).setValue(uri.toString() );
                                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference( "users" ).child( FirebaseAuth.getInstance().getCurrentUser().getUid().toString() );
                                databaseReference1.addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        user newuser = new user();
                                        newuser = dataSnapshot.getValue( user.class );
                                        sharedPref.setUser( newuser );
                                        getFragmentManager().popBackStack();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                } );




                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText( getContext(), "Image Url Corrupted", Toast.LENGTH_SHORT ).show();
                            }
                        } );

                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( getContext(), "Upload UnSuccessfull!", Toast.LENGTH_SHORT ).show();
                    }
                } );



    }


}
