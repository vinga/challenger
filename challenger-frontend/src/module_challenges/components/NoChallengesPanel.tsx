import * as React from "react";
import {Row, Col} from "../../views/common-components/Flexboxgrid";
import {Paper, RaisedButton, Divider} from "material-ui";
import {challengeStatusSelector, selectedChallengeSelector, acceptedByMeChallengeSelector, waitingForMyAcceptanceChallengeSelector} from "../challengeSelectors";
import {ReduxState} from "../../redux/ReduxState";
import {connect} from "react-redux";
import {loggedUserSelector} from "../../module_accounts/accountSelectors";
import {CREATE_NEW_CHALLENGE} from "../challengeActionTypes";
import {ChallengeStatus, ChallengeDTO, NO_CHALLENGES_LOADED_YET} from "../ChallengeDTO";
import {acceptOrRejectChallenge} from "../challengeActions";
import ChallengeAcceptRejectMessageItem from "./ChallengeAcceptRejectMessageItem";


interface ReduxProps {
    userId: number,
    userLabel: string,

    hasAnyChallenges: boolean
challengeIsActive: boolean

    stillLoading: boolean,
    waitingForMyAcceptance: ChallengeDTO[]
}
interface  ReduxFunc {
    onCreateNewChallenge: (creatorLabel: string) => void;
    onAcceptRejectChallenge: (challengeId: number, accept: boolean) => void
}
class NoChallengesPanelInternal extends React.Component<{} & ReduxProps & ReduxFunc, void> {

    handleConfirmChallenge = (ch: ChallengeDTO) => {
        this.props.onAcceptRejectChallenge(ch.id, true);
    }
    handleRejectChallenge = (ch: ChallengeDTO) => {
        this.props.onAcceptRejectChallenge(ch.id, false);
    }

    render() {
        if (this.props.challengeIsActive) {
            return null
        }

        if (this.props.stillLoading) {
            return null;
            /*            <RowCol horizontal="center">
             <CircularProgress />
             </RowCol>*/
        }

        return <Row style={{marginTop:"50px"}}>
            <Col col="10-6" offset="1-3">
                <Paper style={{padding:"30px"}}>
                    {!this.props.hasAnyChallenges &&

                        <Row style={{marginBottom:"20px"}}>
                            <Col    style={{marginTop:"10px",marginRight:"10px",}}>
                                - You don't have any active challenges
                            </Col>
                            <Col>
                                <RaisedButton
                                    style={{ minWidth:'200px'}}
                                    label="Create new Challenge"
                                    primary={true}
                                    className="right" onClick={()=>this.props.onCreateNewChallenge(this.props.userLabel)}/>

                            </Col>
                        </Row>
                    }
                    {this.props.waitingForMyAcceptance.length > 0 &&
                    <div style={{marginBottom:"20px", marginTop:"40px"}}>
                        - You have {this.props.waitingForMyAcceptance.length} challenge(s) waiting for your acceptance:
                    </div>

                    }


                    {this.props.waitingForMyAcceptance.map(challenge =>
                        [<ChallengeAcceptRejectMessageItem key={challenge.id} challenge={challenge}/>,
                            <Divider key={"d"+challenge.id} style={{margin:"20px"}}/>]
                    )}


                </Paper>
            </Col>
        </Row>
    }
}

const mapStateToProps = (state: ReduxState): ReduxProps => {
    return {
        stillLoading: state.challenges.selectedChallengeId == NO_CHALLENGES_LOADED_YET,
        hasAnyChallenges: acceptedByMeChallengeSelector(state).length > 0,//state.challenges.visibleChallenges.length > 0,
        waitingForMyAcceptance: waitingForMyAcceptanceChallengeSelector(state),
        userId: loggedUserSelector(state).id,
        userLabel: loggedUserSelector(state).login,
        challengeIsActive: challengeStatusSelector(state) == ChallengeStatus.ACTIVE,
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