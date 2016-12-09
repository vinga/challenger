import * as React from "react";
import {ReduxState, connect} from "../../../redux/ReduxState";
import FlatButton from "material-ui/FlatButton";
import {TaskUserDTO} from "../../TaskDTO";
import colors from "../../../views/common-components/Colors";
import {CREATE_AND_OPEN_EDIT_TASK} from "../../taskActionTypes";
import {TaskTableHeaderAccountPanel} from "../../../module_accounts/index";
import {makeCalculateAllAndCheckedCount} from "../../taskSelectors";


interface Props {
    user: TaskUserDTO,
    challengeId: number,
    no: number
    onOpenDialogForLoginSecondUser: (event: EventTarget)=>void;
}

interface ReduxProps {
    allPoints: number,
    checkedPoints: number,

}
interface ReduxPropsFunc {
    onAddNewTaskFunc: (creatorId: number, forUserId: number, challengeId: number)=>void;

}

class TaskTableHeaderInternal extends React.Component<Props & ReduxProps & ReduxPropsFunc ,void> {
    constructor(props) {
        super(props);
    }


    render() {

        return (<div >

            <TaskTableHeaderAccountPanel
                onOpenDialogForLoginSecondUser={this.props.onOpenDialogForLoginSecondUser}
                no={this.props.no}
                userId={this.props.user.id}
                userLabel={this.props.user.label}
                userLogin={this.props.user.login}
                challengeStatus={this.props.user.challengeStatus}
            >{this.props.children}</TaskTableHeaderAccountPanel>

            <div style={{display: "flex",flexFlow: "row nowrap", justifyContent:"space-between"}}>
                <span className="left" style={{margin: '3px'}}>Points: {this.props.checkedPoints}</span>

                <div className="right" style={{display: "inline-block"}}>
                    <FlatButton
                        onClick={()=>this.props.onAddNewTaskFunc(this.props.user.id, this.props.user.id, this.props.challengeId)}
                        label="Add task"
                        labelPosition="before"
                        primary={true}
                        style={{color: colors.userColors[this.props.no]}}
                    />
                </div>
            </div>
        </div>);
    }


}


const mapStateToProps = () => {
    // component instance
    var calculateAllAndCheckedCount = makeCalculateAllAndCheckedCount();


    return (state: ReduxState, ownProps: Props): ReduxProps => {
        var obj = calculateAllAndCheckedCount(state, ownProps.user.id);
        return {
            allPoints: obj.allPoints,
            checkedPoints: obj.checkedPoints
        };
    };
}
const mapDispatchToProps = (dispatch): ReduxPropsFunc => {
    return {
        onAddNewTaskFunc: (creatorId: number, forUserId: number, challengeId: number) => {
            dispatch(CREATE_AND_OPEN_EDIT_TASK.new({creatorId, forUserId, challengeId}))
        }

    }
};
export const TaskTableHeader = connect(mapStateToProps as any, mapDispatchToProps)(TaskTableHeaderInternal) as React.ComponentClass<Props>;



