package com.example.megaport.go4lunch.main.Controllers.activities;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.Utils.Filters;
import com.example.megaport.go4lunch.main.Utils.NotificationHelper;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.simple_toolbar) Toolbar mToolbar;
    @BindView(R.id.settings_switch) Switch mSwitch;
    @BindView( R.id.settings_save ) Button mButtonSave;
    @BindView(R.id.settings_radius_edit_text) TextInputEditText mRadiusEditText;
    @BindView(R.id.settings_zoom_edit_text) TextInputEditText mZoomEditText;
    @BindView(R.id.settings_zoom_edit_layout) TextInputLayout mZoomEditLayout;
    @BindView(R.id.settings_radius_edit_layout) TextInputLayout mRadiusEditLayout;

    @BindView( R.id.settings_profile_img ) ImageView mImageViewProfile;
    @BindView( R.id.settings_username ) TextView mTextViewUsername;
    @BindView( R.id.setting_user_email ) TextView mTextViewEmail;

    private static final String ZOOM_MIN_VALUE = "6";
    private static final String ZOOM_MAX_VALUE = "18";
    private static final String RADIUS_MIN_VALUE = "150";
    private static final String RADIUS_MAX_VALUE = "10000";

    private CommunicationViewModel mViewModel;
    private NotificationHelper mNotificationHelper;

    private static final int DELETE_USER_TASK = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_setting );
        ButterKnife.bind(this);


        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        this.updateUIWhenCreating();
        this.configureToolbar();
        this.retrieveUserSettings();
        this.setListenerAndFilters();
        this.createNotificationHelper();
    }

    // -------------
    // CONFIGURATION
    // -------------

    private void configureToolbar(){
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void retrieveUserSettings(){
        UserHelper.getUsersCollection().document( Objects.requireNonNull( getCurrentUser() ).getUid()).addSnapshotListener( (documentSnapshot, e) -> {
            if (e != null) {
                Log.e("TAG", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.e("TAG", "Current data: " + documentSnapshot.getData());

                mZoomEditText.setText( Objects.requireNonNull( Objects.requireNonNull( documentSnapshot.getData() ).get( "defaultZoom" ) ).toString());
                mRadiusEditText.setText( Objects.requireNonNull( documentSnapshot.getData().get( "searchRadius" ) ).toString());

                if (Objects.requireNonNull( documentSnapshot.getData().get( "notificationOn" ) ).equals(true)){
                    mSwitch.setChecked(true);
                    mNotificationHelper.scheduleRepeatingNotification();
                }else{
                    mSwitch.setChecked(false);
                    mNotificationHelper.cancelAlarmRTC();
                }

                mViewModel.updateCurrentUserZoom(Integer.parseInt( Objects.requireNonNull( documentSnapshot.getData().get( "defaultZoom" ) ).toString()));
                mViewModel.updateCurrentUserRadius(Integer.parseInt( Objects.requireNonNull( documentSnapshot.getData().get( "searchRadius" ) ).toString()));
            } else {
                Log.e("TAG", "Current data: null");
            }
        } );
    }

    private void createNotificationHelper(){
        mNotificationHelper = new NotificationHelper(getBaseContext());
    }

    // 1 - Update UI when activity is creating
    private void updateUIWhenCreating(){

        if (this.getCurrentUser() != null){

            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply( RequestOptions.circleCropTransform())
                        .into(mImageViewProfile);
            }

            //Get email from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String name = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            mTextViewEmail.setText(email);
            mTextViewUsername.setText(name);
        }
    }



    private void setListenerAndFilters(){
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> { });
        mZoomEditText.setFilters(new InputFilter[]{new Filters(ZOOM_MIN_VALUE,ZOOM_MAX_VALUE)});
        mRadiusEditText.setFilters(new InputFilter[]{new Filters(RADIUS_MIN_VALUE,RADIUS_MAX_VALUE)});
    }

    // ------------
    // ACTIONS
    // ------------


    @OnClick (R.id.settings_save)
    public void saveSettings(){
        boolean error = false;
        int zoom = MainActivity.DEFAULT_ZOOM;
        int radius = MainActivity.DEFAULT_SEARCH_RADIUS;
        if (!(Objects.requireNonNull( mZoomEditText.getText() ).toString().equals(""))){
            zoom = Integer.parseInt(mZoomEditText.getText().toString());
            if (zoom < Integer.parseInt(ZOOM_MIN_VALUE) || zoom > Integer.parseInt(ZOOM_MAX_VALUE)){
                mZoomEditLayout.setError(getResources().getString(R.string.settings_save_error_zoom,ZOOM_MIN_VALUE,ZOOM_MAX_VALUE));
                error = true;
            }else{
                mZoomEditLayout.setError(null);
            }
        }else{
            mZoomEditLayout.setError(getResources().getString(R.string.settings_save_error_zoom,ZOOM_MIN_VALUE,ZOOM_MAX_VALUE));
            error = true;
        }

        if (!(Objects.requireNonNull( mRadiusEditText.getText() ).toString().equals(""))){
            radius = Integer.parseInt(mRadiusEditText.getText().toString());
            if (radius < Integer.parseInt(RADIUS_MIN_VALUE) || radius > Integer.parseInt(RADIUS_MAX_VALUE)){
                mRadiusEditLayout.setError(getResources().getString(R.string.settings_save_error_radius,RADIUS_MIN_VALUE,RADIUS_MAX_VALUE));
                error = true;
            }else{
                mRadiusEditLayout.setError(null);
            }
        }else{
            mRadiusEditLayout.setError(getResources().getString(R.string.settings_save_error_radius,RADIUS_MIN_VALUE,RADIUS_MAX_VALUE));
            error = true;
        }

        if (mSwitch.isChecked()){
            mNotificationHelper.scheduleRepeatingNotification();
        }else{
            mNotificationHelper.cancelAlarmRTC();
        }


        if (!(error)){
            UserHelper.updateUserSettings( Objects.requireNonNull( getCurrentUser() ).getUid(),zoom,mSwitch.isChecked(),radius).addOnSuccessListener(
                    updateTask ->{
                        Log.e("SETTINGS_ACTIVITY", "saveSettings: DONE" );
                        Toast.makeText(this, getResources().getString(R.string.settings_save_ok), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton(){
        new AlertDialog.Builder( this )
                .setMessage( R.string.popup_message_confirmation_delete_account )
                .setPositiveButton( R.string.popup_message_choice_yes, (dialog, which) -> deleteUserFromFirebase() )
                .setNegativeButton( R.string.popup_message_choice_no, null )
                .show();
    }

    private void deleteUserFromFirebase(){
        if(this.getCurrentUser() != null) {
            UserHelper.deleteUser( this.getCurrentUser().getUid() ).addOnFailureListener( this.onFailureListener() );

            AuthUI.getInstance()
                    .delete( this )
                    .addOnSuccessListener( this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case DELETE_USER_TASK:
                    finish();
                    this.returnLoginActivity();
                    break;
                default:
                    break;
            }
        };
    }

    private void returnLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }



}
