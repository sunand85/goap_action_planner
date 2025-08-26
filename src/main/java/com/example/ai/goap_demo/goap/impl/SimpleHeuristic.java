package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.HeuristicFunction;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.List;

/**
 * A simple heuristic function that counts the number of unsatisfied conditions.
 */
public class SimpleHeuristic implements HeuristicFunction {
    @Override
    public float calculate(WorldState currentState, Goal goal, List<Action> availableActions) {
        int unsatisfiedConditions = 0;
        
        for (Condition condition : goal.getConditions()) {
            if (!currentState.satisfies(condition)) {
                unsatisfiedConditions++;
            }
        }
        
        return unsatisfiedConditions;
    }
}
