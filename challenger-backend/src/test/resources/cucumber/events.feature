Feature:Conversations
  As a challenger user
  I want to comment and read other user comments on tasks and challenges


  Scenario: I want to read tasks posts
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction" for "me"
    Given "my friend" commented action "testAction" with words "whoaa!! brave of you"
    When "I" fetch all posts for action "testAction"
    Then "I" see that action "testAction" has 1 post

  Scenario: I want to read last 10 total posts - 1
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction" for "me"
    Given "I" created in challenge "boom" new action "testAction2" for "me"
    Given "my friend" commented action "testAction" with words "action" 5 times
    Given "my friend" commented action "testAction2" with words "action2" 5 times
    Given "my friend" commented action "testAction" with words "comment for test action" 2 times
    Given "my friend" commented action "testAction2" with words "comment for test action2" 6 times
    Given "I" commented action "testAction2" with words "comment for test action2" 1 times
    Given "my friend" commented challenge "boom" with words "hi" 1 times

    When "I" fetch 10 total posts for challenge "boom"
    Then "I" see that last 10 comments of challenge "boom" contains 2 posts with "comment for test action"
    Then "I" see that last 10 comments of challenge "boom" contains 7 posts with "comment for test action2"
    Then "I" see that last 10 comments of challenge "boom" contains 1 post with "hi"
    Then "I" see that last 10 comments of challenge "boom" contains 0 posts with "action"
    Then "I" see that last 10 comments of challenge "boom" contains 0 posts with "action2"
