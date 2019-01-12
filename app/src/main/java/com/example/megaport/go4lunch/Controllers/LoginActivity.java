package com.example.megaport.go4lunch.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import com.example.megaport.go4lunch.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import java.util.Arrays;
import butterknife.BindView;

public class LoginActivity extends BaseActivity {


    public static final int RC_SIGN_IN = 123;


    @BindView( R.id.login_activity_layout ) ConstraintLayout mConstraintLayout;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        if (!this.isCurrentUserLogged()){
            this.startSignInActivity();
        }else {
            this.startMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 4 - Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // -------------
    // UI
    // -------------

    private void showSnackBar(ConstraintLayout mConstraintLayout, String message){
        Snackbar.make( mConstraintLayout, message, Snackbar.LENGTH_SHORT ).show();
    }

    // -------------
    // NAVIGATION
    // -------------

        private void startSignInActivity () {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setTheme( R.style.LoginTheme )
                            .setAvailableProviders(
                                    Arrays.asList( new AuthUI.IdpConfig.FacebookBuilder().build(),   // SUPPORT FACEBOOK
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))           // SUPPORT GOOGLE
                            .setIsSmartLockEnabled( false, true )
                            .setLogo( R.drawable.go4lunch )
                            .build(),
                    RC_SIGN_IN );
        }


    // -------------
    // UTILS
    // -------------

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent( data );

        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){ // SUCCESS
                showSnackBar(this.mConstraintLayout, getString( R.string.connection_succeed ) );
                this.startMainActivity();
            } else {  // ERRORS
                if (response == null){
                    showSnackBar( this.mConstraintLayout, getString( R.string.error_authentication_canceled ) );
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR){
                    showSnackBar( this.mConstraintLayout, getString( R.string.error_unknown_error ) );
                }
            }
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent( this, MainActivity.class );
        startActivity( intent );
    }
}
