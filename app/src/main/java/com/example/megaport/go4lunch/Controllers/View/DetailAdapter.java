package com.example.megaport.go4lunch.Controllers.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.megaport.go4lunch.Controllers.Models.User;
import com.example.megaport.go4lunch.R;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {
    // FOR DATA
    private final List<User> mResults;

    // CONSTRUCTOR
    public DetailAdapter(List<User> result) {
        this.mResults = result;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate( R.layout.detail_activity_item, parent,false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
