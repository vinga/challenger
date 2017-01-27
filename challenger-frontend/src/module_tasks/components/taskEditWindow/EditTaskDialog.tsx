import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import Dialog from "material-ui/Dialog";
import FlatButton from "material-ui/FlatButton";
import TextField from "material-ui/TextField";
import {RadioButton, RadioButtonGroup} from "material-ui/RadioButton";
import DatePicker from "material-ui/DatePicker";
import IconChooserButton from "./IconChooserButton";
import {TouchTapEvent, IconButton, Subheader} from "material-ui";
import {TaskDTO, TaskType} from "../../TaskDTO";
import {updateTask, deleteTask} from "../../taskActions";
import {CLOSE_EDIT_TASK} from "../../taskActionTypes";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../../../views/Constants";
import {YesNoConfirmationDialog} from "../../../views/common-components/YesNoConfirmationDialog";
import {Row, Col} from "../../../views/common-components/Flexboxgrid";
import {MonthdaysGroup} from "./MonthdaysGroup";
import {WeekdaysGroup} from "./WeekdaysGroup";
import {challengeParticipantsSelector} from "../../../module_challenges/challengeSelectors";


interface Props {
    task: TaskDTO,
}
interface ReduxProps {
    isTaskUserLogged: boolean,
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
    taskDeleteConfirmation: boolean,
    visibilityType: string
}

class EditTaskDialogInternal extends React.Component<Props & ReduxProps & PropsFunc, State> {
    taskNameTextField: TextField;

    constructor(props) {
        super(props);
        var vType = "day"
        if (props.task.monthDays == null)
            vType = "weekday"

        this.state = {
            task: this.props.task,
            submitDisabled: false,
            taskDeleteConfirmation: false,
            visibilityType: vType
        };
    }

    componentDidMount = () => {
        setTimeout(()=> {
            this.taskNameTextField.focus();
        },200);
    }

    isDisabled = () => {
        return this.props.task.id > 0;
    }

    handleActionNameFieldChange = (event) => {
        this.state.task.label = event.target.value;
        this.setState(this.state);
    };

    handleSubmit = () => {
        if (this.state.task.taskType == TaskType.daily) {
            if (this.state.visibilityType == "weekday")
                this.state.task.monthDays = null;
            else
                this.state.task.weekDays = null;
        }


        this.props.onTaskSuccessfullyUpdatedFunc(this.state.task);
        this.props.onCloseFunc();
    };

    handleStartDateChange = (event, date) => {
        this.state.task.startDate = date.getTime();
        if (new Date(this.state.task.startDate).getTime()>new Date(this.state.task.dueDate).getTime()) {
            this.state.task.dueDate=date.getTime();
        }
        this.setState(this.state);
    };

    handleDueDateChange = (event, date) => {
        this.state.task.dueDate = date.getTime();
        if (new Date(this.state.task.startDate).getTime()>new Date(this.state.task.dueDate).getTime()) {
            this.state.task.startDate=date.getTime();
        }
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


    handleMonthDaysChanged = (days: string) => {
        this.state.task.monthDays = days;
        this.setState(this.state);
    }
    handleWeekDaysChanged = (days: string) => {
        this.state.task.weekDays = days;
        this.setState(this.state);
    }
    handleDailyTaskVisibilityChange = (event) => {
        this.state.visibilityType = event.target.value;
        this.setState(this.state);
    }
    shouldDisableDate = (date: Date): boolean => {
        return new Date(date.toDateString()) < new Date(new Date().toDateString());
    }



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

        return (
            <div>
                <Dialog
                    actions={actions}
                    contentStyle={{height: '600px'}}
                    modal={true}
                    open={true}
                    style={{height: "600px", overflow: "none", display: "block"}}
                >

                    <div style={{display: "table", marginBottom: '0px',width:'100%'}}>
                        <div style={{display: "inline-block"}}>
                            <IconChooserButton
                                disabled={this.isDisabled()}
                                icon={this.state.task.icon}
                                onClick={this.handleIconChange}/>
                        </div>
                        <div style={{display: "inline-block", marginLeft: '10px'}}>
                        </div>
                        <TextField
                            disabled={this.isDisabled()}
                            floatingLabelText="Name"
                            hintText="Name"
                            defaultValue={this.state.task.label}
                            ref={(c)=>this.taskNameTextField=c}
                            onChange={this.handleActionNameFieldChange}
                        />
                        {  this.props.task.id > 0 && this.props.isTaskUserLogged &&
                        <div style={{float: "right"}}>
                            <IconButton  style={{fontSize: 20}}
                                        onClick={this.handleTaskDelete}>
                                <i className={'fa fa-trash' }
                                         style={{fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                            </IconButton>
                        </div>
                        }

                    </div>
                    <div style={{marginLeft: '10px',marginBottom: '10px',clear: "both"}}>Created by: {this.props.creatorUserLabel}</div>

                    <Row >
                        <Col>
                            <Subheader>Difficulty</Subheader>
                            <RadioButtonGroup name="difficulty"
                                              defaultSelected={"" + this.state.task.difficulty}>

                                <RadioButton
                                    value="0"
                                    label="Easy"
                                    checkedIcon={<DiffSimpleIcon/>}
                                    disabled={this.isDisabled()}
                                />

                                <RadioButton
                                    value="1"
                                    label="Medium"
                                    checkedIcon={<DiffMediumIcon />}
                                    disabled={this.isDisabled()}
                                    style={{display: 'block', float: 'left'}}
                                />

                                <RadioButton
                                    value="2"
                                    label="Hard"
                                    checkedIcon={<DiffHardIcon />}
                                    disabled={this.isDisabled()}
                                    style={{display: 'block', float: 'left'}}
                                />
                            </RadioButtonGroup>
                        </Col>
                        <Col>
                            <Subheader>Frequency</Subheader>
                            <RadioButtonGroup name="actiontype"
                                              defaultSelected={this.state.task.taskType}
                                              onChange={this.handleTaskTypeChange}>
                                <RadioButton
                                    value={TaskType.onetime}
                                    label="Onetime"
                                    disabled={this.isDisabled()}
                                />
                                <RadioButton
                                    value={TaskType.daily}
                                    label="Daily"
                                    disabled={this.isDisabled()}
                                />
                                <RadioButton
                                    value={TaskType.weekly}
                                    label="Weekly"
                                    disabled={this.isDisabled()}
                                />
                                <RadioButton
                                    value={TaskType.monthly}
                                    label="Monthly"
                                    disabled={this.isDisabled()}
                                />
                            </RadioButtonGroup>

                        </Col>
                        {this.props.task.taskType == TaskType.onetime &&
                        <Col col="6">
                            <Subheader>Visibility</Subheader>
                            <DatePicker
                                textFieldStyle={{width: '100px'}}
                                hintText="Task start date"
                                value={new Date(this.state.task.startDate)}
                                onChange={this.handleStartDateChange}
                                floatingLabelText="Start date"
                                container="inline"
                                disabled={this.isDisabled()}
                                shouldDisableDate={this.shouldDisableDate}
                            />

                            <DatePicker
                                textFieldStyle={{width: '100px'}}
                                hintText="Task due date"
                                value={new Date(this.state.task.dueDate)}
                                onChange={this.handleDueDateChange}
                                floatingLabelText="Due date"
                                container="inline"
                                disabled={this.isDisabled()}
                                shouldDisableDate={this.shouldDisableDate}
                            />
                        </Col>
                        }

                        { this.props.task.taskType == TaskType.weekly &&
                        <Col col="6">
                            <Subheader>Visibility</Subheader>
                            <WeekdaysGroup days={this.state.task.weekDays} onDaysChanged={this.handleWeekDaysChanged} disabled={this.isDisabled()}/>

                        </Col> }

                        { this.props.task.taskType == TaskType.monthly &&
                        <Col col="6">
                            <Subheader>Visibility</Subheader>
                            <MonthdaysGroup days={this.state.task.monthDays} onDaysChanged={this.handleMonthDaysChanged} disabled={this.isDisabled()}/>
                        </Col> }

                        { this.props.task.taskType == TaskType.daily &&
                        <Col col="6">


                            <Subheader>Visibility</Subheader>

                            <RadioButtonGroup
                                name="visibility"
                                onChange={this.handleDailyTaskVisibilityChange}
                                style={{marginBottom: '20px'}}
                                defaultSelected={this.state.visibilityType}>

                                <RadioButton
                                    label="Week day"
                                    value="weekday"
                                    disabled={this.isDisabled()}
                                />
                                <RadioButton
                                    label="Day in the month"
                                    value="day"
                                    disabled={this.isDisabled()}
                                />
                            </RadioButtonGroup>
                            <Row>

                                {this.state.visibilityType == "weekday" &&
                                <Col col="4">
                                    <WeekdaysGroup days={this.state.task.weekDays} onDaysChanged={this.handleWeekDaysChanged} disabled={this.isDisabled()}/>
                                </Col>
                                }

                                {this.state.visibilityType == "day" &&
                                <Col col="8" style={{display:"flex", flexDirection: "row", flexWrap:"wrap"}}>
                                    <MonthdaysGroup days={this.state.task.monthDays} onDaysChanged={this.handleMonthDaysChanged} disabled={this.isDisabled()}/>
                                </Col>
                                }
                            </Row>
                        </Col> }
                    </Row>


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
        isTaskUserLogged: challengeParticipantsSelector(state).some(chp=>chp.id==ownProps.task.userId && chp.jwtToken!=null),
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

