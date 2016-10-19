package com.kameo.challenger.utils;

import org.springframework.beans.BeanUtils;


public class ReflectionUtils {

    /**
     * Copies properites of SRC to DST
     */
    public static <SRC, DST> DST copy(SRC f, DST e) {
        BeanUtils.copyProperties(f, e);
        return e;
    }


}
