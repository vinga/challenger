import Rx from 'rxjs/Rx';

$(function() {
    $.ajaxSetup({
        error: function(jqXHR, exception) {

            console.log("Error "+jqXHR.status+" "+jqXHR.responseText);


            /*        if (jqXHR.status === 0) {
             alert('Not connect.\n Verify Network.');
             } else if (jqXHR.status == 404) {
             alert('Requested page not found. [404]');
             } else if (jqXHR.status == 500) {
             alert('Internal Server Error [500].');
             } else if (jqXHR.status == 401) {
             console.log("UNAUTHORIZED [401]");
             } else if (exception === 'parsererror') {
             alert('Requested JSON parse failed.');
             } else if (exception === 'timeout') {
             alert('Time out error.');
             } else if (exception === 'abort') {
             alert('Ajax request aborted.');
             } else {
             alert('Uncaught Error.\n' + jqXHR.responseText);
             }*/
        }
    });
});

class AjaxWrapper {

/*    login(login, pass, okCallbackData, errorCallbackjqXHRException) {
        $.ajax({
            url: this.baseUrl + "/newToken",
            type: 'POST',
            data: {
                'login': login,
                'pass': pass
            },
        }).then(
            (data)=>okCallbackData(data),
            (jqXHR, exception) => errorCallbackjqXHRException(jqXHR, exception)
        );
    }*/

    login(login, pass) {
        return $.ajax({
            url: this.baseUrl + "/newToken",
            type: 'POST',
            data: {
                'login': login,
                'pass': pass
            },
        });
    }

    loadVisibleChallenges(callbackData) {
        $.ajax({
            url: this.baseUrl + "/visibleChallenges",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then(data=>callbackData(data));
    }

    loadVisibleChallengesObservable() {
        return Rx.Observable.fromPromise($.ajax({
            url: this.baseUrl + "/visibleChallenges",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }));
    }

    loadTasksFromServerObservable(challengeId, userNo, date) {
        var formattedDate = date.toISOString().slice(0, 10);
        return Rx.Observable.fromPromise($.ajax({
            url: this.baseUrl+ ((userNo==0)?"/tasksForMe" : "/tasksForOther")+"/"+challengeId +"/"+formattedDate ,
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }));
    }

    //not used
    loadIconFromServer(iconId, callbackData) {
        $.ajax({
            url: this.baseUrl + "/newAvatar/" + iconId,
            cache: false,
            success:
                (data)=>callbackData(data)

        });
    }

    loadTasks(challengeId, date) {
        var formattedDate = date.toISOString().slice(0, 10);
        return $.ajax({
            url: this.baseUrl+ "/tasks"+"/"+challengeId +"/"+formattedDate ,
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        });
    }

    loadTasksFromServer(challengeId, userNo, date, callback) {
    var formattedDate = date.toISOString().slice(0, 10);
    $.ajax({
        url: this.baseUrl+ ((userNo==0)?"/tasksForMe" : "/tasksForOther")+"/"+challengeId +"/"+formattedDate ,
        headers: {
            "Authorization": "Bearer " + this.webToken
        }
    }).then((data)=>callback(data));
    }


    updateTask(task, callback) {
        $.ajax({
            url: this.baseUrl+ "/updateTask",
            data: JSON.stringify(task),

            contentType:  "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then(function (updatedTask) {
            callback(updatedTask);
        });
    }

    updateTaskProgress(taskProgress, callback) {
        $.ajax({
            url: this.baseUrl+ "/updateTaskProgress",
            data: JSON.stringify(taskProgress),

            contentType:  "application/json; charset=utf-8",
            dataType: "json",
            type: "POST",
            headers: {
                "Authorization": "Bearer " + this.webToken
            }
        }).then(function (updatedTaskProgress) {
            callback(updatedTaskProgress);
        });
    }

}
const ajaxWrapper=new AjaxWrapper();
export default ajaxWrapper;