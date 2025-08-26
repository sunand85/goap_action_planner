package com.example.ai.goap_demo.pizzabot;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.ActionResult;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.Plan;
import com.example.ai.goap_demo.goap.core.Planner;
import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.AStarPathfinder;
import com.example.ai.goap_demo.goap.impl.GOAPPlanner;
import com.example.ai.goap_demo.goap.impl.HashMapWorldState;
import com.example.ai.goap_demo.goap.impl.SimpleGoal;
import com.example.ai.goap_demo.goap.impl.SimpleHeuristic;
import com.example.ai.goap_demo.pizzabot.actions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PizzaBot that uses GOAP to plan and execute pizza preparation.
 */
public class PizzaBot {
    private WorldState worldState; // Removed final to allow reassignment
    private final List<Action> availableActions;
    private final Planner planner;
    private Plan currentPlan;
    
    public PizzaBot() {
        // Initialize world state
        this.worldState = new HashMapWorldState();
        
        // Initialize available actions
        this.availableActions = new ArrayList<>();
        availableActions.add(new TakeOrderAction());
        availableActions.add(new CheckIngredientsAction());
        availableActions.add(new PrepareDoughAction());
        availableActions.add(new UsePremadeDoughAction()); // Add alternative dough action
        availableActions.add(new AddToppingsAction());
        availableActions.add(new BakePizzaAction());
        availableActions.add(new ServePizzaAction());
        
        // Initialize planner
        AStarPathfinder pathfinder = new AStarPathfinder(new SimpleHeuristic());
        this.planner = new GOAPPlanner(pathfinder);
        
        // Set initial world state
        worldState.setProperty("customerPresent", true);
    }
    
    /**
     * Creates a plan to achieve the goal of serving a pizza
     */
    public Plan createPlan() {
        // Define the goal: serve a pizza
        Map<String, Object> goalState = new HashMap<>();
        goalState.put("pizzaServed", true);
        Goal goal = new SimpleGoal(goalState);
        
        // Create a plan
        Plan plan = planner.createPlan(worldState, goal, availableActions);
        
        if (plan != null) {
            System.out.println("Plan created successfully:");
            System.out.println(plan);
            currentPlan = plan;
            return plan;
        } else {
            System.out.println("Failed to create a plan!");
            return null;
        }
    }
    
    /**
     * Executes the current plan
     */
    public boolean executePlan() {
        return executePlan(0);
    }
    
    /**
     * Executes the current plan with a replan counter to prevent infinite recursion
     * 
     * @param replanCount The number of times replanning has occurred
     * @return true if the plan was executed successfully, false otherwise
     */
    private boolean executePlan(int replanCount) {
        // Limit the number of replans to prevent infinite recursion
        final int MAX_REPLANS = 3;
        
        if (replanCount >= MAX_REPLANS) {
            System.out.println("Maximum number of replans reached (" + MAX_REPLANS + ")");
            return false;
        }
        
        if (currentPlan == null || currentPlan.isEmpty()) {
            System.out.println("No plan to execute!");
            return false;
        }
        
        System.out.println("Executing plan... (replan count: " + replanCount + ")");
        
        for (Action action : currentPlan.getActions()) {
            System.out.println("\n--- Executing: " + action.getName() + " ---");
            
            // Check if action's preconditions are still met
            if (!action.checkPreconditions(worldState)) {
                System.out.println("Preconditions not met for action: " + action.getName());
                System.out.println("Replanning...");
                
                System.out.println("\n=== REPLANNING PROCESS STARTED ===");
                System.out.println("Current world state: " + worldState);
                System.out.println("Reason for replanning: Preconditions not met for action: " + action.getName());
                
                // Try to create a new plan
                Plan newPlan = createPlan();
                if (newPlan == null) {
                    System.out.println("Failed to replan!");
                    return false;
                }
                
                System.out.println("=== REPLANNING PROCESS COMPLETED ===");
                System.out.println("New plan created successfully. Continuing execution...\n");
                
                // Restart execution with the new plan, incrementing the replan counter
                return executePlan(replanCount + 1);
            }
            
            // Execute the action
            ActionResult result = action.execute(worldState);
            
            if (!result.isSuccess()) {
                System.out.println("Action failed: " + result.getErrorMessage());
                System.out.println("Replanning...");
                
                System.out.println("\n=== REPLANNING PROCESS STARTED ===");
                System.out.println("Current world state: " + worldState);
                System.out.println("Reason for replanning: Action failed: " + result.getErrorMessage());
                
                // Try to create a new plan
                Plan newPlan = createPlan();
                if (newPlan == null) {
                    System.out.println("Failed to replan!");
                    return false;
                }
                
                System.out.println("=== REPLANNING PROCESS COMPLETED ===");
                System.out.println("New plan created successfully. Continuing execution...\n");
                
                // Restart execution with the new plan, incrementing the replan counter
                return executePlan(replanCount + 1);
            }
            
            // Apply the action's effects to the world state
            worldState = action.applyEffects(worldState);
        }
        
        System.out.println("\nPlan executed successfully!");
        return true;
    }
    
    /**
     * Gets the current world state
     */
    public WorldState getWorldState() {
        return worldState;
    }
}
