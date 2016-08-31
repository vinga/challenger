package challenger.odb.api;

import java.io.Serializable;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
public interface IIdentity extends Serializable {
    public static final String id_column="id";
    long getId();
}
