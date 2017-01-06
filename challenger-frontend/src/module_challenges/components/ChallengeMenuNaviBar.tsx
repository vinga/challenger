import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";
import IconButton from "material-ui/IconButton";
import FontIcon from "material-ui/FontIcon";
import Divider from "material-ui/Divider";
import {ChallengeStatus, VisibleChallengesDTO, ChallengeDTO} from "../ChallengeDTO";
import {incrementDayAction} from "../../redux/actions/dayActions";
import {selectedChallengeSelector} from "../challengeSelectors";
import {changeChallengeAction, acceptOrRejectChallenge} from "../challengeActions";
import {CREATE_NEW_CHALLENGE} from "../challengeActionTypes";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import {YesNoConfirmationDialog} from "../../views/common-components/YesNoConfirmationDialog";
import {UnreadNotificationsList} from "../../module_events/EventDTO";
import {Badge} from "material-ui";
const menuIconStyle = {fontSize: '15px', textAlign: 'center', lineHeight: '24px', height: '24px'};
import IconMenuProps = __MaterialUI.Menus.IconMenuProps;
import _ = require("lodash");

interface Props {
    selectedChallengeId: number,
    selectedChallengeLabel: string,
    visibleChallengesDTO: VisibleChallengesDTO,
    day: Date,
    creatorLabel: string
    loggedUserId: number,
    unreadNotifications: UnreadNotificationsList,
    totalUnreadNotifications: number

}

interface PropsFunc {
    onIncrementDayFunc: (amount: number)=> void;
    onChangeChallenge: (challengeId: number)=>void;
    onCreateNewChallenge: (creatorLabel: string) => void;
    onAcceptRejectChallenge: (challengeId: number, accept: boolean) => void

}

interface State {
    showConfirmDialog: boolean,
    confirmingChallenge?: ChallengeDTO
}


class ChallengeMenuNaviBarInternal extends React.Component<Props & PropsFunc & {  style: IconMenuProps },State> {
    constructor(props) {
        super(props);
        this.state = {
            showConfirmDialog: false
        }
    }

    onChangeChallengeHanlder = (challenge: ChallengeDTO) => {
        /*      if (challenge.userLabels.some(ul=> ul.id == this.props.loggedUserId && ul.challengeStatus == ChallengeStatus.WAITING_FOR_ACCEPTANCE)) {
         this.setState({
         showConfirmDialog: true,
         confirmingChallenge: challenge
         });
         } else*/
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

    getLabel = (ch: ChallengeDTO) => {
        var un = this.props.unreadNotifications[ch.id];
        if (un != null && un>0 && ch.id!=this.props.selectedChallengeId && this.props.selectedChallengeId!=-1)
            return <div>{ch.label}<Badge
                badgeContent={un}
                primary={true}
                badgeStyle={{top: 17, right: 5}}
            /></div>;
        return ch.label;
    }

    render() {

        return (<div style={{display:"flex"}}>
            <IconButton onClick={()=>this.props.onIncrementDayFunc(-1)}>
                <FontIcon className="fa fa-caret-left white-text"/>
            </IconButton>

            <div style={{paddingTop:"5px"}}>{this.props.day.dayMonth3()}</div>

            <IconButton onClick={()=>this.props.onIncrementDayFunc(1)}>
                <FontIcon className="fa fa-caret-right white-text"/>
            </IconButton>


            <div style={{paddingTop:"5px"}}>{this.props.selectedChallengeLabel}</div>



            <IconMenu style={this.props.style}
                      iconButtonElement={<IconButton> <FontIcon
                         className="fa fa-reorder white-text"/></IconButton>}
                      anchorOrigin={{horizontal: 'left', vertical: 'top'}}
                      targetOrigin={{horizontal: 'left', vertical: 'top'}}
            >
                {
                    this.props.visibleChallengesDTO.visibleChallenges.map(
                        ch =>
                            <MenuItem key={ch.id}
                                      rightIcon={this.calculateChallengeStatusIcon(ch)}
                                      onTouchTap={()=>this.onChangeChallengeHanlder(ch)}
                                      primaryText={this.getLabel(ch)}/>)
                }
                {this.props.visibleChallengesDTO.visibleChallenges.length > 0 &&
                <Divider />
                }
                <MenuItem
                    leftIcon={<FontIcon
                    style={menuIconStyle}
                    className={"fa fa-plus-circle cyan-text"}/>}
                    primaryText="Create new challenge"
                    onTouchTap={()=>this.props.onCreateNewChallenge(this.props.creatorLabel)}
                />
            </IconMenu>


            {this.props.totalUnreadNotifications > 0 ?
                <div >
                    <Badge
                        badgeContent={this.props.totalUnreadNotifications}
                        primary={true}
                        badgeStyle={{top: 7, right: 5}}
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
    _.forOwn(state.eventsState.unreadNotifications, function (value, key) {

        if (value != null  && ""+state.challenges.selectedChallengeId!=key)
            count += value;
    });
    return {
        selectedChallengeId: state.challenges.selectedChallengeId,
        selectedChallengeLabel: selectedChallengeSelector(state) != null ? selectedChallengeSelector(state).label : "<not set>",
        visibleChallengesDTO: state.challenges,
        day: state.currentSelection.day,
        creatorLabel: loggedUserSelector(state).login,
        loggedUserId: loggedUserSelector(state).id,
        unreadNotifications: state.eventsState.unreadNotifications,
        totalUnreadNotifications: count

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
        }
    }
};

export const ChallengeMenuNaviBar = connect(mapStateToProps, mapDispatchToProps)(ChallengeMenuNaviBarInternal)
