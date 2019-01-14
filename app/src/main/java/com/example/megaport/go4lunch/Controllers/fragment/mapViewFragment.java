package com.example.megaport.go4lunch.Controllers.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.megaport.go4lunch.R;

public class mapViewFragment extends android.support.v4.app.Fragment {

    public static mapViewFragment newInstance(){
        return (new mapViewFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate( R.layout.map_view_fragment, container, false );
    }

}
