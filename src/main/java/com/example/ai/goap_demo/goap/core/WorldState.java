package com.example.ai.goap_demo.goap.core;

import java.util.Map;

/**
 * Represents the state of the world in the GOAP system.
 * Contains properties that describe the current state.
 */
public interface WorldState {
    /**
     * Get a property from the world state
     */
    Object getProperty(String key);
    
    /**
     * Set a property in the world state
     */
    void setProperty(String key, Object value);
    
    /**
     * Check if a condition is satisfied in this state
     */
    boolean satisfies(Condition condition);
    
    /**
     * Create a deep copy of this state
     */
    WorldState copy();
    
    /**
     * Get all properties in this state
     */
    Map<String, Object> getAllProperties();
}
