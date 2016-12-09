import * as React from "react";
import CSSProperties = __React.CSSProperties;

interface Props {
    tooltip: JSX.Element | string,
    tooltipStyle?: CSSProperties,
    delay?: string // eg 2s
}

export class Tooltip extends React.Component<Props,void> {

    render() {

        var style=this.props.tooltipStyle;
        if (this.props.delay!=null) {
            style=Object.assign({},style,{transitionDelay: this.props.delay});
        }

        return <div  className="tooltip">
            {this.props.children}
            <span className="tooltiptext" style={style}>{this.props.tooltip}</span>
        </div>
    }

}