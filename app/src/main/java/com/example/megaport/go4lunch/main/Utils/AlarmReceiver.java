package com.example.megaport.go4lunch.main.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.example.megaport.go4lunch.R;
import com.example.megaport.go4lunch.main.Api.RestaurantsHelper;
import com.example.megaport.go4lunch.main.Api.UserHelper;
import com.example.megaport.go4lunch.main.Controllers.activities.MainActivity;
import com.example.megaport.go4lunch.main.Controllers.fragment.mapViewFragment;
import com.example.megaport.go4lunch.main.Models.PlaceDetailsInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_CHANNEL_ID = "5000";
    public static final String NOTIFICATION_CHANNEL_NAME = "Go4Lunch";

    private NotificationCompat.Builder mBuilder;
    private List<String> usersList;
    private String mRestaurantName;
    private String mRestaurantAddress;
    private Context mContext;
    private Disposable mDisposable;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        usersList = new ArrayList<>();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            RestaurantsHelper.getBooking(FirebaseAuth.getInstance().getCurrentUser().getUid(),getTodayDate()).addOnCompleteListener( restaurantTask -> {
                if (restaurantTask.isSuccessful()){
                    if (!(Objects.requireNonNull( restaurantTask.getResult() ).isEmpty())){ // User did a booking for today
                        Log.e("TAG", "onReceive: Sending notifications" );
                        for (DocumentSnapshot restaurant : restaurantTask.getResult()){
                            RestaurantsHelper.getTodayBooking( Objects.requireNonNull( Objects.requireNonNull( restaurant.getData() ).get( "restaurantId" ) ).toString(), getTodayDate()).addOnCompleteListener( bookingTask -> {
                                if (bookingTask.isSuccessful()){
                                    for (QueryDocumentSnapshot booking : Objects.requireNonNull( bookingTask.getResult() )){
                                        UserHelper.getUser( Objects.requireNonNull( booking.getData().get( "userId" ) ).toString()).addOnCompleteListener( userTask -> {
                                            if (userTask.isSuccessful()){
                                                if (!(Objects.requireNonNull( Objects.requireNonNull( Objects.requireNonNull( userTask.getResult() ).getData() ).get( "uid" ) ).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))){
                                                    Log.e("TAG", "ALARM_RECEIVER | User : " + Objects.requireNonNull( userTask.getResult().getData() ).get("username") );
                                                    String username = Objects.requireNonNull( userTask.getResult().getData().get( "username" ) ).toString();
                                                    usersList.add(username);
                                                }
                                            }
                                            if (usersList.size() == bookingTask.getResult().size() - 1){
                                                this.executeHttpRequestWithRetrofit( Objects.requireNonNull( restaurant.getData().get( "restaurantId" ) ).toString());
                                            }
                                        });
                                    }
                                    Log.e("TAG", "onReceive: " + usersList.toString() );

                                }
                            });
                        }
                    }else{
                        Log.e("TAG", "onReceive: No booking for this user today" );
                    }
                }
            });
        }
    }

    private void executeHttpRequestWithRetrofit(String placeId){
        this.mDisposable = LunchStreams.streamSimpleFetchPlaceInfo(placeId, mapViewFragment.API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver<T> createObserver() {
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof PlaceDetailsInfo) {
                    mRestaurantName = ((PlaceDetailsInfo) t).getResult().getName();
                    mRestaurantAddress = ((PlaceDetailsInfo) t).getResult().getVicinity();
                    if (usersList.size() > 0){
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < usersList.size(); i++){
                            sb.append(usersList.get(i));
                            if (!(i == usersList.size() - 1)){
                                sb.append(", ");
                            }
                        }
                        sendNotification(mContext.getResources().getString(
                                R.string.notification_message_big,
                                mRestaurantName,
                                mRestaurantAddress,
                                sb));
                    }else{
                        sendNotification(mContext.getResources().getString(
                                R.string.notification_message_big,
                                mRestaurantName,
                                mRestaurantAddress,
                                mContext.getResources().getString(R.string.notification_message_no_workmates)));
                    }
                } else {
                    Log.e("TAG", "onNext: " + t.getClass());
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                disposeWhenComplete();
            }
        };
    }


    // Create and push notification

    public void sendNotification(String users)
    {
        Log.e("TAG", "sendNotification: USERS " + users );
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext , MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NotificationHelper.ALARM_TYPE_RTC, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        Notification repeatedNotification = buildLocalNotification(mContext, pendingIntent, users).build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService( Context.NOTIFICATION_SERVICE );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            Objects.requireNonNull( notificationManager ).createNotificationChannel(notificationChannel);
        }
        Objects.requireNonNull( notificationManager ).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
    }

    public NotificationCompat.Builder buildLocalNotification(Context mContext, PendingIntent pendingIntent, String users) {
        Log.e("TAG", "buildLocalNotification: USERS " + users );
        mBuilder = new NotificationCompat.Builder(mContext,NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(mContext.getResources().getString(R.string.notification_title))
                .setContentText(mContext.getResources().getString(R.string.notification_message))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(users))
                .setAutoCancel(true);

        return mBuilder;
    }

    protected String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }

    private void disposeWhenComplete(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }
}
