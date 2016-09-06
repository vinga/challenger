import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import Checkbox from "material-ui/Checkbox";
import FlatButton from "material-ui/FlatButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "./Constants";
import ChallengeEditDialogWindow from "./ChallengeEditDialogWindow";
import ChallengeActionTableUserIcon from "./ChallengeActionTableUserIcon";
import SecondUserAuthorizePopover from "./SecondUserAuthorizePopover";
import Paper from "material-ui/Paper";

var grepOne = function (array, func) {
    var result = $.grep(array, func);
    return result[0];
};




export default class ChallengeActionTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            userTableDTO: this.props.userTableDTO,
             contractId: this.props.ctx.contractId

        }
       // console.log("ChallengeActionTable is logged "+this.props.ctx.logged+" "+ this.props.ctx.selectedContract);
        if (this.props.ctx.logged && this.props.ctx.selectedContract!=undefined)
            this.loadChallengesFromServer();
    }

    componentWillReceiveProps(nextProps){
        console.log("ChallengeActionTable componentWillReceiveProps "+this.state.contractId+" "+nextProps.ctx.contractId);
        if (this.state.contractId!=nextProps.ctx.contractId) {
            this.state.contractId=nextProps.ctx.contractId;
            this.loadChallengesFromServer();
        }
    }

    loadChallengesFromServer = () => {
        $.ajax({
            url: this.props.ctx.baseUrl+ ((this.props.no==0)?"/challengeActionsForMe" : "/challengeActionsForOther")+"/"+this.props.ctx.selectedContract.id,
            headers: {
                "Authorization": "Bearer " + this.props.ctx.webToken
            }
        }).then(function(data) {
            this.state.userTableDTO.actionsList=data;

            this.setState({userTableDTO: this.state.userTableDTO});
        }.bind(this));
    }

    calculateCheckedCount() {
        var doneCounter = 0;
        for (var i = 0; i < this.state.userTableDTO.actionsList.length; i++) {
            if (this.state.userTableDTO.actionsList[i].actionStatus == 'Done')
                doneCounter++;
        }
        ;
        return doneCounter;
    }

    onChallengeActionStateChanged = (ev, complete, id) => {
        var result = grepOne(this.state.userTableDTO.actionsList, function (e) {
            return e.id == id;
        });
        //console.log('complete ' + complete + ' id ' + id);
        if (!this.state.userTableDTO.authorized) {
            this.state.userTableDTO.authorizePanel = true;
        }
        else if (complete)
            result.actionStatus = 'Done';
        else
            result.actionStatus = 'Pending';
        this.setState({
            userTableDTO: this.state.userTableDTO
        });
    }

    onAddNewChallengeAction = () => {
        this.refs.challengeEditDialogWindow.handleOpen();
       // var dialogExampleModalWindow = new ChallengeEditDialogWindowComponent({state: open});
        //dialogExampleModalWindow.state = 'open';
    }

    prepareDifficultyIcon = (action, icon) => {

        var background;

        var styleName = {fill: "#80deea", width: '40px', height: '40px'}; //cyan-lighten3
        if (this.props.userTableDTO.no == 0)
            styleName = {fill: "#ffcc80", width: '40px', height: '40px'};

        if (action.difficulty == 0)
            background = <DiffSimpleIcon className="fa-stack-2x" style={styleName}/>;
        else if (action.difficulty == 1)
            background = <DiffMediumIcon className="fa-stack-2x" style={styleName}/>;
        else
            background = <DiffHardIcon className="fa-stack-2x" style={styleName}/>;

        var icon;
        if (icon.startsWith("fa-")) {

            icon = <i className={'fa ' + icon + ' valign fa-stack-1x center-align'}
                      style={{paddingLeft: '12px'}}></i>;
        } else icon = <i className="material-icons valign fa-stack-1x center-align"
                         style={{paddingLeft: '8px'}}>{icon}</i>;
        return <div className="fa-stack valign-wrapper">{background}{icon}</div>;
    }


    prepareCheckbox(action) {
        var checkbox;
        if (action.actionStatus == "Failed")
            checkbox = "Failed";
        else {
            var onCheck = (actionId) => (event, isInputChecked) => {
                this.onChallengeActionStateChanged(event, isInputChecked, actionId);
                if (this.state.userTableDTO.authorizePanel)
                    this.refs.authPopover.setState({
                        anchorEl: event.currentTarget,
                        open: true
                    })
            };

            var iconCheckStyle = {fill: '#00bcd4'}; // cyan
            if (this.state.userTableDTO.no == 0)
                iconCheckStyle = {fill: '#ff9800'}; // orange
            else if (!this.state.userTableDTO.authorized)
                iconCheckStyle = {fill: 'lightgrey'};


            checkbox = [<Checkbox
                key="statusCb"
                checked={action.actionStatus === 'Done'}
                onCheck={onCheck(action.id)}
                iconStyle={iconCheckStyle}
                style={{display: 'inline-block', width: '30px'}}/>];
            //checkbox.push(<div>{action.actionStatus}</div>);
        }
        return checkbox;
    }

    render() {

        var popover = <SecondUserAuthorizePopover ref="authPopover" userName={this.state.userTableDTO.userName}/>
        var mCommentNodes = [];
        for (var i = 0; i < this.state.userTableDTO.actionsList.length; i++) {
            var action = this.state.userTableDTO.actionsList[i];
            //<span className="daysBadge2">1</span>
            mCommentNodes.push(<TableRow key={action.id}>

                    <TableRowColumn style={{
                        width: '40px',
                        padding: '0px'
                    }}>{this.prepareDifficultyIcon(action, action.icon)}</TableRowColumn>
                    <TableRowColumn style={{padding: '5px'}}>

                        {action.label}
                    </TableRowColumn>
                    <TableRowColumn style={{
                        width: '40px',
                        padding: '0px',
                        color: 'grey',
                        fontSize: '11px'
                    }}>{action.actionType}</TableRowColumn>
                    <TableRowColumn
                        style={{width: '40px', padding: '5px'}}>{this.prepareCheckbox(action)}</TableRowColumn>
                </TableRow>);
        }

        var col='#00bcd4'; // cyan
        if (this.state.userTableDTO.no == 0)
            col= '#ff9800'; // orange
        return (
            <div style={{marginRight:'10px',marginLeft:'10px', marginTop:'20px',marginBottom:'30px'}}>

                <h5 className="center">
                    <ChallengeActionTableUserIcon
                        iconId={this.props.userTableDTO.no}
                        ctx={this.props.ctx}
                        userNo={this.props.userTableDTO.no}/> {this.props.userName}

                    <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>/
                    <span>{this.props.userTableDTO.actionsList.length}</span>

                </h5>


                <div className="right" style={{display:"inline-block"}} >
                        <FlatButton
                            onClick={this.onAddNewChallengeAction}
                            label="Add task"
                            labelPosition="before"
                            primary={true}
                            style={{color: col}}

                        /> {/* icon={<i className="fa fa-plus-circle" />}*/}
                    </div>

                <Paper style={{padding:'10px', display:"inline-block"}}>



                <Table selectable={false}
                       fixedHeader={true}
height={Math.max(document.documentElement.clientHeight, window.innerHeight || 0)-400+"px"}
                >
                    <TableBody displayRowCheckbox={false}>
                        {mCommentNodes}
                    </TableBody>
                </Table>
                </Paper>
                <ChallengeEditDialogWindow ref="challengeEditDialogWindow"/>
                {popover}
            </div>
        );
    }
}



