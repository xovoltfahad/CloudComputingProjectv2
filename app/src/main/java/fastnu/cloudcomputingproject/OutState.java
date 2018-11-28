package fastnu.cloudcomputingproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class OutState extends Fragment {
    private RecyclerView comp_recycler_view;
    private OutStateListAdapter cAdapter;
    List <imagePost> arr=  new ArrayList<imagePost>(  );
    MysharedPrefrencess sharedPref;
    private AlertDialog.Builder mAttachImageDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_out_state, container, false );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        sharedPref= new MysharedPrefrencess( getContext() );
        comp_recycler_view = (RecyclerView) view.findViewById( R.id.outstate_recylerview );
        cAdapter = new OutStateListAdapter( arr, getActivity(), getActivity() );
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager( getActivity() );
        comp_recycler_view.setLayoutManager( mLayoutManager );
        comp_recycler_view.setItemAnimator( new DefaultItemAnimator() );
        comp_recycler_view.setAdapter( cAdapter );
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(comp_recycler_view.getContext(),LinearLayout.VERTICAL );
        comp_recycler_view.addItemDecoration(dividerItemDecoration);
        cAdapter.notifyDataSetChanged();

        fetchPosts();
    }

    public void fetchPosts(){
        if(Integer.parseInt(  sharedPref.getUser().getPostCount())!=0) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference( "posts" ).child( FirebaseAuth.getInstance()
                    .getUid() );
            databaseReference.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count=Integer.parseInt( sharedPref.getUser().getPostCount() );
                   for(int i=0;i<count;i++)
                   {
                       imagePost post= new imagePost(  );
                       post= dataSnapshot.child( String.valueOf( i ) ).getValue(imagePost.class);
                       arr.add( post );
                   }
                   cAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }
    }

    public class OutStateListAdapter extends RecyclerView.Adapter<OutStateListAdapter.MyViewHolder>{
        private Activity activity;
        private List<imagePost> arr;
        private Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder {


            private TextView userName,description;
            private ImageView image;
            private Button rateNreview;
            //RelativeLayout parentLayout;
            ImageButton OptionsMenuButton;
            public MyViewHolder(View view) {
                super(view);
                userName= view.findViewById( R.id.post_username );
                description=view.findViewById( R.id.post_description );
                image=view.findViewById( R.id.post_image );
                OptionsMenuButton= view.findViewById( R.id.options );
                rateNreview=view.findViewById( R.id.post_rateNreview );
                //parentLayout= view.findViewById( R.id.parent_layout );
            }
        }

        public OutStateListAdapter(List<imagePost> arr, Context context, Activity activity) {
            this.arr = arr;
            this.context=context;
            this.activity=activity;
        }

        @Override
        public OutStateListAdapter.MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_item_image, parent, false);

            final OutStateListAdapter.MyViewHolder myViewHolder = new OutStateListAdapter.MyViewHolder(itemView);

            return  myViewHolder;
        }

        @Override
        public void onBindViewHolder(OutStateListAdapter.MyViewHolder holder, final int position) {

            imagePost c = arr.get(position);
            holder.userName.setText(c.getUsername());
            holder.description.setText(c.getDescription());
            Glide.with(getContext())
                    .load(c.getPicUrl())
                    .into(holder.image);
            holder.OptionsMenuButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAttachImageDialog = new AlertDialog.Builder(getActivity());
                    mAttachImageDialog.setItems(new CharSequence[]{"Edit Post", "Delete Post"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    // Delete();
                                    break;
                                case 1:
                                    //EditPost();
                                    break;
                            }
                        }
                    });
                    mAttachImageDialog.show();
                }
            } );

          /*  holder.parentLayout.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Orders viewOrder = arr.get( position );
                    getPropertyRequest( Double.parseDouble( viewOrder.getLat() ),Double.parseDouble( viewOrder.getLng() ) );
                }
            } );*/
        }

        @Override
        public int getItemCount() {
            return arr.size();
        }
    }

}
