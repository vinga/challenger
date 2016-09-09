import React, {Component} from "react";
import colors from "../../logic/Colors"

export default class TaskTableUserIcon extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            iconSvg: null
        }

    }


    render() {
        var className = "fa fa-circle fa-stack-2x " + colors.userColorsTextClass[this.props.userNo];

        var ucolor = colors.userColorsLighten[this.props.userNo];
        var userIcon = (
            <div  className="left" style={{width: '100%', overflow:'hidden'}}>
                <div style={{
                    position: 'relative', left: '33px',
                    right: '160px',
                    top: 64,
                    fontSize: "22px", paddingLeft: 60, paddingBottom:13,borderBottom: '2px solid ' + ucolor
                }}>{this.props.userName}</div>
                <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <i className="fa fa-user fa-stack-1x fa-inverse" style={{opacity: 0.9}}></i>


               <div style={{
                   textAlign: "left",
                   fontSize: "14px",
                   position: "relative",
                   left: 62,
                   top: 22
               }}>{this.props.counts}</div>
                        </span>


            </div>

        );
 /*       if (this.state.iconSvg != null) {


            var color = colors.userColorsLighten[this.props.userNo];
            color = "white";

            var obj = {__html: this.state.iconSvg};

            var userIcon = (
                <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <div className="fa-stack-1x"
                                 style={{color: color, marginLeft: '0px', marginTop: '7px', fill: color, stroke: color}}
                                 dangerouslySetInnerHTML={obj}/>
                        </span>);

        }*/

        return userIcon;
    }
}