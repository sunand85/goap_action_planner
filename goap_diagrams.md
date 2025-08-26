# GOAP System Diagrams

## GOAP System Architecture

```mermaid
graph TD
    A[PizzaBot] -->|creates goal| B[GOAPPlanner]
    A -->|provides| C[Available Actions]
    A -->|provides| D[Current World State]
    B -->|delegates to| E[AStarPathfinder]
    E -->|uses| F[Heuristic Function]
    E -->|returns| G[Action Sequence]
    B -->|returns| H[Plan]
    A -->|executes| H
    
    subgraph "Action Execution"
        H -->|action 1| I[Check Preconditions]
        I -->|success| J[Execute Action]
        J -->|success| K[Apply Effects]
        K --> L[Next Action]
        J -->|failure| M[Replan]
        M --> B
    end
```

## A* Pathfinding Process

```mermaid
graph TD
    A[Start State] -->|initialize| B[Create Start Node]
    B -->|add to| C[Open Set]
    
    subgraph "Main Loop"
        C -->|get lowest f| D[Current Node]
        D -->|check| E{Goal Reached?}
        E -->|yes| F[Reconstruct Path]
        E -->|no| G[Add to Closed Set]
        G -->|for each| H[Available Action]
        H -->|check| I{Preconditions Met?}
        I -->|yes| J[Apply Effects]
        J -->|create| K[New Node]
        K -->|check| L{In Closed Set?}
        L -->|no| M{In Open Set?}
        M -->|yes, better path| N[Update Node]
        M -->|no| O[Add to Open Set]
        N --> C
        O --> C
    end
    
    F -->|return| P[Action Sequence]
```

## Replanning Process

```mermaid
graph TD
    A[Action Execution] -->|action fails| B[Set doughPreparationFailed=true]
    B -->|request| C[New Plan]
    C -->|A* search| D[Find Alternative Path]
    D -->|includes| E[UsePremadeDough]
    E -->|instead of| F[PrepareDough]
    E -->|leads to| G[Goal Achieved]
```

## State Transitions Example

```mermaid
graph LR
    A[Initial State] -->|Take Order| B[orderTaken=true]
    B -->|Check Ingredients| C[ingredientsChecked=true]
    
    subgraph "Original Path"
        C -->|Prepare Dough| D[doughPrepared=true]
    end
    
    subgraph "Alternative Path"
        C -->|Prepare Dough Fails| E[doughPreparationFailed=true]
        E -->|Use Premade Dough| F[doughPrepared=true]
    end
    
    D -->|Add Toppings| G[toppingsAdded=true]
    F -->|Add Toppings| G
    G -->|Bake Pizza| H[pizzaBaked=true]
    H -->|Serve Pizza| I[pizzaServed=true]
```

These diagrams illustrate how the GOAP system works, particularly focusing on the A* pathfinding algorithm and the replanning process when actions fail.
