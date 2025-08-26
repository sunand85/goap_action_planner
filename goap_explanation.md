# Explanation of GOAP Planning System

This document explains how the GOAP (Goal-Oriented Action Planning) system works, focusing on the two key classes: GOAPPlanner and AStarPathfinder.

## GOAPPlanner Class

The `GOAPPlanner` class is the high-level planner that uses A* pathfinding to create plans. Here's how it works:

```java
public Plan createPlan(WorldState currentState, Goal goal, List<Action> availableActions) {
    // Check if the goal is already satisfied
    if (goal.isSatisfied(currentState)) {
        return new Plan(List.of());
    }
    
    // Use A* to find the optimal path from current state to goal
    List<Action> actionSequence = pathfinder.findPath(currentState, goal, availableActions);
    
    if (actionSequence != null) {
        return new Plan(actionSequence);
    }
    
    return null; // No valid plan found
}
```

This method:
1. First checks if the goal is already satisfied in the current state - if so, returns an empty plan
2. Otherwise, delegates to the A* pathfinder to find a sequence of actions
3. If a valid sequence is found, wraps it in a Plan object; otherwise returns null

The `isPlanValid` method simulates executing a plan to verify it achieves the goal:

```java
public boolean isPlanValid(Plan plan, WorldState state, Goal goal) {
    // ...
    // Simulate plan execution to check if it achieves the goal
    WorldState simulatedState = state.copy();
    
    for (Action action : plan.getActions()) {
        // Check if action's preconditions are met
        if (!action.checkPreconditions(simulatedState)) {
            return false;
        }
        
        // Apply action's effects
        simulatedState = action.applyEffects(simulatedState);
    }
    
    // Check if goal is satisfied in the final state
    return goal.isSatisfied(simulatedState);
}
```

This method:
1. Creates a copy of the current world state
2. Simulates applying each action in sequence, checking preconditions and applying effects
3. Checks if the goal is satisfied in the final simulated state

## AStarPathfinder Class

The `AStarPathfinder` class implements the A* search algorithm to find the optimal sequence of actions. Here's how it works:

### Key Components

1. **Node**: Represents a state in the search space
   ```java
   private static class Node {
       WorldState state;      // Current world state
       Node parent;           // Parent node (previous state)
       Action action;         // Action that led to this state
       float g;               // Cost from start to this node
       float f;               // Total estimated cost (g + h)
   }
   ```

2. **Open Set**: Priority queue of nodes to be evaluated, ordered by f-value (lowest first)
   ```java
   PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparing(node -> node.f));
   ```

3. **Closed Set**: Set of already evaluated nodes
   ```java
   Set<String> closedSet = new HashSet<>();
   ```

4. **Node Lookup Map**: For efficient node retrieval by state hash
   ```java
   Map<String, Node> openNodesByHash = new HashMap<>();
   ```

### The A* Algorithm

The `findPath` method implements the A* algorithm:

1. **Initialization**:
   - Create start node with the current world state
   - Add it to the open set and node lookup map

2. **Main Loop**:
   - While the open set is not empty and within iteration limit:
     - Get the node with lowest f-value from open set
     - If this node satisfies the goal, reconstruct and return the path
     - Add the node to the closed set
     - For each available action:
       - Check if action is applicable in current state
       - If applicable, apply action to get new state
       - Skip if new state is in closed set
       - Calculate costs (g = cost from start, h = heuristic estimate to goal)
       - If new state is already in open set with higher cost, update it
       - Otherwise, add new state to open set

3. **Path Reconstruction**:
   - Once goal is reached, trace back through parent nodes to build action sequence

### State Hashing

The `getStateHash` method creates a consistent string representation of a world state:

```java
private String getStateHash(WorldState state) {
    Map<String, Object> props = state.getAllProperties();
    StringBuilder sb = new StringBuilder();
    
    // Sort keys for consistent ordering
    List<String> keys = new ArrayList<>(props.keySet());
    Collections.sort(keys);
    
    for (String key : keys) {
        Object value = props.get(key);
        sb.append(key).append("=").append(value).append(";");
    }
    
    return sb.toString();
}
```

This ensures that states with the same properties but in different order are recognized as identical.

## How They Work Together

1. The PizzaBot creates a goal (e.g., "pizzaServed = true")
2. It passes the current world state, goal, and available actions to the GOAPPlanner
3. The GOAPPlanner delegates to the AStarPathfinder to find an optimal action sequence
4. The AStarPathfinder searches through possible action sequences:
   - It tries different combinations of actions
   - Each action has preconditions (what must be true to use it) and effects (how it changes the world)
   - The heuristic function guides the search toward promising paths
5. When a valid path is found, it's returned as a Plan
6. The PizzaBot executes the actions in the Plan
7. If an action fails, the PizzaBot requests a new plan from the current state

## Key Improvements Made

1. **Preventing Infinite Loops**:
   - Added a maximum iteration limit to the A* search
   - Improved state hash generation with sorted properties
   - Enhanced node management with a HashMap for quick lookups

2. **Alternative Action Paths**:
   - Created a NotCondition class to check if a property is not set to a specific value
   - Added UsePremadeDoughAction as an alternative to PrepareDoughAction
   - Modified PrepareDoughAction to exclude itself when dough preparation has failed
   - Set a flag in the world state when dough preparation fails

These improvements ensure that when an action fails (like PrepareDoughAction), it updates the world state with information that helps the planner find alternative paths (like using UsePremadeDoughAction instead).
