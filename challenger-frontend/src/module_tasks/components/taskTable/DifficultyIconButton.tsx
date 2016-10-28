import * as React from "react";
import IconButton from "material-ui/IconButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../../../views/Constants";
import {TaskDTO} from "../../TaskDTO";


interface Props {
    no:number,
    task:TaskDTO,
}

interface PropsFunc {
    onEditTask?:(task:TaskDTO)=>void;
    onShowTaskEvents?:(task:TaskDTO, no:number)=>void;
}

export default class DifficultyIconButton extends React.Component<Props & PropsFunc,void> {
    constructor(props) {
        super(props);
    }

    onEditTask = () => {
        if (this.props.onEditTask!=null)
            this.props.onEditTask(this.props.task);
        if (this.props.onShowTaskEvents!=null)
            this.props.onShowTaskEvents(this.props.task, this.props.no);
    };


    render() {
        var background;
        var styleName = {fill: "#80deea", width: '40px', height: '40px'}; //cyan-lighten3
        if (this.props.no == 0)
            styleName = {fill: "#ffcc80", width: '40px', height: '40px'};

        if (this.props.task.difficulty == 0)
            background = <DiffSimpleIcon className="centered fa-stack-2xz" style={styleName}/>;
        else if (this.props.task.difficulty == 1)
            background = <DiffMediumIcon className="centered fa-stack-2xz" style={styleName}/>;
        else
            background = <DiffHardIcon className="centered fa-stack-2xz" style={styleName}/>;
        var icon;
        if (this.props.task.icon.startsWith("fa-")) {
            icon = <i className={'fa ' + this.props.task.icon + ' centered'}
                      style={{fontSize: '15px', textAlign: 'center'}}></i>;
        } else icon = <i className="material-icons centered"
                         style={{paddingLeft: '8px', fontSize: '15px'}}>{this.props.task.icon}</i>;
        return <IconButton className="center"
                           style={{width: '50px', height: '50px', padding: '0px', marginTop: '4px'}}
                           onClick={this.onEditTask}
        >
            <div className="" style={{}}>
                {background}{icon}
            </div>
        </IconButton>//;

    };

};

