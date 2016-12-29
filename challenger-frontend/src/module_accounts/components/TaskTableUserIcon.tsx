import * as React from "react";
import {ChallengeStatus} from "../../module_challenges/ChallengeDTO";
import {getColor} from "../../views/common-components/Colors";

export default class TaskTableUserIcon extends React.Component<{userNo:number, challengeStatus: string},void> {
    constructor(props) {
        super(props);
    }


    render() {
        var className = "fa fa-circle fa-stack-2x ";

        var ucolor = getColor(this.props.userNo);
        var style: any={opacity: 0.9, fontSize: '24px'}
        var userIconName = "fa-user";
        if(this.props.challengeStatus == ChallengeStatus.WAITING_FOR_ACCEPTANCE) {
            userIconName = "fa-hourglass-half"
            style.opacity= 0.7;
            style.fontSize="20px"
        }
        else if(this.props.challengeStatus == ChallengeStatus.REFUSED)
            userIconName = "fa-minus-square"
        var userIcon = (
            <div  style={{ overflow: 'hidden'}}>

                <span className="fa-stack fa-lg" style={{marginRight: '10px'}}>
                            <i className={className} style={{color: ucolor}}></i>
                            <i className={"fa " + userIconName + " fa-stack-1x fa-inverse"} style={style}></i>


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