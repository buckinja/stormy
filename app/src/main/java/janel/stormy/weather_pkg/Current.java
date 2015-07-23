package janel.stormy.weather_pkg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Current {
    private String mIcon;
    private long mTime;
    private double mTemp;
    private double mHumidity;
    private double mPrecipChance;
    private String mSummary;
    private String mTimeZone;

    public String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone(mTimeZone));
        Date dateTime = new Date(getTime() * 1000);
        return formatter.format(dateTime);
    }
    /*Sets appropriate icon depending on weather result.*/
    public int getIconId() {
        return Forecast.getIconId(mIcon);
    }

    public long getTime() {
        return mTime;
    }

    public int getTemp() {
        return (int)Math.round(mTemp);
    }

    public double getHumidity() {
        return mHumidity;
    }

    public int getPrecipChance() {
        return (int)Math.round(mPrecipChance * 100);
    }

    public String getSummary() {
        return mSummary;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void setTemp(double temp) {
        mTemp = temp;
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public void setPrecipChance(double precipChance) {
        mPrecipChance = precipChance;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }
}
