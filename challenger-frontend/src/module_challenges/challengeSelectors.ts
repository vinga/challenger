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

export const selectedChallengeParticipantsSelector: Selector<ReduxState,Array<ChallengeParticipantDTO>> = createSelector(
    selectedChallengeSelector,
    (selectedChallenge: ChallengeDTO) =>
        selectedChallenge != null ?
            selectedChallenge.userLabels : []
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




