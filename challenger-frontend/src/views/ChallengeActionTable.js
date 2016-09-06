import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import FlatButton from "material-ui/FlatButton";
import ChallengeEditDialogWindow from "../ChallengeEditDialogWindow";
import ChallengeActionTableUserIcon from "../ChallengeActionTableUserIcon";
import SecondUserAuthorizePopover from "../SecondUserAuthorizePopover";
import Paper from "material-ui/Paper";
import ajaxWrapper from "../presenters/AjaxWrapper";
import DifficultyIconButton from "./DifficultyIconButton";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox";

const iconStyle={
    width: '50px',
    padding: '0px'
};
const labelStyle={
  padding: '5px'
};
const actionTypeStyle={
    width: '40px',
        padding: '0px',
        color: 'grey',
        fontSize: '11px'
};

export default class ChallengeActionTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            actionsList: [],
            authorized: this.props.no == 0,
            authorizePanel: false
        }
        //console.log("ChallengeActionTable is logged "+this.props.ctx.logged+" "+ this.props.ctx.selectedContract);
    }

    componentWillReceiveProps(nextProps) {
        // console.log("ChallengeActionTable componentWillReceiveProps "+this.props.selectedContract.id+" "+nextProps.selectedContract.id);
        if (this.props.selectedContract == null || this.props.selectedContract.id != nextProps.selectedContract.id) {
            this.loadChallengeActionsFromServer(nextProps.selectedContract);
        }
    }

    loadChallengeActionsFromServer = (contract) => {
        ajaxWrapper.loadChallengeActionsFromServer(contract.id, this.props.no,
            (data)=> {
                this.state.actionsList = data;
                this.setState(this.state);
            }
        )
    }

    calculateCheckedCount() {
        var doneCounter = 0;
        for (var i = 0; i < this.state.actionsList.length; i++) {
            if (this.state.actionsList[i].actionStatus == 'Done')
                doneCounter++;
        }
        return doneCounter;
    }


    onAddNewChallengeAction = () => {
        this.refs.challengeEditDialogWindow.handleOpen();
    }

    onActionCheckedStateChanged = () => {
        this.setState(this.state);
    }

    showAuthorizePanel = (anchor, isInputChecked) => {
        if (!this.state.authorized) {
            this.state.authorizePanel = true;
        }
        if (this.state.authorizePanel) {
            this.refs.authPopover.setState({
                anchorEl: anchor,
                open: true
            });
            return true;
        }
        return false;
    }

    render() {
        var col = '#00bcd4'; // cyan
        if (this.props.no == 0)
            col = '#ff9800'; // orange
        return (
            <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '30px'}}>
                <h5 className="center">
                    <ChallengeActionTableUserIcon
                        iconId={this.props.no}
                        ctx={this.props.ctx}
                        userNo={this.props.no}/> {this.props.userName}

                    <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>/
                    <span>{this.state.actionsList.length}</span>
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

                <Paper style={{padding: '10px', display: "inline-block"}}>
                    <Table selectable={false}
                           fixedHeader={true}
                           height={Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 400) + "px"}>
                        <TableBody displayRowCheckbox={false}>
                            { this.state.actionsList.map(action =>
                                    <TableRow key={action.id}>
                                        <TableRowColumn style={iconStyle}>
                                            <DifficultyIconButton
                                                no={this.props.no}
                                                difficulty={action.difficulty}
                                                icon={action.icon}/>
                                        </TableRowColumn>
                                        <TableRowColumn style={labelStyle}>
                                            {action.label}
                                        </TableRowColumn>
                                        <TableRowColumn style={actionTypeStyle}>
                                            {action.actionType}
                                        </TableRowColumn>
                                        <TableRowColumn  style={{width: '45px', padding: '10px'}}>
                                            <ChallengeTableCheckbox
                                                no={this.props.no}
                                                action={action}
                                                showAuthorizePanel={this.showAuthorizePanel}
                                                onActionCheckedStateChanged={this.onActionCheckedStateChanged}
                                            />
                                        </TableRowColumn>
                                    </TableRow>
                                )}
                        </TableBody>
                    </Table>
                </Paper>
                <ChallengeEditDialogWindow ref="challengeEditDialogWindow"/>
                <SecondUserAuthorizePopover ref="authPopover" userName={this.props.userName}/>
            </div>
        );
    }
}



