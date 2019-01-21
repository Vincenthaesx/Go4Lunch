package com.example.megaport.go4lunch.Controllers.Utils;

import com.example.megaport.go4lunch.Controllers.Models.MapPlacesInfo;
import com.example.megaport.go4lunch.Controllers.Models.PlaceDetailsInfo;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LunchService {

    // MAP INFO

    @GET("https://maps.googleapis.com/maps/api/place/search/json")
    Observable<MapPlacesInfo> getNearbyPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    // PLACE INFO


    @GET("details/json")
    Observable<PlaceDetailsInfo> getPlacesInfo(@Query("placeid") String placeId, @Query("key") String key);



    // RETROFIT
    // ----------------------

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory( GsonConverterFactory.create())
            .addCallAdapterFactory( RxJava2CallAdapterFactory.create())
            .client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)).build())
            .build();
}
