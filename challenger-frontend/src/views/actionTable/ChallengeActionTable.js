import React, {Component} from "react";
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui/Table";
import SecondUserAuthorizePopover from "../SecondUserAuthorizePopover";
import Paper from "material-ui/Paper";
import ajaxWrapper from "../../presenters/AjaxWrapper";
import DifficultyIconButton from "./DifficultyIconButton";
import ChallengeTableCheckbox from "./ChallengeTableCheckbox";
import ChallengeActionTableHeader from "./ChallengeActionTableHeader";
import Chip from "material-ui/Chip";
import {ChallengeActionStatus} from "../Constants";
import FontIcon from "material-ui/FontIcon";

const styles = {
    icon: {
        width: '50px',
        padding: '0px'
    },
    label: {
        padding: '5px'
    },
    actionType: {
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
        marginLeft: '25px'
    }
}

export default class ChallengeActionTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            actionsList: [],
            authorized: this.props.no == 0,
            authorizePanel: false

        }
        //console.log("comp created");
        //console.log("ChallengeActionTable is logged "+this.props.ctx.logged+" "+ this.props.ctx.selectedContract);
    }

    handleResize = (e) => {
        this.setState(this.state);
    }

    componentDidMount = () => {
        window.addEventListener('resize', this.handleResize);
        //console.log("component did mount");
        if (this.props.selectedContract != null)
            this.loadChallengeActionsFromServer(this.props.selectedContract);
    }

    componentWillUnmount = () => {
        window.removeEventListener('resize', this.handleResize);
    }

    componentWillReceiveProps(nextProps) {
        //console.log("component rec props");
        if (this.props.selectedContract == null || this.props.selectedContract.id != nextProps.selectedContract.id) {
            this.loadChallengeActionsFromServer(nextProps.selectedContract);
        }
    }

    loadChallengeActionsFromServer = (contract) => {
        ajaxWrapper.loadChallengeActionsFromServer(contract.id, this.props.no,
            (data)=> {
                this.state.actionsList = data;
                this.setState(this.state);
            }
        )
    }

    onActionCheckedStateChanged = () => {
        this.setState(this.state);

    }

    onChallengeActionSuccessfullyUpdated = (newChallenge) => {
        var found=false;
        $.each(this.state.actionsList, (k,v) => {
            if (v.id == newChallenge.id) {
                this.state.actionsList[k] = newChallenge;
                found=true;
            }
        });
        if (!found) {
            this.state.actionsList.push(newChallenge);
        }
        this.setState(this.state);

    }


    showAuthorizePanel = (anchor, isInputChecked) => {
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
                <ChallengeActionTableHeader no={this.props.no}
                                            ctx={this.props.ctx}
                                            actionsList={this.state.actionsList}
                                            userName={this.props.userName}
                                            onChallengeActionSuccessfullyUpdated={this.onChallengeActionSuccessfullyUpdated}
                                            contractId={this.props.selectedContract!=null? this.props.selectedContract.id: -1}
                />

                <Paper style={{padding: '10px', display: "inline-block", height: height}}>
                    <Table selectable={false}
                           fixedHeader={true}
                    >
                        <TableBody displayRowCheckbox={false}>
                            { this.state.actionsList.map(action =>
                                <TableRow key={action.id}>
                                    <TableRowColumn style={styles.icon}>
                                        <DifficultyIconButton
                                            no={this.props.no}
                                            action={action}
                                            difficulty={action.difficulty}
                                            icon={action.icon}
                                            onChallengeActionSuccessfullyUpdated={this.onChallengeActionSuccessfullyUpdated}
                                        />
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.label}>
                                        {action.label}


                                        { action.actionStatus == ChallengeActionStatus.waiting_for_acceptance &&

                                        <FontIcon className={'fa fa-question-circle-o' }
                                                  color={this.props.no==0? "red": "grey"}
                                                  hoverColor="orange"
                                                  style={{margin:'5px',fontSize: '15px', textAlign: 'center'}}
                                        onClick={()=>alert('jaja')}>

                                        </FontIcon>

                                        }
                                        {false && action.actionStatus == ChallengeActionStatus.waiting_for_acceptance &&

                                            <div style={styles.wrapper} >
                                                <div style={{verticalAlign:"middle"}}>{action.label}</div>
                                        <Chip style={styles.chip}
                                              onRequestDelete={()=> {
                                            }}

                                        >
                                            Accept
                                        </Chip><Chip style={styles.chip}>Reject</Chip></div>
                                        }
                                    </TableRowColumn>
                                    <TableRowColumn style={styles.actionType}>
                                        {action.actionType}
                                    </TableRowColumn>
                                    <TableRowColumn style={{width: '45px', padding: '10px'}}>
                                        <ChallengeTableCheckbox
                                            no={this.props.no}
                                            action={action}
                                            showAuthorizePanel={this.showAuthorizePanel}
                                            onActionCheckedStateChanged={this.onActionCheckedStateChanged}
                                        />
                                    </TableRowColumn>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </Paper>
                <SecondUserAuthorizePopover ref="authPopover" userName={this.props.userName}/>
            </div>
        );
    }
}



