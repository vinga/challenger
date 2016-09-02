package com.kameo.challenger.odb;

import com.kameo.challenger.odb.api.IIdentity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    @NotNull
    String email;


    @ManyToMany
    List<ChallengeContractODB> contracts;


    public UserODB(long id) {
        this.id=id;
    }


}
