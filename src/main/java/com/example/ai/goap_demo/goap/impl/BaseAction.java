package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.Effect;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.List;

/**
 * Base implementation of Action that handles common functionality.
 */
public abstract class BaseAction implements Action {
    private final String id;
    private final String name;
    private final List<Condition> preconditions;
    private final List<Effect> effects;
    private final float cost;
    private final boolean critical;
    
    public BaseAction(String id, String name, List<Condition> preconditions, 
                     List<Effect> effects, float cost, boolean critical) {
        this.id = id;
        this.name = name;
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.critical = critical;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean checkPreconditions(WorldState state) {
        for (Condition condition : preconditions) {
            if (!state.satisfies(condition)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public WorldState applyEffects(WorldState state) {
        WorldState newState = state.copy();
        for (Effect effect : effects) {
            effect.apply(newState);
        }
        return newState;
    }
    
    @Override
    public float getCost() {
        return cost;
    }
    
    @Override
    public List<Condition> getPreconditions() {
        return preconditions;
    }
    
    @Override
    public List<Effect> getEffects() {
        return effects;
    }
    
    @Override
    public boolean isCritical() {
        return critical;
    }
    
    @Override
    public String toString() {
        return name + " [" + id + "]";
    }
}
