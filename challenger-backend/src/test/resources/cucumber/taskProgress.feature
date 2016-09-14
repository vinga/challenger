Feature: Challenger todo list
  As a challenger user
  I want to mark tasks as done
  So that I can gain points



  Scenario: Seeing new tasks as undone
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction1" for "mine"
    Given "my friend" created in challenge "boom" new action "testAction2" for "me"
    Then "I" see task "testAction1" as "undone"
    And "I" see task "testAction2" as "undone"


  Scenario: I cannot mark as done tasks assigned to other person
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction1" for "my friend"
    When "I" mark task "testAction1" as "done"
    Then I get an exception


  Scenario: I can mark as done and undone tasks assigned to me
    Given "I" have accepted challenge "boom" with "my friend"
    Given "I" created in challenge "boom" new action "testAction1" for "mine"
    Given "my friend" created in challenge "boom" new action "testAction2" for "me"
    When "I" mark task "testAction1" as "done"
    Then I don't get an exception
    When "I" mark task "testAction2" as "done"
    Then I don't get an exception




