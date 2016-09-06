Feature: Challenger challenges access
  As a challenger user
  I want to have access to challenges
  So that I can choose among them

  Scenario: Seeing mine all challenges
    Given I have 1 accepted challenge with my friend
    And I have 1 challenge sent by me to my friend waiting for acceptance
    And I have 1 challenge waiting for my acceptance
    And I have 1 challenge rejected by me
    And I have 1 challenge sent by me and rejected by my friend
    Then I should see 3 challenges on my list
    And see which challenge was recently chosen from the list