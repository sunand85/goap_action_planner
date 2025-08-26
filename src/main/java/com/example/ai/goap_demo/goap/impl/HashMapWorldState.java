package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of WorldState using a HashMap to store properties.
 */
public class HashMapWorldState implements WorldState {
    private final Map<String, Object> properties;
    
    public HashMapWorldState() {
        this.properties = new HashMap<>();
    }
    
    public HashMapWorldState(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }
    
    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    @Override
    public boolean satisfies(Condition condition) {
        return condition.isSatisfied(this);
    }
    
    @Override
    public WorldState copy() {
        return new HashMapWorldState(new HashMap<>(properties));
    }
    
    @Override
    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    @Override
    public String toString() {
        return properties.toString();
    }
}
