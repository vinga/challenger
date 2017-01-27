import * as React from "react";
import HTMLAttributes = React.HTMLAttributes;



export var ResizeAware:any;
export var HidableComponent:any;


import ReactElement = React.ReactElement;
import Component = React.Component;


export class DiffHardIcon extends React.Component<HTMLAttributes<any>,void> {
}
export class DiffSimpleIcon extends React.Component<HTMLAttributes<any>,void> {
}
export class DiffMediumIcon extends React.Component<HTMLAttributes<any>,void> {
}
declare global {
    interface Date {
        yy_mm_dd(): string;
        yyyy_mm_dd(): string;
        yyyyxmmxdd(): string;
        mm_dd(): string;
        dayMonth3(): string;

    }


}



