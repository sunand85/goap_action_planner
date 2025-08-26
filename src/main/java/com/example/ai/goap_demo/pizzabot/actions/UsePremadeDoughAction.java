package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;

/**
 * Action for using premade dough when fresh dough preparation fails.
 * This is an alternative action that achieves the same effect as PrepareDoughAction.
 */
public class UsePremadeDoughAction extends PizzaBotAction {
    
    public UsePremadeDoughAction() {
        super(
            "use_premade_dough",
            "Use Premade Dough",
            List.of(
                new SimpleCondition("ingredientsChecked", true),
                new SimpleCondition("ingredientsAvailable", true),
                // This condition ensures this action is only considered after a dough preparation failure
                new SimpleCondition("doughPreparationFailed", true)
            ),
            List.of(
                new SimpleEffect("doughPrepared", true)
            ),
            3.0f, // Higher cost than regular dough preparation to make it less preferable
            false
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        System.out.println("PizzaBot: Fresh dough preparation failed. Using premade dough for " + pizzaType + " pizza...");
        System.out.println("PizzaBot: 1. Retrieving premade dough from refrigerator...");
        System.out.println("PizzaBot: 2. Letting the dough come to room temperature...");
        System.out.println("PizzaBot: 3. Rolling out the premade dough into a circle...");
        System.out.println("PizzaBot: Premade dough ready for toppings!");
        
        return true;
    }
}
