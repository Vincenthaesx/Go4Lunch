package com.example.megaport.go4lunch.main.Controllers.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import java.util.Arrays;
import java.util.Objects;
import static com.example.megaport.go4lunch.main.Controllers.activities.MainActivity.DEFAULT_NOTIFICATION;
import static com.example.megaport.go4lunch.main.Controllers.activities.MainActivity.DEFAULT_SEARCH_RADIUS;
import static com.example.megaport.go4lunch.main.Controllers.activities.MainActivity.DEFAULT_ZOOM;

public class LoginActivity extends BaseActivity {

    // FOR DATA
    private static final int RC_SIGN_IN = 1000;

    private CommunicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_login);

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        if (!this.isCurrentUserLogged()){
            this.startSignInActivity();
        }else {
            this.mViewModel.updateCurrentUserUID( Objects.requireNonNull( getCurrentUser() ).getUid());
            launchMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.FacebookBuilder().build(),        // FACEBOOK
                                              new AuthUI.IdpConfig.GoogleBuilder().build()))         // GOOGLE

                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.go4lunch)
                        .build(),
                RC_SIGN_IN);
    }

    // --------------------
    // REST REQUEST
    // --------------------

    // Http request that create user in firestore
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: LOGGED" );
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            this.mViewModel.updateCurrentUserUID(uid);
            this.mViewModel.updateCurrentUserZoom(DEFAULT_ZOOM);
            this.mViewModel.updateCurrentUserRadius(DEFAULT_SEARCH_RADIUS);
            UserHelper.createUser(uid, username, urlPicture, DEFAULT_SEARCH_RADIUS, DEFAULT_ZOOM, DEFAULT_NOTIFICATION).addOnFailureListener(this.onFailureListener());
        }else{
            Log.e("LOGIN_ACTIVITY", "createUserInFirestore: NOT LOGGED" );
        }
    }

    // -------------
    // UTILS
    // -------------

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                launchMainActivity();
            } else { // ERRORS
                if (response == null) {
                    Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show();
                    startSignInActivity();
                } else if (Objects.requireNonNull( response.getError() ).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // ---------------
    // ACTION
    // ---------------

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
