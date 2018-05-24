package com.cybexmobile.Market;

import android.os.Parcel;
import android.os.Parcelable;

public class MarketTrade implements Parcelable {

    public String date;
    public double price;
    public double baseAmount;
    public double quoteAmount;
    public String showRed;
    public String quote;
    public String base;

    protected MarketTrade(Parcel in) {
        date = in.readString();
        price = in.readDouble();
        baseAmount = in.readDouble();
        quoteAmount = in.readDouble();
        showRed = in.readString();
        quote = in.readString();
        base = in.readString();
    }

    public static final Creator<MarketTrade> CREATOR = new Creator<MarketTrade>() {
        @Override
        public MarketTrade createFromParcel(Parcel in) {
            return new MarketTrade(in);
        }

        @Override
        public MarketTrade[] newArray(int size) {
            return new MarketTrade[size];
        }
    };

    public MarketTrade() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeDouble(price);
        dest.writeDouble(baseAmount);
        dest.writeDouble(quoteAmount);
        dest.writeString(showRed);
        dest.writeString(quote);
        dest.writeString(base);
    }
}
