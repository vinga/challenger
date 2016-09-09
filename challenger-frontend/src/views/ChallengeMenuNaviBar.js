import React, {Component} from "react";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";
import IconButton from "material-ui/IconButton/IconButton";
import FontIcon from "material-ui/FontIcon";
import {ChallengeStatus} from "./Constants";
import Divider from "material-ui/Divider";
import ajaxWrapper from "../logic/AjaxWrapper";

const menuIconStyle = {fontSize: '15px', textAlign: 'center', lineHeight: '24px', height: '24px'};

export default class ChallengeMenuNaviBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: "",//this.calculateTitle(this.props.selectedChallengeId),
            visibleChallengesDTO: {
                selectedChallengeId: -1,
                visibleChallenges: []
            }
        };
        this.loadChallengesFromServer();
    }

    loadChallengesFromServer = () => {
        ajaxWrapper.loadVisibleChallenges(
             (data) => {
                this.state.visibleChallengesDTO = data;
                this.setState(this.state);
                this.onChallengeSelected(this.state.visibleChallengesDTO.selectedChallengeId);
            });
    }

    calculateChallengeStatusIcon(challengeDTO) {
        var iconText;
        switch (challengeDTO.challengeStatus) {
            case ChallengeStatus.ACTIVE:
                iconText = null;
                break;
            case ChallengeStatus.WAITING_FOR_ACCEPTANCE:
                if (challengeDTO.firstUserId == challengeDTO.myId)
                    iconText = "fa-hourglass";
                else
                    iconText = "fa-question";
                break;
            case ChallengeStatus.REFUSED:
                iconText = "fa-cancel";
                break;
            case ChallengeStatus.INACIVE:
                throw "Not supported here";
                break;
        }


        if (iconText != undefined)
            return <FontIcon
                style={menuIconStyle}
                className={"fa " + iconText + " cyan-text"}/>
        else return null;

    }


    calculateTitle(challengeId) {
       var rres =this.state.visibleChallengesDTO.visibleChallenges.find(ch=>ch.id==challengeId);
       return rres!=undefined? rres.label: "<not set>";
    }

    onChallengeSelected = (challengeId) => {
        var changed = true;//this.state.visibleChallengesDTO.selectedChallengeId!=challengeId;
        this.state.visibleChallengesDTO.selectedChallengeId = challengeId;
        this.setState(this.state);
        if (changed) {
            var selectedChallenge=this.state.visibleChallengesDTO.visibleChallenges.find(ch=>ch.id==challengeId);
            if (selectedChallenge !== undefined)
                this.props.onSelectedChallengeChanged(selectedChallenge);
        }
    }


    render() {
        var rows = [];


        return ( <div>{this.calculateTitle(this.state.visibleChallengesDTO.selectedChallengeId)}

            <IconMenu style={this.props.style}

                      iconButtonElement={<IconButton> <FontIcon
                          className="fa fa-reorder white-text"/></IconButton>}
                      anchorOrigin={{horizontal: 'left', vertical: 'top'}}
                      targetOrigin={{horizontal: 'left', vertical: 'top'}}
            >

                {
                    this.state.visibleChallengesDTO.visibleChallenges.map(
                        ch =>
                            <MenuItem key={ch.id}
                                      rightIcon={this.calculateChallengeStatusIcon(ch)}
                                      onTouchTap={()=>this.onChallengeSelected(ch.id)}
                                      primaryText={ch.label}/>)
                }
                {this.state.visibleChallengesDTO.visibleChallenges.length > 0 &&
                <Divider />
                }
                <MenuItem
                    leftIcon={<FontIcon
                        style={menuIconStyle}
                        className={"fa fa-plus-circle cyan-text"}/>}

                    primaryText="Create new challenge"/>
            </IconMenu></div>);
    }


}
