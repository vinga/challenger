import { rootReducer} from './reducers/reducers';
import thunkMiddleware from "redux-thunk";
import {combineReducers, createStore, applyMiddleware} from "redux";
import {
    incrementDay,
} from "./actions/actions.ts";


function configureStore(initialState) {
    const store = createStore(rootReducer, initialState , applyMiddleware(
        thunkMiddleware, // lets us dispatch() functions
        // loggerMiddleware // neat middleware that logs actions
    ));

    if (module.hot) {
        // Enable Webpack hot module replacement for reducers
        module.hot.accept('./reducers/reducers', () => {
            const nextRootReducer = require('./reducers/reducers');
            store.replaceReducer(nextRootReducer);
        });
    }

    return store;
}
let store =configureStore({});

/*let store = createStore(combinedReducers, applyMiddleware(
    thunkMiddleware, // lets us dispatch() functions
    // loggerMiddleware // neat middleware that logs actions
));//applyMiddleware(createEpicMiddleware(combinedEpics)))*/
export default store;

console.log(store.getState())

let unsubscribe = store.subscribe(() =>
    console.log(store.getState())
)




store.dispatch(incrementDay(1));
store.dispatch(incrementDay(-1));

unsubscribe();