
export enum WebState {
    NEED_REFETCH,
    FETCHING,
    FETCHING_VISIBLE, // components may be invalid but we want to display them only if too much time took web call
    FETCHED
}
export const WEB_STATUS_NOTHING_RETURNED_YET=307; // for async calls
export const WEB_STATUS_UNAUTHORIZED=401;