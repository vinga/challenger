import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import SecondUserAuthorizePopover from "../SecondUserAuthorizePopover";
import Paper from "material-ui/Paper";
import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import DifficultyIconButton from "./DifficultyIconButton.tsx";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox";
import TaskTableHeader from "./TaskTableHeader.tsx";
import {ResizeAware} from "../Constants";
import TaskLabel from "./TaskLabel.tsx";

const styles = {
    icon: {
        width: '50px',
        padding: '0px'
    },
    label: {
        padding: '5px'
    },
    taskType: {
        width: '40px',
        padding: '0px',
        color: 'grey',
        fontSize: '11px'
    },

}

class TaskTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            tasksList: [],
            authorized: this.props.no == 0,
            authorizePanel: false,
            busy: false

        }
    }

    handleResize = (e) => {
        this.setState(this.state);
    }

    componentDidMount = () => {
        if (this.props.selectedChallengeDTO != null)
            this.loadTasksFromServer(this.props.selectedChallengeDTO, this.props.currentDate);
    }

    componentWillReceiveProps(nextProps) {
        //console.log("component rec props");
        if (this.props.selectedChallengeDTO == null || this.props.selectedChallengeDTO.id != nextProps.selectedChallengeDTO.id || this.props.currentDate != nextProps.currentDate) {
            this.loadTasksFromServer(nextProps.selectedChallengeDTO, nextProps.currentDate);
        }
    }

    loadTasksFromServer = (contract, date) => {
        //TODO test if really needed
        this.state.busy = true;

        this.setState(this.state);
        ajaxWrapper.loadTasksFromServer(contract.id, this.props.no, date,
            (data)=> {
                this.state.tasksList = data;
                this.state.busy = false;
                this.setState(this.state);
            }
        )
    }

    onTaskCheckedStateChangedFunc = (taskDTO) => {

        var taskProgressDTO = {
            taskId: taskDTO.id,
            done: taskDTO.done,
            progressTime: this.props.currentDate.getTime()
        };
        ajaxWrapper.updateTaskProgress(taskProgressDTO,
            (data)=> {
               this.loadTasksFromServer(this.props.selectedChallengeDTO, this.props.currentDate);
            }
        );

        this.setState(this.state);

    }

    onTaskSuccessfullyUpdatedFunc = (newTask) => {
        var found = false;
        $.each(this.state.tasksList, (k, v) => {
            if (v.id == newTask.id) {
                this.state.tasksList[k] = newTask;
                found = true;
            }
        });
        if (!found) {
            this.state.tasksList.push(newTask);
        }
        this.setState(this.state);

    }


    showAuthorizePanelFunc = (anchor, isInputChecked) => {
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
        var height = Math.max(300, Math.max(document.documentElement.clientHeight, window.innerHeight || 0) - 400) + "px";
        return (
            <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '20px'}}>
                <TaskTableHeader no={this.props.no}
                                 userDTO={this.props.userDTO.label}
                                 tasksList={this.state.tasksList}
                                 userName={this.props.userDTO.label}
                                 onTaskSuccessfullyUpdatedFunc={this.onTaskSuccessfullyUpdatedFunc}
                                 challengeId={this.props.selectedChallengeDTO != null ? this.props.selectedChallengeDTO.id : -1}
                />

                <Paper style={{padding: '10px', display: "inline-block"}}>
                    <div style={{minHeight: height, height: height, overflowY: "auto", overflowX: "none"}}>
                        <Table selectable={false}
                               fixedHeader={true}


                        >
                            <TableBody displayRowCheckbox={false}


                            >
                                { !this.state.busy && this.state.tasksList.map(task =>
                                    <TableRow key={task.id}>
                                        <TableRowColumn style={styles.icon}>
                                            <DifficultyIconButton
                                                no={this.props.no}
                                                taskDTO={task}
                                                onTaskSuccessfullyUpdatedFunc={this.onTaskSuccessfullyUpdatedFunc}
                                            />
                                        </TableRowColumn>
                                        <TableRowColumn style={styles.label}>
                                            <TaskLabel
                                                no={this.props.no}
                                                taskDTO={task}
                                                userId={this.props.userDTO.id}
                                                userLabel={this.props.userDTO.label}
                                                authorized={this.state.authorized}/>
                                        </TableRowColumn>
                                        <TableRowColumn style={styles.taskType}>
                                            {task.taskType}
                                        </TableRowColumn>
                                        <TableRowColumn style={{width: '45px', padding: '10px'}}>
                                            <ChallengeTableCheckbox
                                                no={this.props.no}
                                                taskDTO={task}
                                                showAuthorizePanelFunc={this.showAuthorizePanelFunc}
                                                onTaskCheckedStateChangedFunc={this.onTaskCheckedStateChangedFunc}
                                                authorized={this.state.authorized}
                                            />
                                        </TableRowColumn>
                                    </TableRow>
                                )}

                                {this.state.busy && <TableRow>Loading</TableRow>}

                            </TableBody>
                        </Table>
                    </div>
                </Paper>
                <SecondUserAuthorizePopover ref="authPopover" userName={this.props.userDTO.label}/>
            </div>
        );
    }
}
TaskTable.propTypes = {
    no: React.PropTypes.number.isRequired,

    userDTO: React.PropTypes.object.isRequired,
    selectedChallengeDTO: React.PropTypes.object.isRequired,
    currentDate: React.PropTypes.object.isRequired // date
};



const mapStateToProps = (state, ownprops) => {

    //console.log(state.users);
    var selectedChallengeDTO = state.visibleChallengesDTO.visibleChallenges.filter(ch=>ch.id == state.visibleChallengesDTO.selectedChallengeId).pop();
    var primaryUserId = state.users.filter(u=>{  return u.primary == true;}).map(u=>u.userId).pop();



        var u1 = {
            id: selectedChallengeDTO.firstUserId,
            label: selectedChallengeDTO.firstUserLabel,
            authorized: state.users.filter(u=>u.userId == selectedChallengeDTO.firstUserId && u.jwtToken != null)[0]
        }
        var u2 = {
            id: selectedChallengeDTO.secondUserId,
            label: selectedChallengeDTO.secondUserLabel,
            authorized: state.users.filter(u=>u.userId == selectedChallengeDTO.secondUserId && u.jwtToken != null)[0]
        }
    return {
        userDTO: ownprops.no==0 && u1.id==primaryUserId? u1: u2,
        selectedChallengeDTO: selectedChallengeDTO,
        currentDate: state.mainReducer.day
       // taskList: state.tasks.filter( state.visibleChallengesDTO.selectedChallengeId + "-" + state.mainReducer.day.toISOString().slice(0, 10)])
    }
}

import { connect } from 'react-redux'
const Ext = connect(mapStateToProps)(ResizeAware(TaskTable))

export default Ext;

