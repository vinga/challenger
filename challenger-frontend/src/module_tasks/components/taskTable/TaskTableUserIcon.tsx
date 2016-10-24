import * as React from "react";
import colors from "../../../views/common-components/Colors";

export default class TaskTableUserIcon extends React.Component<{userNo:number},void> {
    constructor(props) {
        super(props);
    }


    render() {
        var className = "fa fa-circle fa-stack-2x " + colors.userColorsTextClass[this.props.userNo];

        /*var ucolor = colors.userColorsLighten[this.props.userNo];*/
        var userIcon = (
            <div className="left" style={{ overflow: 'hidden', display: 'inline-block'}}>

                <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className}></i>
                            <i className="fa fa-user fa-stack-1x fa-inverse" style={{opacity: 0.9}}></i>


               <div style={{
                   textAlign: "left",
                   fontSize: "14px",
                   position: "relative",
                   left: 62,
                   top: 22
               }}/>



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