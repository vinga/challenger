import {Selector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {AccountDTO} from "./AccountDTO";

export const loggedUserSelector: Selector<ReduxState,AccountDTO> = (state: ReduxState): AccountDTO => {
    return state.accounts.find(u=>u.primary == true && u.jwtToken != null)
}


export const getAccountsSelector: Selector<ReduxState,Array<AccountDTO>> = (state: ReduxState): Array<AccountDTO> => state.accounts


export const loggedAccountByIdSelector = (state: ReduxState, userId: number): AccountDTO => {
    return state.accounts.find(a=>a.id == userId && a.jwtToken != null);
}

export const anyUserAsAccountSelector = (state: ReduxState, userId: number, label: string, login: string): AccountDTO => {
    var account: AccountDTO = getAccountsSelector(state).find(u=>u.id == userId);
    if (account != null) {
        return Object.assign({},account, {  label, login}) ;
    } else {
        return Object.assign({}, { id: userId, label, login, errorDescription:null, inProgress:false, primary: false} as AccountDTO);
    }
}


