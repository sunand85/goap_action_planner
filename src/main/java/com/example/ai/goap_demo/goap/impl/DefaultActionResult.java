package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.ActionResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of ActionResult.
 */
public class DefaultActionResult implements ActionResult {
    private final boolean success;
    private final String errorMessage;
    private final Map<String, Object> data;
    
    public DefaultActionResult(boolean success, Map<String, Object> data) {
        this.success = success;
        this.errorMessage = null;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }
    
    public DefaultActionResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = new HashMap<>();
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }
    
    @Override
    public String toString() {
        if (success) {
            return "Success: " + data;
        } else {
            return "Failure: " + errorMessage;
        }
    }
}
