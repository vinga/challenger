import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import Dialog from "material-ui/Dialog";
import FlatButton from "material-ui/FlatButton";
import TextField from "material-ui/TextField";
import {RadioButton, RadioButtonGroup} from "material-ui/RadioButton";
import DatePicker from "material-ui/DatePicker";
import IconChooserButton from "./IconChooserButton.tsx";
import {TouchTapEvent, IconButton} from "material-ui";
import {TaskDTO, TaskType} from "../../TaskDTO";
import {updateTask, deleteTask} from "../../taskActions";
import {CLOSE_EDIT_TASK} from "../../taskActionTypes";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../../../views/Constants";
import {YesNoConfirmationDialog} from "../../../views/common-components/YesNoConfirmationDialog";


interface Props {
    task: TaskDTO,
}
interface ReduxProps {
    creatorUserLabel: string
}

interface PropsFunc {
    onTaskSuccessfullyUpdatedFunc: (task: TaskDTO)=>void;
    onTaskDeleteFunc: (task: TaskDTO)=>void;
    onCloseFunc?: (event?: TouchTapEvent) => void,
}
interface State {
    task: TaskDTO,
    submitDisabled: boolean,
    taskDeleteConfirmation: boolean
}

/**
 * A modal dialog can only be closed by selecting one of the actions.
 */
class EditTaskDialogInternal extends React.Component<Props & ReduxProps & PropsFunc, State> {
    constructor(props) {
        super(props);
        //var task:TaskDTO = $.extend({}, this.props.task);
        this.state = {
            task: this.props.task,
            submitDisabled: false,
            taskDeleteConfirmation: false
        };
    }

    handleActionNameFieldChange = (event) => {
        this.state.task.label = event.target.value;
        this.setState(this.state);
    };


    handleSubmit = () => {
        this.props.onTaskSuccessfullyUpdatedFunc(this.state.task);
        this.props.onCloseFunc();
    };

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

    getSubmitButtonTitle = () => {
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
    };
    handleTaskDelete = () => {
        this.state.taskDeleteConfirmation = true;
        this.setState(this.state);
    };


    render() {
        if (this.props.task == null)
            return <div/>;

        const actions = [];
        if (this.props.task.id <= 0) {
            actions.push(
                <FlatButton
                    label={this.getSubmitButtonTitle()}
                    primary={true}
                    disabled={this.state.submitDisabled}
                    onTouchTap={this.handleSubmit}
                />);
        }

        actions.push(
            <FlatButton
                label="Cancel"
                primary={false}
                onTouchTap={this.props.onCloseFunc}
            />
        );

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
//width: "800px",
        return (
            <div>
                <Dialog
                    actions={actions}

                    modal={true}
                    open={true}
                    style={{height: "600px", overflow: "none", display: "block"}}
                >


                    <div style={{display: "table", marginBottom: '0px',width:'100%'}}>
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
                        {  this.props.task.id > 0 &&
                        <div style={{float: "right"}}>
                            <IconButton style={{width: 60, height: 60}}
                                        onClick={this.handleTaskDelete}>
                                &nbsp;<i className={'fa fa-trash' }
                                         style={{fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                            </IconButton>
                        </div>
                        }

                    </div>
                    <div style={{marginLeft: '10px',marginBottom: '20px',clear: "both"}}>Created by: {this.props.creatorUserLabel}</div>

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
                    { this.state.taskDeleteConfirmation &&
                    <YesNoConfirmationDialog
                        closeYes={()=>{  this.props.onTaskDeleteFunc(this.state.task);  }}
                        closeDialog={()=>{this.state.taskDeleteConfirmation=false; this.setState(this.state);}}
                    >
                        Do you want to remove this task?
                    </YesNoConfirmationDialog>
                    }
                </Dialog>
            </div>
        );
    }

}
const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {


    return {
        creatorUserLabel: state.challenges.visibleChallenges.filter(ch=>ch.id == state.challenges.selectedChallengeId).pop().userLabels.filter(u=>u.id == state.tasksState.editedTask.createdByUserId).pop().label
    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onTaskSuccessfullyUpdatedFunc: (task: TaskDTO)=> {
            dispatch(updateTask(task));
        },
        onTaskDeleteFunc: (task: TaskDTO)=> {
            dispatch(deleteTask(task));
        },
        onCloseFunc: (event: TouchTapEvent)=> {
            dispatch(CLOSE_EDIT_TASK.new({}));
        },

    }
};


export const EditTaskDialog = connect(mapStateToProps, mapDispatchToProps)(EditTaskDialogInternal);

