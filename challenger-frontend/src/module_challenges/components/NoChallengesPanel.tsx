import * as React from "react";
import {Row, Col, RowCol} from "../../views/common-components/Flexboxgrid";
import {Paper, RaisedButton} from "material-ui";
import {challengeStatusSelector, selectedChallengeSelector} from "../challengeSelectors";
import {ReduxState} from "../../redux/ReduxState";
import {connect} from "react-redux";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import {CREATE_NEW_CHALLENGE} from "../challengeActionTypes";
import {ChallengeStatus, ChallengeDTO, NO_CHALLENGES_LOADED_YET} from "../ChallengeDTO";
import {acceptOrRejectChallenge} from "../challengeActions";
import {CircularProgress} from "material-ui";


interface ReduxProps {
    userId: number,
    userLabel: string,
    challengeIsActive: boolean,
    hasAnyChallenges: boolean
    challengeState?: string,
    selectedChallenge: ChallengeDTO,
    stillLoading: boolean
}
interface  ReduxFunc {
    onCreateNewChallenge: (creatorLabel: string)=>void;
    onAcceptRejectChallenge: (challengeId: number, accept: boolean) => void
}
class NoChallengesPanelInternal extends React.Component<{} & ReduxProps & ReduxFunc, void> {

    handleConfirmChallenge = () => {
        this.props.onAcceptRejectChallenge(this.props.selectedChallenge.id, true);
    }
    handleRejectChallenge = () => {
        this.props.onAcceptRejectChallenge(this.props.selectedChallenge.id, false);
    }

    render() {
        if (this.props.challengeIsActive) {
            return null
        }

        if (this.props.stillLoading) {
            return <RowCol horizontal="center">
                    <CircularProgress />
            </RowCol>
        }

        return <Row style={{marginTop:"100px"}}>
            <Col col="6-4" offset="3-4">
                <Paper style={{padding:"30px"}}>

                    {!this.props.hasAnyChallenges &&
                    <div>
                        You haven't created any challenge yet.
                        <div>
                            <RaisedButton
                                style={{marginBottom:"20px", marginTop: "40px"}}
                                fullWidth={true}
                                label="Create new Challenge"
                                primary={true}
                                className="right" onClick={()=>this.props.onCreateNewChallenge(this.props.userLabel)}/>
                            &nbsp;
                        </div>
                    </div>}


                    {this.props.challengeState == ChallengeStatus.WAITING_FOR_ACCEPTANCE &&
                    <div>
                        Please decide if do you want accept or reject challenge <b>{this.props.selectedChallenge.label}</b>?<br/><br/>
                        Challenge participants:<br/>
                        {
                            this.props.selectedChallenge.userLabels.filter(ul=>ul.id != this.props.userId).map(ul=>
                                <div key={ul.id}>- {ul.label}
                                    {this.props.selectedChallenge.creatorId == ul.id && " (Creator)" }
                                </div>)
                        }
                        <br/>
                        <Row style={{padding:"10px"}}>
                            <Col style={{padding:"10px"}}>
                                <RaisedButton

                                    fullWidth={true}
                                    label="Accept"
                                    primary={true}
                                    className="right" onClick={this.handleConfirmChallenge}/>
                            </Col>
                            <Col style={{padding:"10px"}}>
                                <RaisedButton

                                    fullWidth={true}
                                    label="Reject"
                                    secondary={true}
                                    className="right" onClick={this.handleRejectChallenge}/>
                            </Col>
                        </Row>
                    </div>
                    }


                </Paper>
            </Col>
        </Row>
    }
}

const mapStateToProps = (state: ReduxState): ReduxProps => {
    return {
        stillLoading: state.challenges.selectedChallengeId==NO_CHALLENGES_LOADED_YET,
        hasAnyChallenges:  state.challenges.visibleChallenges.length > 0,
        userId: loggedUserSelector(state).id,
        userLabel: loggedUserSelector(state).login,
        challengeIsActive: challengeStatusSelector(state) == ChallengeStatus.ACTIVE,
        challengeState: challengeStatusSelector(state),
        selectedChallenge: selectedChallengeSelector(state),

    }
};
const mapDispatchToProps = (dispatch): ReduxFunc => {
    return {
        onCreateNewChallenge: (creatorLabel: string) => {
            dispatch(CREATE_NEW_CHALLENGE.new({creatorLabel: creatorLabel}))
        },
        onAcceptRejectChallenge: (challengeId: number, accept: boolean) => {
            dispatch(acceptOrRejectChallenge(challengeId, accept))
        }
    }
};
let NoChallengesPanel = connect(mapStateToProps, mapDispatchToProps)(NoChallengesPanelInternal);
export default NoChallengesPanel;