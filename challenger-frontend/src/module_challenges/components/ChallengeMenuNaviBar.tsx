import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";
import IconButton from "material-ui/IconButton";
import FontIcon from "material-ui/FontIcon";
import Divider from "material-ui/Divider";
import {ChallengeStatus, ChallengeDTO, NO_CHALLENGES_LOADED_YET} from "../ChallengeDTO";
import {incrementDayAction} from "../../redux/actions/dayActions";
import {selectedChallengeSelector, waitingForMyAcceptanceChallengeSelector, acceptedByMeChallengeSelector} from "../challengeSelectors";
import {changeChallengeAction, acceptOrRejectChallenge} from "../challengeActions";
import {CREATE_NEW_CHALLENGE, EDIT_CHALLENGE} from "../challengeActionTypes";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import {YesNoConfirmationDialog} from "../../views/common-components/YesNoConfirmationDialog";
import {UnreadNotificationsList} from "../../module_events/EventDTO";
import {Badge} from "material-ui";
import {SHOW_GLOBAL_NOTIFICATIONS_DIALOG} from "../../module_events/eventActionTypes";
import IconMenuProps = __MaterialUI.Menus.IconMenuProps;
import _ = require("lodash");

interface Props {
    selectedChallengeId: number,
    selectedChallengeLabel: string,
    acceptedChallenges: ChallengeDTO[]
    day: Date,
    creatorLabel: string
    loggedUserId: number,
    unreadNotifications: UnreadNotificationsList,
    totalNotifications: number,
    totalGlobalUnreadNotifications: number
}

interface PropsFunc {
    onIncrementDayFunc: (amount: number)=> void;
    onChangeChallenge: (challengeId: number)=>void;
    onCreateNewChallenge: (creatorLabel: string) => void;
    onEditChallenge: (challengeId: number, creatorLabel: string) => void;
    onAcceptRejectChallenge: (challengeId: number, accept: boolean) => void;
    onShowNotificationPanel: () => void;

}

interface State {
    showConfirmDialog: boolean,
    confirmingChallenge?: ChallengeDTO
}

const menuIconStyle = {fontSize: '15px', textAlign: 'center', lineHeight: '24px', height: '24px'};
const badgeCssStyle = {padding:"6px 12px 12px 24px"};


class ChallengeMenuNaviBarInternal extends React.Component<Props & PropsFunc & {  style: IconMenuProps },State> {
    constructor(props) {
        super(props);
        this.state = {
            showConfirmDialog: false
        }
    }

    onChangeChallengeHanlder = (challenge: ChallengeDTO) => {
        this.props.onChangeChallenge(challenge.id);
    }
    handleConfirmChallenge = () => {
        this.props.onAcceptRejectChallenge(this.state.confirmingChallenge.id, true);
        this.handleCloseConfirmDialog();
    }
    handleRejectChallenge = () => {
        this.props.onAcceptRejectChallenge(this.state.confirmingChallenge.id, false);
        this.handleCloseConfirmDialog();
    }

    handleCloseConfirmDialog = () => {
        this.setState({
            showConfirmDialog: false
        });

    }

    calculateChallengeStatusIcon(challengeDTO: ChallengeDTO) {
        var iconText;
        switch (challengeDTO.challengeStatus) {
            case ChallengeStatus.ACTIVE:
                iconText = null;
                break;
            case ChallengeStatus.WAITING_FOR_ACCEPTANCE:
                if (challengeDTO.userLabels.some(ul=> ul.id == this.props.loggedUserId && ul.challengeStatus == ChallengeStatus.WAITING_FOR_ACCEPTANCE))
                    iconText = "fa-question";
                else
                    iconText = "fa-hourglass-half";
                break;
            case ChallengeStatus.REFUSED:
                iconText = "fa-cancel";
                break;
            case ChallengeStatus.INACTIVE:
                throw "Not supported here";
        }
        if (iconText != undefined)
            return <FontIcon
                style={menuIconStyle}
                className={"fa " + iconText + " cyan-text"}/>;
        else return null;

    }

    getLabel = (ch: ChallengeDTO):any => {
        var un = this.props.unreadNotifications[ch.id];
        if (un != null && un.length > 0 && ch.id != this.props.selectedChallengeId && this.props.selectedChallengeId != -1)
            return <div>{ch.label}<Badge
                badgeContent={un.length}
                primary={true}
                style={badgeCssStyle}
            /></div>;
        return ch.label;
    }



    render() {



        return (<div style={{display:"flex"}}>
            <IconButton onClick={()=>this.props.onIncrementDayFunc(-1)} >
                <FontIcon className="fa fa-caret-left white-text"/>
            </IconButton>

            <div style={{paddingTop:"5px"}}>{this.props.day.dayMonth3()}</div>

            <IconButton onClick={()=>this.props.onIncrementDayFunc(1)}>
                <FontIcon className="fa fa-caret-right white-text"/>
            </IconButton>


            <div style={{paddingTop:"5px"}}>{this.props.selectedChallengeLabel}</div>


            <IconMenu
                      iconButtonElement={<IconButton > <FontIcon
                         className="fa fa-reorder white-text"/></IconButton>}
                      anchorOrigin={{horizontal: 'left', vertical: 'top'}}
                      targetOrigin={{horizontal: 'left', vertical: 'top'}}
            >
                {
                    this.props.acceptedChallenges.map(
                        ch =>
                            <MenuItem key={ch.id}
                                      rightIcon={this.calculateChallengeStatusIcon(ch)}
                                      onTouchTap={()=>this.onChangeChallengeHanlder(ch)}
                                      primaryText={this.getLabel(ch)}/>)
                }


                { (this.props.totalGlobalUnreadNotifications || this.props.acceptedChallenges.length > 0) &&
                <Divider />

                }
                {this.props.totalGlobalUnreadNotifications > 0 && [
                    <MenuItem
                        key="globalUnreadNotifs"
                        leftIcon={<FontIcon
                                    style={menuIconStyle}
                                    className={"fa fa-envelope cyan-text"}/>}
                        primaryText={<div>
                                        Notifications
                                        <Badge
                                            badgeContent={this.props.totalGlobalUnreadNotifications}
                                            primary={true}
                                            style={badgeCssStyle}
                                        />
                                     </div>}
                        onTouchTap={this.props.onShowNotificationPanel}
                    />,

                    <Divider key="divider1"/>
                ]}

                {this.props.selectedChallengeId != NO_CHALLENGES_LOADED_YET && this.props.selectedChallengeId != null && [
                    <MenuItem
                        key="challengeDetails"
                        leftIcon={<FontIcon
                                style={menuIconStyle}
                                className={"fa fa-info-circle cyan-text"}/>}
                        primaryText="Challenge details"
                        onTouchTap={()=>this.props.onEditChallenge(this.props.selectedChallengeId, this.props.creatorLabel)}
                    />,
                    <Divider key="divider2" />]
                }
                <MenuItem
                    key="newChallenge"
                    leftIcon={<FontIcon
                    style={menuIconStyle}
                    className={"fa fa-plus-circle cyan-text"}/>}
                    primaryText="Create new challenge"
                    onTouchTap={()=>this.props.onCreateNewChallenge(this.props.creatorLabel)}
                />


            </IconMenu>


            {(this.props.totalNotifications > 0) ?
                <div >
                    <Badge
                        badgeContent={this.props.totalNotifications}
                        primary={true}
                        style={badgeCssStyle}

                    />
                </div>
                :  <div style={{minWidth:"36px"}}/>}

            {this.state.showConfirmDialog &&
            <YesNoConfirmationDialog
                closeYes={this.handleConfirmChallenge}
                closeNo={this.handleRejectChallenge}
                closeDialog={this.handleCloseConfirmDialog}
                showCancel={true}
                width="600px"
            >
                Do you accept challenge <span style={{fontSize: '22px'}}>{this.state.confirmingChallenge.label}</span> ?

            </YesNoConfirmationDialog> }

        </div>);
    }
}

const mapStateToProps = (state: ReduxState): Props => {

    var count = 0;
    acceptedByMeChallengeSelector(state).forEach(ch=> {
        var value=state.eventsState.unreadNotifications[ch.id]
        if (value != null && state.challenges.selectedChallengeId != ch.id)
            count += value.length;
    })


    return {
        selectedChallengeId: state.challenges.selectedChallengeId,
        selectedChallengeLabel: selectedChallengeSelector(state) != null ? selectedChallengeSelector(state).label : "",
        acceptedChallenges: acceptedByMeChallengeSelector(state),
        day: state.currentSelection.day,
        creatorLabel: loggedUserSelector(state).login,
        loggedUserId: loggedUserSelector(state).id,


        totalNotifications: count+state.eventsState.globalUnreadEvents.length+waitingForMyAcceptanceChallengeSelector(state).length,
        totalGlobalUnreadNotifications: state.eventsState.globalUnreadEvents.length+waitingForMyAcceptanceChallengeSelector(state).length,
        unreadNotifications: state.eventsState.unreadNotifications,


    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onChangeChallenge: (challengeId: number) => {
            dispatch(changeChallengeAction(challengeId))
        },
        onIncrementDayFunc: (amount: number) => {
            dispatch(incrementDayAction(amount))
        },
        onCreateNewChallenge: (creatorLabel: string) => {
            dispatch(CREATE_NEW_CHALLENGE.new({creatorLabel: creatorLabel}))
        },
        onAcceptRejectChallenge: (challengeId: number, accept: boolean) => {
            dispatch(acceptOrRejectChallenge(challengeId, accept))
        },
        onEditChallenge: (challengeId: number, creatorLabel: string) => {
            dispatch(EDIT_CHALLENGE.new({challengeId: challengeId}))
        },
        onShowNotificationPanel: () => {
            dispatch(SHOW_GLOBAL_NOTIFICATIONS_DIALOG.new({show: true}));
        }
    }
};

export const ChallengeMenuNaviBar = connect(mapStateToProps, mapDispatchToProps)(ChallengeMenuNaviBarInternal)
