import React, {Component} from "react";
import IconButton from "material-ui/IconButton";
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../Constants";
import ChallengeEditDialogWindow from "../ChallengeEditDialogWindow";

export default class DifficultyIconButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showNewWindow: false
        };
    }

    onEditTask = () => {
        this.setState({
            showNewWindow: true
        })
    }
    handleEditWindowClose = () => {
        this.setState({
            showNewWindow: false
        });
    }

    render() {
        var background;
        var styleName = {fill: "#80deea", width: '40px', height: '40px'}; //cyan-lighten3
        if (this.props.no == 0)
            styleName = {fill: "#ffcc80", width: '40px', height: '40px'};

        if (this.props.taskDTO.difficulty == 0)
            background = <DiffSimpleIcon className="centered fa-stack-2xz" style={styleName}/>;
        else if (this.props.taskDTO.difficulty == 1)
            background = <DiffMediumIcon className="centered fa-stack-2xz" style={styleName}/>;
        else
            background = <DiffHardIcon className="centered fa-stack-2xz" style={styleName}/>;
        var icon;
        if (this.props.taskDTO.icon.startsWith("fa-")) {
            icon = <i className={'fa ' + this.props.taskDTO.icon + ' centered'}
                      style={{fontSize: '15px', textAlign: 'center'}}></i>;
        } else icon = <i className="material-icons centered"
                         style={{paddingLeft: '8px', fontSize: '15px'}}>{this.props.taskDTO.icon}</i>;
        return <IconButton className="center"
                           style={{width: '50px', height: '50px', padding: '0px', marginTop: '4px'}}
                           onClick={this.onEditTask}
        >
            <div className="" style={{}}>
                {background}{icon}
            </div>
            {this.state.showNewWindow &&
            <ChallengeEditDialogWindow open={this.state.showNewWindow} taskDTO={this.props.taskDTO}
                                       onClose={this.handleEditWindowClose}
                                       onChallengeActionSuccessfullyUpdated={this.props.onChallengeActionSuccessfullyUpdated}
            />
            }
        </IconButton>//;

    }

};

DifficultyIconButton.propTypes = {
    taskDTO: React.PropTypes.object.isRequired,
    //showAuthorizePanelFunc: React.PropTypes.func.isRequired,
    onTaskSuccessfullyUpdatedFunc: React.PropTypes.func.isRequired,
}