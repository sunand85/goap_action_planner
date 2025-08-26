package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;
import java.util.Map;

/**
 * Action for baking the pizza.
 */
public class BakePizzaAction extends PizzaBotAction {
    
    // Map of pizza types to their baking times (in minutes)
    private static final Map<String, Integer> BAKING_TIMES = Map.of(
        "Margherita", 8,
        "Pepperoni", 10,
        "Vegetarian", 9
    );
    
    public BakePizzaAction() {
        super(
            "bake_pizza",
            "Bake Pizza",
            List.of(
                new SimpleCondition("toppingsAdded", true)
            ),
            List.of(
                new SimpleEffect("pizzaBaked", true)
            ),
            3.0f,
            true // Critical action - shouldn't be interrupted
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        Integer bakingTime = BAKING_TIMES.get(pizzaType);
        if (bakingTime == null) {
            System.out.println("PizzaBot: Error - Unknown pizza type: " + pizzaType);
            return false;
        }
        
        System.out.println("PizzaBot: Preheating oven to 475°F (245°C)...");
        System.out.println("PizzaBot: Placing " + pizzaType + " pizza in the oven...");
        System.out.println("PizzaBot: Baking for " + bakingTime + " minutes...");
        
        // Simulate baking time (just for demonstration)
        try {
            System.out.println("PizzaBot: Baking in progress...");
            Thread.sleep(1000); // Sleep for 1 second to simulate baking
        } catch (InterruptedException e) {
            System.out.println("PizzaBot: Baking interrupted!");
            return false;
        }
        
        System.out.println("PizzaBot: Pizza is done baking!");
        System.out.println("PizzaBot: Removing pizza from oven...");
        
        return true;
    }
}
