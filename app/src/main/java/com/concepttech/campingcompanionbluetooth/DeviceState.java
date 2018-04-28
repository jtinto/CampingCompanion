package com.concepttech.campingcompanionbluetooth;

import static com.concepttech.campingcompanionbluetooth.Constants.LightStaticStatus;
import static com.concepttech.campingcompanionbluetooth.Constants.MaxColorPrograms;
import static com.concepttech.campingcompanionbluetooth.Constants.Rainbow1Status;
import static com.concepttech.campingcompanionbluetooth.Constants.Rainbow2Status;
import static com.concepttech.campingcompanionbluetooth.Constants.TheaterStatus;

public class DeviceState {
    private int batterylevel, numlights, solarpower, windpower, numnodes;
    private int[] lightcolors;
    private double temperature, humidity, barometricpressure, latitude, longitude;
    private boolean broadcast;
    private boolean[]  networked, allowscontrol, slave, connectionerror;
    private boolean[][] otherlightstatuses;
    private String lightstatus, hubname;
    private String[] lightnames, nodenames;
    private String[][] otherlightnames;

    public DeviceState(){
        lightcolors = new int[3];
        lightcolors[0] = 0;
        lightcolors[1] = 0;
        lightcolors[2] = 0;
        lightstatus = "";
    }
    public String getLightStatusDataString() {
        String returnstring = "Error";
        if (lightstatus != null) {
            return lightstatus;
        }
        return returnstring;
    }
    public String getLightColorDataString() {
        String returnstring = "Error";
        boolean error = false;
        if (lightcolors != null) {
            if (lightcolors.length > 0) {
                int i;
                returnstring = Constants.BODYBEGIN + Constants.ASIZE + Constants.LABELDATASEP + lightcolors.length +
                        Constants.DATAEND + Constants.VALUES + Constants.LABELDATASEP;
                int RGBi;
                for (RGBi = 0; RGBi < Constants.MAXCOLORCODES; RGBi++) {
                    if (lightcolors[RGBi] >= 0 && lightcolors[RGBi] <= Constants.MAXCOLORCODEVALUE) {
                        returnstring += lightcolors[RGBi];
                    } else error = true;
                    if (error) {
                        error = false;
                        returnstring += "BadData";
                    }
                    if (RGBi != Constants.MAXCOLORCODES - 1)
                        returnstring += Constants.DATASEP;
                }
            } else returnstring += "BadData|BadData|BadData";
            returnstring += Constants.DATAEND + Constants.BODYEND;
        }
        return returnstring;
    }

    public double getTemperature() {
        return temperature;
    }
    public double getHumidity() {
        return humidity;
    }
    public double getBarometricpressure() {
        return barometricpressure;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
    public void setBarometricpressure(double barometricpressure) {
        this.barometricpressure = barometricpressure;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void TurnLightOn(){lightstatus = LightStaticStatus;}
    public void TurnLightOff(){lightstatus = "off";}
    public void TurnLightsTheater(int which){
        if(which >= 0 && which <= MaxColorPrograms){
            switch (which){
                case 0:
                    //theater
                    lightstatus = TheaterStatus;
                    break;
                case 1:
                    //rainbow1
                    lightstatus = Rainbow1Status;
                    break;
                case 2:
                    //rainbow1
                    lightstatus = Rainbow2Status;
                    break;
            }
        }
    }
    public void setLightcolors(int red, int green, int blue){
        if(lightcolors != null) {
            if (lightcolors.length == Constants.MAXCOLORCODES && red >= 0 && red < Constants.MAXCOLORCODEVALUE
                    && green >= 0 && green < Constants.MAXCOLORCODEVALUE
                    && blue >= 0 && blue < Constants.MAXCOLORCODEVALUE) {
                lightcolors[0] = red;
                lightcolors[1] = green;
                lightcolors[2] = blue;
            }
        }
    }
}
