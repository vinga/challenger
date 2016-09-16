import * as React from 'react';
import HTMLAttributes = __React.HTMLAttributes;


/*interface Props extends HTMLAttributes {
    icon: string
}*/

export default class ChallengeIcon extends React.Component<{icon: string, style?:{}},void> {
    constructor(props) {
        super(props);

    }

    render() {
        var icon;
        if (this.props.icon.startsWith("fa-")) {
            var cssClasses = 'fa ' + this.props.icon;
            icon = <i style={this.props.style} className={cssClasses}></i>;
        } else icon = <i style={this.props.style} className="material-icons">{this.props.icon}</i>;
        return icon;
    }
}