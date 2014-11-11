/*
 * Copyright (C) 2013ff. mycitizen.net
 *
 * Licensed under the GPLv3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mycitizen.mcn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {

    private String baseApiUrl = "https://demo.mycitizen.net/API/";
    public static String localStorageName = "MyCitizen";
    public static final String version = "0.1 Beta";

    public static final String DEBUG_TAG = "mcn";

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

        this.ctx = ctx;

        SharedPreferences settings = ctx.getSharedPreferences(Config.localStorageName, 0);

        String deployment = settings.getString("usedApi", null);
        if (deployment != null) {
            this.baseApiUrl = deployment;
        }
        Log.d(Config.DEBUG_TAG, "CONFIG init API: " + this.baseApiUrl);
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


    public static LinkedHashMap<String, String> getUiLanguages() {

        LinkedHashMap<String, String> languages = new LinkedHashMap<String, String>();
        languages.put("eng", "English");
        languages.put("ces", "Čeština");
        languages.put("hlt", "Matu");
        languages.put("mrh", "Mara");
        languages.put("zom", "Zolai");
        languages.put("hin", "नहीं");

        return languages;

    }


    public static String codeToLocale(Context context, String ui_language) {
        SharedPreferences settings = context.getSharedPreferences(Config.localStorageName, 0);
        String loc;
        if (ui_language.equals("ces")) {
            loc = "cs";
        } else if (ui_language.equals("hlt")) {
            loc = "ht";
        } else if (ui_language.equals("mrh")) {
            loc = "mh";
        } else if (ui_language.equals("zom")) {
            loc = "zu";
        } else if (ui_language.equals("hin")) {
            loc = "hi";
        } else {
            loc = "en";
        }
        return loc;
    }


    public static String translateLanguageNameToCode(Context context, String languageName) {

        ApiConnector api = new ApiConnector(context);
        LinkedHashMap<String, String> languages = api.getSupportedLanguages();
        if (languages != null) {
            Iterator<Map.Entry<String, String>> it = languages.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                if (pairs.getValue().toString().equals(languageName)) {
                    return pairs.getKey().toString();
                }
            }
        }
        languages = getUiLanguages();
        if (languages != null) {
            Iterator<Map.Entry<String, String>> it = languages.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                if (pairs.getValue().toString().equals(languageName)) {
                    return pairs.getKey().toString();
                }
            }
        }
        return "eng";
    }

    public static String translateLanguageCodeToName(Context context, String languageCode) {

        ApiConnector api = new ApiConnector(context);
        LinkedHashMap<String, String> languages = api.getSupportedLanguages();
        if (languages != null) {
            Iterator<Map.Entry<String, String>> it = languages.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                if (pairs.getKey().toString().equals(languageCode)) {
                    return pairs.getValue().toString();
                }
            }
        }
        languages = getUiLanguages();
        if (languages != null) {
            Iterator<Map.Entry<String, String>> it = languages.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                if (pairs.getKey().toString().equals(languageCode)) {
                    return pairs.getValue().toString();
                }
            }
        }
        return "English";
    }

    public static LinkedHashMap<Integer, String> getSupportedTimers(Context context) {

        LinkedHashMap<Integer, String> notifications = new LinkedHashMap<Integer, String>();
        notifications.put(0, "-");
        notifications.put(1, context.getString(R.string.once_per_hour));
        notifications.put(24, context.getString(R.string.once_per_day));
        notifications.put(168, context.getString(R.string.once_per_week));

        return notifications;

    }

}
