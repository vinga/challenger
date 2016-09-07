import React, {Component} from "react";
import ChallengeActionTableUserIcon from "./ChallengeActionTableUserIcon"
import FlatButton from "material-ui/FlatButton";
import ChallengeEditDialogWindow from "../ChallengeEditDialogWindow";
import {ChallengeActionStatus} from "../Constants"
export default class ChallengeActionTableHeader extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showNewWindow: false
        };

    }


    calculateCheckedCount() {
        var doneCounter = 0;
        for (var i = 0; i < this.props.actionsList.length; i++) {
            if (this.props.actionsList[i].actionStatus == ChallengeActionStatus.done) {
                doneCounter += this.props.actionsList[i].difficulty;
            }
        }
        return doneCounter;
    }
    calculateAllCount() {
        var doneCounter = 0;
        for (var i = 0; i < this.props.actionsList.length; i++) {

            if (this.props.actionsList[i].actionStatus != ChallengeActionStatus.waiting_for_acceptance)
                doneCounter+=this.props.actionsList[i].difficulty;
        }
        return doneCounter;
    }

    onAddNewChallengeAction = () => {
        this.setState({showNewWindow: true});
       // this.refs.challengeEditDialogWindow.handleOpen();
    }
    handleEditWindowClose = () => {
        this.setState({showNewWindow: false});
    }

    render() {
        var col = '#00bcd4'; // cyan
        if (this.props.no == 0)
            col = '#ff9800'; // orange
        return (<div>
            <h5 className="center">
                <ChallengeActionTableUserIcon
                    iconId={this.props.no}
                    ctx={this.props.ctx}
                    userNo={this.props.no}/> {this.props.userName}

                <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>/
                <span>{this.calculateAllCount()}</span>
            </h5>

            <div className="right" style={{display: "inline-block"}}>
                <FlatButton
                    onClick={this.onAddNewChallengeAction}
                    label="Add task"
                    labelPosition="before"
                    primary={true}
                    style={{color: col}}
                /> {/* icon={<i className="fa fa-plus-circle" />}*/}
            </div>

            {this.state.showNewWindow &&
             <ChallengeEditDialogWindow open={this.state.showNewWindow}
                                        onClose={this.handleEditWindowClose}
                                        onChallengeActionSuccessfullyUpdated={this.props.onChallengeActionSuccessfullyUpdated}
                                        userId={this.props.userId}
                                        contractId={this.props.contractId}

             />
            }
        </div>);
    }

}