package com.thefidebox.fidebox.firebasecloudmessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME="fcmsharedpref";
    private static final String KEY_ACCESS_TOKEN="tokenId";

    private static Context mCtx;
    private static  SharedPrefManager mInstance;

    public SharedPrefManager(Context context) {
        mCtx=context;
    }

    public static synchronized  SharedPrefManager getInstance(Context context){
        if(mInstance==null){
            mInstance=new SharedPrefManager(context);
        }
        return mInstance;
    }

    public boolean storeToken (String token){

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mCtx);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(KEY_ACCESS_TOKEN,token);
        editor.apply();
        return  true;
    }

    public String getToken(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mCtx);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN,null);
    }

}
