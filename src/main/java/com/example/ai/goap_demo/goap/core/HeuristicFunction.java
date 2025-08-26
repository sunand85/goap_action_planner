package com.example.ai.goap_demo.goap.core;

import java.util.List;

/**
 * A heuristic function for the A* algorithm.
 * It estimates the cost from the current state to the goal state.
 */
public interface HeuristicFunction {
    /**
     * Calculates the estimated cost from current state to goal
     */
    float calculate(WorldState currentState, Goal goal, List<Action> availableActions);
}
