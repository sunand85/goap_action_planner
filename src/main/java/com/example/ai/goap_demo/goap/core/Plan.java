package com.example.ai.goap_demo.goap.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a plan consisting of a sequence of actions.
 */
public class Plan {
    private final String id;
    private final List<Action> actions;
    
    public Plan(List<Action> actions) {
        this.id = UUID.randomUUID().toString();
        this.actions = new ArrayList<>(actions);
    }
    
    public String getId() {
        return id;
    }
    
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
    
    public boolean isEmpty() {
        return actions.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Plan ").append(id).append(":\n");
        for (int i = 0; i < actions.size(); i++) {
            sb.append(i + 1).append(". ").append(actions.get(i).getName()).append("\n");
        }
        return sb.toString();
    }
}
