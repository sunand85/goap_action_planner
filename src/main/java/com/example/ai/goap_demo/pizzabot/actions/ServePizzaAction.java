package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;

/**
 * Action for serving the pizza to the customer.
 */
public class ServePizzaAction extends PizzaBotAction {
    
    public ServePizzaAction() {
        super(
            "serve_pizza",
            "Serve Pizza",
            List.of(
                new SimpleCondition("pizzaBaked", true)
            ),
            List.of(
                new SimpleEffect("pizzaServed", true)
            ),
            1.0f,
            false
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        System.out.println("PizzaBot: Slicing the " + pizzaType + " pizza...");
        System.out.println("PizzaBot: Placing pizza on a serving tray...");
        System.out.println("PizzaBot: Here's your " + pizzaType + " pizza! Enjoy your meal!");
        
        return true;
    }
}
