package com.example.megaport.go4lunch.Controllers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.megaport.go4lunch.Controllers.fragment.listViewFragment;
import com.example.megaport.go4lunch.Controllers.fragment.mapViewFragment;
import com.example.megaport.go4lunch.Controllers.fragment.workmatesFragment;
import com.example.megaport.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // FOR DESIGN
    private DrawerLayout drawerLayout;
    private android.support.v7.widget.Toolbar toolbar;
    @BindView( R.id.bottom_navigation ) BottomNavigationView mBottomNavigationView;

    // FOR DATA
    private static final int SIGN_OUT_TASK = 10;

    // FOR FRAGMENTS
    private listViewFragment fragmentListView;
    private mapViewFragment fragmentMapView;
    private workmatesFragment fragmentWorkmates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        if(savedInstanceState == null) {
            this.configureAndShowMainFragment();
        }
        else {
            fragmentMapView = (mapViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_view);
        }

        this.configureToolbar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureBottomView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu );

        final MenuItem searchItem = menu.findItem( R.id.menu_search );
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView( searchItem );

        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // CODE
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        } );

        return true;
    }

    // -------------
    // ACTIONS
    // -------------


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.activity_main_drawer_dining :
                break;
            case R.id.activity_main_drawer_settings :
                break;
            case R.id.activity_main_drawer_logout :
                this.signOutUserFromFirebase();
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer( GravityCompat.START );

        return true;
    }

    private void startLoginActivity(){
        Intent intent = new Intent( this, LoginActivity.class );
        startActivity( intent );
    }


    // ---------
    @Override
    public void onBackPressed() {
        // 5 - Handle back click to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // -------------------
    // CONFIGURATION
    // -------------------

    private void configureAndShowMainFragment(){
        if (fragmentMapView == null) {
            fragmentMapView = new mapViewFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_view, fragmentMapView)
                    .commit();
        }
    }

    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> updateMainFragment( item.getItemId() ));
    }
    
    private void configureToolbar(){
        this.toolbar = findViewById( R.id.simple_toolbar );
        setSupportActionBar( toolbar );
    }

    private void configureDrawerLayout(){
        this.drawerLayout = findViewById( R.id.activity_main_drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawerLayout, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawerLayout.addDrawerListener( toggle );
        toggle.syncState();

    }

    private void configureNavigationView(){
        NavigationView navigationView = findViewById( R.id.activity_main_nav_view );
        navigationView.setNavigationItemSelectedListener(this);

        String userName = getCurrentUser().getDisplayName();
        String userEmail = getCurrentUser().getEmail();
        Uri userPhotoUrl = getCurrentUser().getPhotoUrl();

        View hView =  navigationView.getHeaderView(0);
        ImageView nav_picture = hView.findViewById(R.id.nav_header_profile_img);
        TextView nav_user = hView.findViewById(R.id.nav_header_username);
        TextView nav_email = hView.findViewById(R.id.nav_header_user_email);

        Glide.with(getApplicationContext())
                .load(userPhotoUrl)
                .apply( RequestOptions.circleCropTransform() )
                .into(nav_picture);
        nav_user.setText(userName);
        nav_email.setText(userEmail);
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


    // ------------
    // UTILS
    // ------------

    protected FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected Boolean isCurrentUserLogged(){
        return (this.getCurrentUser() != null);
    }

    // ---------------
    // REST REQUESTS
    // ---------------

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut( this )
                .addOnSuccessListener( this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK) );
    }


    // ------------------
    // ERROR HANDLER
    // ------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString( R.string.error_unknown_error ), Toast.LENGTH_LONG).show();
            }
        };
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                        startLoginActivity();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    // --------
    // UI
    // --------

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
            default :
                break;
        }
        return true;
    }




}
