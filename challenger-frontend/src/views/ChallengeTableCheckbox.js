import React, {Component} from "react";
import Checkbox from "material-ui/Checkbox";

export default class ChallengeTableCheckbox extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
          action: this.props.action
        };
    }

    render() {
        var checkbox;
        if (this.state.action.actionStatus == "Failed")
            checkbox = <div>Failed</div>;
        else {
            var onCheck = (actionId) => (event, isInputChecked) => {
                //this.onChallengeActionStateChanged(event, isInputChecked, actionId);
                if (!this.props.showAuthorizePanel(event.currentTarget, isInputChecked)) {
                    console.log("are we here");
                    if (isInputChecked)
                        this.state.action.actionStatus = 'Done';
                    else
                        this.state.action.actionStatus = 'Pending';
                    this.setState(this.state);
                    this.props.onActionCheckedStateChanged();
                }

            };

            var iconCheckStyle = {fill: '#00bcd4'}; // cyan
            if (this.props.no == 0)
                iconCheckStyle = {fill: '#ff9800'}; // orange
            else if (!this.state.authorized)
                iconCheckStyle = {fill: 'lightgrey'};

            checkbox = <Checkbox
                key="statusCb"
                checked={this.props.action.actionStatus === 'Done'}
                onCheck={onCheck(this.props.action.id)}
                iconStyle={iconCheckStyle}
                style={{display: 'inline-block', width: '30px'}}/>;
        }
        return checkbox;
    }

};