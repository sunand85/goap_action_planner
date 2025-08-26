package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Effect;
import com.example.ai.goap_demo.goap.core.WorldState;

/**
 * A simple implementation of Effect that sets a property to a value.
 */
public class SimpleEffect implements Effect {
    private final String propertyKey;
    private final Object value;
    
    public SimpleEffect(String propertyKey, Object value) {
        this.propertyKey = propertyKey;
        this.value = value;
    }
    
    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
    
    @Override
    public void apply(WorldState state) {
        state.setProperty(propertyKey, value);
    }
    
    @Override
    public String toString() {
        return propertyKey + " := " + value;
    }
}
