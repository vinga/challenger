package com.kameo.challenger.utils.odb.newapi;

import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.utils.odb.AnyDAONew;
import kotlin.Unit;

import javax.persistence.metamodel.SingularAttribute;


class SugarTest {

    public void foo(AnyDAONew ad) {
        SingularAttribute<TaskODB, UserODB> _user = (SingularAttribute) ad.getEm().getMetamodel().entity(UserODB.class).getDeclaredAttribute("user");
        SingularAttribute<TaskODB, String> _label = (SingularAttribute) ad.getEm().getMetamodel().entity(TaskODB.class).getDeclaredAttribute("label");




        ad.getAll(TaskODB.class, UserODB.class, it -> {
                    it.inIds(10,12,13)
                      .eqId(10)
                      .newOr(it2 -> {
                          it2.eq(_user, new UserODB(2));
                          it2.eq(_user, new UserODB(3));
                          return Unit.INSTANCE;
                      })
                      .newOr(it2 -> {
                          it2.eq(_user, new UserODB(2));
                          it2.eq(_user, new UserODB(3));
                          return Unit.INSTANCE;
                      })
                      .get(_user).eqId(30);


                    return it.select(it.get(_user));
                }
        );

        /*ad.getOne(TaskODB.class, UserODB.class, new ISugarQuery<TaskODB, UserODB>() {

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
        }, UserODB.class);*/
    }
}
