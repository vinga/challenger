import {Selector, createSelector} from "reselect";
import {ReduxState, copy} from "../redux/ReduxState";
import {ChallengeDTO, ChallengeParticipantDTO} from "./ChallengeDTO";
import {AccountDTO, getAccountsSelector} from "../module_accounts/index";
import {TaskDTO} from "../module_tasks/TaskDTO";


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
            var account: AccountDTO = accounts.find(u=>u.id == us.id);
            if (account != null) {
                return copy(account).and(us);
            } else {
                return Object.assign({}, us, {id: us.id} as AccountDTO);
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
        var uniqueParticipants:Array<ChallengeParticipantDTO>=[];
        var uniqueEmails=[];
        visibleChallenges.map(vc=> {
          vc.userLabels.map(ul=> {
              if ($.inArray(ul.label,uniqueEmails)==-1) {
                  if (editedChallenge.userLabels.find(chp=>chp.label==ul.label)==null) {
                      uniqueEmails.push(ul.label);
                      uniqueParticipants.push(ul);
                  }
              }
          })
        });
        return uniqueParticipants;
    });

export const jwtTokensOfChallengeParticipants: Selector<ReduxState,Array<String>> = createSelector(
    challengeAccountsSelector,
    (challengeAccounts: Array<AccountDTO>): Array<String> => {
        var jwtTokensOfApprovingUsers: Array<String> = challengeAccounts.filter(a=>a.jwtToken != null)
            .map(a=>a.jwtToken);
        return jwtTokensOfApprovingUsers;
    }
);



