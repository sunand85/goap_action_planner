package com.example.ai.goap_demo.goap.core;

import java.util.List;

/**
 * Plans a sequence of actions to achieve a goal from a given state.
 */
public interface Planner {
    /**
     * Creates a plan to achieve the goal from the current state
     */
    Plan createPlan(WorldState currentState, Goal goal, List<Action> availableActions);
    
    /**
     * Checks if a plan is still valid given a state and goal
     */
    boolean isPlanValid(Plan plan, WorldState state, Goal goal);
}
