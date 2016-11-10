import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {challengeAccountsSelector} from "../module_challenges/challengeSelectors";
import {AccountDTO} from "../module_accounts/AccountDTO";

export const jwtTokensOfChallengeParticipants: Selector<ReduxState,Array<String>> = createSelector(
    challengeAccountsSelector,
    (challengeAccounts: Array<AccountDTO>): Array<String> => {
        var jwtTokensOfApprovingUsers: Array<String> = challengeAccounts.filter(a=>a.jwtToken != null)
            .map(a=>a.jwtToken);
        return jwtTokensOfApprovingUsers;
    }
);




