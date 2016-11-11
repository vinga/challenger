Feature: Challenger Invitations
  As an challenger user
  I want to invite others to challenger
  so I can challenge them

Scenario: Inviting to new challenge - finding user by login
Given I am existing challenger user
When I search friend's name by login
Then I get all logins starting with provided texts


Scenario: Existing user gets email notification when invited
Given I am existing challenger user
When I invite non existing email contact to new challenge
Then He gets email notification

Scenario: Not existing user gets email notification when invited
Given I am existing challenger user
When I invite existing email contact to new challenge
Then He gets email notification


Scenario: Non existing user confirms email notification
Given I am not challenger user
And I received email invitation from my friend
When I accept email link
Then my account will be created
And my friend challenge will be accepted

Scenario: Existing user confirms email notification
Given I am existing challenger user
And I received email invitation from my friend
When I accept email link
Then my friend challenge will be accepted

Scenario: Inviting to new challenge
Given I am existing challenger user
Given my friend is existing challenger user
When I invite my friend to new challenge
Then my friend should see 1 unanswered challenge invitation



