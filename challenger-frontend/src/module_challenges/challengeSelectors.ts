import {Selector, createSelector} from "reselect";
import {ReduxState, copy} from "../redux/ReduxState";
import {ChallengeDTO} from "./ChallengeDTO";
import {UserDTO} from "./UserDTO";
import {AccountDTO, getAccountsSelector} from "../module_accounts/index";
import {DisplayedEventUI} from "../views/EventGroup";



const selectedChallengeId:Selector<ReduxState,number> = (state: ReduxState): number => state.challenges.selectedChallengeId
const getChallenges:Selector<ReduxState,Array<ChallengeDTO>> = (state: any): Array<ChallengeDTO> => state.challenges.visibleChallenges


export const selectedChallengeSelector:Selector<ReduxState,ChallengeDTO> = createSelector(
    getChallenges, selectedChallengeId,
    (challenges: Array<ChallengeDTO>, selectedChallengeId: number) =>
        challenges.filter(ch=>ch.id == selectedChallengeId).pop()
);

const selectedChallengeUserLabelsSelector:Selector<ReduxState,Array<UserDTO>> = createSelector(
    selectedChallengeSelector,
    (selectedChallenge: ChallengeDTO) =>
    selectedChallenge!=null?
        selectedChallenge.userLabels: []
);

export const selectedChallengeParticipantIds:Selector<ReduxState,Array<number>> = createSelector(
    selectedChallengeUserLabelsSelector,
    (userLabels: Array<UserDTO>) =>  userLabels.map(u=>u.id)
);



// creates AccountDTO for all challenge  users
export const challengeAccountsSelector:Selector<ReduxState,Array<AccountDTO>> = createSelector(
    selectedChallengeUserLabelsSelector,
    getAccountsSelector,
    (selectedChallengeUsers: Array<UserDTO>, accounts: Array<AccountDTO>):Array<AccountDTO> => {
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

export const challengeUserLabel = (state: ReduxState, userId: number):string => {
    var challenge=selectedChallengeSelector(state);
    if (challenge!=null)
        return challenge.userLabels.find(u=>u.id==userId).label;
    else return null;
}
export const challengeUserIndex = (state: ReduxState, userId: number):number => {
    var challenge=selectedChallengeSelector(state);
    if (challenge!=null)
        return challenge.userLabels.findIndex(u=>u.id==userId);
    else return null;
}

//EXTERNAL
const displayLogSelector:Selector<ReduxState,Boolean> = (state: ReduxState): Boolean => state.currentSelection.showChallengeConversation


export const challengeEventsSelector:Selector<ReduxState,Array<DisplayedEventUI>> = createSelector(
    (state:ReduxState)=>state,
    selectedChallengeSelector,
    displayLogSelector,
    (state:ReduxState,challenge:ChallengeDTO, displayLog: Boolean) => {
        //console.log("display" +displayLog+" "+challenge.displayedConversation);
        if (!displayLog || challenge==null || challenge.displayedConversation==null) {
            return null;
        }
        else return challenge.displayedConversation.posts.map(p=> {
            return {
                id: p.id,
                authorId: p.authorId,
                authorOrdinal: challengeUserIndex(state, p.authorId),
                authorLabel: challengeUserLabel(state, p.authorId),
                postContent: p.content
            }
        });
    }
)

