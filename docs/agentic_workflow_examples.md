# Agentic Workflow Orchestrator: Examples and Usage

## Table of Contents
1. [Project Setup](#project-setup)
2. [Basic Usage](#basic-usage)
3. [Creating Custom Actions](#creating-custom-actions)
4. [Defining Goals](#defining-goals)
5. [Handling Plan Transitions](#handling-plan-transitions)
6. [Parallel Action Execution](#parallel-action-execution)
7. [Complete Example: Support Agent](#complete-example-support-agent)
8. [Complete Example: Financial Operator](#complete-example-financial-operator)

## Project Setup

To set up a new project using the Agentic Workflow Orchestrator, you'll need to include the necessary dependencies in your Maven or Gradle build file.

### Maven Setup

```xml
<dependencies>
    <!-- Core dependencies -->
    <dependency>
        <groupId>com.agentic</groupId>
        <artifactId>workflow-orchestrator-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Action implementations -->
    <dependency>
        <groupId>com.agentic</groupId>
        <artifactId>workflow-orchestrator-actions</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Optional: LLM client integration -->
    <dependency>
        <groupId>com.agentic</groupId>
        <artifactId>workflow-orchestrator-llm</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle Setup

```groovy
dependencies {
    // Core dependencies
    implementation 'com.agentic:workflow-orchestrator-core:1.0.0'
    
    // Action implementations
    implementation 'com.agentic:workflow-orchestrator-actions:1.0.0'
    
    // Optional: LLM client integration
    implementation 'com.agentic:workflow-orchestrator-llm:1.0.0'
}
```

## Basic Usage

Here's a simple example of how to set up and use the Agentic Workflow Orchestrator:

```java
import com.agentic.workflow.*;
import com.agentic.workflow.actions.*;
import com.agentic.workflow.goap.*;
import com.agentic.workflow.state.*;

public class SimpleWorkflowExample {
    public static void main(String[] args) {
        // Create initial world state
        WorldState initialState = new HashMapWorldState();
        initialState.setProperty("hasUserQuery", true);
        initialState.setProperty("userQuery", "How do I reset my password?");
        initialState.setProperty("hasResponse", false);
        
        // Create state manager
        StateManager stateManager = new DefaultStateManager(initialState);
        
        // Create action repository and register actions
        ActionRepository actionRepository = new DefaultActionRepository();
        registerActions(actionRepository);
        
        // Create heuristic function
        HeuristicFunction heuristicFunction = new CompositeHeuristic();
        
        // Create A* pathfinder
        AStarPathfinder pathfinder = new AStarPathfinder(heuristicFunction);
        
        // Create GOAP planner
        Planner planner = new GOAPPlanner(pathfinder);
        
        // Create parallel execution manager
        ParallelExecutionManager parallelExecutionManager = 
            new DefaultParallelExecutionManager(stateManager, 4); // 4 threads
        
        // Create execution engine
        ExecutionEngine executionEngine = 
            new DefaultExecutionEngine(stateManager, parallelExecutionManager);
        
        // Create goal factory
        GoalFactory goalFactory = new SimpleGoalFactory();
        
        // Create workflow orchestrator
        WorkflowOrchestrator orchestrator = new DefaultWorkflowOrchestrator(
            planner, stateManager, executionEngine, actionRepository, goalFactory);
        
        // Create a user request
        UserRequest request = new UserRequest("Answer user query", Map.of(
            "hasResponse", true
        ));
        
        // Handle the request
        orchestrator.handleUserRequest(request);
    }
    
    private static void registerActions(ActionRepository repository) {
        // Register actions
        repository.registerAction(new QueryUnderstandingAction(
            "query_understanding",
            "Understand user query",
            List.of(new SimpleCondition("hasUserQuery", true)),
            List.of(new SimpleEffect("queryUnderstood", true)),
            1.0f,
            false,
            new LLMClientImpl(),
            "Understand the following user query: {{userQuery}}",
            Map.of("userQuery", "userQuery")
        ));
        
        repository.registerAction(new KnowledgeRetrievalAction(
            "knowledge_retrieval",
            "Retrieve knowledge",
            List.of(new SimpleCondition("queryUnderstood", true)),
            List.of(new SimpleEffect("hasKnowledge", true)),
            2.0f,
            false
        ));
        
        repository.registerAction(new ResponseGenerationAction(
            "response_generation",
            "Generate response",
            List.of(
                new SimpleCondition("queryUnderstood", true),
                new SimpleCondition("hasKnowledge", true)
            ),
            List.of(new SimpleEffect("hasResponse", true)),
            1.5f,
            false,
            new LLMClientImpl(),
            "Generate a response to the user query: {{userQuery}} using the following knowledge: {{knowledge}}",
            Map.of(
                "userQuery", "userQuery",
                "knowledge", "knowledge"
            )
        ));
    }
}
```

## Creating Custom Actions

You can create custom actions by extending the `BaseAction` class or implementing the `Action` interface directly.

### Example: Custom HTTP API Action

```java
public class WeatherAPIAction extends BaseAction {
    private final HttpClient httpClient;
    private final String apiKey;
    
    public WeatherAPIAction(String id, String name, List<Condition> preconditions, 
                           List<Effect> effects, float cost, boolean critical,
                           HttpClient httpClient, String apiKey) {
        super(id, name, preconditions, effects, cost, critical);
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }
    
    @Override
    public ActionResult execute(WorldState state) {
        try {
            // Get location from state
            String location = (String) state.getProperty("location");
            
            // Build API URL
            String url = "https://api.weather.com/forecast?location=" + 
                         URLEncoder.encode(location, StandardCharsets.UTF_8) + 
                         "&apiKey=" + apiKey;
            
            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            // Process response
            if (response.statusCode() == 200) {
                // Parse JSON response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                
                // Extract weather data
                String forecast = root.path("forecast").path("summary").asText();
                int temperature = root.path("current").path("temperature").asInt();
                
                // Create result data
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("forecast", forecast);
                resultData.put("temperature", temperature);
                
                return new DefaultActionResult(true, resultData);
            } else {
                return new DefaultActionResult(false, "API error: " + response.statusCode());
            }
        } catch (Exception e) {
            return new DefaultActionResult(false, e.getMessage());
        }
    }
}
```

### Example: Custom Business Logic Action

```java
public class DataProcessingAction extends BaseAction {
    private final DataProcessor processor;
    
    public DataProcessingAction(String id, String name, List<Condition> preconditions, 
                               List<Effect> effects, float cost, boolean critical,
                               DataProcessor processor) {
        super(id, name, preconditions, effects, cost, critical);
        this.processor = processor;
    }
    
    @Override
    public ActionResult execute(WorldState state) {
        try {
            // Get input data from state
            Object inputData = state.getProperty("inputData");
            
            // Process data
            Object processedData = processor.process(inputData);
            
            // Create result data
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("processedData", processedData);
            
            return new DefaultActionResult(true, resultData);
        } catch (Exception e) {
            return new DefaultActionResult(false, e.getMessage());
        }
    }
}
```

## Defining Goals

Goals define the desired state that the planner should achieve. You can create custom goals by implementing the `Goal` interface.

### Example: Simple Goal

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
```

### Example: Goal Factory

```java
public class SimpleGoalFactory implements GoalFactory {
    @Override
    public Goal createFromRequest(UserRequest request) {
        return new SimpleGoal(request.getDesiredState());
    }
}
```

## Handling Plan Transitions

Plan transitions occur when a new goal is received or when an action fails. Here's an example of how to handle plan transitions:

```java
public class PlanTransitionExample {
    public static void main(String[] args) {
        // ... Initialize orchestrator as in the basic example ...
        
        // Add execution listener
        executionEngine.addExecutionListener(new ExecutionListener() {
            @Override
            public void onActionFailed(Action action, String errorMessage) {
                System.out.println("Action failed: " + action.getName() + " - " + errorMessage);
                
                // Get current state
                WorldState currentState = stateManager.getCurrentState();
                
                // Handle action failure
                orchestrator.handleActionFailure(action, currentState);
            }
            
            // Implement other listener methods...
        });
        
        // Start with initial request
        UserRequest initialRequest = new UserRequest("Initial goal", Map.of(
            "goalProperty", "initialValue"
        ));
        orchestrator.handleUserRequest(initialRequest);
        
        // Later, handle a new request
        UserRequest newRequest = new UserRequest("New goal", Map.of(
            "goalProperty", "newValue"
        ));
        orchestrator.handleUserRequest(newRequest);
    }
}
```

## Parallel Action Execution

You can execute actions in parallel by creating action groups:

```java
public class ParallelExecutionExample {
    public static void main(String[] args) {
        // ... Initialize components as in the basic example ...
        
        // Create action group
        ActionGroup group = new DefaultActionGroup();
        
        // Add actions to group
        Action action1 = actionRepository.getAction("action1");
        Action action2 = actionRepository.getAction("action2");
        Action action3 = actionRepository.getAction("action3");
        
        if (group.canAddAction(action1)) {
            group.addAction(action1);
        }
        
        if (group.canAddAction(action2)) {
            group.addAction(action2);
        }
        
        if (group.canAddAction(action3)) {
            group.addAction(action3);
        }
        
        // Create a plan with the action group
        Plan plan = new Plan(List.of(group));
        
        // Execute the plan
        executionEngine.execute(plan);
    }
}
```

## Complete Example: Support Agent

Here's a complete example of a support agent that helps users with common issues:

```java
public class SupportAgentExample {
    public static void main(String[] args) {
        // Create initial world state
        WorldState initialState = new HashMapWorldState();
        initialState.setProperty("hasUserQuery", true);
        initialState.setProperty("userQuery", "I can't log in to my account");
        initialState.setProperty("hasResponse", false);
        
        // Create components
        StateManager stateManager = new DefaultStateManager(initialState);
        ActionRepository actionRepository = new DefaultActionRepository();
        HeuristicFunction heuristicFunction = new CompositeHeuristic();
        AStarPathfinder pathfinder = new AStarPathfinder(heuristicFunction);
        Planner planner = new GOAPPlanner(pathfinder);
        ParallelExecutionManager parallelExecutionManager = 
            new DefaultParallelExecutionManager(stateManager, 4);
        ExecutionEngine executionEngine = 
            new DefaultExecutionEngine(stateManager, parallelExecutionManager);
        GoalFactory goalFactory = new SimpleGoalFactory();
        
        // Register support agent actions
        registerSupportAgentActions(actionRepository);
        
        // Create workflow orchestrator
        WorkflowOrchestrator orchestrator = new DefaultWorkflowOrchestrator(
            planner, stateManager, executionEngine, actionRepository, goalFactory);
        
        // Add execution listener for logging
        executionEngine.addExecutionListener(new LoggingExecutionListener());
        
        // Create a user request
        UserRequest request = new UserRequest("Help user with issue", Map.of(
            "hasResponse", true,
            "userSatisfied", true
        ));
        
        // Handle the request
        orchestrator.handleUserRequest(request);
    }
    
    private static void registerSupportAgentActions(ActionRepository repository) {
        LLMClient llmClient = new LLMClientImpl();
        
        // Query understanding action
        repository.registerAction(new LLMAction(
            "query_understanding",
            "Understand user query",
            List.of(new SimpleCondition("hasUserQuery", true)),
            List.of(
                new SimpleEffect("queryUnderstood", true),
                new SimpleEffect("queryCategory", "{{category}}")
            ),
            1.0f,
            false,
            llmClient,
            "Categorize the following user query into one of these categories: " +
            "LOGIN_ISSUE, PASSWORD_RESET, BILLING_ISSUE, ACCOUNT_CREATION, OTHER. " +
            "User query: {{userQuery}}\n\n" +
            "Output only the category name.",
            Map.of("userQuery", "userQuery")
        ));
        
        // Knowledge retrieval action
        repository.registerAction(new KnowledgeBaseAction(
            "knowledge_retrieval",
            "Retrieve knowledge",
            List.of(
                new SimpleCondition("queryUnderstood", true),
                new SimpleCondition("queryCategory", "LOGIN_ISSUE")
            ),
            List.of(new SimpleEffect("hasKnowledge", true)),
            2.0f,
            false,
            "login_issues"
        ));
        
        repository.registerAction(new KnowledgeBaseAction(
            "password_knowledge_retrieval",
            "Retrieve password reset knowledge",
            List.of(
                new SimpleCondition("queryUnderstood", true),
                new SimpleCondition("queryCategory", "PASSWORD_RESET")
            ),
            List.of(new SimpleEffect("hasKnowledge", true)),
            2.0f,
            false,
            "password_reset"
        ));
        
        // Response generation action
        repository.registerAction(new LLMAction(
            "response_generation",
            "Generate response",
            List.of(
                new SimpleCondition("queryUnderstood", true),
                new SimpleCondition("hasKnowledge", true)
            ),
            List.of(new SimpleEffect("hasResponse", true)),
            1.5f,
            false,
            llmClient,
            "Generate a helpful response to the user query: {{userQuery}}\n\n" +
            "Use the following knowledge: {{knowledge}}\n\n" +
            "Make the response friendly and concise.",
            Map.of(
                "userQuery", "userQuery",
                "knowledge", "knowledge"
            )
        ));
        
        // User satisfaction check
        repository.registerAction(new LLMAction(
            "satisfaction_check",
            "Check user satisfaction",
            List.of(new SimpleCondition("hasResponse", true)),
            List.of(new SimpleEffect("userSatisfied", true)),
            1.0f,
            false,
            llmClient,
            "Based on the user query and our response, determine if the user is likely satisfied.\n\n" +
            "User query: {{userQuery}}\n" +
            "Our response: {{response}}\n\n" +
            "Output only YES or NO.",
            Map.of(
                "userQuery", "userQuery",
                "response", "response"
            )
        ));
    }
}
```

## Complete Example: Financial Operator

Here's a complete example of a financial operator agent that processes transactions:

```java
public class FinancialOperatorExample {
    public static void main(String[] args) {
        // Create initial world state
        WorldState initialState = new HashMapWorldState();
        initialState.setProperty("hasTransactionRequest", true);
        initialState.setProperty("transactionAmount", 1000.0);
        initialState.setProperty("sourceAccount", "12345");
        initialState.setProperty("destinationAccount", "67890");
        initialState.setProperty("transactionProcessed", false);
        
        // Create components
        StateManager stateManager = new DefaultStateManager(initialState);
        ActionRepository actionRepository = new DefaultActionRepository();
        HeuristicFunction heuristicFunction = new CompositeHeuristic();
        AStarPathfinder pathfinder = new AStarPathfinder(heuristicFunction);
        Planner planner = new GOAPPlanner(pathfinder);
        ParallelExecutionManager parallelExecutionManager = 
            new DefaultParallelExecutionManager(stateManager, 4);
        ExecutionEngine executionEngine = 
            new DefaultExecutionEngine(stateManager, parallelExecutionManager);
        GoalFactory goalFactory = new SimpleGoalFactory();
        
        // Register financial operator actions
        registerFinancialOperatorActions(actionRepository);
        
        // Create workflow orchestrator
        WorkflowOrchestrator orchestrator = new DefaultWorkflowOrchestrator(
            planner, stateManager, executionEngine, actionRepository, goalFactory);
        
        // Add execution listener for logging
        executionEngine.addExecutionListener(new LoggingExecutionListener());
        
        // Create a transaction request
        UserRequest request = new UserRequest("Process transaction", Map.of(
            "transactionProcessed", true,
            "transactionSuccessful", true
        ));
        
        // Handle the request
        orchestrator.handleUserRequest(request);
    }
    
    private static void registerFinancialOperatorActions(ActionRepository repository) {
        // Account validation action
        repository.registerAction(new AccountValidationAction(
            "account_validation",
            "Validate accounts",
            List.of(
                new SimpleCondition("hasTransactionRequest", true),
                new SimpleCondition("sourceAccount", "12345") // For simplicity
            ),
            List.of(new SimpleEffect("accountsValidated", true)),
            1.0f,
            true // Critical action
        ));
        
        // Fraud detection action
        repository.registerAction(new FraudDetectionAction(
            "fraud_detection",
            "Detect fraud",
            List.of(new SimpleCondition("accountsValidated", true)),
            List.of(new SimpleEffect("fraudChecked", true)),
            2.0f,
            true // Critical action
        ));
        
        // Balance check action
        repository.registerAction(new BalanceCheckAction(
            "balance_check",
            "Check balance",
            List.of(
                new SimpleCondition("accountsValidated", true),
                new SimpleCondition("fraudChecked", true)
            ),
            List.of(
                new SimpleEffect("balanceChecked", true),
                new SimpleEffect("sufficientFunds", true)
            ),
            1.0f,
            false
        ));
        
        // Transaction processing action
        repository.registerAction(new TransactionProcessingAction(
            "transaction_processing",
            "Process transaction",
            List.of(
                new SimpleCondition("accountsValidated", true),
                new SimpleCondition("fraudChecked", true),
                new SimpleCondition("balanceChecked", true),
                new SimpleCondition("sufficientFunds", true)
            ),
            List.of(
                new SimpleEffect("transactionProcessed", true),
                new SimpleEffect("transactionSuccessful", true)
            ),
            3.0f,
            true // Critical action
        ));
        
        // Notification action
        repository.registerAction(new NotificationAction(
            "notification",
            "Send notification",
            List.of(new SimpleCondition("transactionProcessed", true)),
            List.of(new SimpleEffect("notificationSent", true)),
            1.0f,
            false
        ));
    }
}
