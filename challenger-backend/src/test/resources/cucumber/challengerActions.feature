Feature: Challenger list access
  As a challenger user
  I want to have access to challenger actions
  So that I can mark them as done


  Scenario: Seeing challenger actions definitions
    Given I have accepted challenge with my friend
    When my friend created new action for me
    Then I should see it


  Scenario: Waiting for acceptance actions with due date in the past are ignored
    Given I have accepted challenge with my friend
    Given my friend created new "daily" action for me
    Given my friend created new onetime action for me with due date in the past
    When I get list of waiting for acceptance actions
    Then I should see only 1 waiting for acceptance actions


  Scenario: Onetime actions are not included in daily-todo-list day after due date
    Given I have accepted challenge with my friend
    Given my friend created new onetime action for me with due date in the past
    When I accept all actions before due date
    Then I should see 0 actions on daily-todo-list