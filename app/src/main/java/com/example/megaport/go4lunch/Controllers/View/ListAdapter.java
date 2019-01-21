package com.example.megaport.go4lunch.Controllers.View;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.megaport.go4lunch.Controllers.Models.PlaceDetails;
import com.example.megaport.go4lunch.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    // FOR DATA
    private List<PlaceDetails> mResults;
    private String mLocation;

    // CONSTRUCTOR
    public ListAdapter(List<PlaceDetails> result, String location) {
        this.mResults = result;
        this.mLocation = location;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate( R.layout.list_view_fragment_item, parent,false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position), this.mLocation);
    }

    public PlaceDetails getRestaurant(int position){
        return this.mResults.get(position);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}