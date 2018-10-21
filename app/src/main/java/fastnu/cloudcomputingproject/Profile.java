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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment {

    ImageButton editProfile;
    TextView name;MysharedPrefrencess share;
    CircleImageView profilePic;



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

        new DownlaodImage().execute( share.getProfilePic() );

        name= view.findViewById( R.id.profileFrag_name );
        name.setText( share.getName() );
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
}
