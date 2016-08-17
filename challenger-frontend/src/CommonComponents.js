import React, { Component } from 'react';




export default class ChallengeIcon extends React.Component {
    constructor(props) {
        super(props);

    }

    render() {
        var icon;
        if (this.props.icon.startsWith("fa-")) {
            var cssClasses = 'fa ' + this.props.icon;
            icon = <i {...this.props} className={cssClasses}></i>;
        } else icon = <i {...this.props} className="material-icons">{this.props.icon}</i>;
        return icon;
    }
}