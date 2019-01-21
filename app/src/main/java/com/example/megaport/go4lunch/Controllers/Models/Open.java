package com.example.megaport.go4lunch.Controllers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Open {
    @Expose
    public Integer day;
    @SerializedName("time")
    @Expose
    public String time;

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}