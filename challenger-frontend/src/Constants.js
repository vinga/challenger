import React from 'react';
import {blue500, red500, greenA200} from 'material-ui/styles/colors';
import SvgIcon from 'material-ui/SvgIcon';


const DiffHardIcon = (props) => {
    var wanted={};
    if (props.className!=undefined)
        wanted.className=props.className;
    if (props.style!=undefined)
        wanted.style=props.style;
    return (
        <SvgIcon {...wanted} viewBox="0 0 90 90">
            <path d="M90 45a45 45 0 0 1-45 45A45 45 0 0 1 0 45 45 45 0 0 1 45 0a45 45 0 0 1 45 45z"/>
        </SvgIcon>
    );
}

const DiffSimpleIcon = (props) => {

    var wanted={};
    if (props.className!=undefined)
        wanted.className=props.className;
    if (props.style!=undefined)
        wanted.style=props.style;
return (
    <SvgIcon {...wanted} viewBox="0 0 90 90">
        <path d="M90 45a45 45 0 0 1-45 45A45 45 0 0 1 0 45 45 45 0 0 1 45 0a45 45 0 0 1 45 45z" fillOpacity=".388"/>
        <path d="M75 45a30 30 0 0 1-30 30 30 30 0 0 1-30-30 30 30 0 0 1 30-30 30 30 0 0 1 30 30z" fillOpacity=".1"/>
        <path d="M60 45a15 15 0 0 1-15 15 15 15 0 0 1-15-15 15 15 0 0 1 15-15 15 15 0 0 1 15 15z"/>
    </SvgIcon>);
}

const DiffMediumIcon = (props) => {
    var wanted={};
    if (props.className!=undefined)
        wanted.className=props.className;
    if (props.style!=undefined)
        wanted.style=props.style;
    return (
        <SvgIcon {...wanted} viewBox="0 0 90 90">
            <path d="M90 45a45 45 0 0 1-45 45A45 45 0 0 1 0 45 45 45 0 0 1 45 0a45 45 0 0 1 45 45z" fillOpacity=".388"/>
            <path d="M75 45a30 30 0 0 1-30 30 30 30 0 0 1-30-30 30 30 0 0 1 30-30 30 30 0 0 1 30 30z"/>
        </SvgIcon>
    );
}


const ChallengeStatus = {
    INACIVE : "INACIVE",
    ACTIVE: "ACTIVE",
    WAITING_FOR_ACCEPTANCE: "WAITING_FOR_ACCEPTANCE",
    REFUSED:"REFUSED"
};

export { DiffHardIcon as DiffHardIcon,
 DiffMediumIcon as DiffMediumIcon,
 DiffSimpleIcon as DiffSimpleIcon,
ChallengeStatus as ChallengeStatus}
