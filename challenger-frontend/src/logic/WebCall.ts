//export const baseApiUrl = "http://localhost:9080/api";

import {WebCallData} from "../redux/ReduxState";
import {WEB_CALL_START, WEB_CALL_END, WEB_CALL_END_ERROR} from "../redux/actions/actions";
export const baseApiUrl = "/api";


export interface CallMeta {
    alwaysDisplayProgress?: boolean // default true
    async?: boolean //default false
}
function registerStartWebCall(meta: CallMeta, dispatch) {
    let callUid = Math.random();
    var webCallData: WebCallData = {
        callUid: callUid,
        startDate: new Date(),
        fromStart: meta == null || meta.alwaysDisplayProgress
    };
    if (meta==null || meta.async==false)
        dispatch(WEB_CALL_START.new({webCallData}));
    return callUid;
}
class BaseWebCall {
    webToken: string;


    postUnauthorized(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);

        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(payload)
        })).then(e=> {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        });
    }

    // unlike Json it sends simple string in POST without parenthesis
    postUnauthorizedString(dispatch, path: string, payload: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: payload
        })).then(e=> {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        });
    }

    postUnauthorizedNoJson(dispatch, path: string, payload: any = null, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        var data: any = {
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",

        };
        if (payload != null) {
            data.data = payload;
        }
        return Promise.resolve($.ajax(data)).then(e=> {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        });
    }

    postCustomNoJson(dispatch, path: string, payload: any, customJWT: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",
            data: payload,
            headers: {
                "Authorization": "Bearer " + customJWT
            },
        }))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: customJWT})
            });
    }


    get(dispatch, path: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);

        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            headers: {
                "Authorization": "Bearer " + this.webToken
            },
        }))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
    }

    /*    getAuthorizedCustom(dispatch, path: string, customJWT: string, meta?: CallMeta): Promise<any> {
     var callUid = registerStartWebCall(meta, dispatch);
     return Promise.resolve($.ajax({
     url: baseApiUrl + path,
     type: 'GET',
     contentType: "application/json; charset=utf-8",
     headers: {
     "Authorization": "Bearer " + customJWT
     },
     })).then(e=>{
     dispatch(WEB_CALL_END.new({callUid}));
     return e;
     }).catch((reason: XMLHttpRequest) => {
     dispatch(WEB_CALL_END_ERROR.new({callUid}));
     throw Object.assign({}, reason, {jwtToken: customJWT})
     });
     }*/

    put(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        var req: any = {
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "PUT",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        };
        if (payload != null) {
            req.data = JSON.stringify(payload);
        }
        return Promise.resolve($.ajax(req))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
    }

    putAuthorizedCustom(dispatch, path: string, payload: any, jwtToken: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        var req: any = {
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "PUT",
            headers: {
                "Authorization": "Bearer " + jwtToken
            }
        };
        if (payload != null) {
            req.data = JSON.stringify(payload);
        }
        return Promise.resolve($.ajax(req))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: jwtToken})
            });
    }

    post(dispatch, path: string, payload: any, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        var data: any = {
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        };
        if (payload != null)
            data.data = JSON.stringify(payload);

        return Promise.resolve($.ajax(data))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
    }

    postWithFailureIfTrue(dispatch, path: string, payload: any, causeFailure: boolean, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);

        var data = {
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + ((causeFailure) ? "2343AA" : this.webToken)
            }
        };
        if (payload != null)
            data.data = JSON.stringify(payload);


        return Promise.resolve($.ajax(data))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            })
            .catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });

    }

    postAuthorizedCustom(dispatch, path: string, payload: any, jwtToken: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);

        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtToken
            }
        }))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: jwtToken})
            });
    }

    postMultiAuthorized(dispatch, path: string, payload: any, jwtTokens: Array<String>, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);

        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtTokens.join(" ")
            }
        }))
            .then(e=> {
                dispatch(WEB_CALL_END.new({callUid}));
                return e;
            }).catch((reason: XMLHttpRequest) => {
                dispatch(WEB_CALL_END_ERROR.new({callUid}));
                throw Object.assign({}, reason, {jwtToken: jwtTokens})
            });
    }

    delete(dispatch, path: string, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        })).then(e=> {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        }).catch((reason: XMLHttpRequest) => {
            dispatch(WEB_CALL_END_ERROR.new({callUid}));
            throw Object.assign({}, reason, {jwtToken: this.webToken})
        });
    }

    deleteMultiAuthorized(dispatch, path: string, jwtTokens: Array<String>, meta?: CallMeta): Promise<any> {
        var callUid = registerStartWebCall(meta, dispatch);
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + jwtTokens.join(" ")
            }
        })).then(e=> {
            dispatch(WEB_CALL_END.new({callUid}));
            return e;
        }).catch((reason: XMLHttpRequest) => {
            dispatch(WEB_CALL_END_ERROR.new({callUid}));
            throw Object.assign({}, reason, {jwtToken: jwtTokens})
        });
    }

}


export const baseWebCall = new BaseWebCall();