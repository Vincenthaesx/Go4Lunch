package com.example.megaport.go4lunch.main.View;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Api.RestaurantsHelper;
import com.example.megaport.go4lunch.main.Models.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.mates_main_picture) ImageView mImageView;
    @BindView(R.id.mates_textview_username) TextView mTextView;

    WorkmatesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    void updateWithData(User results){
        RequestManager glide = Glide.with(itemView);
        if (!(results.getUrlPicture() == null)){
            glide.load(results.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(mImageView);
        }else{
            glide.load(R.drawable.ic_no_image_available).apply(RequestOptions.circleCropTransform()).into(mImageView);
        }

        RestaurantsHelper.getBooking(results.getUid(), getTodayDate()).addOnCompleteListener( restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                if (Objects.requireNonNull( restaurantTask.getResult() ).size() == 1){ // User already booked a restaurant today
                    for (QueryDocumentSnapshot restaurant : restaurantTask.getResult()) {
                        this.mTextView.setText(itemView.getResources().getString(R.string.mates_is_eating_at, results.getUsername(), restaurant.getData().get("restaurantName")));
                        this.changeTextColor(R.color.colorBlack);
                    }
                }else{ // No restaurant booked for this user today
                    this.mTextView.setText(itemView.getResources().getString(R.string.mates_hasnt_decided, results.getUsername()));
                    this.changeTextColor(R.color.colorGray);
                }
            }
        });
    }

    private void changeTextColor(int color){
        int mColor = itemView.getContext().getResources().getColor(color);
        this.mTextView.setTextColor(mColor);
    }

    private String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }
}
