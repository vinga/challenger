
export enum WebState {
    NEED_REFETCH,
    FETCHING,
    FETCHING_VISIBLE, // components may be invalid but we want to display them only if too much time took web call
    FETCHED
}
