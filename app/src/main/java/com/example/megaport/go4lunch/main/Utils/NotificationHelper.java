package com.example.megaport.go4lunch.main.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;

public class NotificationHelper {

    private final Context mContext;
    private AlarmManager alarmManagerRTC;
    private PendingIntent alarmIntentRTC;

    public static final int ALARM_TYPE_RTC = 100;

    public NotificationHelper(Context context) {
        mContext = context;
    }

    public void scheduleRepeatingNotification(){
        //get calendar instance to be able to select what time notification should be scheduled
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,12);

        //Setting intent to class where Alarm broadcast message will be handled
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        //Setting alarm pending intent
        alarmIntentRTC = PendingIntent.getBroadcast(mContext, ALARM_TYPE_RTC, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting instance of AlarmManager service
        alarmManagerRTC = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        alarmManagerRTC.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntentRTC);


    }

    public void cancelAlarmRTC() {
        if (alarmManagerRTC!= null) {
            alarmManagerRTC.cancel(alarmIntentRTC);
        }
    }
}
