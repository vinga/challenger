package com.kameo.challenger.utils;

import java.util.Random;

class RandomUtil {
    private static final Random r=new Random();

    public static int randomInt(int min, int max) {
        return min + r.nextInt(max);
    }
    public static int randomInt(Random r,int min, int max) {
        return min + r.nextInt(max);
    }
}
