package com.example.ai.goap_demo.goap.core;

/**
 * Represents a condition that must be satisfied for an action to be performed
 * or for a goal to be achieved.
 */
public interface Condition {
    /**
     * Get the property key this condition checks
     */
    String getPropertyKey();
    
    /**
     * Check if this condition is satisfied in the given state
     */
    boolean isSatisfied(WorldState state);
}
