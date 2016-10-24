import {Selector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {AccountDTO} from "./AccountDTO";

export const loggedUserSelector:Selector<ReduxState,AccountDTO> = (state: ReduxState): AccountDTO => {
    return state.accounts.find(u=>u.primary == true && u.jwtToken!=null)
}


export const getAccountsSelector:Selector<ReduxState,Array<AccountDTO>> = (state: ReduxState): Array<AccountDTO> => state.accounts


export const loggedAccountByIdSelector = (state: ReduxState, userId: number):AccountDTO => {
    return state.accounts.find(a=>a.userId==userId && a.jwtToken!=null);
}