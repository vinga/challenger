import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {AccountDTO} from "../AccountDTO";
import {ON_LOGOUT_SECOND_USER} from "../accountActionTypes";
import TaskTableUserIcon from "./TaskTableUserIcon";
import {IconButton} from "material-ui";
import {anyUserAsAccountSelector} from "../accountSelectors";
import {ChallengeStatus} from "../../module_challenges/ChallengeDTO";


interface Props {
    userId: number,
    no: number,
    userLabel: string,
    userLogin: string,
    challengeStatus: string
}
interface ReduxProps {
    user: AccountDTO
}


interface ReduxPropsFunc {
    onSecondUserLogout: (userId: number)=>void;

}
interface PropsFunc {
    onOpenDialogForLoginSecondUser: (event: EventTarget)=>void;
}


class TaskTableHeaderAccountPanelInternal extends React.Component<Props & ReduxProps & ReduxPropsFunc & PropsFunc,void> {


    render() {
        return <h2 style={{display: "flex",flexFlow: "row nowrap", justifyContent:"space-between"}}>

            <div style={{display:"flex",flexFlow: "row nowrap"}}>
            <TaskTableUserIcon
                userNo={this.props.no}
                challengeStatus={this.props.challengeStatus}
            />

            <span style={{}}><span style={{lineHeight: '65px'}}>{this.props.user.label}</span>
                {this.props.no != 0 && this.props.challengeStatus == ChallengeStatus.ACTIVE && (this.props.user.jwtToken != null ?

                        <IconButton
                            onClick={() => {this.props.onSecondUserLogout(this.props.user.id)}}>
                            &nbsp;<i className={'fa fa-power-off' }
                                     style={{marginTop: '3px',fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                        </IconButton>

                        :

                        <IconButton
                            onClick={(event) => this.props.onOpenDialogForLoginSecondUser(event.currentTarget)}
                        >
                            &nbsp;<i className={'fa fa-lock' }
                                     style={{marginTop: '3px',fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                        </IconButton>

                )}


                </span></div>

            {this.props.children}
        </h2>;
    }
}

const mapStateToProps = (state: ReduxState, ownProps: Props & PropsFunc): {} => {
    return {
        user: anyUserAsAccountSelector(state, ownProps.userId, ownProps.userLabel, ownProps.userLogin)

    };
};
const mapDispatchToProps = (dispatch): ReduxPropsFunc => {
    return {
        onSecondUserLogout: (userId: number) => {
            dispatch(ON_LOGOUT_SECOND_USER.new({userId}));
        }
    }
};

export const TaskTableHeaderAccountPanel = connect(mapStateToProps, mapDispatchToProps)(TaskTableHeaderAccountPanelInternal);
