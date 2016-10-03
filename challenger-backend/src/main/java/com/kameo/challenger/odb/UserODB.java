package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Data
@ToString(of=IIdentity.id_column)
@NoArgsConstructor
public class UserODB implements IIdentity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    String login;
    String passwordHash;
    String salt;

    @NotNull
    String email;

    @NotNull
    @Enumerated
    UserStatus userStatus=UserStatus.ACTIVE;

    @Temporal(TemporalType.TIMESTAMP)
    Date suspendedDueDate;
    int failedLoginsNumber;

    @ManyToMany
    List<ChallengeODB> contracts;

    Long defaultChallengeContract;

    public UserODB(long id) {
        this.id=id;
    }

    // email is never null
    @Transient
    public String getLoginOrEmail() {
        return getLogin()!=null? getLogin(): getEmail();
    }

    public static UserODB ofEmail(String email) {
        UserODB u=new UserODB();
        u.setId(-1);
        u.setEmail(email);
        return u;
    }
}
