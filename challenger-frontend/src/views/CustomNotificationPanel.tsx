import * as React from "react";
import {ReduxState} from "../redux/ReduxState";
import {connect} from "react-redux";
import {Col, RowCol, Row} from "./common-components/Flexboxgrid";

interface Props {
    closableText?: string
}
class CustomNotificationPanelInternal extends React.Component<Props, void> {


    render() {
        if (this.props.closableText == null)
            return null;


        return <Row horizontal="center">
            <Col col="8-5-3">
                <RowCol horizontal="start">
                    <h2><div dangerouslySetInnerHTML={{__html: this.props.closableText}}/></h2>
                </RowCol>
            </Col>
        </Row>;
    }
}

const mapStateToProps = (state: ReduxState): Props => {
    return {
        closableText: state.currentSelection.closableText
    }
}

export var CustomNotificationPanel = connect(mapStateToProps)(CustomNotificationPanelInternal);
