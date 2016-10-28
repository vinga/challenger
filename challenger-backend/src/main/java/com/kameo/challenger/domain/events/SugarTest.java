package com.kameo.challenger.domain.events;

import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.utils.odb.AnyDAONew;
import com.kameo.challenger.utils.odb.ISugarQuery;
import com.kameo.challenger.utils.odb.newapi.ISugarQuerySelect;
import com.kameo.challenger.utils.odb.newapi.PathWrap.ClousureWrap;
import com.kameo.challenger.utils.odb.newapi.RootWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.metamodel.SingularAttribute;

import static org.apache.coyote.http11.Constants.a;


public class SugarTest {

    public void foo(AnyDAONew ad) {
        SingularAttribute<TaskODB, UserODB> _user = (SingularAttribute) ad.getEm().getMetamodel().entity(UserODB.class).getDeclaredAttribute("user");
        SingularAttribute<TaskODB, String> _label = (SingularAttribute) ad.getEm().getMetamodel().entity(TaskODB.class).getDeclaredAttribute("label");
        ad.getOne(TaskODB.class, new ISugarQuery<TaskODB, UserODB>() {

            @Nullable
            @Override
            public ISugarQuerySelect<UserODB> query(@NotNull RootWrap<TaskODB, TaskODB> it) {

                it.eqId(12L);
                it.get(_user).eqId(8);

                ClousureWrap<TaskODB, TaskODB> or = it.newOr();
                    or.get(_user).eqId(10);
                    or.ref(it.get(_label)).eq("faa");
                or.finish();

                it.newOr((it2)-> {
                        it2.eqId(100);
                        it2.eqId(300);
                    return null;

                });

                it.get(_user).eqId(10);

                return it.select(it.get(_user));

            }
        }, UserODB.class);
    }
}
