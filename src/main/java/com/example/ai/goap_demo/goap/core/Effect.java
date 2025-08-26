package com.example.ai.goap_demo.goap.core;

/**
 * Represents the effect of an action on the world state.
 */
public interface Effect {
    /**
     * Get the property key this effect modifies
     */
    String getPropertyKey();
    
    /**
     * Apply this effect to the given state
     */
    void apply(WorldState state);
}
