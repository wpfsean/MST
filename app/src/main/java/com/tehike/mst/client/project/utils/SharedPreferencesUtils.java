package com.tehike.mst.client.project.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 描述：保存数据
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/28 8:13
 * @version V1.0
 */

public class SharedPreferencesUtils {

    /**
     * 文件名
     */
    private static final String FILE_NAME = "tehike";

    /**
     * 保存数据的方法
     */
    public static void putObject(Context context , String key, Object object){

        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if("String".equals(type)){
            editor.putString(key, (String)object);
        }
        else if("Integer".equals(type)){
            editor.putInt(key, (Integer)object);
        }
        else if("Boolean".equals(type)){
            editor.putBoolean(key, (Boolean)object);
        }
        else if("Float".equals(type)){
            editor.putFloat(key, (Float)object);
        }
        else if("Long".equals(type)){
            editor.putLong(key, (Long)object);
        }

        editor.commit();
    }

    /**
     * 得到保存数据的方法
     */
    public static Object getObject(Context context , String key, Object defaultObject){
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        if("String".equals(type)){
            return sp.getString(key, (String)defaultObject);
        }

        else if("Integer".equals(type)){
            return sp.getInt(key, (Integer)defaultObject);
        }

        else if("Boolean".equals(type)){
            return sp.getBoolean(key, (Boolean)defaultObject);
        }

        else if("Float".equals(type)){
            return sp.getFloat(key, (Float)defaultObject);
        }

        else if("Long".equals(type)){
            return sp.getLong(key, (Long)defaultObject);
        }

        return null;
    }

    /**
     * 清除所有数据
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear().commit();
    }

    /**
     * 清除指定数据
     */
    public static void clearAll(Context context,String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }


}
