package com.example.megaport.go4lunch.main.Controllers.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.megaport.go4lunch.main.Api.RestaurantsHelper;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.main.Controllers.fragment.listViewFragment;
import com.example.megaport.go4lunch.main.Controllers.fragment.mapViewFragment;
import com.example.megaport.go4lunch.main.Controllers.fragment.workmatesFragment;
import com.example.megaport.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener{

    // BIND
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.bottom_navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.simple_toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_view) NavigationView mNavigationView;

    //FOR DATA
    private static final int SIGN_OUT_TASK = 10;
    private static final int TITLE_HUNGRY = R.string.hungry;

    // FOR FRAGMENTS
    private listViewFragment fragmentListView;
    private mapViewFragment fragmentMapView;
    private workmatesFragment fragmentWorkmates;

    //Identity each activity with a number
    private static final int ACTIVITY_SETTINGS = 5;
    private static final int ACTIVITY_DETAIL = 6 ;
    private static final int ACTIVITY_CHAT = 8 ;
    private static final int ACTIVITY_LOGIN = 7 ;

    //Default data to create user
    public static final int DEFAULT_ZOOM = 15;
    public static final int DEFAULT_SEARCH_RADIUS = 1000;
    public static final boolean DEFAULT_NOTIFICATION = false;

    private CommunicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(savedInstanceState == null) {
            this.configureAndShowMainFragment();
        }
        else {
            fragmentMapView = (mapViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_view);
        }

        this.updateUIWhenCreating();
        this.configureNavigationView();
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureBottomView();
        this.retrieveCurrentUser();

    }
    // ------------
    // FRAGMENTS
    // ------------

    // Generic method that will replace and show a fragment inside the MainActivity Frame Layout
    private void startTransactionFragment(android.support.v4.app.Fragment fragment){
        if (!fragment.isVisible()){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_view, fragment).commit();
        }
    }

    // create each fragment page and show it

    private void showMapViewFragment(){
        if (this.fragmentMapView == null)this.fragmentMapView = mapViewFragment.newInstance();
        startTransactionFragment(fragmentMapView );
    }

    private void showListViewFragment(){
        if (this.fragmentListView == null)this.fragmentListView = listViewFragment.newInstance();
        startTransactionFragment( fragmentListView );
    }

    private void showWorkmatesFragment(){
        if (this.fragmentWorkmates == null)this.fragmentWorkmates = workmatesFragment.newInstance();
        startTransactionFragment( fragmentWorkmates );
    }


    // ---------------------
    // ACTIVITY
    // ---------------------

    private void showActivity(int activityIdentifier){
        switch (activityIdentifier){
            case ACTIVITY_SETTINGS:
                launchActivity(SettingActivity.class,null);
                break;
            case ACTIVITY_CHAT:
                launchActivity(MessageActivity.class,null);
                break;
            case ACTIVITY_DETAIL:
                RestaurantsHelper.getBooking( Objects.requireNonNull( getCurrentUser() ).getUid(),getTodayDate()).addOnCompleteListener( bookingTask -> {
                    if (bookingTask.isSuccessful()){
                        if (Objects.requireNonNull( bookingTask.getResult() ).isEmpty()){
                            Toast.makeText(this, getResources().getString(R.string.drawer_no_restaurant_booked), Toast.LENGTH_SHORT).show();
                        }else{
                            Map<String,Object> extra = new HashMap<>();
                            for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                                extra.put("PlaceDetailResult", Objects.requireNonNull( booking.getData().get( "restaurantId" ) ) );
                            }
                            launchActivity( DetailActivity.class,extra);
                        }

                    }
                });
                break;
            case ACTIVITY_LOGIN:
                launchActivity( LoginActivity.class,null);
                break;
        }
    }

    private void launchActivity(Class mClass, Map<String,Object> info){
        Intent intent = new Intent(this, mClass);
        if (info != null){
            for (Object key : info.keySet()) {
                String mKey = (String)key;
                String value = (String) info.get(key);
                intent.putExtra(mKey, value);
            }
        }
        startActivity(intent);
    }

    // ---------------------
    // ACTIONS
    // ---------------------

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Handle Navigation Item Click
        int id = item.getItemId();

        switch (id){
            case R.id.activity_main_drawer_dining :
                showActivity(ACTIVITY_DETAIL);
                break;
            case R.id.activity_main_drawer_settings:
                showActivity(ACTIVITY_SETTINGS);
                break;
            case R.id.activity_main_drawer_logout:
                this.signOutUserFromFirebase();
                break;
            default:
                break;
        }

        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------

    // show main fragment
    private void configureAndShowMainFragment(){
        if (fragmentMapView == null) {
            fragmentMapView = new mapViewFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_view, fragmentMapView)
                    .commit();
        }
    }

    // Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Configure NavigationView
    private void configureNavigationView(){
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    // Configure Toolbar
    private void configureToolBar(){
        setSupportActionBar(toolbar);
        Objects.requireNonNull( getSupportActionBar() ).setTitle(TITLE_HUNGRY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        // Handle back click to close menu
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> updateMainFragment( item.getItemId() ));
    }

    private void retrieveCurrentUser(){
        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);
        this.mViewModel.updateCurrentUserUID( Objects.requireNonNull( getCurrentUser() ).getUid());
        UserHelper.getUsersCollection().document(getCurrentUser().getUid()).addSnapshotListener( (documentSnapshot, e) -> {
            if (e != null) {
                Log.e("MAIN_ACTIVITY", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.e("MAIN_ACTIVITY", "Current data: " + documentSnapshot.getData());
                mViewModel.updateCurrentUserZoom(Integer.parseInt( Objects.requireNonNull( Objects.requireNonNull( documentSnapshot.getData() ).get( "defaultZoom" ) ).toString()));
                mViewModel.updateCurrentUserRadius(Integer.parseInt( Objects.requireNonNull( documentSnapshot.getData().get( "searchRadius" ) ).toString()));
            } else {
                Log.e("MAIN_ACTIVITY", "Current data: null");
            }
        } );
    }

    // --------------------
    // UI
    // --------------------

    // 1 - Update UI when activity is creating
    private void updateUIWhenCreating(){

        if (this.getCurrentUser() != null){
            View headerContainer = mNavigationView.getHeaderView(0); // This returns the container layout in nav_drawer_header.xml (e.g., your RelativeLayout or LinearLayout)
            ImageView mImageView = headerContainer.findViewById(R.id.nav_header_profile_img);
            TextView mNameText = headerContainer.findViewById(R.id.nav_header_username);
            TextView mEmailText = headerContainer.findViewById(R.id.nav_header_user_email);


            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mImageView);
            }

            //Get email from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String name = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            mEmailText.setText(email);
            mNameText.setText(name);
        }
    }

    // Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(){
        return aVoid -> {
            switch (MainActivity.SIGN_OUT_TASK){
                case SIGN_OUT_TASK:
                    finish();
                    showActivity(ACTIVITY_LOGIN);
                    break;
                default:
                    break;
            }
        };
    }

    // --------------------
    // REST REQUEST
    // --------------------

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted());
    }

    // ----------
    // UI
    // ----------

    private Boolean updateMainFragment( Integer integer){
        switch (integer){
            case R.id.action_map :
                this.showMapViewFragment();
                break;
            case R.id.action_list_view :
                this.showListViewFragment();
                break;
            case R.id.action_workmates :
                this.showWorkmatesFragment();
                break;
            case R.id.chat:
                this.showActivity(ACTIVITY_CHAT);
                break;
            default :
                break;
        }
        return true;
    }

}

