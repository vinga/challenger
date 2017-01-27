import * as React from "react";

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
    horizontal?: "start"|"center"|"end",
    vertical?: string,
    distribution?: string,
}
interface ColProps {
    col?: string,
    offset?: string
}


export const Row = (props: RowProps & {  style?: React.CSSProperties,   children?: any  }) => {
    const horizontalsToClasses = (shortcut: string): string => {
        return shortcut.split("-").map((e, iter)=> {
            if (e != "center" && e != "start" && e != "end") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
    }
    const verticalsToClasses = (shortcut: string): string => {
        return shortcut.split("-").map((e, iter)=> {
            if (e != "top" && e != "middle" && e != "bottom") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
    }
    const distributionToClasses = (shortcut: string): string => {
        return shortcut.split("-").map((e, iter)=> {
            if (e != "around" && e != "between") {
                throw new Error("Invalid value: " + e);
            }
            return e + "-" + sizes[iter];
        }).join(" ");
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


export const Col = (props: ColProps & { style?: React.CSSProperties, children?: any  }) => {
    const colsToClasses = (shortcut: string): string => {
        return shortcut.split("-").map((e, iter)=> {
            return "col-" + sizes[iter] + "-" + e;
        }).join(" ");
    }
    const offsetToClasses = (shortcut: string): string => {
        return shortcut.split("-").map((e, iter)=> {
            return "col-" + sizes[iter] + "-offset-" + e;
        }).join(" ");
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

export const RowCol = (props: RowProps & ColProps & { rowStyle?: React.CSSProperties,colStyle?: React.CSSProperties, children?: any  }) => {
    return <Row {...props} style={props.rowStyle}><Col {...props} style={props.colStyle}>{props.children}</Col></Row>;
}

