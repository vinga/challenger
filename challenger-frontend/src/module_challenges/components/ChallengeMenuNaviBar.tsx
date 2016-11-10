import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";
import IconButton from "material-ui/IconButton";
import FontIcon from "material-ui/FontIcon";
import Divider from "material-ui/Divider";
import {ChallengeStatus, VisibleChallengesDTO, ChallengeDTO} from "../ChallengeDTO";
import {incrementDayAction} from "../../redux/actions/dayActions";
const menuIconStyle = {fontSize: '15px', textAlign: 'center', lineHeight: '24px', height: '24px'};
import IconMenuProps = __MaterialUI.Menus.IconMenuProps;
import {selectedChallengeSelector} from "../challengeSelectors";
import {changeChallengeAction} from "../challengeActions";
import {CREATE_NEW_CHALLENGE} from "../challengeActionTypes";

interface Props {
    selectedChallengeLabel: string,
    visibleChallengesDTO: VisibleChallengesDTO,
    day: Date,


}

interface PropsFunc {
    onIncrementDayFunc: (amount: number)=> void;
    onChangeChallenge: (challengeId: number)=>void;
    onCreateNewChallenge: () => void;

}
class ChallengeMenuNaviBarInternal extends React.Component<Props & PropsFunc & {  style: IconMenuProps },void> {
    constructor(props) {
        super(props);
    }


    calculateChallengeStatusIcon(challengeDTO: ChallengeDTO) {
        var iconText;
        switch (challengeDTO.challengeStatus) {
            case ChallengeStatus.ACTIVE:
                iconText = null;
                break;
            case ChallengeStatus.WAITING_FOR_ACCEPTANCE:
                if (challengeDTO.creatorId == challengeDTO.myId)
                    iconText = "fa-hourglass";
                else
                    iconText = "fa-question";
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


    render() {
        return (<div>
            <IconButton onClick={()=>this.props.onIncrementDayFunc(-1)}>
                <FontIcon className="fa fa-caret-left white-text"/>
            </IconButton>

            {this.props.day.yyyy_mm_dd()}

            <IconButton onClick={()=>this.props.onIncrementDayFunc(1)}>
                <FontIcon className="fa fa-caret-right white-text"/>
            </IconButton>
            {this.props.selectedChallengeLabel}

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
                                      onTouchTap={()=>this.props.onChangeChallenge(ch.id)}
                                      primaryText={ch.label}/>)
                }
                {this.props.visibleChallengesDTO.visibleChallenges.length > 0 &&
                <Divider />
                }
                <MenuItem
                    leftIcon={<FontIcon
                    style={menuIconStyle}
                    className={"fa fa-plus-circle cyan-text"}/>}
                    primaryText="Create new challenge"
                    onTouchTap={()=>this.props.onCreateNewChallenge()}
                />
            </IconMenu>
        </div>);
    }
}

const mapStateToProps = (state: ReduxState): Props => {
    return {
        selectedChallengeLabel: selectedChallengeSelector(state) != null ? selectedChallengeSelector(state).label : "<not set>",
        visibleChallengesDTO: state.challenges,
        day: state.currentSelection.day
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
        onCreateNewChallenge: () => {
            dispatch(CREATE_NEW_CHALLENGE.new({}))
        }
    }
};

export const ChallengeMenuNaviBar = connect(mapStateToProps, mapDispatchToProps)(ChallengeMenuNaviBarInternal)
