import * as React from "react";
import {LongCallVisible, ReduxState} from "../redux/ReduxState";
import {CircularProgress} from "material-ui";
import store from "../redux/store";
import {checkForTooLongWebCalls} from "../redux/actions/dayActions";
import {connect} from "react-redux";

interface Props {
    longCallVisible?: LongCallVisible
}

store.dispatch(checkForTooLongWebCalls());

const GlobalWebCallProgressInternal = (props: Props)=> {

    function getColor(longCallVisible: LongCallVisible): string {
        switch (props.longCallVisible) {
            case LongCallVisible.LONG:
                return "orange";
            case LongCallVisible.VERY_LONG:
                return "red";
            case LongCallVisible.FROM_START:
                return "#26C6DA"; // cyan
            case LongCallVisible.ERROR:
                return "red"; // cyan
        }
    }
    return props.longCallVisible != null ?
        <CircularProgress color={getColor(props.longCallVisible)}
                          size={30}
                          style={{right: '10px',top: '60px',position:"fixed"}}/>
        : null
}


const mapStateToProps = (state: ReduxState): Props => {
    return {
        longCallVisible: state.currentSelection.longCallVisible
    }
}

export var GlobalWebCallProgress = connect(mapStateToProps)(GlobalWebCallProgressInternal);

