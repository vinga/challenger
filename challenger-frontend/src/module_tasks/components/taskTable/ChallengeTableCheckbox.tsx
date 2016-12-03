import * as React from "react";
import Checkbox from "material-ui/Checkbox";
import {TaskDTO} from "../../TaskDTO";
import colors from "../../../views/common-components/Colors";


interface Props {
    no: number,
    userId: number,
    authorized: boolean,
    taskDTO: TaskDTO,
    showAuthorizeFuncIfNeeded: (event: EventTarget, userId: number)=>(Promise<boolean>);
    onTaskCheckedStateChangedFunc: (task: TaskDTO)=>void;
}

export default class TaskCheckbox extends React.Component<Props, void> {

    render() {

        var onCheck = (taskId) => (event, isInputChecked) => {
            this.props.showAuthorizeFuncIfNeeded(event.currentTarget, this.props.userId).then((b)=> {
                if (b == true) {
                    var newTask = Object.assign({}, this.props.taskDTO);
                    newTask.done = isInputChecked;
                    this.props.onTaskCheckedStateChangedFunc(newTask);
                }
            })
        };

        return <Checkbox
            key="statusCb"
            checked={this.props.taskDTO.done === true}
            onCheck={onCheck(this.props.taskDTO.id)}
            iconStyle={{fill: this.props.authorized ? colors.userColors[this.props.no] : "lightgrey"}}
            style={{display: 'inline-block', width: '30px'}}/>;

    }

};
