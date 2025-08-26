package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple implementation of Goal that is satisfied when all conditions are met.
 */
public class SimpleGoal implements Goal {
    private final Map<String, Object> desiredState;
    private final List<Condition> conditions;
    
    public SimpleGoal(Map<String, Object> desiredState) {
        this.desiredState = new HashMap<>(desiredState);
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
        return Collections.unmodifiableList(conditions);
    }
    
    @Override
    public Set<String> getRequiredProperties() {
        return desiredState.keySet();
    }
    
    @Override
    public Object getDesiredValue(String key) {
        return desiredState.get(key);
    }
    
    @Override
    public String toString() {
        return "Goal: " + desiredState;
    }
}
