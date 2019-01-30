package com.example.megaport.go4lunch.main.Utils;

import android.text.InputFilter;
import android.text.Spanned;

public class Filters implements InputFilter {
    private int min;
    private final int max;

    public Filters(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public Filters(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            if(end==1)
                min=Integer.parseInt(source.toString());
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException ignored) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
