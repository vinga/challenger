/**
 * Created by evizone on 2016-08-18.
 */
import React from "react";
import Dialog from "material-ui/Dialog";
import Divider from "material-ui/Divider";
import FlatButton from "material-ui/FlatButton";
import TextField from "material-ui/TextField";
import {RadioButton, RadioButtonGroup} from "material-ui/RadioButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "./Constants";
import DatePicker from "material-ui/DatePicker";


/**
 * A modal dialog can only be closed by selecting one of the actions.
 */
export default class ChallengeEditDialogWindow extends React.Component {
    constructor(props) {
        super(props);
        var task;
        if (this.props.task === undefined) {
            var today = new Date();
            task = {
                icon: "fa-book",
                difficulty: 0,
                actionName: "Example task 1",
                actionType: "onetime",
                actionStatus: "Done",
                dueDate: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7)
            }


        } else
            task = this.props.task;

        this.state = {
            task: task,
            open: false
        };


    }

    handleOpen = () => {
        this.setState({open: true});
    };

    handleActionNameFieldChange = (event) => {
        this.state.task.actionName = event.target.value;
        this.setState({task: this.state.task});
    }

    handleClose = () => {
        this.setState({open: false});
    };

    resolveWindowTitle = () => {
        var title;
        if (this.props.task === undefined)
            title = "New task";
        else
            title = "Edit task " + this.props.task.actionName;
        return title;
    }
    handleDueDateChange = (event, date) => {
        this.state.task.dueDate = date;
        this.setState({task: this.state.task});
    };

    handleActionTypeChange = (event) => {
        this.state.task.actionType = event.target.value;
        this.setState({task: this.state.task});
    };

    handleSubmitButtonTitle = () => {
        var title;
        if (this.props.task === undefined)
            title = "Create";
        else
            title = "Save";
        return title;
    };


    render() {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onTouchTap={this.handleClose}
            />,
            <FlatButton
                label={this.handleSubmitButtonTitle()}
                primary={true}
                disabled={true}
                onTouchTap={this.handleClose}
            />,
        ];


        var datePicker = undefined;

        if (this.state.task.actionType === "onetime") {
            datePicker = <div style={{display: "block", float: "left"}}>
                <DatePicker
                    textFieldStyle={{width: '100px'}}
                hintText="Challenge due date"
                value={this.state.task.dueDate}
                onChange={this.handleDueDateChange}
                floatingLabelText="Due date"
                container="inline"

            /></div>;
        }

        return (
            <div>

                <Dialog
                    title={this.resolveWindowTitle()}
                    actions={actions}
                    modal={true}
                    open={this.state.open}
                    style={{height: "400px", overflow: "none", display: "block"}}
                >

                    <div style={{display: "block", marginBottom:'30px'}}>
                        <TextField

                            hintText="Name"
                            ref="actionName"
                            onChange={this.handleActionNameFieldChange}

                        />

                    </div>
                    <div style={{display: "block", float: "left", width: "200px"}}>
                        <RadioButtonGroup name="difficulty" defaultSelected="easy">

                            <RadioButton
                                value="easy"
                                label="Easy"
                                checkedIcon={<DiffSimpleIcon/>
                                }

                                style={{display: 'block', float: 'left'}}
                            />


                            <RadioButton
                                value="medium"
                                label="Medium"
                                checkedIcon={<DiffMediumIcon />}
                                style={{display: 'block', float: 'left'}}
                            />


                            <RadioButton
                                value="hard"
                                label="Hard"
                                checkedIcon={<DiffHardIcon />}
                                style={{display: 'block', float: 'left'}}

                            />
                        </RadioButtonGroup>
                    </div>
                    <div style={{display: "block", float: "left", width: "200px"}}>
                        <RadioButtonGroup name="actiontype" defaultSelected={this.state.task.actionType}
                                          onChange={this.handleActionTypeChange}>
                            <RadioButton
                                value="onetime"
                                label="Onetime"

                            />
                            <RadioButton
                                value="daily"
                                label="Daily"

                            />
                            <RadioButton
                                value="weekly"
                                label="Weekly"

                            />
                            <RadioButton
                                value="monthly"
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