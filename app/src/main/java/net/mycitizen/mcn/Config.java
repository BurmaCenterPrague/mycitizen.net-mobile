package net.mycitizen.mcn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    private String baseApiUrl = "https://demo.mycitizen.net/API/";
    public static String localStorageName = "MyCitizen";
    public static final String version = "0.1 Beta";

    private String predefinedApiUrl[] = {
            "https://myanmar.mycitizen.net/API/",
            "https://ncrwg.mycitizen.net/API/",
            "https://chin-cso.mycitizen.net/API/",
            "https://demo.mycitizen.net/API/"
    };
    private String predefinedApiUrlLabels[] = {
            "myanmar.mycitizen.net",
            "ncrwg.mycitizen.net",
            "chin-cso.mycitizen.net",
            "demo.mycitizen.net"
    };
    private Context ctx;

    public Config(Context ctx) {
        System.out.println("COnfig constructor");
        this.ctx = ctx;

        SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);

        String deployment = settings.getString("usedApi", null);
        if (deployment != null) {
            this.baseApiUrl = deployment;
        }
        System.out.println("CONFIG init API: " + this.baseApiUrl);
    }

    public void setApiUrl(String api) {
        this.baseApiUrl = api;

        SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("MYCITIZEN_SID", null);
        editor.putString("UserAgent", null);

        editor.commit();
    }

    public String getApiUrl() {
        return this.baseApiUrl;
    }

    public String translateApiUrlLabel(String url) {
        int index = -1;
        for (int i = 0; i < predefinedApiUrlLabels.length; i++) {
            if (predefinedApiUrlLabels[i].equals(url)) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            return predefinedApiUrl[index];
        }
        return null;
    }

    public String translateApiUrl(String url) {
        int index = -1;
        for (int i = 0; i < predefinedApiUrl.length; i++) {
            if (predefinedApiUrl[i].equals(url)) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            return predefinedApiUrlLabels[index];
        }
        return null;
    }

    public String[] getDeploymentLabels() {
        return this.predefinedApiUrlLabels;
    }


    public String getVersion() {
        return version;
    }

    public static String retrieveLength(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);
        int speed = settings.getInt("connection_strength", 0);
        if (speed <= 50) {
            return "10";
        } else {
            return "20";
        }
    }
}
