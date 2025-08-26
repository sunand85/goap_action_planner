package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;
import java.util.Map;

/**
 * Action for adding toppings to the pizza.
 */
public class AddToppingsAction extends PizzaBotAction {
    
    // Map of pizza types to their toppings
    private static final Map<String, List<String>> PIZZA_TOPPINGS = Map.of(
        "Margherita", List.of("tomato sauce", "mozzarella", "basil"),
        "Pepperoni", List.of("tomato sauce", "mozzarella", "pepperoni"),
        "Vegetarian", List.of("tomato sauce", "mozzarella", "bell peppers", "mushrooms", "onions")
    );
    
    public AddToppingsAction() {
        super(
            "add_toppings",
            "Add Toppings",
            List.of(
                new SimpleCondition("doughPrepared", true)
            ),
            List.of(
                new SimpleEffect("toppingsAdded", true)
            ),
            2.0f,
            false
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        System.out.println("PizzaBot: Adding toppings for " + pizzaType + " pizza...");
        
        List<String> toppings = PIZZA_TOPPINGS.get(pizzaType);
        if (toppings == null) {
            System.out.println("PizzaBot: Error - Unknown pizza type: " + pizzaType);
            return false;
        }
        
        System.out.println("PizzaBot: 1. Spreading tomato sauce...");
        
        for (int i = 1; i < toppings.size(); i++) {
            System.out.println("PizzaBot: " + (i + 1) + ". Adding " + toppings.get(i) + "...");
        }
        
        System.out.println("PizzaBot: Toppings added successfully!");
        
        return true;
    }
}
