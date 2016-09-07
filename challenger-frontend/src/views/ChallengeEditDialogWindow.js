import React from "react";
import Dialog from "material-ui/Dialog";
import FlatButton from "material-ui/FlatButton";
import TextField from "material-ui/TextField";
import {RadioButton, RadioButtonGroup} from "material-ui/RadioButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon, ChallengeActionStatus, ChallengeActionType} from "./Constants";
import DatePicker from "material-ui/DatePicker";
import ajaxWrapper from "../presenters/AjaxWrapper";
import IconChooserButton from "./IconChooserButton";

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
                label: "Example task 1",
                actionType: ChallengeActionType.onetime,
                actionStatus: ChallengeActionStatus.pending,
                dueDate: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7).getTime(),
                userId: this.props.userId,
                contractId: this.props.contractId
            }

        } else {
            var taskCopy = jQuery.extend({}, this.props.task)
            task = taskCopy;
        }

        this.state = {
            task: task,
            open: this.props.open,
            submitDisabled: false
        };


    }


    handleOpen = () => {
        this.setState({open: true});
    };

    handleActionNameFieldChange = (event) => {
        this.state.task.label = event.target.value;
        this.setState(this.state);
    }

    handleClose = () => {
        this.setState({open: false});
        this.props.onClose();
    };

    handleSubmit = () => {
        //console.log(this.state.task);
        ajaxWrapper.updateChallengeAction(this.state.task, (updatedChallengeAction)=>this.props.onChallengeActionSuccessfullyUpdated(updatedChallengeAction));
        this.setState({open: false});
        this.props.onClose();
    };

    resolveWindowTitle = () => {
        var title;
        if (this.props.task === undefined)
            title = "New task";
        else
            title = "Edit task " + this.props.task.label;
        return title;
    }
    handleDueDateChange = (event, date) => {
        this.state.task.dueDate = date;
        this.setState(this.state);
    };

    handleActionTypeChange = (event) => {
        this.state.task.actionType = event.target.value;
        this.setState(this.state);
    };

    handleSubmitButtonTitle = () => {
        var title;
        if (this.props.task === undefined)
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
                onTouchTap={this.handleClose}
            />,
        ];


        var datePicker = undefined;
        if (this.state.task.actionType === ChallengeActionType.onetime) {
            datePicker = <div style={{display: "block", float: "left"}}>
                <DatePicker
                    textFieldStyle={{width: '100px'}}
                    hintText="Challenge due date"
                    value={new Date(this.state.task.dueDate)}
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


                    <div style={{display: "table", marginBottom: '30px'}}>
                        <div style={{display: "inline-block"}}>
                            <IconChooserButton
                                icon={this.state.task.icon}
                                onClick={this.handleIconChange}/>
                        </div>
                        <div style={{display: "inline-block",  marginLeft:'10px'}}>
                        </div>
                        <TextField
                            floatingLabelText="Name"
                            hintText="Name"
                            defaultValue={this.state.task.label}
                            ref="actionName"
                            onChange={this.handleActionNameFieldChange}

                        />


                    </div>

                    <div style={{display: "block", float: "left", marginLeft:'20px',width: "200px"}}>
                        <RadioButtonGroup name="difficulty" defaultSelected={this.state.task.difficulty}>

                            <RadioButton
                                value={0}
                                label="Easy"
                                checkedIcon={<DiffSimpleIcon/>}

                            />

                            <RadioButton
                                value={1}
                                label="Medium"
                                checkedIcon={<DiffMediumIcon />}
                                style={{display: 'block', float: 'left'}}
                            />

                            <RadioButton
                                value={2}
                                label="Hard"
                                checkedIcon={<DiffHardIcon />}
                                style={{display: 'block', float: 'left'}}

                            />
                        </RadioButtonGroup>
                    </div>
                    <div style={{display: "block", float: "left", width: "200px"}}>
                        <RadioButtonGroup name="actiontype"
                                          defaultSelected={this.state.task.actionType}
                                          onChange={this.handleActionTypeChange}>
                            <RadioButton
                                value={ChallengeActionType.onetime}
                                label="Onetime"
                            />
                            <RadioButton
                                value={ChallengeActionType.daily}
                                label="Daily"
                            />
                            <RadioButton
                                value={ChallengeActionType.weekly}
                                label="Weekly"
                            />
                            <RadioButton
                                value={ChallengeActionType.monthly}
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