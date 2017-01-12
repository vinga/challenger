import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {ChallengeDTO, ChallengeParticipantDTO, ChallengeStatus} from "./ChallengeDTO";
import {AccountDTO, getAccountsSelector} from "../module_accounts/index";
import {loggedUserSelector} from "../module_accounts/accountSelectors";


export const selectedChallengeIdSelector: Selector<ReduxState,number> = (state: ReduxState): number => state.challenges.selectedChallengeId
const getChallenges: Selector<ReduxState,Array<ChallengeDTO>> = (state: any): Array<ChallengeDTO> => state.challenges.visibleChallenges


export const selectedChallengeSelector: Selector<ReduxState,ChallengeDTO> = createSelector(
    getChallenges, selectedChallengeIdSelector,
    (challenges: Array<ChallengeDTO>, selectedChallengeId: number) =>
        challenges.find(ch=>ch.id == selectedChallengeId)
);


export const waitingForMyAcceptanceChallengeSelector: Selector<ReduxState,ChallengeDTO[]> = createSelector(
    getChallenges, loggedUserSelector,
    (challenges: Array<ChallengeDTO>, mainLoggedUser: AccountDTO) =>
        challenges.filter(challengeDTO=>challengeDTO.userLabels.some(ul=> ul.id == mainLoggedUser.id && ul.challengeStatus == ChallengeStatus.WAITING_FOR_ACCEPTANCE))
);
export const acceptedByMeChallengeSelector: Selector<ReduxState,ChallengeDTO[]> = createSelector(
    getChallenges, loggedUserSelector,
    (challenges: Array<ChallengeDTO>, mainLoggedUser: AccountDTO) =>
        challenges.filter(challengeDTO=>!challengeDTO.userLabels.some(ul=> (ul.id == mainLoggedUser.id && ul.challengeStatus == ChallengeStatus.WAITING_FOR_ACCEPTANCE)))
);

export const challengeStatusSelector: Selector<ReduxState,string> = createSelector(
    selectedChallengeSelector,
    loggedUserSelector,
    (challenge: ChallengeDTO, mainLoggedUser: AccountDTO): string => {
        if (challenge == null)
            return null;
        var up = challenge.userLabels.find(ul=> ul.id == mainLoggedUser.id);
        return up.challengeStatus;
    }
);


export const customChallengeStatusSelector: Selector<ReduxState,string> = (state: ReduxState, challenge: ChallengeDTO) => {
    var mainLoggedUser=loggedUserSelector(state);
    if (challenge == null)
        return null;
    var up = challenge.userLabels.find(ul=> ul.id == mainLoggedUser.id);
    return up.challengeStatus;
}

createSelector(
    selectedChallengeSelector,
    loggedUserSelector,
    (challenge: ChallengeDTO, mainLoggedUser: AccountDTO): string => {
        if (challenge == null)
            return null;
        var up = challenge.userLabels.find(ul=> ul.id == mainLoggedUser.id);
        return up.challengeStatus;
    }
);

export const selectedChallengeParticipantsSelector: Selector<ReduxState,Array<ChallengeParticipantDTO>> = createSelector(
    selectedChallengeSelector,
    (selectedChallenge: ChallengeDTO) =>
        selectedChallenge != null ?
            selectedChallenge.userLabels : []
);


export const challengeParticipantsSelector: Selector<ReduxState,Array<ChallengeParticipantDTO>> = createSelector(
    selectedChallengeParticipantsSelector,
    getAccountsSelector,
    (selectedChallengeUsers: Array<ChallengeParticipantDTO>, accounts: Array<AccountDTO>): Array<ChallengeParticipantDTO> => {
        return selectedChallengeUsers.map(us=> {
            var account: AccountDTO = accounts.find(u=>u.id == us.id);
            if (account != null) {
                return Object.assign({}, account, {label: us.label, ordinal: us.ordinal, challengeStatus: us.challengeStatus})
            } else {
                return Object.assign({}, us, {id: us.id});
            }
        });
    }
);

const editedChallenge: Selector<ReduxState,ChallengeDTO> = (state: any): ChallengeDTO => state.challenges.editedChallenge


export const possibleChallengeParticipantsSelector: Selector<ReduxState,Array<ChallengeParticipantDTO>> =
    createSelector(
        getChallenges,
        editedChallenge,
        (visibleChallenges: Array<ChallengeDTO>, editedChallenge: ChallengeDTO) => {
            var uniqueParticipants: Array<ChallengeParticipantDTO> = [];
            var uniqueEmails = [];
            visibleChallenges.map(vc=> {
                vc.userLabels.map(ul=> {
                    if ($.inArray(ul.label, uniqueEmails) == -1) {
                        if (editedChallenge.userLabels.find(chp=>chp.label == ul.label) == null) {
                            uniqueEmails.push(ul.label);
                            uniqueParticipants.push(ul);
                        }
                    }
                })
            });
            return uniqueParticipants;
        });

export const jwtTokensOfChallengeParticipants: Selector<ReduxState,Array<string>> = createSelector(
    challengeParticipantsSelector,
    (challengeAccounts: Array<ChallengeParticipantDTO>): Array<string> => {
        var jwtTokensOfApprovingUsers: Array<string> = challengeAccounts.filter(a=>a.jwtToken != null)
            .map(a=>a.jwtToken);
        return jwtTokensOfApprovingUsers;
    }
);

export const jwtTokenOfUserWithId = (state: ReduxState, userId: number): string => {
    return challengeParticipantsSelector(state).find(a=>a.id == userId).jwtToken
}


