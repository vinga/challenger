import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import Checkbox from "material-ui/Checkbox";
import FlatButton from "material-ui/FlatButton";
import Popover from "material-ui/Popover";
import TextField from "material-ui/TextField";
import ChallengeIcon from "./CommonComponents";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "./Constants";





import ReactDOM from 'react-dom';

var grepOne = function (array, func) {
    var result = $.grep(array, func);
    return result[0];
};


export default class SecondUserAuthorizePopover extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false
        };
    }

    handleTouchTap = (event) => {
        // This prevents ghost click.
        event.preventDefault();

        this.setState({
            open: true,
            anchorEl: event.currentTarget,
        });
    };

    handleRequestClose = () => {
        this.setState({
            open: false,
        });
    };

    componentDidUpdate(){
        //console.log("FOCUS NOW");
        //ReactDOM.findDOMNode(this.refs.passInput).focus();
    }

    render() {
        return (<Popover
            open={this.state.open}
            anchorEl={this.state.anchorEl}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'left', vertical: 'top'}}
            onRequestClose={this.handleRequestClose}
            ref="popover"
        >
            <div className="margined10">
                <div>
                    <ChallengeIcon icon="fa-key" style={{marginRight:'5px'}}/>
                    Please authorize as <b>{this.props.userName}</b>:
                </div>

                <div style={{display: 'block'}}>
                    <TextField
                        autoFocus
                        className="noShadow"
                        hintText="Password Field"
                        floatingLabelText="Password"
                        type="password"
                    />
                    <br/>
                </div>

                <div style={{marginBottom: '20px'}}>


                    <FlatButton
                        className="right"
                        onTouchTap={this.handleRequestClose}
                        label="Cancel"
                    />
                    <FlatButton
                        className="right"
                        onTouchTap={this.handleRequestClose}
                        label="OK"
                    />
                </div>
            </div>
        </Popover>);
    }
}


export default class ChallengeActionTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            userTableDTO: this.props.userTableDTO
        }
        this.loadCommentsFromServer();
    }

// this.toggleChecked = this.toggleChecked.bind(this);
    // tick = () => {
    //  loadCommentsFromServer() {
    loadCommentsFromServer = () => {
        $.ajax({
            url: this.props.url,
            dataType: 'json',
            cache: false,
            success: function (data) {
                this.setState({userTableDTO: data});
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
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
        // onChallengeActionStateChanged(complete, id) {
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

    prepareUserIcon() {
        var className = "fa fa-circle fa-stack-2x cyan-text";
        if (this.props.userTableDTO.no == 0)
            className = "fa fa-circle fa-stack-2x orange-text";
        var userIcon = (
            <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <i className="fa fa-user fa-stack-1x fa-inverse" style={{opacity:0.9}}></i>
                        </span>
        );
        return userIcon;
    }

    prepareDifficultyIcon = (action, icon) => {

        var background;

        var styleName = {fill:"#80deea", width: '40px', height: '40px'}; //cyan-lighten3
        if (this.props.userTableDTO.no == 0)
            styleName = {fill:"#ffcc80", width: '40px', height: '40px'};

        if (action.difficulty==0)
            background= <DiffSimpleIcon className="fa-stack-2x" style={styleName} />;
        else if (action.difficulty==1)
            background= <DiffMediumIcon className="fa-stack-2x" style={styleName}/>;
        else
            background= <DiffHardIcon  className="fa-stack-2x" style={styleName}/>;

        var icon;
        if (icon.startsWith("fa-")) {

            icon = <i {...this.props} className={'fa ' + icon+' valign fa-stack-1x center-align'} style={{paddingLeft:'12px'}}  ></i>;
        } else icon = <i {...this.props} className="material-icons valign fa-stack-1x center-align" style={{paddingLeft:'9px'}}>{icon}</i>;
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

            var iconCheckStyle;
            if (this.state.userTableDTO.no == 0)
                iconCheckStyle = {fill: '#ff9800'};
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

//<TableRowColumn style={{width: '30px', padding: '5px'}}><ChallengeIcon
            //icon={action.icon}/></TableRowColumn>

            mCommentNodes.push(
                <TableRow key={action.id}>

                    <TableRowColumn style={{width: '40px', padding: '0px'}}>{this.prepareDifficultyIcon(action, action.icon)}</TableRowColumn>
                    <TableRowColumn style={{width: '290px', padding: '5px'}}>{action.actionName}</TableRowColumn>
                    <TableRowColumn style={{padding: '5px'}}>{action.actionType}</TableRowColumn>
                    <TableRowColumn style={{padding: '10px'}}>{this.prepareCheckbox(action)}</TableRowColumn>
                </TableRow>);
        }

// <TableHeaderColumn style={{width: '30px', padding: '5px'}}></TableHeaderColumn>
        return (
            <div>
                <h5 className="center">
                    {this.prepareUserIcon()} {this.props.userTableDTO.userName}

                    <span style={{marginLeft: 20 + 'px'}}>{this.calculateCheckedCount()}</span>/
                    <span>{this.props.userTableDTO.actionsList.length}</span>
                </h5>
                <Table selectable={false} fixedHeader={true}>
                    <TableHeader displaySelectAll={false}
                                 adjustForCheckbox={false}>
                        <TableRow  >

                            <TableHeaderColumn style={{width: '40px', padding: '0px'}}></TableHeaderColumn>
                            <TableHeaderColumn style={{width: '290px', padding: '5px'}}>Name</TableHeaderColumn>
                            <TableHeaderColumn style={{padding: '5px'}}>Type</TableHeaderColumn>

                            <TableHeaderColumn style={{padding: '10px'}}>Done</TableHeaderColumn>
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false}>
                        {mCommentNodes}
                    </TableBody>
                </Table>

                {popover}
            </div>
        );
    }
}



