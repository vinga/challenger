export const baseApiUrl: string = "http://localhost:9080/api";

class WebCall {
    webToken: string;


    postUnauthorized(path: string, payload: any): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(payload)
        });
    }

    postUnauthorizedNoJson(path: string, payload: any): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            type: 'POST',
            //contentType: "charset=utf-8",
            data: payload
        });
    }

    get(path: string): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            headers: {
                "Authorization": "Bearer " + this.webToken
            },
        });
    }

    getAuthorizedCustom(path: string, customJWT: string): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            headers: {
                "Authorization": "Bearer " + customJWT
            },
        });
    }

    put(path: string, payload: any): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "PUT",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        });
    }

    post(path: string, payload: any): JQueryPromise<any> {
        if (payload != null) {
            return $.ajax({
                url: baseApiUrl + path,
                data: JSON.stringify(payload),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + this.webToken
                }
            });
        } else
            return $.ajax({
                url: baseApiUrl + path,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                type: "POST",
                headers: {
                    "Authorization": "Bearer " + this.webToken
                }
            });
    }

    postAuthorizedCustom(path: string, payload: any, jwtToken: string): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtToken
            }
        });
    }

    postMultiAuthorized(path: string, payload: any, jwtTokens: Array<String>): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            data: JSON.stringify(payload),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + jwtTokens.join(" ")
            }
        });
    }

    delete(path: string): JQueryPromise<any> {
        return $.ajax({
            url: baseApiUrl + path,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        });
    }

}

export const webCall = new WebCall();