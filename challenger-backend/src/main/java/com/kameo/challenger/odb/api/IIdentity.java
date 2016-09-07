package com.kameo.challenger.odb.api;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
public interface IIdentity extends Serializable {
    public static final String id_column="id";
    long getId();

    default boolean isNew() {
        return getId()<0;
    }

    public static class IdentityComparator implements Comparator<IIdentity> {

        @Override
        public int compare(IIdentity o1, IIdentity o2) {
            if (o1.getId()>o2.getId())
                return 1;
            else if (o1.getId()<o2.getId())
                return -1;
            else return 0;
        }
    }


    public static int compare(IIdentity o1, IIdentity o2) {
        if (o1.getId()>o2.getId())
            return 1;
        else if (o1.getId()<o2.getId())
            return -1;
        else return 0;
    }
}
