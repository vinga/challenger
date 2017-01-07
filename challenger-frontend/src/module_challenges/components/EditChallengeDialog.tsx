import {ChallengeDTO, ChallengeParticipantDTO} from "../ChallengeDTO";
import {TouchTapEvent, FlatButton} from "material-ui";
import Dialog from "material-ui/Dialog";
import {createChallengeAction, updateChallengeAction, deleteChallengeAction} from "../challengeActions";
import {CLOSE_EDIT_CHALLENGE, DELETE_CHALLENGE_PARTICIPANT, UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION} from "../challengeActionTypes";
import * as React from "react";
import {ReduxState, connect} from "../../redux/ReduxState";
import TextField from "material-ui/TextField";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import AutoComplete from "material-ui/AutoComplete";
import Chip from "material-ui/Chip";
import {possibleChallengeParticipantsSelector} from "../challengeSelectors";
import {updateChallengeParticipantsAction} from "../../module_accounts/accountActions";
import Subheader from "material-ui/Subheader";
import {validateEmail, default as TextFieldExt} from "../../views/common-components/TextFieldExt";
import {FontIcon} from "material-ui";
import {IconButton} from "material-ui";
import {YesNoConfirmationDialog} from "../../views/common-components/YesNoConfirmationDialog";

interface Props {

}

interface ReduxProps {
    challenge: ChallengeDTO,
    currentUserId: number,
    possibleParticipants: Array<ChallengeParticipantDTO>,
    possibleLabels: Array<string>,
    errorText: string,
    canSubmit: boolean
}

interface PropsFunc {
    onCloseFunc?: (event?: TouchTapEvent) => void,
    onCreateChallengeFunc: (challenge: ChallengeDTO)=>void;
    updateChallengeParticipant: (loginOrEmail: string) => void,
    deleteChallengeParticipant: (label: string) => void,
    updateErrorText: (errorText: string) => void
    deleteChallengeFunc: (challenge: ChallengeDTO)=>void;
}
interface State {
    challenge: ChallengeDTO,
    submitDisabled: boolean,
    searchText: string,
    showConfirmDialog: boolean
    showConfirmDeleteDialog: boolean
}

class EditChallengeDialogInternal extends React.Component<Props & ReduxProps & PropsFunc, State> {
    textField: TextField;
    constructor(props) {
        super(props);
        this.state = {
            challenge: this.props.challenge,
            submitDisabled: false,
            searchText: "",
            showConfirmDialog: false,
            showConfirmDeleteDialog: false
        };
    }

    handleSubmit = () => {
        if(this.state.searchText.length > 0) {
            this.state.showConfirmDialog=true;
            this.setState(this.state);
            return;
          /*  if (confirm('Add '+ this.state.searchText +' to users list?')) {
                console.log(this.state.searchText);
                this.handleNewRequest(this.state.searchText);
                return;
            }*/
        }
        this.props.onCreateChallengeFunc(this.state.challenge);
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

    resolveTitle = () => {
        if(this.props.challenge.id <= 0) {
            return "Create New Challenge";
        }
        return "Edited Challenge: " + this.props.challenge.label;
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


    componentDidMount = () => {

        setTimeout(()=> {
            this.textField.focus();
        },200);
    }

    render() {
        const actions = [
            <FlatButton
                label="Submit"
                primary={true}
                disabled={this.state.submitDisabled || !this.props.canSubmit}
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
                title={this.resolveTitle()}
            >
                <div>
                    <TextField
                        ref={(c)=>this.textField=c}
                        autoFocus={true}
                        floatingLabelText="Challenge Name"
                        hintText="Challenge name"
                        defaultValue={this.props.challenge.label}
                        onChange={this.handleActionNameFieldChange}

                    />

                    {  this.props.challenge.id > 0 && this.props.challenge.creatorId==this.props.currentUserId &&
                    <div style={{float: "right"}}>
                        <IconButton style={{width: 60, height: 60}}
                                    onClick={()=>{this.state.showConfirmDeleteDialog=true; this.setState(this.state); }}>
                            &nbsp;<i className={'fa fa-trash' }
                                     style={{fontSize: '20px', color: "grey", textAlign: 'center'}}></i>
                        </IconButton>
                    </div>
                    }
                </div>
                <div>
                <div style={{fontSize: '10px', marginTop: '10px'}}>
                    Selected Participants
                </div>
                <div style={{display:"flex", flexFlow:"wrap", marginTop: '10px'}}>


                    {
                        this.props.challenge.userLabels.map((ch,index) =>
                            <Chip
                                style={{marginRight:'5px', marginBottom:'5px'}}
                                key={ch.label}
                                onRequestDelete={index==0? null: () => this.handleRequestDelete(ch.label)}
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
                <IconButton onClick={()=>this.handleNewRequest(this.state.searchText)} > <FontIcon
                    className="fa fa-plus-circle cyan-text"/></IconButton>

            </div>
            </Dialog>

            {this.state.showConfirmDialog &&
            <YesNoConfirmationDialog closeYes={()=>{    this.handleNewRequest(this.state.searchText); }}
                                     closeNo={()=>{this.state.searchText="";this.setState(this.state); }}
                                     closeDialog={()=>{this.state.showConfirmDialog=false; this.setState(this.state); }}>
                Add {this.state.searchText} to users list?'
            </YesNoConfirmationDialog> }


            {this.state.showConfirmDeleteDialog &&
            <YesNoConfirmationDialog closeYes={()=>{ this.props.deleteChallengeFunc(this.props.challenge); this.props.onCloseFunc() }}
                                     closeDialog={()=>{this.state.showConfirmDeleteDialog=false; this.setState(this.state); }}>
               Do you really want to remove the challenge?
            </YesNoConfirmationDialog> }

        </div>);


    }
}

const mapStateToProps = (state: ReduxState, ownProps: Props): ReduxProps => {

    return {
        challenge: state.challenges.editedChallenge,
        canSubmit: state.challenges.challengeParticipantIsChecked!=true,
        currentUserId: loggedUserSelector(state).id,
        possibleParticipants: possibleChallengeParticipantsSelector(state),
        possibleLabels: possibleChallengeParticipantsSelector(state).map(u=>u.label),
        errorText: state.challenges.errorText,

    }
};
const mapDispatchToProps = (dispatch): PropsFunc => {
    return {
        onCreateChallengeFunc: (challenge: ChallengeDTO)=> {
            if (challenge.id<=0)
                dispatch(createChallengeAction(challenge));
            else
                dispatch(updateChallengeAction(challenge));
        },
        deleteChallengeFunc: (challenge: ChallengeDTO)=> {
            dispatch(deleteChallengeAction(challenge.id));
        },
        onCloseFunc: (event: TouchTapEvent)=> {
            dispatch(CLOSE_EDIT_CHALLENGE.new({}));
        },
        updateChallengeParticipant: (loginOrEmail: string) => {
            dispatch(updateChallengeParticipantsAction(loginOrEmail));
        },
        deleteChallengeParticipant: (label: string) => {
            dispatch(DELETE_CHALLENGE_PARTICIPANT.new({label}))
        },
        updateErrorText: (errorText: string) => {
            dispatch(UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION.new({errorText}))
        },


    }
};


export const EditChallengeDialog = connect(mapStateToProps, mapDispatchToProps)(EditChallengeDialogInternal);


