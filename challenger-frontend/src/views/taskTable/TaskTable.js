import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import SecondUserAuthorizePopover from "../SecondUserAuthorizePopover";
import Paper from "material-ui/Paper";
import ajaxWrapper from "../../logic/AjaxWrapper";
import DifficultyIconButton from "./DifficultyIconButton";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox";
import TaskTableHeader from "./TaskTableHeader";
import Chip from "material-ui/Chip";
import {TaskStatus} from "../Constants";
import FontIcon from "material-ui/FontIcon";

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
    wrapper: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        marginRight: '5px',
        cursor: 'pointer'
    }
}

export default class TaskTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            tasksList: [],
            authorized: this.props.no == 0,
            authorizePanel: false

        }
        //console.log("comp created");

    }

    handleResize = (e) => {
        this.setState(this.state);
    }

    componentDidMount = () => {
        window.addEventListener('resize', this.handleResize);
        if (this.props.selectedChallengeDTO != null)
            this.loadTasksFromServer(this.props.selectedChallengeDTO);
    }

    componentWillUnmount = () => {
        window.removeEventListener('resize', this.handleResize);
    }

    componentWillReceiveProps(nextProps) {
        //console.log("component rec props");
        if (this.props.selectedChallengeDTO == null || this.props.selectedChallengeDTO.id != nextProps.selectedChallengeDTO.id) {
            this.loadTasksFromServer(nextProps.selectedChallengeDTO);
        }
    }

    loadTasksFromServer = (contract) => {
        //TODO test if really needed
        ajaxWrapper.loadTasksFromServer(contract.id, this.props.no,
            (data)=> {
                this.state.tasksList = data;
                this.setState(this.state);
            }
        )
    }

    onTaskCheckedStateChangedFunc = () => {
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
            <div style={{marginRight: '10px', marginLeft: '10px', marginTop: '20px', marginBottom: '30px'}}>
                <TaskTableHeader no={this.props.no}
                                            userDTO={this.props.userDTO}
                                            ctx={this.props.ctx}
                                            tasksList={this.state.tasksList}
                                            userName={this.props.userName}
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
                                { this.state.tasksList.map(task =>
                                    <TableRow key={task.id}>
                                        <TableRowColumn style={styles.icon}>
                                            <DifficultyIconButton
                                                no={this.props.no}
                                                taskDTO={task}
                                                onTaskSuccessfullyUpdatedFunc={this.onTaskSuccessfullyUpdatedFunc}
                                            />
                                        </TableRowColumn>
                                        <TableRowColumn style={styles.label}>

                                            { task.taskStatus != TaskStatus.waiting_for_acceptance &&
                                            task.label
                                            }


                                            { false && task.taskStatus == TaskStatus.waiting_for_acceptance &&

                                            <FontIcon className={'fa fa-question-circle-o' }
                                                      color={this.props.no == 0 ? "red" : "grey"}
                                                      hoverColor="orange"
                                                      style={{margin: '5px', fontSize: '15px', textAlign: 'center'}}
                                                      onClick={()=>alert('jaja')}>

                                            </FontIcon>

                                            }
                                            { task.taskStatus == TaskStatus.waiting_for_acceptance &&

                                            <div style={styles.wrapper}>

                                                <div className="taskLabel">{task.label}</div>

                                                <Chip style={styles.chip} className="clickableChip">
                                                    <i className="fa fa-check"></i> Accept
                                                </Chip>

                                                <Chip style={styles.chip} className="clickableChip">
                                                    <i className="fa fa-close"></i> Reject
                                                </Chip></div>
                                            }
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
    userDTO: React.PropTypes.object.isRequired,
    selectedChallengeDTO: React.PropTypes.object.isRequired,
    no: React.PropTypes.number.isRequired,
};


