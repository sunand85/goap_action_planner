package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.NotCondition;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;

/**
 * Action for preparing the pizza dough.
 */
public class PrepareDoughAction extends PizzaBotAction {
    
    public PrepareDoughAction() {
        super(
            "prepare_dough",
            "Prepare Dough",
            List.of(
                new SimpleCondition("ingredientsChecked", true),
                new SimpleCondition("ingredientsAvailable", true),
                // This condition ensures this action won't be considered after a failure
                new NotCondition("doughPreparationFailed", true)
            ),
            List.of(
                new SimpleEffect("doughPrepared", true)
            ),
            2.0f,
            false
        );
    }
    
    // Add a counter to simulate occasional failures
    private static int executionCount = 0;
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        System.out.println("PizzaBot: Preparing dough for " + pizzaType + " pizza...");
        System.out.println("PizzaBot: 1. Mixing flour, water, yeast, and salt...");
        System.out.println("PizzaBot: 2. Kneading the dough...");
        
        // Simulate a failure on the first execution to trigger replanning
        executionCount++;
        if (executionCount == 1) {
            System.out.println("PizzaBot: Oh no! The dough is too sticky. Need to find an alternative.");
            // Set a flag in the world state to indicate that dough preparation failed
            // This will allow the planner to consider alternative actions
            state.setProperty("doughPreparationFailed", true);
            return false;
        }
        
        System.out.println("PizzaBot: 3. Letting the dough rise...");
        System.out.println("PizzaBot: 4. Rolling out the dough into a circle...");
        System.out.println("PizzaBot: Dough preparation complete!");
        
        return true;
    }
}
