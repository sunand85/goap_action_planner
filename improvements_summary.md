GOAP Demo Improvements Summary

## Fixed Issues

1. **Infinite Loop in A* Pathfinder:**
   - Added a maximum iteration limit (10,000)
   - Improved state hash generation with property sorting
   - Enhanced node management with a HashMap for quick lookups

2. **Infinite Recursion in PizzaBot:**
   - Added a replan counter to limit replanning attempts to 3
   - Enhanced the replanning process output

## Added Alternative Action Paths

1. **Created NotCondition class:**
   - Allows checking if a property is not set to a specific value

2. **Added UsePremadeDoughAction:**
   - Alternative to PrepareDoughAction when fresh dough preparation fails
   - Only considered when doughPreparationFailed flag is true

3. **Modified PrepareDoughAction:**
   - Sets doughPreparationFailed flag when it fails
   - Excludes itself from consideration when doughPreparationFailed is true

## Demonstration Scenarios

1. **Scenario 1 (With Replanning):**
   - Fresh dough preparation fails
   - System replans and chooses to use premade dough instead
   - Pizza is successfully served using the alternative path

2. **Scenario 2 (Without Replanning):**
   - All actions succeed on the first try
   - No replanning is needed
   - Pizza is successfully served using the original plan

This demonstrates how the GOAP system can adapt to failures by finding alternative paths to achieve the goal, while avoiding infinite loops.
