import React, {Component} from "react";
import {TaskStatus} from "../Constants";
import Chip from "material-ui/Chip";
import colors from "../../logic/Colors";

const styles = {
    wrapper: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        marginRight: '5px',
        cursor: 'pointer'
    },

}

export default class TaskLabel extends React.Component {

    render() {


        if (this.props.taskDTO.taskStatus != TaskStatus.waiting_for_acceptance) {
            return <div>{this.props.taskDTO.label}</div>

        } else if (!this.props.authorized && this.props.taskDTO.createdByUserId!=this.props.userId) {

            var chipWaiting= {
                marginRight: '5px',
                    cursor: 'pointer',
                    backgroundColor: 'white',
                color: 'red!important'
            }
            return (<div style={styles.wrapper}>

                <div className="taskLabel">{this.props.taskDTO.label}</div>

                <Chip style={chipWaiting} className="clickableChip">
                    <div style={{lineHeight:'12px',fontSize: '12px',color:colors.userColorsLighten[this.props.no]}}>Waiting for {this.props.userLabel}'s<br/> acceptance  <i className="fa fa-hourglass-o"></i></div>
                </Chip>

               </div>);


        } else {

            return (<div style={styles.wrapper}>

                <div className="taskLabel">{this.props.taskDTO.label}</div>

                <Chip style={styles.chip} className="clickableChip">
                    <i className="fa fa-check"></i> Accept
                </Chip>

                <Chip style={styles.chip} className="clickableChip">
                    <i className="fa fa-close"></i> Reject
                </Chip></div>);
        }
    }
}