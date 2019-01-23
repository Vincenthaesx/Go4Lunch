package com.example.megaport.go4lunch.main.Controllers.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Api.RestaurantsHelper;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.Controllers.activities.DetailActivity;
import com.example.megaport.go4lunch.main.Controllers.activities.MainActivity;
import com.example.megaport.go4lunch.main.Models.User;
import com.example.megaport.go4lunch.main.Utils.ItemClickSupport;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.main.View.WorkmatesAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;

public class workmatesFragment extends BaseFragment {

    @BindView(R.id.mates_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.mates_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private List<User> mUsers;
    private WorkmatesAdapter mWorkmatesAdapter;

    private CommunicationViewModel mCommunicationViewModel;

    public static workmatesFragment newInstance() {
        return (new workmatesFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.workmates_fragment, container, false );
        ButterKnife.bind( this, view );

        setHasOptionsMenu( true );

        mCommunicationViewModel = ViewModelProviders.of( Objects.requireNonNull( getActivity() ) ).get(CommunicationViewModel.class);
        mCommunicationViewModel.currentUserUID.observe(this, uid -> {
            configureRecyclerView();
            updateUIWhenCreating();
            configureOnClickRecyclerView();
            configureOnSwipeRefresh();
        } );

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu( menu, inflater );
        menu.clear();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private void configureRecyclerView(){
        this.mUsers = new ArrayList<>();
        this.mWorkmatesAdapter = new WorkmatesAdapter(this.mUsers);
        this.mRecyclerView.setAdapter(this.mWorkmatesAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnSwipeRefresh(){
        mSwipeRefreshLayout.setOnRefreshListener( this::updateUIWhenCreating );
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo( mRecyclerView, R.layout.workmates_fragment_item )
                .setOnItemClickListener( ((recyclerView, position, v) -> {
                    User result = mWorkmatesAdapter.getMates( position );
                    retrieveBookedRestaurantByUser(result);
                }) );
    }

    // ----------
    // ACTION
    // ----------

    private void retrieveBookedRestaurantByUser(User user){
        RestaurantsHelper.getBooking( user.getUid(), getTodayDate() ).addOnCompleteListener(bookingTask ->{
            if (bookingTask.isSuccessful()){
                if(!(Objects.requireNonNull( bookingTask.getResult() ).isEmpty())){
                    for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                        showBookedRestaurantByUser(booking.getData().get( "restaurantId" ).toString());
                    }
                } else{
                    Toast.makeText( getContext(), getResources().getString( R.string.mates_hasnt_decided, user.getUsername() ), Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    private void showBookedRestaurantByUser(String placeId){
        Intent intent = new Intent(getActivity(), DetailActivity.class );
        intent.putExtra( "PlaceDetailResult", placeId );
        startActivity( intent );
    }

    // ---------
    // UI
    // ---------

    // Update UI when activity is creating
    private void updateUIWhenCreating(){
        this.mSwipeRefreshLayout.setRefreshing(true);
        CollectionReference collectionReference = UserHelper.getUsersCollection();
        collectionReference.get().addOnCompleteListener(task -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (task.isSuccessful()){
                mUsers.clear();
                for (QueryDocumentSnapshot document : Objects.requireNonNull( task.getResult() )) {
                    if (!(mCommunicationViewModel.getCurrentUserUID().equals(document.getData().get("uid").toString()))){
                        String uid = document.getData().get("uid").toString();
                        String username = document.getData().get("username").toString();
                        String urlPicture = document.getData().get("urlPicture").toString();
                        User userToAdd = new User(uid,username,urlPicture, MainActivity.DEFAULT_SEARCH_RADIUS,MainActivity.DEFAULT_ZOOM,false);
                        mUsers.add(userToAdd);
                    }
                }
            }else {
                Log.e("TAG", "Error getting documents: ", task.getException());
            }
            mWorkmatesAdapter.notifyDataSetChanged();
        })
                .addOnFailureListener(e -> {
                    Objects.requireNonNull( getActivity() ).runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(false));
                    handleError(e);
                });
    }




}
