import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {EventDTO} from "../EventDTO";
import {Dialog, FlatButton} from "material-ui";
import {Row, Col} from "../../views/common-components/Flexboxgrid";
import {SHOW_GLOBAL_NOTIFICATIONS_DIALOG} from "../eventActionTypes";
import {markGlobalEventsAsRead} from "../eventActions";
import {waitingForMyAcceptanceChallengeSelector} from "../../module_challenges/challengeSelectors";
import {ChallengeDTO} from "../../module_challenges/ChallengeDTO";
import ChallengeAcceptRejectMessageItem from "../../module_challenges/components/ChallengeAcceptRejectMessageItem";
import {Divider} from "material-ui";


interface Props {
    globalNotifications: EventDTO[],
    challengesWaitingForMyAcceptance: ChallengeDTO[]
}


interface PropsFunc {
    onCloseFunc: (globalNotifications: EventDTO[])=>void

}


class GlobalNotificationsPanelInternal extends React.Component<Props & PropsFunc,void> {
    constructor(props) {
        super(props);
    }



    handleClose = () => {
        this.props.onCloseFunc(this.props.globalNotifications);
    }

    render() {


        return <div>
            <Dialog
                actions={<FlatButton
                            label="Close"
                            primary={false}
                            onTouchTap={this.handleClose}
                         />}
                modal={true}
                open={true}
                style={{height: "600px", display: "block"}}
                title="Messages"
            >
                <div style={{height:"300px",overflowY: "auto"}}>
                {this.props.challengesWaitingForMyAcceptance.map(challenge =>
                        [<ChallengeAcceptRejectMessageItem challenge={challenge}/>,
                            <Divider style={{margin:"20px"}}/>]
                )}

                {this.props.globalNotifications.map((e: EventDTO)=>
                    [<Row key={e.id}><Col>{e.content} </Col></Row>,
                    <Divider style={{margin:"20px"}}/>]
                )}
                </div>

            </Dialog></div>;
    }
}

const mapStateToProps = (state: ReduxState): Props => {
    return {
        globalNotifications: state.eventsState.globalUnreadEvents,
        challengesWaitingForMyAcceptance: waitingForMyAcceptanceChallengeSelector(state)

    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onCloseFunc: (globalNotifications: EventDTO[])=> {
            dispatch(SHOW_GLOBAL_NOTIFICATIONS_DIALOG.new({show: false}))
            //dispatch(markGlobalEventsAsRead(globalNotifications));
        }
    }
};

export const GlobalNotificationsPanel = connect(mapStateToProps, mapDispatchToProps)(GlobalNotificationsPanelInternal)
