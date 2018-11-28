package fastnu.cloudcomputingproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment {

    ImageButton editProfile;
    TextView name,age,city,country,phone,email;
    MysharedPrefrencess share;
    CircleImageView profilePic;
    StorageReference storageReference;

    user curentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_profile, container, false );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        share= new MysharedPrefrencess( getActivity() );
        editProfile= view.findViewById( R.id.imageButtonEditProfile );
        profilePic= view.findViewById( R.id.profile_image );

       /* storageReference= FirebaseStorage.getInstance().getReference().child( curentUser.getProfile() );

       storageReference.getDownloadUrl().addOnSuccessListener( new OnSuccessListener <Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //uri is actually the download url here


            }
        } );

*/
     //   new DownlaodImage().execute( );

        name= view.findViewById( R.id.profileFrag_name );
        age= view.findViewById( R.id.profileFrag_age );
        city= view.findViewById( R.id.profileFrag_city );
        country= view.findViewById( R.id.profileFrag_country );
        phone= view.findViewById( R.id.profileFrag_phone );
        email = view.findViewById( R.id.profileFrag_email );

        editProfile.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace( R.id.mainlayout,new EditProfile() ).addToBackStack( "Profile" ).commit();
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
            profilePic.setImageBitmap( bitmap );
        }
    }

    @Override
    public void onResume() {

        curentUser = share.getUser();
        Glide.with(getContext())
                .load(curentUser.getProfile())
                .into(profilePic);
        name.setText( curentUser.getName() );
        age.setText( curentUser.getAge() );
        city.setText( curentUser.getCity() );
        country.setText( curentUser.getCountry() );
        phone.setText( curentUser.getPhone() );
        email.setText( curentUser.getEmail() );
        super.onResume();
    }
}
