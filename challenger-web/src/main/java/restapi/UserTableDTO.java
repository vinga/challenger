package restapi;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by kmyczkowska on 2016-08-12.
 */
public class UserTableDTO {
    //no:0, userName:"Kamila", date:"2016-08-02", actionsList: kamilaList};
    String userName="Kamila";
    String date="2016-08-02";
    boolean authorized=true;

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    List<ChallengeActionDTO> actionsList= Lists.newArrayList();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ChallengeActionDTO> getActionsList() {
        return actionsList;
    }

    public void setActionsList(List<ChallengeActionDTO> actionsList) {
        this.actionsList = actionsList;
    }
}
