package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.WorldState;

/**
 * A condition that is satisfied when the property does not equal the expected value.
 */
public class NotCondition implements Condition {
    private final String propertyKey;
    private final Object unexpectedValue;
    
    public NotCondition(String propertyKey, Object unexpectedValue) {
        this.propertyKey = propertyKey;
        this.unexpectedValue = unexpectedValue;
    }
    
    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
    
    @Override
    public boolean isSatisfied(WorldState state) {
        Object actualValue = state.getProperty(propertyKey);
        
        // If the property doesn't exist, it's satisfied (not equal to the unexpected value)
        if (actualValue == null) {
            return unexpectedValue != null;
        }
        
        // Otherwise, it's satisfied if the actual value is not equal to the unexpected value
        return !actualValue.equals(unexpectedValue);
    }
    
    @Override
    public String toString() {
        return propertyKey + " != " + unexpectedValue;
    }
}
