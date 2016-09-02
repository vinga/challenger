package com.kameo.challenger.utils;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
public class StringHelper {
    public static String getFirstHrefValue(String str) {
        str=str.replace("\"","'");
        int i=str.indexOf("href='");
        int j=str.indexOf("'",i+6);
        return str.substring(i+6,j);
    }
}
