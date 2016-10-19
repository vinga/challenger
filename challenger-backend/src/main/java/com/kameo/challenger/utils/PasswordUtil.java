package com.kameo.challenger.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordUtil {


    private static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    private static String randomString(int min, int max) {
        int num = randomInt(min, max);
        byte b[] = new byte[num];
        for (int i = 0; i < num; i++)
            b[i] = (byte) randomInt('a', 'z');
        return new String(b);
    }


    public static String createSalt() {
        return randomString(6,6);
    }

    public static String getPasswordHash(String pass, String salt) {

        MessageDigest md;
        StringBuilder sb = new StringBuilder();
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update((salt+pass+salt).getBytes());
            byte byteData[] = md.digest();

            // convert the byte to hex format method 1

            for (byte element : byteData) {
                sb.append(Integer.toString((element & 0xff) + 0x100, 16)
                                 .substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
