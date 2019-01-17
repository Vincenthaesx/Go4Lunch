package com.example.megaport.go4lunch.Controllers.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.megaport.go4lunch.R;

public class listViewFragment extends android.support.v4.app.Fragment {

    public static listViewFragment newInstance(){
        return (new listViewFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate( R.layout.list_view_fragment, container, false );
    }
}
