import * as React from "react";
import IconButton from "material-ui/IconButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../../../views/Constants";
import {TaskDTO} from "../../TaskDTO";
import {globalPopoverReff} from "../../../views/common-components/GlobalPopover";
import * as ReactDOM from "react-dom";
import ReactInstance = __React.ReactInstance;


interface Props {
    no: number,
    task: TaskDTO
    showTooltip: boolean

}

interface PropsFunc {
    onEditTask?: (task: TaskDTO)=>void;
    onShowTaskEvents?: (task: TaskDTO, no: number)=>void;
}


export default class DifficultyIconButton extends React.Component<Props & PropsFunc,void> {
    constructor(props) {
        super(props);

    }

    onEditTask = () => {
        if (this.props.onEditTask != null)
            this.props.onEditTask(this.props.task);

    };
    onShowTaskEvents = () => {
        if (this.props.onShowTaskEvents != null)
            this.props.onShowTaskEvents(this.props.task, this.props.no);
    }


    editPopup = () => {
        return <div>
            <a onClick={()=>{this.onShowTaskEvents(); globalPopoverReff.globalPopover.closePopover(); }} style={{lineHeight:'16px', margin:'5px', color:'#444', cursor:"pointer"}} className="fa fa-comment"/>
            <a onClick={()=>{this.onEditTask(); globalPopoverReff.globalPopover.closePopover(); }} style={{lineHeight:'16px', margin:'5px', color:'#444', cursor:"pointer"}} className="fa fa-info-circle"/>
        </div>;

    }

    onMouseEnter = (ev) => {
        ev.preventDefault()
        ev.stopPropagation()
        if (this.props.showTooltip) {
            var content: JSX.Element = this.editPopup();
            globalPopoverReff.globalPopover.showPopover(content, ReactDOM.findDOMNode(this))
        }
    }



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

        >
            <div className=""
                 onClick={this.onEditTask} onMouseOver={ this.onMouseEnter}


                 style={{display:"block", height: '50px'}}


            >
                {background}{icon}

            </div>


        </IconButton>//;

    };

};

