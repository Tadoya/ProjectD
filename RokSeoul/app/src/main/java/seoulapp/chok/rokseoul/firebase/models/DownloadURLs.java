package seoulapp.chok.rokseoul.firebase.models;

/**
 * Created by choiseongsik on 2016. 10. 13..
 */

public class DownloadURLs {
    private String url;
    private String direction;
    private int azimuth;
    private int pitch;
    private int roll;

    public DownloadURLs(){

    }
    public DownloadURLs(String direction, String url, int azimuth, int pitch, int roll){
        this.direction = direction;
        this.url = url;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }
    public String getUrl(){ return url; }
    public String getDirection() { return direction; }
    public Integer getAzimuth() { return azimuth; }
    public Integer getPitch() { return pitch; }
    public Integer getRoll() { return roll; }

}
