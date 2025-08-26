package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.WorldState;

/**
 * A simple implementation of Condition that checks if a property equals an expected value.
 */
public class SimpleCondition implements Condition {
    private final String propertyKey;
    private final Object expectedValue;
    
    public SimpleCondition(String propertyKey, Object expectedValue) {
        this.propertyKey = propertyKey;
        this.expectedValue = expectedValue;
    }
    
    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
    
    @Override
    public boolean isSatisfied(WorldState state) {
        Object actualValue = state.getProperty(propertyKey);
        
        if (expectedValue == null) {
            return actualValue == null;
        }
        
        return expectedValue.equals(actualValue);
    }
    
    @Override
    public String toString() {
        return propertyKey + " = " + expectedValue;
    }
}
