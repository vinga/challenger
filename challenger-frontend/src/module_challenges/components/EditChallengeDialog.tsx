import {ChallengeDTO, ChallengeParticipantDTO} from "../ChallengeDTO";
import {TouchTapEvent, FlatButton} from "material-ui";
import Dialog from "material-ui/Dialog";
import {updateChallenge} from "../challengeActions";
import {CLOSE_EDIT_CHALLENGE, DELETE_CHALLENGE_PARTICIPANT, UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION} from "../challengeActionTypes";
import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import TextField from "material-ui/TextField";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import AutoComplete from "material-ui/AutoComplete";
import Chip from "material-ui/Chip";
import {possibleChallengeParticipantsSelector} from "../challengeSelectors";
import {updateChallengeParticipants} from "../../module_accounts/accountActions";
import Subheader from "material-ui/Subheader";
import {validateEmail} from "../../views/common-components/TextFieldExt";

interface Props {

}

interface ReduxProps {
    challenge: ChallengeDTO,
    currentUserId: number,
    possibleParticipants: Array<ChallengeParticipantDTO>,
    possibleLabels: Array<string>,
    errorText: string,
}

interface PropsFunc {
    onCloseFunc?: (event?: TouchTapEvent) => void,
    onChallengeSuccessfullyUpdatedFunc: (challenge: ChallengeDTO)=>void;
    updateChallengeParticipant: (loginOrEmail: string) => void,
    deleteChallengeParticipant: (label: string) => void,
    updateErrorText: (errorText: string) => void
}
interface State {
    challenge: ChallengeDTO,
    submitDisabled: boolean,
    searchText: string,
}

class EditChallengeDialogInternal extends React.Component<Props & ReduxProps & PropsFunc, State> {
    constructor(props) {
        super(props);
        this.state = {
            challenge: this.props.challenge,
            submitDisabled: false,
            searchText: ""
        };
    }

    handleSubmit = () => {
        this.props.onChallengeSuccessfullyUpdatedFunc(this.state.challenge);
        this.props.onCloseFunc();
    };

    handleActionNameFieldChange = (event) => {
        this.state.challenge.label = event.target.value;
        this.setState(this.state);
    };

    resolveChallengeDefaultLabel = () => {
        if (this.props.challenge == null)
            return "Challenge name";
        return "" + this.props.challenge.label;

    };

    handleChipTouchTap = (event) => {

    }

    handleRequestDelete = (label) => {
       this.props.deleteChallengeParticipant(label)
    }

    handleUpdateInput = (value) => {
        this.props.updateErrorText(null)
        this.state.searchText=value
        this.setState(this.state);
    }
    handleNewRequest = (value) => {
        this.props.updateChallengeParticipant(value);
        this.state.searchText=""
        this.setState(this.state);


    }

    render() {
        const actions = [
            <FlatButton
                label="Submit"
                primary={true}
                disabled={this.state.submitDisabled}
                onTouchTap={this.handleSubmit}
            />,
            <FlatButton
                label="Cancel"
                primary={false}
                onTouchTap={this.props.onCloseFunc}
            />
        ];

        return (<div>
            <Dialog
                actions={actions}
                modal={true}
                open={true}
                style={{height: "600px", overflow: "none", display: "block"}}
            >
                <div>
                    <TextField
                        floatingLabelText="Challenge Name"
                        hintText="Challenge name"
                        defaultValue={this.props.challenge.label}
                        onChange={this.handleActionNameFieldChange}

                    />
                </div><div>
                <div style={{fontSize: '10px', marginTop: '10px'}}>
                    Selected Participants
                </div>
                <div style={{display:"flex", flexFlow:"wrap", marginTop: '10px'}}>


                    {
                        this.props.challenge.userLabels.map(ch =>
                            <Chip
                                style={{marginRight:'5px', marginBottom:'5px'}}
                                key={ch.label}
                                onRequestDelete={() => this.handleRequestDelete(ch.label)}
                                onTouchTap={this.handleChipTouchTap}
                            >
                                <i className={validateEmail(ch.label)? "fa fa-envelope-o": "fa fa-user"}></i>  {ch.label}
                            </Chip>
                        )
                    }
                </div>


                <AutoComplete
                    errorText={this.props.errorText}
                    searchText={this.state.searchText}
                    hintText="Type username or email address"
                    dataSource={this.props.possibleLabels}
                    onUpdateInput={this.handleUpdateInput}
                    onNewRequest={this.handleNewRequest}
                />



            </div>
            </Dialog>
        </div>);


    }
}

const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {

    return {
        challenge: state.challenges.editedChallenge,
        currentUserId: loggedUserSelector(state).id,
        possibleParticipants: possibleChallengeParticipantsSelector(state),
        possibleLabels: possibleChallengeParticipantsSelector(state).map(u=>u.label),
        errorText: state.challenges.errorText,
    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onChallengeSuccessfullyUpdatedFunc: (challenge: ChallengeDTO)=> {
            dispatch(updateChallenge(challenge));
        },
        onCloseFunc: (event: TouchTapEvent)=> {
            dispatch(CLOSE_EDIT_CHALLENGE.new({}));
        },
        updateChallengeParticipant: (loginOrEmail: string) => {
            dispatch(updateChallengeParticipants(loginOrEmail));
        },
        deleteChallengeParticipant: (label: string) => {
            dispatch(DELETE_CHALLENGE_PARTICIPANT.new({label}))
        },
        updateErrorText: (errorText: string) => {
            dispatch(UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION.new({errorText}))
        }

    }
};


export const EditChallengeDialog = connect(mapStateToProps, mapDispatchToProps)(EditChallengeDialogInternal);

