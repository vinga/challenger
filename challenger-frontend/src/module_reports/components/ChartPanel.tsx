import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import {ReportUserDTO, ReportType, ReportDataDTO, ChallengeReportsDTO} from "../ReportUserDTO";
import {getColorSuperlightenForUser, getColorLightenForUser} from "../../views/common-components/Colors";
import Chart = require("chart.js");
import {progressiveReportsSelector} from "../reportSelectors";



interface Props {
    challengeId: number,
    user: ReportUserDTO,

}
interface ReduxProps {
   report?: ReportDataDTO
}
class ChartPanelInternal extends React.Component<Props & ReduxProps, void> {

    chart:any;
    getChartIdString = () => {
       return "chartPanelInternal"+this.props.user.ordinal;
    }
    componentDidMount = () => {

        this.chart = new Chart(this.getChartIdString() as any, {
            type: 'line',
            data: {
                labels: this.props.report!=null? this.props.report.labels: [],
                datasets: [{
                    label: this.props.user.label,
                    data: this.props.report!=null? this.props.report.values: [],
                    backgroundColor: getColorLightenForUser(this.props.user.ordinal, 50),
                    borderWidth: 1
                }]
            },
            options: {
                maintainAspectRatio: false,
                elements: { point: { radius: 0 } },
                legend: {
                    display: false
                },
                scales: {
                    xAxes: [{
                        display: false,
                    }],
                    yAxes: [{
                        display: false,
                        ticks: {
                            beginAtZero: true,
                            max: this.props.report!=null? this.props.report.maxValue: 100
                        }
                    }]
                }
            }
        });
    }

    componentDidUpdate = () => {
        if (this.chart!=null && this.props.report!=null) {
            this.chart.data.datasets[0].data=this.props.report.values;
            this.chart.data.labels=this.props.report.labels;
            this.chart.options.scales.yAxes[0].ticks.max=this.props.report.maxValue;
            this.chart.update();
        }
    }


    render() {


        return <div style={{width:"100%",display:"block", float:"right", overflow:"hidden"}} >
            <canvas id={this.getChartIdString()} height="70px" ></canvas>
        </div>;

    }
}

const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {
    return {
        report: progressiveReportsSelector(state).find(rep=>rep.userId==ownProps.user.id)
    }
};


export const ChartPanel = connect(mapStateToProps)(ChartPanelInternal);
