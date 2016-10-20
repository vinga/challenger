import * as React from "react";
import IconButton from "material-ui/IconButton";
import FontIcon from "material-ui/FontIcon";

const icons = [
    "fa-car",
    "fa-book",
    "fa-signal",
    "fa-puzzle-piece",
    "fa-clock-o",
    "fa-certificate",
    "fa-phone",
    "fa-comment",
    "fa-laptop",
    "fa-male",
    "fa-exclamation-circle",
    "fa-shopping-basket",
    "fa-heart",
    "fa-money",
    "fa-user-md",
    "fa-wrench",
    "fa-credit-card",
    "fa-star",
    "fa-circle",
    "fa-gift",
    "fa-child",
    "fa-bicycle",
    "fa-asterisk",
    "fa-bomb",
    "fa-cube"

];


export default class IconChooserGrid extends React.Component<{ onClick: (icon: string)=> void},void> {


    render() {
        return (<div>

            { icons.map((icon) => <IconButton
                    key={icon}
                    style={{width: 70, height: 70}}
                    onClick={()=>this.props.onClick(icon)}
                >
                    <FontIcon className={'fa ' + icon }
                                    hoverColor="orange"
                                    style={{fontSize: '30px', textAlign: 'center'}}>

                </FontIcon>
                </IconButton>
            ) }


        </div>);
    }
}