package com.example.megaport.go4lunch.main.Utils;

import com.example.megaport.go4lunch.main.Models.MapPlacesInfo;
import com.example.megaport.go4lunch.main.Models.PlaceDetails;
import com.example.megaport.go4lunch.main.Models.PlaceDetailsInfo;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LunchStreams {

    private static final LunchService mapPlacesInfo = LunchService.retrofit.create(LunchService.class);

    // MAP

    public static Observable<MapPlacesInfo> streamFetchNearbyPlaces(String location, int radius, String type, String key){
        return mapPlacesInfo.getNearbyPlaces(location,radius,type,key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    // LIST

    public static Observable<List<PlaceDetails>> streamFetchPlaceInfo(String location, int radius, String type, String key){
        return mapPlacesInfo.getNearbyPlaces(location,radius,type,key)
                .flatMapIterable( MapPlacesInfo::getResults)
                .flatMap(info -> mapPlacesInfo.getPlacesInfo(info.getPlaceId(), key))
                .map( PlaceDetailsInfo::getResult)
                .toList()
                .toObservable()
                .subscribeOn( Schedulers.io())
                .observeOn( AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    // DETAILS

    public static Observable<PlaceDetailsInfo> streamSimpleFetchPlaceInfo(String placeId, String key){
        return  mapPlacesInfo.getPlacesInfo(placeId, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
