package com.example.megaport.go4lunch.Controllers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressComponent {
    @SerializedName("long_name")
    @Expose
    public String longName;
    @SerializedName("short_name")
    @Expose
    public String shortName;
    @SerializedName("types")
    @Expose
    public List<String> types = null;

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
