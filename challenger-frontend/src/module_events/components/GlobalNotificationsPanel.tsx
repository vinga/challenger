import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {EventDTO, EventType} from "../EventDTO";
import {Dialog, FlatButton, Divider, IconButton, FontIcon} from "material-ui";
import {Row, Col} from "../../views/common-components/Flexboxgrid";
import {SHOW_GLOBAL_NOTIFICATIONS_DIALOG} from "../eventActionTypes";
import {markGlobalEventsAsRead} from "../eventActions";
import {waitingForMyAcceptanceChallengeSelector} from "../../module_challenges/challengeSelectors";
import {ChallengeDTO} from "../../module_challenges/ChallengeDTO";
import ChallengeAcceptRejectMessageItem from "../../module_challenges/components/ChallengeAcceptRejectMessageItem";


interface Props {
    globalNotifications: EventDTO[],
    challengesWaitingForMyAcceptance: ChallengeDTO[]
}


interface PropsFunc {
    onCloseFunc: (globalNotifications: EventDTO[]) => void
    markAsRead: (ev: EventDTO) => void

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
                actions={[<FlatButton
                            label="Close"
                            primary={false}
                            onTouchTap={this.handleClose}
                         />]}
                modal={true}
                open={true}
                style={{height: "600px", display: "block"}}
                title="Messages"
            >
                <div style={{height:"300px",overflowY: "auto"}}>
                    {this.props.challengesWaitingForMyAcceptance.map(challenge =>
                        [
                            <ChallengeAcceptRejectMessageItem
                            key={challenge.id}
                            challenge={challenge}/>
                            ,
                            <Divider style={{margin:"10px"}}/>
                        ]
                    )}

                    {this.props.globalNotifications.map((e: EventDTO) =>
                        [<Row key={e.id}>
                            <Col col="11" style={{ marginTop:"15px", marginBottom: "10px"}}>

                                <div style={{ fontSize:"10px",  borderRight:
                        '10px solid transparent',marginRight:'20px', boxSizing: "border-box"}}>{new Date(e.sentDate).yy_mm_dd()}</div>
                                <div>
                                {e.content}
                                </div>
                                </Col>
                            <Col style={{textAlign:"right", marginRight:"10px"}}>
                                <IconButton
                                    style={{margin:0}}
                                    size={5}
                                    iconStyle={{fontSize:"12px", margin:0, }}
                                    onClick={()=>{this.props.markAsRead(e)}}>
                                    <FontIcon
                                        color="grey"
                                        className={"fa fa-times"}/>
                                </IconButton>
                            </Col>
                        </Row>,
                            <Divider key={"d"+e.id} />]
                    )}
                </div>

            </Dialog></div>;
    }
}

const mapStateToProps = (state: ReduxState): Props => {

/*    var arr: EventDTO[]=[]
    for (let i=0; i<10; i++) {
        arr.push({id:i, challengeId: 0, content: "test",authorId:1, sentDate: new Date().getTime(), forDay:new Date().getTime(), taskId: null, eventType: EventType.REMOVE_CHALLENGE})
    }*/
    return {
        globalNotifications: state.eventsState.globalUnreadEvents,//.concat(arr),
        challengesWaitingForMyAcceptance: waitingForMyAcceptanceChallengeSelector(state)

    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onCloseFunc: (globalNotifications: EventDTO[]) => {
            dispatch(SHOW_GLOBAL_NOTIFICATIONS_DIALOG.new({show: false}))
            //dispatch(markGlobalEventsAsRead(globalNotifications));
        },
        markAsRead: (ev: EventDTO) => {
            dispatch(markGlobalEventsAsRead([ev]));
        }
    }
};

export const GlobalNotificationsPanel = connect(mapStateToProps, mapDispatchToProps)(GlobalNotificationsPanelInternal)
