import * as React from "react";
import Checkbox from "material-ui/Checkbox";
import {TaskDTO} from "../../TaskDTO";
import colors from "../../../views/common-components/Colors";



interface Props {
    no: number,
    authorized: boolean,
    taskDTO: TaskDTO,
    showAuthorizePanelFunc: (event: EventTarget, isInputChecked:boolean)=>(boolean);
    onTaskCheckedStateChangedFunc: (task: TaskDTO)=>void;
}
interface State {
    taskDTO: TaskDTO
}

export default class ChallengeTableCheckbox extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            taskDTO: this.props.taskDTO

        };
    }

    render() {
        var checkbox;
        //if (this.state.taskDTO.taskStatus == TaskStatus.failed)
        //    checkbox = <div>Failed</div>;
        //else {
            var onCheck = (taskId) => (event, isInputChecked) => {
                if (!this.props.showAuthorizePanelFunc(event.currentTarget, isInputChecked)) {
                    this.state.taskDTO.done=isInputChecked;
                    //if (isInputChecked)
                    //    this.state.taskDTO.taskStatus = TaskStatus.done;
                    //else
                    //    this.state.taskDTO.taskStatus = TaskStatus.pending;
                    this.setState(this.state);
                    this.props.onTaskCheckedStateChangedFunc(this.state.taskDTO);
                }

            };

            checkbox = <Checkbox
                key="statusCb"
                checked={this.props.taskDTO.done === true}
                onCheck={onCheck(this.props.taskDTO.id)}
                iconStyle={{fill: this.props.authorized ? colors.userColors[this.props.no] : "lightgrey"}}
                style={{display: 'inline-block', width: '30px'}}/>;
        //}
        return checkbox;
    }

};
