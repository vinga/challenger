package challenger.utils;

import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.List;


public class ReflectionUtils {

    /**
     * Copies properites of SRC to DST
     */
    public static <SRC,DST> DST copy(SRC f, DST e) {
        BeanUtils.copyProperties(f, e);
        return e;
    }

    public static <SRC,DST> DST copy(SRC f, Class<DST> clz) {
        try {
            DST dst = clz.newInstance();
            copy(f, dst);
            return dst;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies list of SRC to list of DST
     */
    public static <SRC,DST> List<DST> copyList(Collection<SRC> fs, Class<DST> clz) {
        List<DST> res= Lists.newArrayList();
        for (SRC f: fs) {
            try {
                res.add(copy(f, clz.newInstance()));
            } catch (InstantiationException | IllegalAccessException e) {
               throw new RuntimeException(e);
            }
        }
        return res;
    }
}
