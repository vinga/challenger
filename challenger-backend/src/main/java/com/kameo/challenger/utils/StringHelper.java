package com.kameo.challenger.utils;


public class StringHelper {
    public static String getFirstHrefValue(String str) {
        str=str.replace("\"","'");
        int i=str.indexOf("href='");
        int j=str.indexOf("'",i+6);
        return str.substring(i+6,j);
    }
}
