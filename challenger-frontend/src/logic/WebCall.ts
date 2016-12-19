export const baseApiUrl: string = "http://localhost:9080/api";

class BaseWebCall {
    webToken: string;


    postUnauthorized(path: string, payload: any): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(payload)
        }));
    }

    postUnauthorizedNoJson(path: string, payload: any): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",
            data: payload
        }));
    }
    postCustomNoJson(path: string, payload: any, customJWT: string): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",
            data: payload,
            headers: {
                "Authorization": "Bearer " + customJWT
            },
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: customJWT})
        });
    }
    get(path: string): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            headers: {
                "Authorization": "Bearer " + this.webToken
            },
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: this.webToken})
        });
    }

    getAuthorizedCustom(path: string, customJWT: string): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            headers: {
                "Authorization": "Bearer " + customJWT
            },
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: customJWT})
        });
    }

    put(path: string, payload: any): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "PUT",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: this.webToken})
        });
    }

    post(path: string, payload: any): Promise<any> {
        if (payload != null) {
            return Promise.resolve($.ajax({
                url: baseApiUrl + path,
                data: JSON.stringify(payload),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + this.webToken
                }
            })).catch((reason: XMLHttpRequest) => {
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
        } else
            return Promise.resolve($.ajax({
                url: baseApiUrl + path,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + this.webToken
                }
            })).catch((reason: XMLHttpRequest) => {
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
    }

    postWithFailureIfTrue(path: string, payload: any, causeFailure: boolean): Promise<any> {
        if (payload != null) {
            return Promise.resolve($.ajax({
                url: baseApiUrl + path,
                data: JSON.stringify(payload),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + ((causeFailure) ? "2343AA" : this.webToken)
                }
            })).catch((reason: XMLHttpRequest) => {
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
        } else
            return Promise.resolve($.ajax({
                url: baseApiUrl + path,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + ((causeFailure) ? "2343AA" : this.webToken)
                }
            })).catch((reason: XMLHttpRequest) => {
                throw Object.assign({}, reason, {jwtToken: this.webToken})
            });
    }

    postAuthorizedCustom(path: string, payload: any, jwtToken: string): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtToken
            }
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: jwtToken})
        });
    }

    postMultiAuthorized(path: string, payload: any, jwtTokens: Array<String>): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtTokens.join(" ")
            }
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: jwtTokens})
        });
    }

    delete(path: string): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: this.webToken})
        });
    }
    deleteMultiAuthorized(path: string, jwtTokens: Array<String>): Promise<any> {
        return Promise.resolve($.ajax({
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + jwtTokens.join(" ")
            }
        })).catch((reason: XMLHttpRequest) => {
            throw Object.assign({}, reason, {jwtToken: jwtTokens})
        });
    }

}


export const baseWebCall = new BaseWebCall();