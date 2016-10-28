import {Selector, createSelector} from "reselect";
import {ReduxState, copy} from "../redux/ReduxState";
import {ChallengeDTO, ChallengeParticipantDTO} from "./ChallengeDTO";
import {AccountDTO, getAccountsSelector} from "../module_accounts/index";


export const selectedChallengeIdSelector: Selector<ReduxState,number> = (state: ReduxState): number => state.challenges.selectedChallengeId
const getChallenges: Selector<ReduxState,Array<ChallengeDTO>> = (state: any): Array<ChallengeDTO> => state.challenges.visibleChallenges


export const selectedChallengeSelector: Selector<ReduxState,ChallengeDTO> = createSelector(
    getChallenges, selectedChallengeIdSelector,
    (challenges: Array<ChallengeDTO>, selectedChallengeId: number) =>
        challenges.find(ch=>ch.id == selectedChallengeId)
);

const selectedChallengeParticipantsSelector: Selector<ReduxState,Array<ChallengeParticipantDTO>> = createSelector(
    selectedChallengeSelector,
    (selectedChallenge: ChallengeDTO) =>
        selectedChallenge != null ?
            selectedChallenge.userLabels : []
);

export const selectedChallengeParticipantIds: Selector<ReduxState,Array<number>> = createSelector(
    selectedChallengeParticipantsSelector,
    (userLabels: Array<ChallengeParticipantDTO>) => userLabels.map(u=>u.id)
);



// creates AccountDTO for all challenge  users
export const challengeAccountsSelector: Selector<ReduxState,Array<AccountDTO>> = createSelector(
    selectedChallengeParticipantsSelector,
    getAccountsSelector,
    (selectedChallengeUsers: Array<ChallengeParticipantDTO>, accounts: Array<AccountDTO>): Array<AccountDTO> => {
        return selectedChallengeUsers.map(us=> {
            var account: AccountDTO = accounts.find(u=>u.userId == us.id);
            if (account != null) {
                return copy(account).and(us);
            } else {
                return Object.assign({}, us, {userId: us.id} as AccountDTO);
            }
        });
    }
);


export const challengeUserLabel = (state: ReduxState, userId: number): string => {
    var challenge = selectedChallengeSelector(state);
    if (challenge != null)
        return challenge.userLabels.find(u=>u.id == userId).label;
    else return null;
}
export const challengeUserIndex = (state: ReduxState, userId: number): number => {
    var challenge = selectedChallengeSelector(state);
    if (challenge != null)
        return challenge.userLabels.findIndex(u=>u.id == userId);
    else return null;
}


