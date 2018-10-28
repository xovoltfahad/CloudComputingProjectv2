package fastnu.cloudcomputingproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;

public class MysharedPrefrencess {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;


    public static final String name="name";
    public static final String profilePic="profilePic";



    public MysharedPrefrencess(Context context){
        sharedPreferences= context.getSharedPreferences("nookinfo",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void setName(String s){
        editor.putString( name,s );
        editor.commit();
    }

    public String getName(){
        return sharedPreferences.getString( name,null );
    }

    public void setProfilePic(Uri uri){
        editor.putString( profilePic,uri.toString() );
        editor.commit();
    }

    public String getProfilePic(){
        return sharedPreferences.getString( profilePic,null );
    }

    public void removeAll(){
        sharedPreferences.edit().clear().commit();
    }

    public void setUser(user User)
    {
        Gson gson = new Gson();
        String json = gson.toJson(User); // myObject - instance of MyObject
        editor.putString("MyObject", json);
        editor.commit();
    }

    public user getUser(){
        Gson gson = new Gson();
        String json = sharedPreferences.getString("MyObject", "");
        return gson.fromJson(json, user.class);
    }
}
