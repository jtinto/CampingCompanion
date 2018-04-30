package com.concepttech.campingcompanionbluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import com.concepttech.campingcompanionbluetooth.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jtint on 6/13/2017.
 */
@IgnoreExtraProperties
 class TimeStamp implements Parcelable{
    private int day;
    private int month;
    private int year;
    private long Milliseconds;
    private boolean isZero;
    public TimeStamp() {
        final Calendar calendar=Calendar.getInstance();
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH)+1;
        this.year = calendar.get(Calendar.YEAR);
        this.Milliseconds = (calendar.get(Calendar.HOUR_OF_DAY)*60*60*1000) + (calendar.get(Calendar.MINUTE)*60*1000)+ (calendar.get(Calendar.SECOND)*1000) + (calendar.get(Calendar.MILLISECOND));
    }
    TimeStamp(int day,int month,int year,int Milliseconds){
        this.day = day;
        this.Milliseconds = Milliseconds;
        this.month = month;
        this.year = year;
        checkSelf();
    }
    void checkSelf(){
        isZero = this.day == 0 && this.month == 0 && this.year == 0 && this.Milliseconds == 0;
    }
    boolean isZero(){return isZero;}
    void buildFromSnapshot(DataSnapshot snapshot){
        if(snapshot != null && snapshot.hasChildren()){
            for (DataSnapshot child : snapshot.getChildren()
                 ) {
                switch (child.getKey()){
                    case "day":
                        if(child.getValue() != null && Constants.isNumber(child.getValue().toString())){
                            day =Integer.valueOf(child.getValue().toString());
                        }
                        break;
                    case "month":
                        if(child.getValue() != null && Constants.isNumber(child.getValue().toString())){
                            month =Integer.valueOf(child.getValue().toString());
                        }
                        break;
                    case "year":
                        if(child.getValue() != null && Constants.isNumber(child.getValue().toString())){
                            year =Integer.valueOf(child.getValue().toString());
                        }
                        break;
                    case "Milliseconds":
                        if(child.getValue() != null && Constants.isNumber(child.getValue().toString())){
                            Milliseconds =Integer.valueOf(child.getValue().toString());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
    String get_time_string(){
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(Milliseconds);
        return month + "/" + day + "/" + year + " " + time.get(Calendar.HOUR_OF_DAY)+ ":" + time.get(Calendar.MINUTE)+ time.get(Calendar.AM_PM);
    }
    Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("day", day);
        result.put("month", month);
        result.put("year", year);
        result.put("Milliseconds", Milliseconds);
        return result;
    }
    boolean isOlderThan(TimeStamp time){
        if(time.year<=this.year){
            if(time.year<this.year) return false;
            else{
                if(time.month<=this.month){
                    if(time.month<this.month) return false;
                    else{
                        if(time.day<=this.day){
                            if(time.day<this.day) return false;
                            else{
                                return time.Milliseconds >this.Milliseconds;
                            }
                        }else return true;
                    }
                }else return true;
            }
        }
        return true;
    }
    boolean isValidDailyTime(){
        final Calendar calendar=Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH)+1;
        int year = calendar.get(Calendar.YEAR);
        return (this.day == day && this.month == month && this.year == year);
    }
    void buildFromDate(Calendar date){
        if(date != null){
            this.day = date.get(Calendar.DAY_OF_MONTH);
            this.month = date.get(Calendar.MONTH)+1;
            this.year = date.get(Calendar.YEAR);
            this.Milliseconds = (date.get(Calendar.HOUR_OF_DAY)*60*60*1000)
                    + (date.get(Calendar.MINUTE)*60*1000)+ (date.get(Calendar.SECOND)*1000) + (date.get(Calendar.MILLISECOND));
            checkSelf();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(day);
        out.writeInt(month);
        out.writeInt(year);
        out.writeLong(Milliseconds);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<TimeStamp> CREATOR = new Parcelable.Creator<TimeStamp>() {
        public TimeStamp createFromParcel(Parcel in) {
            return new TimeStamp(in);
        }

        public TimeStamp[] newArray(int size) {
            return new TimeStamp[size];
        }
    };

    private TimeStamp(Parcel in) {
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
        Milliseconds = in.readLong();
        checkSelf();
    }
}
