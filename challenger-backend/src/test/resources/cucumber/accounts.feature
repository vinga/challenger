@accounts
Feature:Login
  As a challenger user
  I want to safely login to application
  so I can access reserved resources

  Scenario: I cannot login with wrong password
    Given I am existing challenger user
    When I login with wrong password
    Then I got information that my credentials are invalid

  Scenario: My account is blocked for 20 min when I provided wrong password 11 times in a row
    Given I am existing challenger user
    When I login with wrong password 11 times
    Then I got information that my account is blocked for 20 min

  Scenario: My account is not blocked when I provided wrong password 10 times in a row
    Given I am existing challenger user
    When I login with wrong password 10 times
    Then my account is still not blocked

  Scenario: Registering user with existing email doesn't needs email verification
    Given I am not challenger user
    And I no invitation has been sent to me email
    When I register with that email
    Then I don't have to confirm my email before I can login succesfully

  Scenario: Registering user with existing email needs email verification
    Given I am not challenger user
    And I received email invitation from my friend
    But I not confirmed it yet
    When I register with that email
    Then I have to confirm my email before I can login succesfully

  Scenario: I want to reset my password
    Given I am existing challenger user
    When I put my email into reset password option
    Then I received email with password reset link
    And I click on password reset link
    Then I can set my new password

