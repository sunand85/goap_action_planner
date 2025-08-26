package com.example.ai.goap_demo.goap.core;

import java.util.Map;

/**
 * Represents the result of executing an action.
 */
public interface ActionResult {
    /**
     * Check if the action execution was successful
     */
    boolean isSuccess();
    
    /**
     * Get the error message if the action failed
     */
    String getErrorMessage();
    
    /**
     * Get the result data
     */
    Map<String, Object> getData();
}
