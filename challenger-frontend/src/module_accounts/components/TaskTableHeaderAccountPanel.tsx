import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {AccountDTO} from "../AccountDTO";
import {ON_LOGOUT_SECOND_USER} from "../accountActionTypes";
import TaskTableUserIcon from "./TaskTableUserIcon";
import {IconButton} from "material-ui";
import {anyUserAsAccountSelector} from "../accountSelectors";


interface Props {
    userId: number,
    no: number,
    userLabel: string,
    userLogin: string
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
        return <h5>
            <TaskTableUserIcon
                userNo={this.props.no}
            />

            <span style={{}}><span style={{lineHeight: '65px'}}>{this.props.user.label}</span>
                {this.props.no != 0 && (this.props.user.jwtToken != null ?

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
<div style={{display:"block", float:"right"}}>
                {this.props.children}
    </div>
                {this.props.user.errorDescription != null &&
                <span className="red-text text-darken-3"
                      style={{fontSize:'15px'}}>
                        {this.props.user.errorDescription}</span>}
                </span>

        </h5>;
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
