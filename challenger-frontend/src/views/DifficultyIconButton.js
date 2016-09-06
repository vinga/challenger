import React, {Component} from "react";
import IconButton from 'material-ui/IconButton';
import {DiffSimpleIcon, DiffMediumIcon, DiffHardIcon} from "../Constants";


export default class DifficultyIconButton extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        var background;
        var styleName = {fill: "#80deea", width: '40px', height: '40px'}; //cyan-lighten3
        if (this.props.no == 0)
            styleName = {fill: "#ffcc80", width: '40px', height: '40px'};

        if (this.props.difficulty == 0)
            background = <DiffSimpleIcon className="fa-stack-2x" style={styleName}/>;
        else if (this.props.difficulty == 1)
            background = <DiffMediumIcon className="fa-stack-2x" style={styleName}/>;
        else
            background = <DiffHardIcon className="fa-stack-2x" style={styleName}/>;
        var icon;
        if (this.props.icon.startsWith("fa-")) {
            icon = <i className={'fa ' + this.props.icon + ' valign fa-stack-1x center-align'}
                      style={{paddingLeft: '12px', fontSize:'15px'}}></i>;
        } else icon = <i className="material-icons valign fa-stack-1x center-align"
                         style={{paddingLeft: '8px', fontSize:'15px'}}>{icon}</i>;
        return <IconButton style={{width:'40px', padding:'0px', marginTop:'6px'}}><div className="fa-stack valign-wrapper">{background}{icon}</div></IconButton>//;

    }

};