package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.Plan;
import com.example.ai.goap_demo.goap.core.Planner;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.List;

/**
 * Implementation of the GOAP planner using A* pathfinding.
 */
public class GOAPPlanner implements Planner {
    private final AStarPathfinder pathfinder;
    
    public GOAPPlanner(AStarPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }
    
    @Override
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
    
    @Override
    public boolean isPlanValid(Plan plan, WorldState state, Goal goal) {
        if (plan == null) {
            return false;
        }
        
        // If the plan is empty, check if the goal is already satisfied
        if (plan.isEmpty()) {
            return goal.isSatisfied(state);
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
