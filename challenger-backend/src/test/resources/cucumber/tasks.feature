Feature: Challenger tasks
  As a challenger user
  I want to have access to challenger actions
  So that I can mark them as done

  Scenario: Seeing mine challenger actions definitions
    Given "I" have accepted challenge "boom" with "my friend"
    When my friend created new action for me
    Then I should see mine actions

  Scenario: Seeing second person challenger actions definitions
    Given "I" have accepted challenge "boom" with "my friend"
    When my friend created new action for him
    Then I should see my friend's actions


  Scenario: Waiting for acceptance actions with due date in the past are ignored
    Given "I" have accepted challenge "boom" with "my friend"
    Given my friend created new "daily" action for me
    Given my friend created new onetime action for me with due date in the past
    When I get list of waiting for acceptance actions
    Then I should see only 1 waiting for acceptance actions


  Scenario: Onetime actions are not included in daily-todo-list day after due date
    Given "I" have accepted challenge "boom" with "my friend"
    Given my friend created new onetime action for me with due date in the past
    When I accept all actions before due date
    Then I should see 0 actions on daily-todo-list



  Scenario: I cannot edit challenge actions assigned to my friend
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction" for "my friend"
    Then "I" cannot modify action "testAction"
    And "my friend" cannot modify action "testAction"
    And action "testAction" is not accepted

  Scenario: I cannot edit challenge actions created by me for myself
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction" for "me"
    Then "I" cannot modify action "testAction"
    And action "testAction" is not accepted


  Scenario: I can modify last visible challenge - 1
    Given "I" have accepted challenge "boom1" with "my friend"
    And "I" have accepted challenge "boom2" with "my friend"
    When "I" view todo list of challenge "boom1"
    And "I" view todo list of challenge "boom2"
    Then "my" last visible challenge is "boom2"

  Scenario: I can modify last visible challenge - 2
    Given "I" have accepted challenge "boom1" with "my friend"
    And "I" have accepted challenge "boom2" with "my friend"
    When "I" view todo list of challenge "boom2"
    And "I" view todo list of challenge "boom1"
    Then "my" last visible challenge is "boom1"

  Scenario: I can modify last visible challenge - 3
    Given "I" have accepted challenge "boom1" with "my friend"
    And "I" have accepted challenge "boom2" with "my friend"
    And "I" view todo list of challenge "boom1"
    Then "my" last visible challenge is "boom1"

  Scenario: I can modify last visible challenge - 4
    Given "I" have accepted challenge "boom1" with "my friend"
    And "I" have accepted challenge "boom2" with "my friend"
    And "I" view todo list of challenge "boom2"
    Then "my" last visible challenge is "boom2"