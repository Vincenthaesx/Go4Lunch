package com.example.megaport.go4lunch.Controllers.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.megaport.go4lunch.Controllers.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.EasyPermissions;

public class mapViewFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    @BindView( R.id.mapView ) MapView mMapView;

    private static final int PERMS_FINE_COARSE_LOCATION = 100;
    private static final String TAG = mapViewFragment.class.getSimpleName();
    private static final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Disposable disposable;

    private CommunicationViewModel mViewModel;

    public static mapViewFragment newInstance() {
        return (new mapViewFragment());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        mViewModel = ViewModelProviders.of( Objects.requireNonNull( getActivity() ) ).get(CommunicationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate( R.layout.map_view_fragment, container, false );
        ButterKnife.bind( this, rootView );

        setHasOptionsMenu( true );
        mViewModel.currentUserPosition.observe( this, latLng -> {

        } );


        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        this.configureMapView();
        this.configureGoogleApiClient();
        this.configureLocationRequest();
        this.configureLocationCallBack();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu( menu, inflater );
        menu.clear();
        inflater.inflate( R.menu.menu_main, menu );
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            stopLocationUpdates();
            mGoogleApiClient.stopAutoManage( Objects.requireNonNull( getActivity() ) );
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.currentUserPosition.removeObservers( this );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        this.disposeWhenDestroy();
    }

    // ----------------

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (EasyPermissions.hasPermissions( Objects.requireNonNull( getContext() ), perms)) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener( Objects.requireNonNull( getActivity() ), location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            handleNewLocation(location);
                        } else {
                            if (EasyPermissions.hasPermissions(getContext(), perms)) {
                                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                            }

                        }
                    } );
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_perm_access),
                    PERMS_FINE_COARSE_LOCATION, perms);
        }
    }

    private void handleNewLocation(Location location) {
        Log.e(TAG, "handleNewLocation: " );
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        this.mViewModel.updateCurrentUserPosition(new LatLng(currentLatitude, currentLongitude));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(this.mViewModel.getCurrentUserPosition()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.mViewModel.getCurrentUserPosition(), 15));
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    // ---------------
    // CONFIGURATION
    // ---------------

    private void configureMapView() {
        try {
            MapsInitializer.initialize( Objects.requireNonNull( getActivity() ).getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync( mMap -> {
            googleMap = mMap;
            if (checkLocationPermission()) {
                //Request location updates:
                googleMap.setMyLocationEnabled(true);
            }
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).
                    getParent()).findViewById(Integer.parseInt("2"));

            // and next place it, for example, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 70, 70);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
        } );
    }

    private void configureGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient
                .Builder( Objects.requireNonNull( getContext() ) )
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .enableAutoManage( Objects.requireNonNull( getActivity() ), this)
                .build();
    }

    public boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions( Objects.requireNonNull( getContext() ), perms );
    }

    private void configureLocationCallBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    handleNewLocation(location);
                }
            }
        };
    }

    private void configureLocationRequest(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( Objects.requireNonNull( getActivity() ) );
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(100 * 1000)        // 100 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {googleMap.setMyLocationEnabled(true);}
    }


    // -----------------
    // ACTION
    // -----------------



    // ----------------

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

}
