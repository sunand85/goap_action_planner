package com.example.ai.goap_demo.pizzabot;

import com.example.ai.goap_demo.goap.core.ActionResult;
import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.Effect;
import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.BaseAction;
import com.example.ai.goap_demo.goap.impl.DefaultActionResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all PizzaBot actions.
 */
public abstract class PizzaBotAction extends BaseAction {
    
    public PizzaBotAction(String id, String name, List<Condition> preconditions, 
                         List<Effect> effects, float cost, boolean critical) {
        super(id, name, preconditions, effects, cost, critical);
    }
    
    @Override
    public ActionResult execute(WorldState state) {
        // Print the action being executed
        System.out.println("Executing action: " + getName());
        
        // Perform the action
        boolean success = performAction(state);
        
        if (success) {
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("actionName", getName());
            return new DefaultActionResult(true, resultData);
        } else {
            return new DefaultActionResult(false, "Failed to execute action: " + getName());
        }
    }
    
    /**
     * Perform the actual action. Subclasses should override this method.
     * 
     * @param state The current world state
     * @return true if the action was successful, false otherwise
     */
    protected abstract boolean performAction(WorldState state);
}
