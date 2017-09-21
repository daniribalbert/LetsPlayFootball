package com.daniribalbert.letsplayfootball.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Google Gson Utility class.
 */
public class GsonUtils {

    private static Gson buildDefaultGson(){
        final GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder.create();
    }

    public static String toJson(final Object obj){
        final Gson gson = buildDefaultGson();

        return gson.toJson(obj);
    }

    public static <T> T fromJson(final String jsonStr, Type type){
        final Gson gson = buildDefaultGson();
        return gson.fromJson(jsonStr, type);
    }
}
