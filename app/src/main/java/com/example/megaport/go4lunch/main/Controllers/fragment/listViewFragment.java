package com.example.megaport.go4lunch.main.Controllers.fragment;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import com.example.megaport.go4lunch.main.Controllers.activities.DetailActivity;
import com.example.megaport.go4lunch.main.Controllers.activities.MainActivity;
import com.example.megaport.go4lunch.main.Models.PlaceDetails;
import com.example.megaport.go4lunch.main.Utils.ItemClickSupport;
import com.example.megaport.go4lunch.main.Utils.LunchStreams;
import com.example.megaport.go4lunch.main.View.ListAdapter;
import com.example.megaport.go4lunch.main.View.ViewModels.CommunicationViewModel;
import com.example.megaport.go4lunch.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

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
        mViewModel = ViewModelProviders.of( Objects.requireNonNull( getActivity() ) ).get(CommunicationViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.list_view_fragment, container, false );
        ButterKnife.bind( this, view );

        setHasOptionsMenu( true );

        mViewModel.currentUserPosition.observe( this, latLng -> {
            executeHttpRequestWithRetrofit();
            configureRecyclerView();
        } );

        this.configureOnClickRecyclerView();
        this.configureOnSwipeRefresh();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getContext().getSystemService( Context.SEARCH_SERVICE);

        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);
        searchView.setQueryHint(getResources().getString(R.string.toolbar_search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(((MainActivity) getContext()).getComponentName()));

        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2 ){
                    disposable = LunchStreams.streamFetchAutoCompleteInfo(query,mViewModel.getCurrentUserPositionFormatted(),mViewModel.getCurrentUserRadius(),mapViewFragment.API_KEY).subscribeWith(createObserver());
                }else{
                    Toast.makeText(getContext(), getResources().getString(R.string.search_too_short), Toast.LENGTH_LONG).show();
                }
                return true;

            }
            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 2){
                    disposable = LunchStreams.streamFetchAutoCompleteInfo(query,mViewModel.getCurrentUserPositionFormatted(),mViewModel.getCurrentUserRadius(),mapViewFragment.API_KEY).subscribeWith(createObserver());
                }
                return false;
            }
        });

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

    // Configure item click on RecyclerView
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.list_view_fragment_item)
                .setOnItemClickListener((recyclerView, position, v) -> {

                    PlaceDetails result = adapter.getRestaurant(position);
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("PlaceDetailResult", result.getPlaceId());
                    startActivity(intent);
                });
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
                Objects.requireNonNull( getActivity() ).runOnUiThread(() -> mSwipeRefreshLayout.setRefreshing(false));
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
