package data_local;

import android.content.Context;

public class DataLocalManager {
    private static final String PREF_FIRST_INSTALL = "PREF_FIRST_INSTALL";
    private static DataLocalManager instance;
    private MySharedPreferences mySharedPreferences;

    public static void init(Context context){
        instance = new DataLocalManager();
        instance.mySharedPreferences = new MySharedPreferences(context);
    }

    public static DataLocalManager getInstance(){
        if(instance == null){
            instance = new DataLocalManager();
        }
        return instance;
    }

    public static void setPrefFirstInstalled(boolean isFirst){
        DataLocalManager.getInstance().mySharedPreferences.putBooleanValue(PREF_FIRST_INSTALL, isFirst);
    }

    public static boolean getFirstInstalled(){
        return DataLocalManager.getInstance().mySharedPreferences.getBooleanValue(PREF_FIRST_INSTALL);
    }

    public static void setBooleanValue(String key, boolean value){
        DataLocalManager.getInstance().mySharedPreferences.putBooleanValue(key, value);
    }

    public static boolean getBooleanValue(String key) {
        return DataLocalManager.getInstance().mySharedPreferences.getBooleanValue(key);
    }
}
