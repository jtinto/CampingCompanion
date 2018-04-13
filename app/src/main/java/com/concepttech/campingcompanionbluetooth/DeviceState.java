package com.concepttech.campingcompanionbluetooth;

public class DeviceState {
    private int batterylevel, numlights, solarpower, windpower, numnodes;
    private int[][] lightcolors;
    private double temperature, humidity, barometricpressure, latitude, longitude;
    private boolean broadcast;
    private boolean[] lightstatus, networked, allowscontrol, slave, connectionerror;
    private boolean[][] otherlightstatuses;
    private String hubname;
    private String[] lightnames, nodenames;
    private String[][] otherlightnames;

    public DeviceState(){
        lightcolors = new int[1][];
        lightcolors[0] = new int[3];
        lightcolors[0][0] = 0;
        lightcolors[0][1] = 0;
        lightcolors[0][2] = 0;
        lightstatus = new boolean[1];
        lightstatus[0] = false;
    }
    public String getLightStatusDataString() {
        String returnstring = "Error";
        if (lightstatus != null) {
            if (lightstatus.length > 0) {
                int i;
                returnstring = Constants.BODYBEGIN + Constants.ASIZE + Constants.LABELDATASEP + lightstatus.length +
                        Constants.DATAEND + Constants.VALUES + Constants.LABELDATASEP;
                for (i = 0; i < lightstatus.length; i++) {
                    returnstring += lightstatus[i];
                    if (i != lightstatus.length - 1) returnstring += Constants.DATASEP;
                }
                returnstring += Constants.DATAEND + Constants.BODYEND;
            }
        }
        return returnstring;
    }
    public String getLightColorDataString() {
        String returnstring = "Error";
        boolean error = false;
        if (lightcolors != null) {
            if (lightcolors.length > 0) {
                int i;
                returnstring = Constants.BODYBEGIN + Constants.ROWS + Constants.LABELDATASEP + lightcolors.length +
                        Constants.DATAEND + Constants.COLUMNS + Constants.LABELDATASEP + Constants.MAXCOLORCODES +
                        Constants.DATAEND + Constants.VALUES + Constants.LABELDATASEP;
                for (i = 0; i < lightcolors.length; i++) {
                    if (lightcolors[i] != null) {
                        int RGBi;
                        if (i != 0) returnstring += Constants.DATASEP;
                        for (RGBi = 0; RGBi < Constants.MAXCOLORCODES; RGBi++) {
                            if (lightcolors[i][RGBi] >= 0 && lightcolors[i][RGBi] <= Constants.MAXCOLORCODEVALUE) {
                                returnstring += lightcolors[i][RGBi];
                            } else error = true;
                            if (error) {
                                error = false;
                                returnstring += "BadData";
                            }
                            if (RGBi != Constants.MAXCOLORCODES - 1)
                                returnstring += Constants.DATASEP;
                        }
                    } else returnstring += "BadData|BadData|BadData";
                }
                returnstring += Constants.DATAEND + Constants.BODYEND;
            }
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
    public void TurnLightOn(int[] which){
        if(which != null) {
            if (lightstatus != null) {
                int i;
                for(i = 0; i < which.length; i++) {
                    if (which[i] < lightstatus.length) {
                        lightstatus[which[i]] = true;
                    }
                }
            }
        }
    }
    public void TurnLightOff(int[] which){
        if(which != null) {
            if (lightstatus != null) {
                int i;
                for(i = 0; i < which.length; i++) {
                    if (which[i] < lightstatus.length) {
                        lightstatus[which[i]] = false;
                    }
                }
            }
        }
    }
    public void setLightcolors(int which, int red, int green, int blue){
        if(lightcolors != null){
            if(which < lightcolors.length) {
                if (lightcolors[which] != null) {
                    if (lightcolors[which].length == Constants.MAXCOLORCODES && red >= 0 && red < Constants.MAXCOLORCODEVALUE
                            && green >= 0 && green < Constants.MAXCOLORCODEVALUE
                            && blue >= 0 && blue < Constants.MAXCOLORCODEVALUE) {
                        lightcolors[which][0] = red;
                        lightcolors[which][1] = green;
                        lightcolors[which][2] = blue;
                    }
                }
            }
        }
    }
}
