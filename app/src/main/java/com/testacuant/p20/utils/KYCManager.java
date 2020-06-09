package com.testacuant.p20.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class KYCManager {

    private static KYCManager sInstance;
    private SharedPreferences mPreferences;
    public static final String SHARED_PREF_KEY = "KYCOptions";
    private static final String KEY_API_KEY = "24b1f22f-6919-4fa0-a519-a45c783c235a";
    private static final String KEY_MAX_PICTURE_WIDTH = "KycPreferenceKeyMaxPictureWidth";
    private static final String KEY_JSON_WEB_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImlkY2xvdWRwYXJ0bmVycyJ9.eyJqdGkiOiJzb3l5by0xNTg1ODM3NzkwIiwicm9sZXMiOlsic2NzOmV4ZWN1dGVTY2VuYXJpbyJdLCJpYXQiOjE1ODU4Mzc4MDEsImV4cCI6MTU5NDQ3NzgwMSwiaXNzIjoiaWRjbG91ZHBhcnRuZXJzIiwic3ViIjoiaWRjbG91ZHBhcnRuZXJzIn0.4Nju9ifehxI8wIIAmHK7HXUgDsOl1sGZa1rhJzQQF2bvLCrp8-F1TotZ-bBnYvNaQemE-eaJyVaE243sxIuW8MjZkc9Z9erD1DhzREvUAqPxfZ-5VnWrdVrjlig5z6Jq8TamvGeFp0HCrMN6yt5AAbv-BMXzrhHyyg3NHyMjfJ0MPn1Y2_wvBlTiLIJh7zeDnqguU9ZVF_jJ9qozHP0IjSnm6eGTCxCy_t6hpMjBKQDZyFjpI1I4slCPEG4g78kioD1eYrnW-N1vhI4WL1LyZkD3pIXdi0zQ8W7ohTs1rwfdpmYCl7QwO39K-xaSn_RkCSG09Zuv1RpirA5tN0-aww";


    public static synchronized KYCManager getInstance() {
        if (sInstance == null) {
            sInstance = new KYCManager();
        }

        return sInstance;
    }

    public void initialise(final Context context) {
        mPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves last JSON Web Token or predefined value.
     * @return JWT.
     */
    public String getJsonWebToken() {
        return getValueString(KEY_JSON_WEB_TOKEN, null);
    }

    /**
     * Retrieves the value for a given key.
     * @param key Key.
     * @return Value for given key or default value if value is not present.
     */
    private String getValueString(final String key, final String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public int getMaxImageWidth() {
        return getValueInt(KEY_MAX_PICTURE_WIDTH, 1024);
    }

    private int getValueInt(final String key, final int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public String getApiKey() {
        return getValueString(KEY_API_KEY, null);
    }

}
