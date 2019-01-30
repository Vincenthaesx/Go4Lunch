package com.example.megaport.go4lunch.main.View;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.bumptech.glide.RequestManager;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Models.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


public class ChatAdapter extends FirestoreRecyclerAdapter<Message,ChatViewHolder> {
    public interface Listener {
        void onDataChanged();
    }

    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;

    //FOR COMMUNICATION
    private final Listener callback;

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, RequestManager glide, Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Message model) {
        holder.updateWithMessage(model, this.idCurrentUser, this.glide);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                .inflate( R.layout.activity_message_item, parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
