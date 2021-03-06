package com.concepttech.campingcompanionbluetooth;

/**
 * Created by jtint on 11/15/2017.
 */

public class Constants {
    public static final String MTYPE = "messageType", MARGS = "numArgs", ASIZE = "arraySize", VALUES = "values", ROWS = "Rows", COLUMNS = "Columns", DATASEP = "|", LABELDATASEP = ":",
            DATAEND = ";", BODYBEGIN = "<", BODYEND = "/>", INTNUMERICREGEX = "[0-9]+", DOUBLENUMERICREGEX = "[-+]?[0-9]*\\.?[0-9]+", RESENDLOGMESS = "Resend Requested",
            ERR = "Error: ", MESS = "Message: ", RTRERR = "RTR mismatch", MESSAGENULLERR = "Message was null", MESSAGEEMPTYERR = "Message was empty",
            TYPEARGERR = "Message did not contain either type or numargs",
            MESSAGETYPEEMPTY = "Message type was empty",
            MESSAGENUMARGSEMPTY = "Message numArgs was empty",
            MESSAGETYPENOTNUMORLONG = "Message type was not numeric or to long",
            MESSAGEARGSNOTNUMORLONG = "Message numArgs was not numeric or to long",
            MESSAGETYPE = "Message type parsed messagetype: ",
            NOCOLERR = "Message did not contain any colons",
            MESSTOPOSTEMPTY = "Message to post to log was length 0, no resend requested",
            MESSTOPOSTNULL = "Message to post to log was null, no resend requested",
            FALSE = "false", TRUE = "true", DEVICE_NAME = "device_name", TOAST = "toast", LIGHTSTATUS = "LIGHTSTATUS", LIGHTCOLORS ="LIGHTCOLORS", THEATERCOMMAND = "theater",
            HomeFragmentLaunchLights = "Home:Lights",HomeFragmentLaunchStatus = "Home:Status",HomeFragmentLaunchLocation = "Home:Location",HomeFragmentLaunchLog = "Home:Log",
            HomeFragmentLaunchConnection = "Home:Connection",LightsFragmentChangeColor = "Lights:ColorCommand",LightsFragmentBack = "Lights:Home",LocationragmentBack = "Location:Home",
            DeviceName = "PEBL", ScanningText = "Scanning...", ScanText = "Scan", LightStatusOn = "static", LightStatusOff = "off", TheaterStatus = "theater", Rainbow1Status = "rainbow1",
            Rainbow2Status = "rainbow2", LightStaticStatus = "static",PlacesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?", PlacesLocationTag = "&location=",
            PlacesRadiusTag = "&radius=", PlacesTypeTag = "&types=locality", PlacesKeyTag = "&key=", CitiesNearUserDatabaseTag = "CitiesNearUser", FeedFragmentHomeCommand = "Feed:Home",
            PlacesJSONResultsTag = "results" , PlacesJSONNameTag = "name", PlacesJSONIDTag = "id", UserAdminAreaTag = "AdminArea", FeedCameraCommand = "Feed:Camera",
            MainLocationTag = "MainLocation", PhotoCountTag = "PhotoCount", PhotoCountIntentID = "PhotoCountIntentID", PhotoName = "Photo.jpg";
    private final static String MediaIndexRegex = "[0-9]+";
    public static final int MESSAGE_STATE_CHANGE = 1,
            MAXCOLORCODES = 3,
            MaxColorPrograms = 4,
            MAXCOLORCODEVALUE = 256,
            MESSAGE_READ = 2,
            MESSAGE_WRITE = 3,
            MESSAGE_DEVICE_NAME = 4,
            MESSAGE_TOAST = 5,
            INIT = 0,
            UPADATEDATAPHONE2HUB = 1,
            UPDATEDATAHUB2PHONE = 2,
            COMMAND = 3,
            Disconnect = 4,
            COMMANDCONFIRMATION = 5,
            RESENDREQUEST = 6,
            RESENDREQUESTCONFIRMATION = 7,
            GENERALCONFIRMATION = 9,
            MAXALLOWEDTYPEDIGITS = 1,
            INITEXPECTEDARGS = 5,
            MESSAGETYPERETURNERROR = -1,
            MESSAGETYPEMAXVALUE = 9;
    public static final String[] LIGHTCOMMANDLABELS = {LIGHTSTATUS,LIGHTCOLORS};
    public static final String[] LABELS = {
        "TEMP",	"HUMIDITY", "BAROPRESS", "BATTERYLEVEL", "LIGHTSTATUS",
                "NUMLIGHTS", "LIGHTNAMES", "LIGHTCOLORS", "SOLAR","WIND",
                "LATITUDE", "LONGITUDE", "BROADCAST", "HUBNAME", "NETWORKED",
                "NUMNODES", "NODENAMES", "ALLOWSCONTROL", "ALLOWSCHAT", "SLAVE", "OTHERLIGHTNAMES",
                "OTHERNUMLIGHTS", "OTHERLIGHTSTATUS", "CONNECTIONERROR", "MESSAGEDEST", "MESSAGESOURCE",
                "REASON"
    };
    public static String GetDatabaseLocationString(String country, String AdminArea, String Locality, String LocationName){
        if(country != null && AdminArea != null && Locality != null && LocationName != null &&
                country.length() > 1 && AdminArea.length() > 1 && Locality.length() > 1 && LocationName.length() > 1)
            return (country + "/" + AdminArea + "/" + Locality + "/" + LocationName).replace(" ","").replace(",","");
        else return "";
    }
    public static String GetDatabaseLocationDataString(String country, String AdminArea, String Locality, String LocationName){
        if(country != null && AdminArea != null && Locality != null && LocationName != null &&
                country.length() > 1 && AdminArea.length() > 1 && Locality.length() > 1 && LocationName.length() > 1)
            return (country + "/" + AdminArea + "/" + Locality + "/" + LocationName + "/LocationData").replace(" ","").replace(",","");
        else return "";
    }
    public static String GetDatabaseLocationPhotoCountDataString(String country, String AdminArea, String Locality, String LocationName){
        if(country != null && AdminArea != null && Locality != null && LocationName != null &&
                country.length() > 1 && AdminArea.length() > 1 && Locality.length() > 1 && LocationName.length() > 1)
            return (country + "/" + AdminArea + "/" + Locality + "/" + LocationName + "/LocationData/"+PhotoCountTag).replace(" ","").replace(",","");
        else return "";
    }
    public static int LabelIndex(String label){
        if(label != null){
            if(label.length() > 0){
                int i;
                for (i = 0; i < LABELS.length; i++){
                    if(label.equals(LABELS[i])){
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    public static boolean isValidLabel(String labelcandidate){
        if(labelcandidate != null){
            if(labelcandidate.length() > 0){
                int i;
                for (i = 0; i < LABELS.length; i++){
                    if(labelcandidate.equals(LABELS[i])){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static boolean isNumber(String string){
        if(string != null && string.length() > 0) return string.matches(Constants.MediaIndexRegex);
        else return false;
    }
}