Feature:Events
  As a challenger user
  I want to comment and read other user comments on tasks and challenges


  Scenario: I want to read tasks posts
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction" for "me"
    Given "my friend" commented action "testAction" with words "whoaa!! brave of you"
    When "I" fetch all posts for action "testAction"
    Then "I" see that action "testAction" has 1 post

  Scenario: I want to read all posts starting from last unread posts and at least 10 - 1
    Given "I" have accepted challenge "boom" with "my friend"
    Given "my friend" commented challenge "boom" with words "action1" 2 times
    Given "I" read all events in challenge "boom"
    Given "my friend" commented challenge "boom" with words "action2" 2 times
    When "I" fetch 10 total posts for challenge "boom"
    Then "I" see that last 10 comments of challenge "boom" contains 2 posts with "action1"
    Then "I" see that last 10 comments of challenge "boom" contains 2 posts with "action2"


  Scenario: I want to read all posts starting from last unread posts and at least 10 - 2
    Given "I" have accepted challenge "boom" with "my friend"
    Given "my friend" commented challenge "boom" with words "action1" 2 times
    Given "my friend" commented challenge "boom" with words "action2" 9 times
    Given "I" read all events in challenge "boom"
    When "I" fetch 10 total posts for challenge "boom"
    Then "I" see that last 10 comments of challenge "boom" contains 1 posts with "action1"
    Then "I" see that last 10 comments of challenge "boom" contains 9 posts with "action2"

  Scenario: I want to read all posts starting from last unread posts and at least 10 - 3
    Given "I" have accepted challenge "boom" with "my friend"
    Given "my friend" commented challenge "boom" with words "action1" 10 times
    Given "I" read all events in challenge "boom"
    Given "my friend" commented challenge "boom" with words "action2" 2 times
    Given "my friend" commented challenge "boom" with words "action3" 20 times
    When "I" fetch 10 total posts for challenge "boom"
    Then "I" see that last 10 comments of challenge "boom" contains 0 posts with "action1"
    Then "I" see that last 10 comments of challenge "boom" contains 2 posts with "action2"
    Then "I" see that last 10 comments of challenge "boom" contains 20 posts with "action3"