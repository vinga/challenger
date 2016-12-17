import * as React from "react";
import Checkbox from "material-ui/Checkbox";
import {RowCol} from "../../../views/common-components/Flexboxgrid";

interface Props {
    days: string
    onDaysChanged: (monthdays: string)=>void;
}
interface State {
    monthdays: boolean[]
}
const ALL_DAYS_IN_MONTH = 31;
export class MonthdaysGroup extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        var monthdays = [];

        for (var i = 0; i < ALL_DAYS_IN_MONTH; i++) {
            monthdays[i] = false;
        }
        var mm = props.days;
        if (mm != null)
            mm.split(",").filter(d=>d!="").forEach(
                d => monthdays[d] = true
            )
        if (mm == null || mm == "")
            for (var i = 0; i < ALL_DAYS_IN_MONTH; i++) {
                monthdays[i] = true;
            }
        this.state = {monthdays}
    }

    handleOnCheckMonthdays = (index: number, isInputChecked: boolean) => {

        // all are checked, uncheking first one
        if (isInputChecked == false && this.state.monthdays.filter(e=>e == true).length == ALL_DAYS_IN_MONTH) {
            for (var i = 0; i < ALL_DAYS_IN_MONTH; i++) {
                this.state.monthdays[i] = false;
            }
            this.state.monthdays[index] = true;
        }
        // only one checked, uncheking it
        else if (isInputChecked == false && this.state.monthdays.filter(e=>e == true).length==1 && this.state.monthdays[index]==true) {
            for (var i = 0; i < ALL_DAYS_IN_MONTH; i++) {
                this.state.monthdays[i] = true;
            }
        }
        else
            this.state.monthdays[index] = isInputChecked;
        this.setState(this.state);

        var days = this.state.monthdays.map((t, index) => {
            if (t == true)
                return index;
            else
                return null;
        }).filter(t => t != null).join(",");
        if (days != "") {
            days = "," + days + ",";
        }
        this.props.onDaysChanged(days);
    }

    render() {
        return <RowCol colStyle={{display:"flex", flexDirection: "row", flexWrap:"wrap", maxWidth:"200px"}}>
            {
                this.state.monthdays.map(
                    (checked, index) => <div key={index} style={{ width:"25px", fontSize: '10px', textAlign:"center"}}>
                        <Checkbox
                            style={{ textAlign:"left"}}
                            checked={checked}
                            onCheck={(event: any, isInputChecked: boolean) => this.handleOnCheckMonthdays(index, isInputChecked)}
                        />
                        <div >{index + 1}</div>
                    </div>
                )
            }
        </RowCol>
    }
}