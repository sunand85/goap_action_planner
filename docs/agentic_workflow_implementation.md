# Agentic Workflow Orchestrator: Implementation Details

## Table of Contents
1. [GOAP Planner Implementation](#goap-planner-implementation)
2. [A* Pathfinding Implementation](#a-pathfinding-implementation)
3. [Heuristic Functions](#heuristic-functions)
4. [Action Chain Analysis](#action-chain-analysis)
5. [State Manager Implementation](#state-manager-implementation)
6. [Action Repository Implementation](#action-repository-implementation)
7. [Action Types Implementation](#action-types-implementation)
8. [Execution Engine Implementation](#execution-engine-implementation)
9. [Plan Transition Implementation](#plan-transition-implementation)
10. [Parallel Execution Implementation](#parallel-execution-implementation)

## GOAP Planner Implementation

```java
public class GOAPPlanner implements Planner {
    private final AStarPathfinder pathfinder;
    
    public GOAPPlanner(AStarPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }
    
    @Override
    public Plan createPlan(WorldState currentState, Goal goal, List<Action> availableActions) {
        // Use A* to find the optimal path from current state to goal
        List<Action> actionSequence = pathfinder.findPath(currentState, goal, availableActions);
        
        if (actionSequence != null) {
            return new Plan(actionSequence);
        }
        
        return null; // No valid plan found
    }
    
    @Override
    public boolean isPlanValid(Plan plan, WorldState state, Goal goal) {
        if (plan == null || plan.getActions().isEmpty()) {
            return false;
        }
        
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
}
```

## A* Pathfinding Implementation

```java
public class AStarPathfinder {
    private final HeuristicFunction heuristicFunction;
    
    public AStarPathfinder(HeuristicFunction heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }
    
    /**
     * Finds the optimal path from start state to goal
     */
    public List<Action> findPath(WorldState startState, Goal goal, List<Action> availableActions) {
        // Open set contains nodes to be evaluated
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparing(node -> node.f));
        
        // Closed set contains already evaluated nodes
        Set<String> closedSet = new HashSet<>();
        
        // Start node
        Node startNode = new Node(startState, null, null, 0, 
                                 heuristicFunction.calculate(startState, goal, availableActions));
        openSet.add(startNode);
        
        while (!openSet.isEmpty()) {
            // Get node with lowest f value
            Node current = openSet.poll();
            
            // Check if goal is reached
            if (goal.isSatisfied(current.state)) {
                return reconstructPath(current);
            }
            
            // Add to closed set
            closedSet.add(getStateHash(current.state));
            
            // Explore neighbors (states reachable by applying actions)
            for (Action action : availableActions) {
                // Check if action is applicable in current state
                if (!action.checkPreconditions(current.state)) {
                    continue;
                }
                
                // Apply action to get new state
                WorldState newState = action.applyEffects(current.state.copy());
                String newStateHash = getStateHash(newState);
                
                // Skip if already evaluated
                if (closedSet.contains(newStateHash)) {
                    continue;
                }
                
                // Calculate costs
                float g = current.g + action.getCost();
                float h = heuristicFunction.calculate(newState, goal, availableActions);
                float f = g + h;
                
                // Create new node
                Node neighbor = new Node(newState, current, action, g, f);
                
                // Check if already in open set with higher cost
                boolean inOpenSetWithHigherCost = false;
                for (Node openNode : openSet) {
                    if (getStateHash(openNode.state).equals(newStateHash) && openNode.g > g) {
                        openSet.remove(openNode);
                        inOpenSetWithHigherCost = true;
                        break;
                    }
                }
                
                // Add to open set if new or better path found
                if (inOpenSetWithHigherCost || !openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                }
            }
        }
        
        return null; // No path found
    }
    
    /**
     * Reconstructs the path from goal node to start node
     */
    private List<Action> reconstructPath(Node goalNode) {
        List<Action> path = new ArrayList<>();
        Node current = goalNode;
        
        while (current.parent != null) {
            path.add(0, current.action); // Add to front of list
            current = current.parent;
        }
        
        return path;
    }
    
    /**
     * Creates a hash representation of a world state
     */
    private String getStateHash(WorldState state) {
        // Implementation depends on WorldState structure
        return state.toString();
    }
    
    /**
     * Node class for A* search
     */
    private static class Node {
        WorldState state;
        Node parent;
        Action action;
        float g; // Cost from start to this node
        float f; // g + h (estimated total cost)
        
        Node(WorldState state, Node parent, Action action, float g, float f) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.g = g;
            this.f = f;
        }
    }
}
```

## Heuristic Functions

```java
public class CompositeHeuristic implements HeuristicFunction {
    @Override
    public float calculate(WorldState currentState, Goal goal, List<Action> availableActions) {
        float unsatisfiedWeight = 0.5f;
        float distanceWeight = 0.3f;
        float actionChainWeight = 0.2f;
        
        float unsatisfiedScore = countUnsatisfiedConditions(currentState, goal);
        float distanceScore = calculatePropertyDistances(currentState, goal);
        float actionChainScore = estimateActionChain(currentState, goal, availableActions);
        
        return (unsatisfiedWeight * unsatisfiedScore) + 
               (distanceWeight * distanceScore) + 
               (actionChainWeight * actionChainScore);
    }
    
    private float countUnsatisfiedConditions(WorldState currentState, Goal goal) {
        int unsatisfiedConditions = 0;
        
        for (Condition condition : goal.getConditions()) {
            if (!currentState.satisfies(condition)) {
                unsatisfiedConditions++;
            }
        }
        
        return unsatisfiedConditions;
    }
    
    private float calculatePropertyDistances(WorldState currentState, Goal goal) {
        float totalDistance = 0;
        
        for (String key : goal.getRequiredProperties()) {
            Object currentValue = currentState.getProperty(key);
            Object goalValue = goal.getDesiredValue(key);
            
            if (currentValue == null && goalValue != null) {
                totalDistance += 1.0f; // Property missing
            } else if (currentValue != null && !currentValue.equals(goalValue)) {
                // Calculate distance based on property type
                totalDistance += calculatePropertyDistance(key, currentValue, goalValue);
            }
        }
        
        return totalDistance;
    }
    
    private float calculatePropertyDistance(String key, Object current, Object goal) {
        // Different metrics for different property types
        if (current instanceof Number && goal instanceof Number) {
            return Math.abs(((Number)current).floatValue() - ((Number)goal).floatValue());
        } else if (current instanceof Boolean && goal instanceof Boolean) {
            return ((Boolean)current).equals((Boolean)goal) ? 0 : 1;
        }
        
        return 1.0f; // Default distance for non-matching values
    }
    
    private float estimateActionChain(WorldState currentState, Goal goal, List<Action> availableActions) {
        // Find actions that can contribute to achieving the goal
        Set<Action> relevantActions = findActionsWithRelevantEffects(goal, availableActions);
        
        // Estimate minimum number of actions needed
        int minActionsNeeded = estimateMinimumActionsRequired(currentState, goal, relevantActions);
        
        // Use average action cost as a multiplier
        float avgActionCost = calculateAverageActionCost(relevantActions);
        
        return minActionsNeeded * avgActionCost;
    }
    
    private float calculateAverageActionCost(Set<Action> actions) {
        if (actions.isEmpty()) {
            return 1.0f;
        }
        
        float totalCost = 0.0f;
        for (Action action : actions) {
            totalCost += action.getCost();
        }
        
        return totalCost / actions.size();
    }
}
```

## Action Chain Analysis

```java
/**
 * Finds actions that can contribute to achieving the goal state
 */
public Set<Action> findActionsWithRelevantEffects(Goal goal, List<Action> availableActions) {
    Set<Action> relevantActions = new HashSet<>();
    Set<String> goalProperties = extractGoalProperties(goal);
    
    // First pass: Find actions that directly affect goal properties
    for (Action action : availableActions) {
        Set<String> actionEffects = extractActionEffectProperties(action);
        
        // If this action affects any goal property, it's relevant
        if (Sets.intersection(actionEffects, goalProperties).size() > 0) {
            relevantActions.add(action);
        }
    }
    
    // Second pass: Find actions that affect preconditions of relevant actions
    Set<Action> indirectlyRelevantActions = new HashSet<>();
    Set<String> relevantPreconditions = extractPreconditionProperties(relevantActions);
    
    for (Action action : availableActions) {
        if (!relevantActions.contains(action)) {
            Set<String> actionEffects = extractActionEffectProperties(action);
            
            // If this action affects any precondition of a relevant action, it's indirectly relevant
            if (Sets.intersection(actionEffects, relevantPreconditions).size() > 0) {
                indirectlyRelevantActions.add(action);
            }
        }
    }
    
    // Combine directly and indirectly relevant actions
    relevantActions.addAll(indirectlyRelevantActions);
    
    return relevantActions;
}

/**
 * Estimates the minimum number of actions required to achieve the goal
 */
public int estimateMinimumActionsRequired(WorldState currentState, Goal goal, Set<Action> relevantActions) {
    // Build dependency graph
    ActionDependencyGraph graph = buildActionDependencyGraph(relevantActions);
    
    // Identify which goal conditions are already satisfied
    Set<String> unsatisfiedProperties = new HashSet<>();
    for (Condition condition : goal.getConditions()) {
        if (!currentState.satisfies(condition)) {
            unsatisfiedProperties.add(condition.getPropertyKey());
        }
    }
    
    // Find actions that can satisfy each unsatisfied property
    Map<String, Set<Action>> propertyToActions = mapPropertiesToActions(relevantActions);
    
    // Calculate minimum action set using a greedy approach
    Set<Action> minimumActionSet = new HashSet<>();
    while (!unsatisfiedProperties.isEmpty()) {
        // Find the action that satisfies the most unsatisfied properties
        Action bestAction = findBestAction(unsatisfiedProperties, propertyToActions);
        
        if (bestAction == null) {
            // No action can satisfy remaining properties - goal might be unreachable
            return Integer.MAX_VALUE;
        }
        
        minimumActionSet.add(bestAction);
        
        // Remove the properties this action satisfies
        Set<String> actionEffects = extractActionEffectProperties(bestAction);
        unsatisfiedProperties.removeAll(actionEffects);
        
        // Add any unsatisfied preconditions of this action
        for (Condition precondition : bestAction.getPreconditions()) {
            if (!currentState.satisfies(precondition)) {
                unsatisfiedProperties.add(precondition.getPropertyKey());
            }
        }
    }
    
    return minimumActionSet.size();
}
```

## State Manager Implementation

```java
public class HashMapWorldState implements WorldState {
    private final Map<String, Object> properties;
    
    public HashMapWorldState() {
        this.properties = new HashMap<>();
    }
    
    public HashMapWorldState(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }
    
    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    @Override
    public boolean satisfies(Condition condition) {
        return condition.isSatisfied(this);
    }
    
    @Override
    public WorldState copy() {
        return new HashMapWorldState(new HashMap<>(properties));
    }
    
    @Override
    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    @Override
    public String toString() {
        return properties.toString();
    }
}

public class DefaultStateManager implements StateManager {
    private WorldState currentState;
    private final List<StateChangeListener> listeners = new ArrayList<>();
    
    public DefaultStateManager(WorldState initialState) {
        this.currentState = initialState;
    }
    
    @Override
    public synchronized WorldState getCurrentState() {
        return currentState.copy();
    }
    
    @Override
    public synchronized void updateState(WorldState newState) {
        WorldState oldState = currentState;
        currentState = newState;
        
        // Notify listeners
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(oldState, newState);
        }
    }
    
    @Override
    public synchronized void setProperty(String key, Object value) {
        WorldState oldState = currentState.copy();
        currentState.setProperty(key, value);
        
        // Notify listeners
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(oldState, currentState);
        }
    }
    
    @Override
    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }
}
```

## Action Repository Implementation

```java
public class DefaultActionRepository implements ActionRepository {
    private final Map<String, Action> allActions = new HashMap<>();
    private final Set<String> unavailableActionIds = new HashSet<>();
    
    @Override
    public void registerAction(Action action) {
        allActions.put(action.getId(), action);
    }
    
    @Override
    public Collection<Action> getAllActions() {
        return allActions.values();
    }
    
    @Override
    public List<Action> getAvailableActions(WorldState state) {
        return allActions.values().stream()
            .filter(action -> !unavailableActionIds.contains(action.getId()))
            .filter(action -> action.checkPreconditions(state))
            .collect(Collectors.toList());
    }
    
    @Override
    public void markActionAsUnavailable(Action action) {
        unavailableActionIds.add(action.getId());
    }
    
    @Override
    public void resetActionAvailability() {
        unavailableActionIds.clear();
    }
    
    @Override
    public Action getAction(String id) {
        return allActions.get(id);
    }
}
```

## Action Types Implementation

```java
public abstract class BaseAction implements Action {
    private final String id;
    private final String name;
    private final List<Condition> preconditions;
    private final List<Effect> effects;
    private final float cost;
    private final boolean critical;
    
    public BaseAction(String id, String name, List<Condition> preconditions, 
                     List<Effect> effects, float cost, boolean critical) {
        this.id = id;
        this.name = name;
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.critical = critical;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean checkPreconditions(WorldState state) {
        for (Condition condition : preconditions) {
            if (!state.satisfies(condition)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public WorldState applyEffects(WorldState state) {
        WorldState newState = state.copy();
        for (Effect effect : effects) {
            effect.apply(newState);
        }
        return newState;
    }
    
    @Override
    public float getCost() {
        return cost;
    }
    
    @Override
    public List<Condition> getPreconditions() {
        return preconditions;
    }
    
    @Override
    public List<Effect> getEffects() {
        return effects;
    }
    
    @Override
    public boolean isCritical() {
        return critical;
    }
}

public class SimpleCondition implements Condition {
    private final String propertyKey;
    private final Object expectedValue;
    
    public SimpleCondition(String propertyKey, Object expectedValue) {
        this.propertyKey = propertyKey;
        this.expectedValue = expectedValue;
    }
    
    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
    
    @Override
    public boolean isSatisfied(WorldState state) {
        Object actualValue = state.getProperty(propertyKey);
        
        if (expectedValue == null) {
            return actualValue == null;
        }
        
        return expectedValue.equals(actualValue);
    }
}

public class SimpleEffect implements Effect {
    private final String propertyKey;
    private final Object value;
    
    public SimpleEffect(String propertyKey, Object value) {
        this.propertyKey = propertyKey;
        this.value = value;
    }
    
    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
    
    @Override
    public void apply(WorldState state) {
        state.setProperty(propertyKey, value);
    }
}

public class DefaultActionResult implements ActionResult {
    private final boolean success;
    private final String errorMessage;
    private final Map<String, Object> data;
    
    public DefaultActionResult(boolean success, Map<String, Object> data) {
        this.success = success;
        this.errorMessage = null;
        this.data = data != null ? data : new HashMap<>();
    }
    
    public DefaultActionResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = new HashMap<>();
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }
}
```

## Execution Engine Implementation

```java
public class DefaultExecutionEngine implements ExecutionEngine {
    private final StateManager stateManager;
    private final List<ExecutionListener> listeners = new ArrayList<>();
    private final ParallelExecutionManager parallelExecutionManager;
    
    private Plan currentPlan;
    private int currentActionIndex;
    private boolean isPaused;
    private boolean isAborting;
    
    public DefaultExecutionEngine(StateManager stateManager, ParallelExecutionManager parallelExecutionManager) {
        this.stateManager = stateManager;
        this.parallelExecutionManager = parallelExecutionManager;
    }
    
    @Override
    public void execute(Plan plan) {
        currentPlan = plan;
        currentActionIndex = 0;
        isPaused = false;
        isAborting = false;
        
        // Start execution in a separate thread
        Thread executionThread = new Thread(this::executeActions);
        executionThread.start();
    }
    
    @Override
    public void finishCurrentActionThenPause() {
        isPaused = true;
    }
    
    @Override
    public void abortExecution() {
        isAborting = true;
    }
    
    @Override
    public Action getCurrentAction() {
        if (currentPlan == null || currentActionIndex >= currentPlan.getActions().size()) {
            return null;
        }
        
        return currentPlan.getActions().get(currentActionIndex);
    }
    
    @Override
    public List<Action> getRemainingActions() {
        if (currentPlan == null || currentActionIndex >= currentPlan.getActions().size()) {
            return Collections.emptyList();
        }
        
        return currentPlan.getActions().subList(currentActionIndex, currentPlan.getActions().size());
    }
    
    @Override
    public int getRemainingActionsCount() {
        if (currentPlan == null) {
            return 0;
        }
        
        return currentPlan.getActions().size() - currentActionIndex;
    }
    
    @Override
    public void addExecutionListener(ExecutionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Execute actions in the current plan
     */
    private void executeActions() {
        if (currentPlan == null || currentPlan.getActions().isEmpty()) {
            notifyExecutionComplete(true);
            return;
        }
        
        boolean success = true;
        
        while (currentActionIndex < currentPlan.getActions().size() && !isAborting) {
            // Get current action or action group
            Object current = currentPlan.getActions().get(currentActionIndex);
            
            if (current instanceof Action) {
                // Single action
                Action action = (Action) current;
                success = executeAction(action);
            } else if (current instanceof ActionGroup) {
                // Parallel action group
                ActionGroup group = (ActionGroup) current;
                success = executeActionGroup(group);
            }
            
            if (!success) {
                break;
            }
            
            // Move to next action
            currentActionIndex++;
            
            // Check if paused
            if (isPaused) {
                notifyExecutionPaused();
                break;
            }
        }
        
        // Check if aborted
        if (isAborting) {
            notifyExecutionAborted();
            return;
        }
        
        // Check if completed
        if (currentActionIndex >= currentPlan.getActions().size() && !isPaused) {
            notifyExecutionComplete(success);
        }
    }
    
    // Implementation of executeAction, executeActionGroup, and notification methods...
}
```

## Plan Transition Implementation

```java
public class DefaultWorkflowOrchestrator implements WorkflowOrchestrator {
    private final Planner planner;
    private final StateManager stateManager;
    private final ExecutionEngine executionEngine;
    private final ActionRepository actionRepository;
    private final GoalFactory goalFactory;
    
    // Current execution state
    private Plan currentPlan;
    private Goal currentGoal;
    private boolean isExecuting;
    private TransitionStrategy defaultTransitionStrategy = TransitionStrategy.COMPLETE_CURRENT_ACTION;
    
    // Transition tracking
    private boolean lastActionFailed = false;
    private boolean userRequestedNewGoal = false;
    private boolean worldStateChanged = false;
    
    public DefaultWorkflowOrchestrator(Planner planner, StateManager stateManager, 
                                      ExecutionEngine executionEngine, ActionRepository actionRepository,
                                      GoalFactory goalFactory) {
        this.planner = planner;
        this.stateManager = stateManager;
        this.executionEngine = executionEngine;
        this.actionRepository = actionRepository;
        this.goalFactory = goalFactory;
    }
    
    @Override
    public void handleUserRequest(UserRequest request) {
        // Convert user request to a formal goal
        Goal newGoal = goalFactory.createFromRequest(request);
        userRequestedNewGoal = true;
        
        // Get current world state
        WorldState currentState = stateManager.getCurrentState();
        
        // Check available actions (might have changed)
        List<Action> availableActions = actionRepository.getAvailableActions(currentState);
        
        // Create a new plan
        Plan newPlan = planner.createPlan(currentState, newGoal, availableActions);
        
        // Handle the transition to the new plan
        transitionToPlan(newPlan, newGoal);
    }
    
    @Override
    public void transitionToPlan(Plan newPlan, Goal newGoal) {
        // Record transition for analytics and debugging
        recordPlanTransition(currentPlan, newPlan, currentGoal, newGoal);
        
        TransitionStrategy strategy = determineTransitionStrategy(currentPlan, newPlan);
        
        if (isExecuting) {
            switch (strategy) {
                case COMPLETE_CURRENT_ACTION:
                    // Allow the current action to complete before transitioning
                    executionEngine.finishCurrentActionThenPause();
                    
                    // Update the world state after the action completes
                    WorldState updatedState = stateManager.getCurrentState();
                    
                    // Check if the new plan is still valid with the updated state
                    if (!planner.isPlanValid(newPlan, updatedState, newGoal)) {
                        // Replan if necessary
                        newPlan = planner.createPlan(updatedState, newGoal, 
                                                   actionRepository.getAvailableActions(updatedState));
                    }
                    break;
                    
                case IMMEDIATE_ABORT:
                    // Immediately stop the current action and transition
                    executionEngine.abortExecution();
                    
                    // Capture the state after abortion
                    WorldState abortedState = stateManager.getCurrentState();
                    
                    // We might need to replan if aborting changed the state
                    if (!planner.isPlanValid(newPlan, abortedState, newGoal)) {
                        newPlan = planner.createPlan(abortedState, newGoal,
                                                   actionRepository.getAvailableActions(abortedState));
                    }
                    break;
                    
                case MERGE_PLANS:
                    // Try to merge the current plan with the new plan
                    Plan mergedPlan = mergePlans(currentPlan, newPlan);
                    if (mergedPlan != null) {
                        newPlan = mergedPlan;
                    } else {
                        // Fall back to COMPLETE_CURRENT_ACTION if merge fails
                        executionEngine.finishCurrentActionThenPause();
                    }
                    break;
            }
        }
        
        // Update execution metadata
        currentPlan = newPlan;
        currentGoal = newGoal;
        
        // Store transition in state manager for context
        stateManager.setProperty("last_plan_transition_time", System.currentTimeMillis());
        stateManager.setProperty("last_plan_transition_reason", getTransitionReason());
        
        // Start executing the new plan
        executionEngine.execute(newPlan);
        isExecuting = true;
    }
    
    @Override
    public void handleActionFailure(Action failedAction, WorldState currentState) {
        // Record that an action failed
        lastActionFailed = true;
        
        // Remove the failed action from consideration temporarily
        actionRepository.markActionAsUnavailable(failedAction);
        
        // Replan with the updated world state
        Plan newPlan = planner.createPlan(currentState, currentGoal, 
                                         actionRepository.getAvailableActions(currentState));
        
        if (newPlan != null) {
            // A new valid plan was found
            transitionToPlan(newPlan, currentGoal);
        } else {
            // No valid plan could be found
            handlePlanningFailure(currentGoal);
        }
    }
    
    // Implementation of helper methods...
    
    /**
     * Enum defining transition strategies
     */
    private enum TransitionStrategy {
        COMPLETE_CURRENT_ACTION,  // Finish the current action before transitioning
        IMMEDIATE_ABORT,          // Abort the current action immediately
        MERGE_PLANS               // Try to merge the current and new plans
    }
}
```

## Parallel Execution Implementation

```java
public class DefaultParallelExecutionManager implements ParallelExecutionManager {
    private final StateManager stateManager;
    private final ExecutorService executorService;
    
    public DefaultParallelExecutionManager(StateManager stateManager, int maxThreads) {
        this.stateManager = stateManager;
        this.executorService = Executors.newFixedThreadPool(maxThreads);
    }
    
    @Override
    public CompletableFuture<List<ActionResult>> executeParallel(List<Action> actions, WorldState initialState) {
        // Create a copy of the state for each action
        List<CompletableFuture<ActionResult>> futures = new ArrayList<>();
        
        for (Action action : actions) {
            CompletableFuture<ActionResult> future = CompletableFuture.supplyAsync(() -> {
                // Check preconditions
                if (!action.checkPreconditions(initialState)) {
                    return new DefaultActionResult(false, "Preconditions not met");
                }
                
                // Execute action
                return action.execute(initialState);
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all actions to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    @Override
    public WorldState mergeResults(WorldState initialState, List<Action> actions, List<ActionResult> results) {
        WorldState mergedState = initialState.copy();
        
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            ActionResult result = results.get(i);
            
            if (result.isSuccess()) {
                // Apply action effects
                WorldState actionState = action.applyEffects(initialState.copy());
                
                // Merge properties from action state into merged state
                for (Map.Entry<String, Object> entry : actionState.getAllProperties().entrySet()) {
                    mergedState.setProperty(entry.getKey(), entry.getValue());
                }
                
                // Add result data
                for (Map.Entry<String, Object> entry :
