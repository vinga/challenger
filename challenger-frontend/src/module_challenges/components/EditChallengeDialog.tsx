import {ChallengeDTO, ChallengeParticipantDTO} from "../ChallengeDTO";
import {TouchTapEvent, FlatButton} from "material-ui";
import Dialog from "material-ui/Dialog";
import {updateChallenge} from "../challengeActions";
import {CLOSE_EDIT_CHALLENGE} from "../challengeActionTypes";
import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import TextField from "material-ui/TextField";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import AutoComplete from "material-ui/AutoComplete";
import {Chip} from "material-ui/Chip";
import {possibleChallengeParticipantsSelector} from "../challengeSelectors";

interface Props {

}

interface ReduxProps {
    challenge: ChallengeDTO,
    currentUserId: number,
    possibleParticipants: Array<ChallengeParticipantDTO>,
    possibleLabels: Array<string>,
}

interface PropsFunc {
    onCloseFunc?:(event?:TouchTapEvent) => void,
    onChallengeSuccessfullyUpdatedFunc:(challenge:ChallengeDTO)=>void;
}
interface State {
    challenge: ChallengeDTO,
    submitDisabled: boolean,
    possibleParticipants: Array<ChallengeParticipantDTO>,
}

class EditChallengeDialogInternal extends React.Component<Props & ReduxProps & PropsFunc, State> {
    constructor(props) {
        super(props);
        this.state = {
            challenge: this.props.challenge,
            submitDisabled: false,
            possibleParticipants: this.props.possibleParticipants,
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
        if(this.props.challenge == null)
            return "Challenge name";
        return ""+this.props.challenge.label;

    };

    handleChipTouchTap = (event) => {

    }

    handleRequestDelete = (event) => {

    }

    handleUpdateInput = (value) => {
        var p = {
            id: 0,
            label: value,
            login: value,
            ordinal: 0, //  ordinal will be different for different users, because caller has always 0
            email: value,
        }
        this.props.challenge.userLabels.push(p);
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
                open={this.props.challenge != null}
                style={{height: "600px", overflow: "none", display: "block"}}
            >
                <div>
                    <TextField
                        floatingLabelText="Challenge Name"
                        hintText="Challenge name"
                        defaultValue={this.props.challenge.label}
                        onChange={this.handleActionNameFieldChange}
                    />

                    <div>
                        {
                            this.props.challenge.userLabels.map(ch =>
                                <Chip
                                    onRequestDelete={this.handleRequestDelete}
                                    onTouchTap={this.handleChipTouchTap}

                                >
                                    {ch.label}
                                </Chip>

                            )
                        }
                    </div>


                    <AutoComplete
                        hintText="Type username or email address"
dataSource={this.props.possibleLabels}
                        onUpdateInput={this.handleUpdateInput}
                    />
                </div>
            </Dialog>
            </div>);
    }
}

const mapStateToProps = (state:ReduxState, ownProps:Props):ReduxProps => {

    return {
        challenge: state.challenges.editedChallenge,
        currentUserId: loggedUserSelector(state).userId,
        possibleParticipants: possibleChallengeParticipantsSelector(state),
        possibleLabels:  possibleChallengeParticipantsSelector(state).map(u=>u.label),

    }
};
const mapDispatchToProps = (dispatch):PropsFunc => {
    return {
        onChallengeSuccessfullyUpdatedFunc: (challenge:ChallengeDTO)=> {
            dispatch(updateChallenge(challenge));
        },
        onCloseFunc: (event:TouchTapEvent)=> {
            dispatch(CLOSE_EDIT_CHALLENGE.new({}));
        },

    }
};


export const EditChallengeDialog = connect(mapStateToProps, mapDispatchToProps)(EditChallengeDialogInternal);


