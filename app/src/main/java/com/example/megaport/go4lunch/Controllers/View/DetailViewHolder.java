package com.example.megaport.go4lunch.Controllers.View;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.megaport.go4lunch.Controllers.Models.User;
import com.example.megaport.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;


class DetailViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.detail_main_picture) ImageView mImageView;
    @BindView(R.id.detail_textview_username) TextView mTextView;

    DetailViewHolder(View itemView) {
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

        this.mTextView.setText(itemView.getResources().getString(R.string.restaurant_detail_recyclerview, results.getUsername()));
        this.changeTextColor();
    }

    private void changeTextColor(){
        int mColor = itemView.getContext().getResources().getColor( R.color.colorBlack );
        this.mTextView.setTextColor(mColor);
    }
}
