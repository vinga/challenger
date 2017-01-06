import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import TextFieldExt from "../../views/common-components/TextFieldExt.tsx";
import {Col, Row, RowCol} from "../../views/common-components/Flexboxgrid";
import {RaisedButton, CircularProgress} from "material-ui";
import {ConfirmationLinkRequestDTO, ConfirmationLinkResponseDTO} from "../AccountDTO";
import {getConfirmationLinkResponse} from "../accountActions";
import {CLEAR_CONFIRMATION_LINK_STATE, REGISTER_SHOW_REGISTRATION_PANEL} from "../accountActionTypes";


interface ReactProps {

}
interface PropsFunc {
    getConfirmationLinkResponse: (uid, confirmationLinkRequest: ConfirmationLinkRequestDTO)=>void;
    stateUid: string,
    clearConfirmationLinkState: () => void;
    handleOpenRegistrationWithFixedEmailFunc: (requiredEmail: string, proposedLogin: string, emailIsConfirmedByConfirmationLink: string) => void;
}

interface Props {
    confirmationLinkResponse?: ConfirmationLinkResponseDTO,
}


class ConfirmationPanelInternal extends React.Component<ReactProps &  PropsFunc & Props, void> {
    private newPasswordField: TextFieldExt;
    private newLoginField: TextFieldExt;


    componentDidMount = () => {
        var req: ConfirmationLinkRequestDTO = {}
        if (this.props.stateUid == null) {
            var c = "confirmation=";
            var cl = window.location.hash;
            var confirmationLinkString = cl.substring(cl.indexOf(c));
            this.props.getConfirmationLinkResponse(confirmationLinkString, req);
        }
    }

    handleConfirmationLink = () => {

        var resp=this.props.confirmationLinkResponse;
        if (resp.done && resp.displayRegisterButton) {
            this.props.handleOpenRegistrationWithFixedEmailFunc(resp.emailRequiredForRegistration, resp.loginProposedForRegistration, resp.emailIsConfirmedByConfirmationLink);
            this.props.clearConfirmationLinkState();
            return;
        }

        if(resp.done && this.props.confirmationLinkResponse.displayLoginButton) {
            this.props.clearConfirmationLinkState();
            return;
        }


        var req: ConfirmationLinkRequestDTO = {}
        if (this.props.confirmationLinkResponse.newPasswordRequired) {
            req.newPassword = this.newPasswordField.state.fieldValue
            if (!this.newPasswordField.checkIsValid()) {
                return;
            }
        }


        this.props.getConfirmationLinkResponse(this.props.stateUid, req);

    }

    render() {


        if (this.props.confirmationLinkResponse == null)
            return <RowCol horizontal="center">
                <CircularProgress />
            </RowCol>;


        var buttonTitle = "Ok";
        if (this.props.confirmationLinkResponse.newPasswordRequired) {
            buttonTitle = "Change Password";
        } else if (this.props.confirmationLinkResponse.displayLoginButton) {
            buttonTitle = "Login";
        } else if (this.props.confirmationLinkResponse.displayRegisterButton) {
            buttonTitle = "Register";
        }

        return (


            <div id="main" className="container ">
                <Row horizontal="center">

                    <Col col="3" style={{marginTop:"60px"}}>
                        <RowCol>

                        </RowCol>
                    </Col>

                </Row>


                <Row horizontal="center">
                    <Col col="8-5-3">

                        <RowCol>
                            <h5>{this.props.confirmationLinkResponse.description}</h5>
                        </RowCol>
                        {this.props.confirmationLinkResponse.validationError}

                        { this.props.confirmationLinkResponse.newPasswordRequired &&
                        <RowCol>
                            <TextFieldExt
                                ref={(c)=>{this.newPasswordField=c}}
                                fullWidth={true}
                                floatingLabelText="Password:"
                                type="password"
                                validateOnChange={true}
                                minLengthNumber={6}
                                maxLengthNumber={30}
                                useRequiredValidator={true}
                            />
                        </RowCol>
                        }
                        <RowCol colStyle={{paddingTop: '30px', paddingBottom: '30px'}}>
                            <div style={{display:"block"}}>
                                <RaisedButton
                                    fullWidth={true}
                                    label={buttonTitle}
                                    primary={true}
                                    className="right" onClick={this.handleConfirmationLink}/>
                            </div>
                        </RowCol>

                    </Col>

                </Row>


            </div >);
    }
}

const mapStateToProps = (state: ReduxState, props: Props): any => {
    if (state.confirmationLinkState == null)
        return {};

    var resp = state.confirmationLinkState.confirmationLinkResponse;
    if (resp != null) {
        return {
            confirmationLinkResponse: resp,
            stateUid: state.confirmationLinkState.uid
        }
    } else
        return {
            confirmationLinkResponse: null,
            stateUid: (state.confirmationLinkState != null) ? state.confirmationLinkState.uid : null
        }
};

const mapDispatchToProps = (dispatch): any => {
    return {
        getConfirmationLinkResponse: (uid, confirmationLinkRequest: ConfirmationLinkRequestDTO) => {
            dispatch(getConfirmationLinkResponse(uid, confirmationLinkRequest));
        },
        clearConfirmationLinkState: () => {
            dispatch(CLEAR_CONFIRMATION_LINK_STATE.new({}));
        },
        handleOpenRegistrationWithFixedEmailFunc: (requiredEmail: string, proposedLogin: string, emailIsConfirmedByConfirmationLink: string) =>  {
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({requiredEmail, proposedLogin, emailIsConfirmedByConfirmationLink }))
        }
    }
};


export const ConfirmationPanel = connect(
    mapStateToProps,
    mapDispatchToProps
)(ConfirmationPanelInternal);

