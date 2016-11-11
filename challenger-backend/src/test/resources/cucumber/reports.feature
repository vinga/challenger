Feature:Reports
  As a challenger user
  I want to see reports in a form of charts


  Scenario:  I want to see progressive challenge points for all users - 1
    Given "I" created challenge "boom" with user "my friend" 3 days ago
    Given "my friend" accepted challenge "boom"
    Given "I" created in challenge "boom" new task "testAction" for "me" 3 days ago
    Given "my friend" accepted task "testAction" in challenge "boom"
    Given "I" marked task "testAction" as done every day
    When "I" request for Progressive Points Report for challenge "boom" for last 4 days
    Then "I" see that "I" has total points in progressive report
      |1|
      |2|
      |3|
      |4|


  Scenario: I want to see progressive challenge points for all users - 2
    Given "I" created challenge "boom" with user "my friend" 10 days ago
    Given "my friend" accepted challenge "boom"
    Given "I" created in challenge "boom" new task "testAction" for "me" 10 days ago
    Given "I" created in challenge "boom" new task "testAction2" for "me" 10 days ago
    Given "I" created in challenge "boom" new difficult task "testActionSuperHard" for "my friend" 5 days ago
    Given "my friend" accepted task "testAction" in challenge "boom"
    Given "my friend" accepted task "testAction2" in challenge "boom"
    Given "my friend" accepted task "testActionSuperHard" in challenge "boom"
    Given "I" marked task "testAction" as done every day
    Given "I" marked task "testAction2" as done every day
    Given "my friend" marked task "testActionSuperHard" as done for 2 last days


    When "I" request for Progressive Points Report for challenge "boom" for last 5 days
    Then "I" see that "my friend" has total points in progressive report
      |0|
      |0|
      |0|
      |3|
      |6|
    Then "I" see that "I" has total points in progressive report
      |14|
      |16|
      |18|
      |20|
      |22|


