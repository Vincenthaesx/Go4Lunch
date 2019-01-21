package com.example.megaport.go4lunch.Controllers.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.megaport.go4lunch.Controllers.Models.PlaceDetails;
import com.example.megaport.go4lunch.Controllers.Utils.LunchStreams;
import com.example.megaport.go4lunch.Controllers.View.ListAdapter;
import com.example.megaport.go4lunch.Controllers.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.R;
import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.support.v4.content.ContextCompat.getDrawable;

public class listViewFragment extends BaseFragment {

    @BindView(R.id.list_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.list_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private Disposable disposable;
    private List<PlaceDetails> mResults;
    private ListAdapter adapter;

    private CommunicationViewModel mViewModel;

    public static listViewFragment newInstance(){
        return (new listViewFragment());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        mViewModel = ViewModelProviders.of(getActivity()).get(CommunicationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.list_view_fragment, container, false );
        ButterKnife.bind( this, view );

        setHasOptionsMenu( true );

        mViewModel.currentUserPosition.observe( this, latLng -> {
            executeHttpRequestWithRetrofit();
            configureRecyclerView();
        } );

        this.configureOnSwipeRefresh();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.currentUserPosition.removeObservers( this );
    }


    // ----------------
    // CONFIGURATION
    // ----------------

    private void configureRecyclerView(){
        this.mResults = new ArrayList<>();
        this.adapter = new ListAdapter(this.mResults, mViewModel.getCurrentUserPositionFormatted());
        this.mRecyclerView.setAdapter(this.adapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureOnSwipeRefresh(){
        mSwipeRefreshLayout.setOnRefreshListener(this::executeHttpRequestWithRetrofit);
    }

    // ----------
    // ACTION
    // ----------


    // ---------------
    // HTTP RX JAVA
    // ---------------

    private void executeHttpRequestWithRetrofit(){
        mSwipeRefreshLayout.setRefreshing(true);
        this.disposable = LunchStreams.streamFetchPlaceInfo(mViewModel.getCurrentUserPositionFormatted(), mViewModel.getCurrentUserRadius(), mapViewFragment.SEARCH_TYPE,mapViewFragment.API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver<T> createObserver(){
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof ArrayList){
                    updateUI((ArrayList) t);
                }
            }
            @Override
            public void onError(Throwable e) {
                getActivity().runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(false));
                handleError(e);}
            @Override
            public void onComplete() { }
        };
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(List<PlaceDetails> results){
        mSwipeRefreshLayout.setRefreshing(false);
        mResults.clear();
        if (results.size() > 0){
            mResults.addAll(results);
        }else{
            Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }
}
