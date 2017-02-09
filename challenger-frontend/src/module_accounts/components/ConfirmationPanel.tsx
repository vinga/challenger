import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import TextFieldExt from "../../views/common-components/TextFieldExt";
import {Col, Row, RowCol} from "../../views/common-components/Flexboxgrid";
import {RaisedButton, CircularProgress} from "material-ui";
import {ConfirmationLinkRequestDTO, ConfirmationLinkResponseDTO, RegisterInternalDataDTO, NextActionType} from "../AccountDTO";
import {getConfirmationLinkResponse} from "../accountActions";
import {CLEAR_CONFIRMATION_LINK_STATE, REGISTER_SHOW_REGISTRATION_PANEL} from "../accountActionTypes";


interface ReactProps {

}
interface PropsFunc {
    getConfirmationLinkResponse: (uid, confirmationLinkRequest: ConfirmationLinkRequestDTO) => void;
    stateUid: string,
    clearConfirmationLinkState: () => void;
    handleOpenRegistrationWithFixedEmailFunc: (requiredEmail: string, proposedLogin: string, emailIsConfirmedByConfirmationLink: string) => void;
    handleOpenRegistration: () => void;
}

interface Props {
    confirmationLinkResponse?: ConfirmationLinkResponseDTO,
}
const ActionButton = (props: {label: string, handleAction: () => void}) => {
    return <RowCol colStyle={{paddingTop: '30px', paddingBottom: '30px'}}>
        <div style={{display:"block"}}>
            <RaisedButton
                fullWidth={true}
                label={props.label}
                primary={true}
                className="right" onClick={props.handleAction}/>
        </div>
    </RowCol>

}

class ConfirmationPanelInternal extends React.Component<ReactProps &  PropsFunc & Props, void> {
    private newPasswordField: TextFieldExt;
    private newLoginField: TextFieldExt;


    componentDidMount = () => {
        var req: ConfirmationLinkRequestDTO = {}
        if (this.props.stateUid == null) {
            var c = "action=";
            var cl = window.location.hash;
            var confirmationLinkString = cl.substring(cl.indexOf(c));
            this.props.getConfirmationLinkResponse(confirmationLinkString, req);
        }
    }

    handleManagedRegister = () => {
        var resp = this.props.confirmationLinkResponse;
        const reg: RegisterInternalDataDTO = resp.registerInternalData;
        this.props.handleOpenRegistrationWithFixedEmailFunc(reg.emailRequiredForRegistration, reg.loginProposedForRegistration, reg.emailIsConfirmedByConfirmationLink);
        this.props.clearConfirmationLinkState();

    }
    handleRegister = () => {
        this.props.handleOpenRegistration();
        this.props.clearConfirmationLinkState();
    }

    handleLogin = () => {
        this.props.clearConfirmationLinkState();
    }

    handleNextStep = () => {
        var resp = this.props.confirmationLinkResponse;


        var req: ConfirmationLinkRequestDTO = {}
        if (this.props.confirmationLinkResponse.newPasswordRequired) {
            req.newPassword = this.newPasswordField.state.fieldValue
            if (!this.newPasswordField.checkIsValid()) {
                return;
            }
        }

        if (this.props.confirmationLinkResponse.newLoginRequired) {
            req.newLogin = this.newLoginField.state.fieldValue
            if (!this.newLoginField.checkIsValid()) {
                return;
            }
        }
        this.props.getConfirmationLinkResponse(this.props.stateUid, req);

    }


    render() {


        if (this.props.confirmationLinkResponse == null)
            return <RowCol horizontal="center" rowStyle={{padding:"30px"}}>
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


                        { this.props.confirmationLinkResponse.newLoginRequired &&
                        <RowCol>
                            <TextFieldExt
                                ref={(c)=>{this.newLoginField=c}}
                                fullWidth={true}
                                floatingLabelText="Login:"
                                validateOnChange={true}
                                minLengthNumber={5}
                                maxLengthNumber={30}
                                useRequiredValidator={true}
                            />
                        </RowCol>
                        }

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
                        {this.props.confirmationLinkResponse.nextActions.map(e => {
                            console.log(e);
                                if (e == NextActionType.REGISTER_BUTTON)
                                    return <ActionButton label="Register" handleAction={this.handleRegister}/>
                                else if (e == NextActionType.MANAGED_REGISTER_BUTTON)
                                    return <ActionButton label="Register" handleAction={this.handleManagedRegister}/>
                                else if (e == NextActionType.LOGIN_BUTTON)
                                    return <ActionButton label="Login" handleAction={this.handleLogin}/>
                                else if (e == NextActionType.MAIN_PAGE)
                                    return <ActionButton label="Home" handleAction={this.handleLogin}/>
                                else if (e == NextActionType.NEXT)
                                    return <ActionButton label="Ok" handleAction={this.handleNextStep}/>
                            else return null;
                            }
                        )}


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
        handleOpenRegistrationWithFixedEmailFunc: (requiredEmail: string, proposedLogin: string, emailIsConfirmedByConfirmationLink: string) => {
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({requiredEmail, proposedLogin, emailIsConfirmedByConfirmationLink}))
        },
        handleOpenRegistration: () => {
            dispatch(REGISTER_SHOW_REGISTRATION_PANEL.new({}))
        }
    }
};


export const ConfirmationPanel = connect(
    mapStateToProps,
    mapDispatchToProps
)(ConfirmationPanelInternal);

