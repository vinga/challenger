import * as React from "react";
import Checkbox from "material-ui/Checkbox";
import {RowCol} from "../../../views/common-components/Flexboxgrid";

interface Props {
    days: string
    onDaysChanged: (monthdays: string)=>void;
}
interface State {
    weekdays: boolean[]
}
const ALL_DAYS_IN_WEEK = 7;
export class WeekdaysGroup extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        var weekdays = [];


        for (var i = 0; i < 7; i++) {
            weekdays[i] = false;
        }
        var mm=props.days;
        if (mm!=null)
        mm.split(",").filter(d=>d!="").forEach(
            d =>  weekdays[d-1] = true
        )
        if (mm==null || mm=="") {
            for (var i = 0; i < 7; i++) {
                weekdays[i] = true;
            }
        }
        this.state={weekdays}
    }
    private mapNumberToWeekDay(dayIndex): string {
        return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"][dayIndex];
    }

    handleOnCheckWeekday = (index: number, isInputChecked: boolean) => {
        // all are checked, uncheking first one
        if (isInputChecked == false && this.state.weekdays.filter(e=>e == true).length == ALL_DAYS_IN_WEEK) {
            for (var i = 0; i < ALL_DAYS_IN_WEEK; i++) {
                this.state.weekdays[i] = false;
            }
            this.state.weekdays[index] = true;
        }
        // only one checked, uncheking it
        else if (isInputChecked == false && this.state.weekdays.filter(e=>e == true).length==1 && this.state.weekdays[index]==true) {
            for (var i = 0; i < ALL_DAYS_IN_WEEK; i++) {
                this.state.weekdays[i] = true;
            }
        }
        else
            this.state.weekdays[index] = isInputChecked;

        this.setState(this.state);

        var days = this.state.weekdays.map((t, index) => {
            if(t == true)
                return index+1;
            else
                return null;
        }).filter(t => t != null).join(",");
        if(days != "") {
            days = "," + days + ",";
        }
        this.props.onDaysChanged(days);
    }

    render() {
        return <div> {
            this.state.weekdays.map(
                (checked, index) =>
                    <Checkbox
                        key={index}
                        label={this.mapNumberToWeekDay(index)}
                        checked={checked}
                        onCheck={(event: any, isInputChecked: boolean) => this.handleOnCheckWeekday(index, isInputChecked)}
                    />
            )
        }</div>
    }
}