package com.example.ai.goap_demo.goap.core;

import java.util.List;
import java.util.Set;

/**
 * Represents a goal that an agent wants to achieve.
 * A goal is satisfied when all of its conditions are met.
 */
public interface Goal {
    /**
     * Check if the goal is satisfied in the given state
     */
    boolean isSatisfied(WorldState state);
    
    /**
     * Get the conditions that must be satisfied for this goal
     */
    List<Condition> getConditions();
    
    /**
     * Get the properties required by this goal
     */
    Set<String> getRequiredProperties();
    
    /**
     * Get the desired value for a property
     */
    Object getDesiredValue(String key);
}
