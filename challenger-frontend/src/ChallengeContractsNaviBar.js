import React, {Component} from "react";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";
import IconButton from "material-ui/IconButton/IconButton";
import FontIcon from "material-ui/FontIcon";
import {ChallengeStatus} from "./Constants";
import Divider from 'material-ui/Divider';


const menuIconStyle = {fontSize:'15px',   textAlign: 'center',    lineHeight: '24px'};

export default class ChallengeContractsNaviBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: "",//this.calculateTitle(this.props.selectedContractId),
            visibleContractsDTO: {
                selectedContractId: -1,
                visibleChallenges: []
            }
        };
        this.loadChallengesFromServer();
    }

    loadChallengesFromServer = () => {

        $.ajax({
            url: this.props.ctx.baseUrl + "/visibleChallenges",
            headers: {
                "Authorization": "Bearer " + this.props.ctx.webToken
            }
        }).then(function (data) {
            this.state.visibleContractsDTO = data;

            this.setState(this.state);
            this.onContractSelected(this.state.visibleContractsDTO.selectedContractId);
        }.bind(this));

    }

    calculateChallengeStatusIcon(challengeContractDTO) {
        var iconText;
        switch (challengeContractDTO.challengeContractStatus) {
            case ChallengeStatus.ACTIVE:
                iconText=null;
                break;
            case ChallengeStatus.WAITING_FOR_ACCEPTANCE:
                if (challengeContractDTO.firstUserId==challengeContractDTO.myId)
                    iconText= "fa-hourglass";
                else
                    iconText= "fa-question";
                break;
            case ChallengeStatus.REFUSED:
                iconText= "fa-cancel";
                break;
            case ChallengeStatus.INACIVE:
                throw "Not supported here";
                break;
        }


        if (iconText!=undefined)
            return <FontIcon
            style={menuIconStyle}
            className={"fa "+iconText+" cyan-text"}/>
        else return null;

    }



    calculateTitle(contractId) {
        for (var i = 0; i < this.state.visibleContractsDTO.visibleChallenges.length; i++) {
            if (this.state.visibleContractsDTO.visibleChallenges[i].id == contractId) {
                return this.state.visibleContractsDTO.visibleChallenges[i].label;
            }
        }
        return "<not set>";
    }

    onContractSelected = (contractId) => {
        var changed = true;//this.state.visibleContractsDTO.selectedContractId!=contractId;
        this.state.visibleContractsDTO.selectedContractId = contractId;
        this.setState(this.state);
        if (changed) {
            var selectedContract;
            for (var i = 0; i < this.state.visibleContractsDTO.visibleChallenges.length; i++) {
                if (this.state.visibleContractsDTO.visibleChallenges[i].id == contractId) {
                    selectedContract = this.state.visibleContractsDTO.visibleChallenges[i];
                    break;
                }
            }
            this.props.onSelectedContractChanged(selectedContract);
        }
    }


    render() {
        var rows = [];


        return ( <div>{this.calculateTitle(this.state.visibleContractsDTO.selectedContractId)}

            <IconMenu style={this.props.style}

                      iconButtonElement={<IconButton> <FontIcon
                          className="fa fa-reorder white-text"/></IconButton>}
                      anchorOrigin={{horizontal: 'left', vertical: 'top'}}
                      targetOrigin={{horizontal: 'left', vertical: 'top'}}
            >

                {
                    this.state.visibleContractsDTO.visibleChallenges.map(
                        ch =>
                            <MenuItem key={ch.id}
                                      rightIcon={this.calculateChallengeStatusIcon(ch)}
                                      onTouchTap={()=>this.onContractSelected(ch.id)}
                                      primaryText={ch.label}/>)
                }
                {this.state.visibleContractsDTO.visibleChallenges.length>0 &&
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
