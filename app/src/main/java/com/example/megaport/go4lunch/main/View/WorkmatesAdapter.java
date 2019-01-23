package com.example.megaport.go4lunch.main.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Models.User;
import java.util.List;

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder>{

    // FOR DATA
    private List<User> mResults;

    // CONSTRUCTOR
    public WorkmatesAdapter(List<User> result) {
        this.mResults = result;
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate( R.layout.workmates_fragment_item, parent,false);
        return new WorkmatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmatesViewHolder viewHolder, int position) {
        viewHolder.updateWithData(this.mResults.get(position));
    }

    public User getMates(int position){
        return this.mResults.get(position);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mResults != null) itemCount = mResults.size();
        return itemCount;
    }
}
