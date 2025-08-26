package com.example.ai.goap_demo.goap.core;

import java.util.List;

/**
 * Represents an action that can be performed by an agent.
 * Actions have preconditions that must be met before they can be performed,
 * and effects that change the world state when they are performed.
 */
public interface Action {
    /**
     * Get the unique ID of this action
     */
    String getId();
    
    /**
     * Get the display name of this action
     */
    String getName();
    
    /**
     * Check if this action's preconditions are met in the given state
     */
    boolean checkPreconditions(WorldState state);
    
    /**
     * Apply this action's effects to the given state
     */
    WorldState applyEffects(WorldState state);
    
    /**
     * Get the cost of executing this action
     */
    float getCost();
    
    /**
     * Execute this action
     */
    ActionResult execute(WorldState state);
    
    /**
     * Get this action's preconditions
     */
    List<Condition> getPreconditions();
    
    /**
     * Get this action's effects
     */
    List<Effect> getEffects();
    
    /**
     * Check if this action is critical (should not be interrupted)
     */
    boolean isCritical();
}
