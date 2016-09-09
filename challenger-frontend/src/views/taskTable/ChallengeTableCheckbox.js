import React, {Component} from "react";
import Checkbox from "material-ui/Checkbox";
import {TaskStatus} from "../Constants"
import colors from "../../logic/Colors"


export default class ChallengeTableCheckbox extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            taskDTO: this.props.taskDTO

        };
    }

    render() {
        var checkbox;
        //if (this.state.taskDTO.taskStatus == TaskStatus.failed)
        //    checkbox = <div>Failed</div>;
        //else {
            var onCheck = (taskId) => (event, isInputChecked) => {
                if (!this.props.showAuthorizePanelFunc(event.currentTarget, isInputChecked)) {
                    this.state.taskDTO.done=isInputChecked;
                    //if (isInputChecked)
                    //    this.state.taskDTO.taskStatus = TaskStatus.done;
                    //else
                    //    this.state.taskDTO.taskStatus = TaskStatus.pending;
                    this.setState(this.state);
                    this.props.onTaskCheckedStateChangedFunc();
                }

            };

            checkbox = <Checkbox
                key="statusCb"
                checked={this.props.taskDTO.done === true}
                onCheck={onCheck(this.props.taskDTO.id)}
                iconStyle={{fill: this.props.authorized ? colors.userColors[this.props.no] : "lightgrey"}}
                style={{display: 'inline-block', width: '30px'}}/>;
        //}
        return checkbox;
    }

};
ChallengeTableCheckbox.propTypes = {
    taskDTO: React.PropTypes.object.isRequired,
    showAuthorizePanelFunc: React.PropTypes.func.isRequired,
    onTaskCheckedStateChangedFunc: React.PropTypes.func.isRequired,
    authorized: React.PropTypes.bool.isRequired
}