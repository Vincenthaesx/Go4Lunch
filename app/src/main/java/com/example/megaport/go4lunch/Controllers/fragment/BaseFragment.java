package com.example.megaport.go4lunch.Controllers.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.megaport.go4lunch.R;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import retrofit2.HttpException;

public class BaseFragment extends Fragment {


    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return getView();
    }

    // -----------------
    // UTILS
    // -----------------

    protected void handleError(Throwable throwable) {
        getActivity().runOnUiThread(() -> {
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                int statusCode = httpException.code();
                Log.e("HttpException", "Error code : " + statusCode);
                Toast.makeText(getContext(), getResources().getString(R.string.http_error_message,statusCode), Toast.LENGTH_SHORT).show();
            } else if (throwable instanceof SocketTimeoutException) {
                Log.e("SocketTimeoutException", "Timeout from retrofit");
                Toast.makeText(getContext(), getResources().getString(R.string.timeout_error_message), Toast.LENGTH_SHORT).show();
            } else if (throwable instanceof IOException) {
                Log.e("IOException", "Error");
                Toast.makeText(getContext(), getResources().getString(R.string.exception_error_message), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Generic handleError", "Error");
                Toast.makeText(getContext(), getResources().getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
            }
        });
    }
}