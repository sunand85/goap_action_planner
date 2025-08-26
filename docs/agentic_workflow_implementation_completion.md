# Agentic Workflow Orchestrator: Implementation Details (Continued)

This document continues the implementation details from `agentic_workflow_implementation.md`, focusing on the Parallel Execution Implementation and other components that were truncated.

## Parallel Execution Implementation (Continued)

```java
public class DefaultParallelExecutionManager implements ParallelExecutionManager {
    // ... previous code from agentic_workflow_implementation.md ...
    
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
                for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
                    mergedState.setProperty("result." + action.getId() + "." + entry.getKey(), entry.getValue());
                }
            }
        }
        
        return mergedState;
    }
    
    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}

public class DefaultActionGroup implements ActionGroup {
    private final List<Action> actions = new ArrayList<>();
    
    @Override
    public void addAction(Action action) {
        actions.add(action);
    }
    
    @Override
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
    
    @Override
    public boolean canAddAction(Action action) {
        // Check if the action can be executed in parallel with existing actions
        for (Action existingAction : actions) {
            if (!areActionsParallelizable(existingAction, action)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean areActionsParallelizable(Action a1, Action a2) {
        // Actions are parallelizable if they don't have conflicting effects or preconditions
        for (Effect effect : a1.getEffects()) {
            String propertyKey = effect.getPropertyKey();
            
            // Check if a2 has a precondition on this property
            for (Condition precondition : a2.getPreconditions()) {
                if (precondition.getPropertyKey().equals(propertyKey)) {
                    return false;
                }
            }
            
            // Check if a2 has an effect on this property
            for (Effect otherEffect : a2.getEffects()) {
                if (otherEffect.getPropertyKey().equals(propertyKey)) {
                    return false;
                }
            }
        }
        
        // Check the other way around
        for (Effect effect : a2.getEffects()) {
            String propertyKey = effect.getPropertyKey();
            
            // Check if a1 has a precondition on this property
            for (Condition precondition : a1.getPreconditions()) {
                if (precondition.getPropertyKey().equals(propertyKey)) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
```

## Specialized Action Implementations

### MCP Tool Action

```java
public class MCPToolAction extends BaseAction {
    private final MCPClient mcpClient;
    private final String serverName;
    private final String toolName;
    private final Map<String, String> argumentMappings;
    
    public MCPToolAction(String id, String name, List<Condition> preconditions, 
                        List<Effect> effects, float cost, boolean critical,
                        MCPClient mcpClient, String serverName, String toolName,
                        Map<String, String> argumentMappings) {
        super(id, name, preconditions, effects, cost, critical);
        this.mcpClient = mcpClient;
        this.serverName = serverName;
        this.toolName = toolName;
        this.argumentMappings = argumentMappings;
    }
    
    @Override
    public ActionResult execute(WorldState state) {
        try {
            // Build arguments from state
            Map<String, Object> arguments = new HashMap<>();
            for (Map.Entry<String, String> entry : argumentMappings.entrySet()) {
                String argName = entry.getKey();
                String stateKey = entry.getValue();
                Object value = state.getProperty(stateKey);
                
                if (value != null) {
                    arguments.put(argName, value);
                }
            }
            
            // Call MCP tool
            MCPResponse response = mcpClient.callTool(serverName, toolName, arguments);
            
            // Create result with response
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("response", response.getResult());
            
            return new DefaultActionResult(response.isSuccess(), resultData);
        } catch (Exception e) {
            return new DefaultActionResult(false, e.getMessage());
        }
    }
}
```

### Business Logic Action

```java
public class BusinessLogicAction extends BaseAction {
    private final Function<WorldState, ActionResult> businessLogic;
    
    public BusinessLogicAction(String id, String name, List<Condition> preconditions, 
                              List<Effect> effects, float cost, boolean critical,
                              Function<WorldState, ActionResult> businessLogic) {
        super(id, name, preconditions, effects, cost, critical);
        this.businessLogic = businessLogic;
    }
    
    @Override
    public ActionResult execute(WorldState state) {
        try {
            // Execute business logic
            return businessLogic.apply(state);
        } catch (Exception e) {
            return new DefaultActionResult(false, e.getMessage());
        }
    }
}
```

## Execution Engine Implementation Details

Here are the missing methods from the `DefaultExecutionEngine` class:

```java
/**
 * Execute a single action
 */
private boolean executeAction(Action action) {
    // Notify listeners
    notifyActionStarted(action);
    
    // Get current state
    WorldState currentState = stateManager.getCurrentState();
    
    // Check preconditions
    if (!action.checkPreconditions(currentState)) {
        notifyActionFailed(action, "Preconditions not met");
        return false;
    }
    
    // Execute action
    ActionResult result = action.execute(currentState);
    
    if (result.isSuccess()) {
        // Apply effects to state
        WorldState newState = action.applyEffects(currentState);
        
        // Update state with result data
        for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
            newState.setProperty("result." + entry.getKey(), entry.getValue());
        }
        
        // Update state manager
        stateManager.updateState(newState);
        
        // Notify listeners
        notifyActionCompleted(action, result);
        
        return true;
    } else {
        // Action failed
        notifyActionFailed(action, result.getErrorMessage());
        return false;
    }
}

/**
 * Execute a group of actions in parallel
 */
private boolean executeActionGroup(ActionGroup group) {
    // Notify listeners
    notifyActionGroupStarted(group);
    
    // Get current state
    WorldState currentState = stateManager.getCurrentState();
    
    try {
        // Execute actions in parallel
        CompletableFuture<List<ActionResult>> future = 
            parallelExecutionManager.executeParallel(group.getActions(), currentState);
        
        // Wait for all actions to complete
        List<ActionResult> results = future.get();
        
        // Check if all actions succeeded
        boolean allSucceeded = results.stream().allMatch(ActionResult::isSuccess);
        
        if (allSucceeded) {
            // Merge results into a new state
            WorldState newState = parallelExecutionManager.mergeResults(
                currentState, group.getActions(), results);
            
            // Update state manager
            stateManager.updateState(newState);
            
            // Notify listeners
            notifyActionGroupCompleted(group, results);
            
            return true;
        } else {
            // Some actions failed
            notifyActionGroupFailed(group, results);
            return false;
        }
    } catch (Exception e) {
        // Execution failed
        notifyActionGroupFailed(group, e.getMessage());
        return false;
    }
}

// Notification methods
private void notifyActionStarted(Action action) {
    for (ExecutionListener listener : listeners) {
        listener.onActionStarted(action);
    }
}

private void notifyActionCompleted(Action action, ActionResult result) {
    for (ExecutionListener listener : listeners) {
        listener.onActionCompleted(action, result);
    }
}

private void notifyActionFailed(Action action, String errorMessage) {
    for (ExecutionListener listener : listeners) {
        listener.onActionFailed(action, errorMessage);
    }
}

private void notifyActionGroupStarted(ActionGroup group) {
    for (ExecutionListener listener : listeners) {
        listener.onActionGroupStarted(group);
    }
}

private void notifyActionGroupCompleted(ActionGroup group, List<ActionResult> results) {
    for (ExecutionListener listener : listeners) {
        listener.onActionGroupCompleted(group, results);
    }
}

private void notifyActionGroupFailed(ActionGroup group, List<ActionResult> results) {
    for (ExecutionListener listener : listeners) {
        listener.onActionGroupFailed(group, results);
    }
}

private void notifyActionGroupFailed(ActionGroup group, String errorMessage) {
    for (ExecutionListener listener : listeners) {
        listener.onActionGroupFailed(group, errorMessage);
    }
}

private void notifyExecutionComplete(boolean success) {
    for (ExecutionListener listener : listeners) {
        listener.onExecutionComplete(currentPlan, success);
    }
}

private void notifyExecutionPaused() {
    for (ExecutionListener listener : listeners) {
        listener.onExecutionPaused(currentPlan, currentActionIndex);
    }
}

private void notifyExecutionAborted() {
    for (ExecutionListener listener : listeners) {
        listener.onExecutionAborted(currentPlan, currentActionIndex);
    }
}
```

## Plan Transition Implementation Details

Here are the missing methods from the `DefaultWorkflowOrchestrator` class:

```java
/**
 * Determines the best strategy for transitioning between plans
 */
private TransitionStrategy determineTransitionStrategy(Plan currentPlan, Plan newPlan) {
    // If no current plan or not executing, immediate transition is fine
    if (currentPlan == null || !isExecuting) {
        return TransitionStrategy.IMMEDIATE_ABORT;
    }
    
    // Get the current action being executed
    Action currentAction = executionEngine.getCurrentAction();
    
    // If the current action is critical or expensive to abort, complete it
    if (currentAction != null && currentAction.isCritical()) {
        return TransitionStrategy.COMPLETE_CURRENT_ACTION;
    }
    
    // Check if plans can be merged (e.g., new plan is an extension of current)
    if (canMergePlans(currentPlan, newPlan)) {
        return TransitionStrategy.MERGE_PLANS;
    }
    
    // Default strategy based on configuration
    return defaultTransitionStrategy;
}

/**
 * Merges two plans into a single coherent plan
 */
private Plan mergePlans(Plan currentPlan, Plan newPlan) {
    List<Action> remainingActions = executionEngine.getRemainingActions();
    List<Action> newActions = newPlan.getActions();
    
    // Find the point of divergence
    int divergencePoint = 0;
    int maxCheck = Math.min(remainingActions.size(), newActions.size());
    
    while (divergencePoint < maxCheck && 
           remainingActions.get(divergencePoint).equals(newActions.get(divergencePoint))) {
        divergencePoint++;
    }
    
    if (divergencePoint == 0) {
        // No overlap, can't merge
        return null;
    }
    
    // Create merged plan: common prefix + remaining new actions
    List<Action> mergedActions = new ArrayList<>(remainingActions.subList(0, divergencePoint));
    mergedActions.addAll(newActions.subList(divergencePoint, newActions.size()));
    
    return new Plan(mergedActions);
}

/**
 * Records the transition for analytics and debugging
 */
private void recordPlanTransition(Plan oldPlan, Plan newPlan, Goal oldGoal, Goal newGoal) {
    PlanTransition transition = new PlanTransition(
        oldPlan, newPlan, oldGoal, newGoal, 
        System.currentTimeMillis(), getTransitionReason()
    );
    
    // Log the transition
    logger.info("Plan transition: {} -> {}, Reason: {}", 
               oldPlan != null ? oldPlan.getId() : "null",
               newPlan.getId(),
               getTransitionReason());
}

/**
 * Gets the reason for the current transition
 */
private String getTransitionReason() {
    if (lastActionFailed) {
        return "ACTION_FAILURE";
    } else if (userRequestedNewGoal) {
        return "USER_REQUEST";
    } else if (worldStateChanged) {
        return "WORLD_STATE_CHANGE";
    } else {
        return "SYSTEM_INITIATED";
    }
}

/**
 * Handle planning failure
 */
private void handlePlanningFailure(Goal goal) {
    // Log the failure
    logger.error("Failed to create a plan for goal: {}", goal);
    
    // Notify listeners
    for (WorkflowListener listener : workflowListeners) {
        listener.onPlanningFailed(goal);
    }
    
    // Reset action availability
    actionRepository.resetActionAvailability();
    
    // Set planning failure in state
    stateManager.setProperty("planningFailed", true);
    stateManager.setProperty("planningFailedGoal", goal.toString());
}
```

## Additional Components

### Goal Implementation

```java
public class SimpleGoal implements Goal {
    private final Map<String, Object> desiredState;
    private final List<Condition> conditions;
    
    public SimpleGoal(Map<String, Object> desiredState) {
        this.desiredState = desiredState;
        this.conditions = desiredState.entrySet().stream()
            .map(entry -> new SimpleCondition(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isSatisfied(WorldState state) {
        for (Condition condition : conditions) {
            if (!state.satisfies(condition)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public List<Condition> getConditions() {
        return conditions;
    }
    
    @Override
    public Set<String> getRequiredProperties() {
        return desiredState.keySet();
    }
    
    @Override
    public Object getDesiredValue(String key) {
        return desiredState.get(key);
    }
}

public class SimpleGoalFactory implements GoalFactory {
    @Override
    public Goal createFromRequest(UserRequest request) {
        return new SimpleGoal(request.getDesiredState());
    }
}
```

### Plan Implementation

```java
public class Plan {
    private final String id;
    private final List<Object> actions; // Can be Action or ActionGroup
    
    public Plan(List<Object> actions) {
        this.id = UUID.randomUUID().toString();
        this.actions = new ArrayList<>(actions);
    }
    
    public String getId() {
        return id;
    }
    
    public List<Object> getActions() {
        return Collections.unmodifiableList(actions);
    }
    
    public boolean isEmpty() {
        return actions.isEmpty();
    }
    
    public boolean isComplete() {
        return false; // Always return false for simplicity
    }
}
```

### User Request Implementation

```java
public class UserRequest {
    private final String description;
    private final Map<String, Object> desiredState;
    
    public UserRequest(String description, Map<String, Object> desiredState) {
        this.description = description;
        this.desiredState = new HashMap<>(desiredState);
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<String, Object> getDesiredState() {
        return Collections.unmodifiableMap(desiredState);
    }
}
```

### Logging Execution Listener

```java
public class LoggingExecutionListener implements ExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(LoggingExecutionListener.class);
    
    @Override
    public void onActionStarted(Action action) {
        logger.info("Action started: {}", action.getName());
    }
    
    @Override
    public void onActionCompleted(Action action, ActionResult result) {
        logger.info("Action completed: {}", action.getName());
    }
    
    @Override
    public void onActionFailed(Action action, String errorMessage) {
        logger.error("Action failed: {} - {}", action.getName(), errorMessage);
    }
    
    @Override
    public void onActionGroupStarted(ActionGroup group) {
        logger.info("Action group started with {} actions", group.getActions().size());
    }
    
    @Override
    public void onActionGroupCompleted(ActionGroup group, List<ActionResult> results) {
        logger.info("Action group completed with {} results", results.size());
    }
    
    @Override
    public void onActionGroupFailed(ActionGroup group, List<ActionResult> results) {
        logger.error("Action group failed");
    }
    
    @Override
    public void onActionGroupFailed(ActionGroup group, String errorMessage) {
        logger.error("Action group failed: {}", errorMessage);
    }
    
    @Override
    public void onExecutionComplete(Plan plan, boolean success) {
        logger.info("Execution complete: {}", success ? "success" : "failure");
    }
    
    @Override
    public void onExecutionPaused(Plan plan, int actionIndex) {
        logger.info("Execution paused at action index: {}", actionIndex);
    }
    
    @Override
    public void onExecutionAborted(Plan plan, int actionIndex) {
        logger.info("Execution aborted at action index: {}", actionIndex);
    }
}
```

This completes the implementation details that were truncated in the original file.
