package com.example.megaport.go4lunch.Controllers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlusCode {
    @SerializedName("compound_code")
    @Expose
    public String compoundCode;
    @SerializedName("global_code")
    @Expose
    public String globalCode;

    public String getCompoundCode() {
        return compoundCode;
    }

    public void setCompoundCode(String compoundCode) {
        this.compoundCode = compoundCode;
    }

    public String getGlobalCode() {
        return globalCode;
    }

    public void setGlobalCode(String globalCode) {
        this.globalCode = globalCode;
    }
}