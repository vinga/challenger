import React from "react";
import Dialog from "material-ui/Dialog";
import FlatButton from "material-ui/FlatButton";
import TextField from "material-ui/TextField";
import {RadioButton, RadioButtonGroup} from "material-ui/RadioButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon, TaskType} from "./Constants";
import DatePicker from "material-ui/DatePicker";
import ajaxWrapper from "../logic/AjaxWrapper";
import IconChooserButton from "./IconChooserButton";

/**
 * A modal dialog can only be closed by selecting one of the actions.
 */
export default class ChallengeEditDialogWindow extends React.Component {
    constructor(props) {
        super(props);
        var task = jQuery.extend({}, this.props.task)
        this.state = {
            task: task,
            submitDisabled: false
        };
    }


    handleActionNameFieldChange = (event) => {
        this.state.task.label = event.target.value;
        this.setState(this.state);
    }


    handleSubmit = () => {
        ajaxWrapper.updateTask(this.state.task, (updatedTask)=>this.props.onTaskSuccessfullyUpdatedFunc(updatedTask));
        this.props.onClose();
    };

    //   title={this.resolveWindowTitle()}
    resolveWindowTitle = () => {
        var title;
        if (this.props.taskDTO === undefined)
            title = "New task";
        else
            title = "Edit task " + this.props.taskDTO.label;
        return title;
    }
    handleDueDateChange = (event, date) => {
        this.state.task.dueDate = date;
        this.setState(this.state);
    };

    handleTaskTypeChange = (event) => {
        this.state.task.taskType = event.target.value;
        if (this.state.task.taskType == TaskType.onetime && this.state.task.dueDate == null) {
            var today = new Date();
            this.state.task.dueDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7).getTime();
        }
        this.setState(this.state);
    };

    handleSubmitButtonTitle = () => {
        var title;
        if (this.props.taskDTO === undefined)
            title = "Create";
        else
            title = "Save";
        return title;
    };
    handleIconChange = (icon) => {
        this.state.task.icon = icon;
        this.setState(this.state);
    }


    render() {

        const actions = [
            <FlatButton
                label={this.handleSubmitButtonTitle()}
                primary={true}
                disabled={this.state.submitDisabled}
                onTouchTap={this.handleSubmit}
            />,
            <FlatButton
                label="Cancel"
                primary={false}
                onTouchTap={this.props.onCloseFunc}
            />,
        ];


        var datePicker = undefined;
        if (this.state.task.taskType === TaskType.onetime) {
            datePicker = <div style={{display: "block", float: "left"}}>
                <DatePicker
                    textFieldStyle={{width: '100px'}}
                    hintText="Task due date"
                    value={new Date(this.state.task.dueDate)}
                    onChange={this.handleDueDateChange}
                    floatingLabelText="Due date"
                    container="inline"

                /></div>;
        }

        return (
            <div>

                <Dialog

                    actions={actions}
                    modal={true}
                    open={this.props.open}
                    style={{height: "400px", overflow: "none", display: "block"}}
                >


                    <div style={{display: "table", marginBottom: '30px'}}>
                        <div style={{display: "inline-block"}}>
                            <IconChooserButton
                                icon={this.state.task.icon}
                                onClick={this.handleIconChange}/>
                        </div>
                        <div style={{display: "inline-block", marginLeft: '10px'}}>
                        </div>
                        <TextField
                            floatingLabelText="Name"
                            hintText="Name"
                            defaultValue={this.state.task.label}
                            ref="actionName"
                            onChange={this.handleActionNameFieldChange}

                        />


                    </div>

                    <div style={{display: "block", float: "left", marginLeft: '20px', width: "200px"}}>
                        <RadioButtonGroup name="difficulty"
                                          defaultSelected={"" + this.state.task.difficulty}>

                            <RadioButton
                                value="0"
                                label="Easy"
                                checkedIcon={<DiffSimpleIcon/>}

                            />

                            <RadioButton
                                value="1"
                                label="Medium"
                                checkedIcon={<DiffMediumIcon />}
                                style={{display: 'block', float: 'left'}}
                            />

                            <RadioButton
                                value="2"
                                label="Hard"
                                checkedIcon={<DiffHardIcon />}
                                style={{display: 'block', float: 'left'}}

                            />
                        </RadioButtonGroup>
                    </div>
                    <div style={{display: "block", float: "left", width: "200px"}}>
                        <RadioButtonGroup name="actiontype"
                                          defaultSelected={this.state.task.taskType}
                                          onChange={this.handleTaskTypeChange}>
                            <RadioButton
                                value={TaskType.onetime}
                                label="Onetime"
                            />
                            <RadioButton
                                value={TaskType.daily}
                                label="Daily"
                            />
                            <RadioButton
                                value={TaskType.weekly}
                                label="Weekly"
                            />
                            <RadioButton
                                value={TaskType.monthly}
                                label="Monthly"
                            />
                        </RadioButtonGroup>
                    </div>
                    {datePicker}
                </Dialog>
            </div>
        );
    }

}


ChallengeEditDialogWindow.propTypes = {
    taskDTO: React.PropTypes.object.isRequired,
    open: React.PropTypes.bool.isRequired,
    onCloseFunc: React.PropTypes.func.isRequired,
    onTaskSuccessfullyUpdatedFunc: React.PropTypes.func.isRequired

};
