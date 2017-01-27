import * as React from "react";
import IconButton from "material-ui/IconButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../../../views/Constants";
import {TaskDTO} from "../../TaskDTO";
import {globalPopoverReff} from "../../../views/common-components/GlobalPopover";
import * as ReactDOM from "react-dom";
import {Tooltip} from "../../../views/common-components/Tooltip";
import {YesNoConfirmationDialog} from "../../../views/common-components/YesNoConfirmationDialog";
import {getColorLightenForUser} from "../../../views/common-components/Colors";


//import * as Tooltip from "@cypress/react-tooltip";


interface Props {
    no: number,
    task: TaskDTO
    showTooltip: boolean
    onCloseTask?: (task: TaskDTO)=>void;
    userIsLogged: boolean
}

interface PropsFunc {
    onEditTask?: (task: TaskDTO)=>void;

    onShowTaskEvents?: (task: TaskDTO, no: number, toggle: boolean)=>void;
}
interface State {
    showCloseTask?: TaskDTO
}

export default class DifficultyIconButton extends React.Component<Props & PropsFunc,State> {
    constructor(props) {
        super(props);
        this.state = {showCloseTask: null}
    }


    onEditTask = () => {
        if (this.props.onEditTask != null)
            this.props.onEditTask(this.props.task);

    };

    onCloseTask = () => {
        this.setState({showCloseTask: this.props.task})
        // if (this.props.onCloseTask != null)
        //    this.props.onCloseTask(this.props.task);

    };
    onShowTaskEvents = () => {
        if (this.props.onShowTaskEvents != null)
            this.props.onShowTaskEvents(this.props.task, this.props.no, true);
    }


    editPopup = () => {
        var style = {lineHeight: '16px', margin: '5px', color: '#444', cursor: "pointer"};
        return <div>

            {/*  <Tooltip tooltip="Filter events" delay="1s">
             <a onClick={()=>{this.onShowTaskEvents(); globalPopoverReff.globalPopover.closePopover(); }} style={style} className=" fa fa-comment"/>
             </Tooltip>*/}


            <Tooltip tooltip="Show details" delay="1s">
                <a onClick={()=>{this.onEditTask(); globalPopoverReff.globalPopover.closePopover(); }} style={style} className="fa fa-info-circle"/>
            </Tooltip>


            {this.props.task.closeDate == null && this.props.userIsLogged &&
            <Tooltip tooltip="Close task" delay="1s">
                <a onClick={()=>{this.onCloseTask(); globalPopoverReff.globalPopover.closePopover(); }} style={style} className="fa fa-close"/>
            </Tooltip>



            }


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
        var userColor = getColorLightenForUser(this.props.no)
        var background;
        var styleName = {fill: userColor, width: '40px', height: '40px'}; //cyan-lighten3


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
                 onClick={this.onShowTaskEvents} onMouseOver={ this.onMouseEnter}


                 style={{display:"block", height: '50px'}}


            >
                {background}{icon}

            </div>
            {this.state.showCloseTask != null &&
            <YesNoConfirmationDialog closeYes={ ()=> this.props.onCloseTask(this.props.task)} closeDialog={()=>this.setState({showCloseTask: null})}>
                Do you want to close task <b>{this.state.showCloseTask.label}</b>? It won't be available in the future.
            </YesNoConfirmationDialog> }

        </IconButton>//;

    };

};

