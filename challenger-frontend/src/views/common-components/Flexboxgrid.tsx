import * as React from "react";
import CSSProperties = __React.CSSProperties;

const sizes = ["xs", "sm", "md", "lg"];

/**
 * translates shortcut to full class names
 *  Example: 12-6  to col-xs-12 cols-sm-6
 * @param shortcut example
 */

const append = (strall: string, strnew: string): string => {
    if (strall == "")
        return strnew;
    else return strall + " " + strnew;
}
export const Grid = (props: { cols?: string, children?: any  }) => {
    const className = (): string => {
        return "";
    }

    return <div className={className()}>{props.children}{className()}</div>;

}

interface RowProps {
    horizontal?: string,
    vertical?: string,
    distribution?: string,
}
interface ColProps {
    col?: string,
    offset?: string
}


export const Row = (props: RowProps & {  style?: CSSProperties,   children?: any  }) => {
    const horizontalsToClasses = (shortcut: string): string => {
        var classNames = shortcut.split("-").map((e, iter)=> {
            if (e != "center" && e != "start" && e != "end") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
        return classNames;
    }
    const verticalsToClasses = (shortcut: string): string => {
        var classNames = shortcut.split("-").map((e, iter)=> {
            if (e != "top" && e != "middle" && e != "bottom") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
        return classNames;
    }
    const distributionToClasses = (shortcut: string): string => {
        var classNames = shortcut.split("-").map((e, iter)=> {
            if (e != "around" && e != "between") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
        return classNames;
    }
    const className = (): string => {
        var classes = "row ";

        if (props.horizontal) {
            classes = append(classes, horizontalsToClasses(props.horizontal));
        }
        if (props.vertical) {
            classes = append(classes, verticalsToClasses(props.vertical));
        }
        if (props.distribution) {
            classes = append(classes, distributionToClasses(props.distribution));
        }

        return classes;
    }

    return <div className={className()} style={props.style}>{props.children}</div>;
}


export const Col = (props: ColProps & { style?: CSSProperties, children?: any  }) => {
    const colsToClasses = (shortcut: string): string => {
        var classNames = shortcut.split("-").map((e, iter)=> {
            return "col-" + sizes[iter] + "-" + e;
        }).join(" ");
        return classNames;
    }
    const offsetToClasses = (shortcut: string): string => {
        var classNames = shortcut.split("-").map((e, iter)=> {
            return "col-" + sizes[iter] + "-offset-" + e;
        }).join(" ");
        return classNames;
    }

    const className = (): string => {
        var classes = "";

        if (props.col == null || props == "") {
            classes = append(classes, "col-xs");
        } else {
            classes = append(classes, colsToClasses(props.col));
        }
        if (props.offset) {
            classes = append(classes, offsetToClasses(props.offset));
        }

        return classes;
    }

    return <div className={className()} style={props.style}>{props.children}</div>;

}

export const RowCol = (props: RowProps & ColProps & { rowStyle?: CSSProperties,colStyle?: CSSProperties, children?: any  }) => {
    return <Row {...props} style={props.rowStyle}><Col {...props} style={props.colStyle}>{props.children}</Col></Row>;
}

/*

// //declare namespace __ReactFlexboxGrid {
// type RowPropsModificatorType = 'xs' | 'sm' | 'md' | 'lg';
// type ColPropsModificatorType = number | boolean;
//
// export interface GridProps {
//     readonly fluid?: boolean,
//     readonly className?: string,
//     readonly tagName?: string,
// }
//
// export interface RowProps {
//     readonly reverse?: boolean,
//     readonly start?: RowPropsModificatorType,
//     readonly center?: RowPropsModificatorType,
//     readonly end?: RowPropsModificatorType,
//     readonly top?: RowPropsModificatorType,
//     readonly middle?: RowPropsModificatorType,
//     readonly bottom?: RowPropsModificatorType,
//     readonly around?: RowPropsModificatorType,
//     readonly between?: RowPropsModificatorType,
//     readonly first?: RowPropsModificatorType,
//     readonly last?: RowPropsModificatorType,
//     readonly className?: string,
//     readonly tagName?: string,
// }
//
// export interface ColProps {
//     readonly xs?: ColPropsModificatorType,
//     readonly sm?: ColPropsModificatorType,
//     readonly md?: ColPropsModificatorType,
//     readonly lg?: ColPropsModificatorType,
//     readonly xsOffset?: number,
//     readonly smOffset?: number,
//     readonly mdOffset?: number,
//     readonly lgOffset?: number,
//     readonly reverse?: boolean,
//     readonly className?: string,
//     readonly tagName?: string,
// }
//
// export class Grid extends Component<GridProps, {}> {
//
// }
//
// export class Row extends Component<RowProps, {}> {
//
// }
//
// export class Col extends Component<ColProps, {}> {
//
// }
// //}
//
// //export = __ReactFlexboxGrid;
*/