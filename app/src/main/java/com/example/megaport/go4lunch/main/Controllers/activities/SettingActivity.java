package com.example.megaport.go4lunch.main.Controllers.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.Utils.Filters;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
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

    private static final String ZOOM_MIN_VALUE = "6";
    private static final String ZOOM_MAX_VALUE = "18";
    private static final String RADIUS_MIN_VALUE = "150";
    private static final String RADIUS_MAX_VALUE = "10000";

    protected CommunicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_setting );
        ButterKnife.bind(this);


        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        this.configureToolbar();
        this.retrieveUserSettings();
        this.setListenerAndFilters();
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

                mViewModel.updateCurrentUserZoom(Integer.parseInt( Objects.requireNonNull( documentSnapshot.getData().get( "defaultZoom" ) ).toString()));
                mViewModel.updateCurrentUserRadius(Integer.parseInt( Objects.requireNonNull( documentSnapshot.getData().get( "searchRadius" ) ).toString()));
            } else {
                Log.e("TAG", "Current data: null");
            }
        } );
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


        if (!(error)){
            UserHelper.updateUserSettings( Objects.requireNonNull( getCurrentUser() ).getUid(),zoom,mSwitch.isChecked(),radius).addOnSuccessListener(
                    updateTask ->{
                        Log.e("SETTINGS_ACTIVITY", "saveSettings: DONE" );
                        Toast.makeText(this, getResources().getString(R.string.settings_save_ok), Toast.LENGTH_SHORT).show();
                    });
        }
    }



}
