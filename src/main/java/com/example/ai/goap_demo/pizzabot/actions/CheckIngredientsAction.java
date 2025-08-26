package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;
import java.util.Map;

/**
 * Action for checking if all required ingredients are available.
 */
public class CheckIngredientsAction extends PizzaBotAction {
    
    // Map of pizza types to their required ingredients
    private static final Map<String, List<String>> PIZZA_INGREDIENTS = Map.of(
        "Margherita", List.of("dough", "tomato sauce", "mozzarella", "basil"),
        "Pepperoni", List.of("dough", "tomato sauce", "mozzarella", "pepperoni"),
        "Vegetarian", List.of("dough", "tomato sauce", "mozzarella", "bell peppers", "mushrooms", "onions")
    );
    
    // Map of available ingredients (simulated inventory)
    private static final Map<String, Boolean> AVAILABLE_INGREDIENTS = Map.of(
        "dough", true,
        "tomato sauce", true,
        "mozzarella", true,
        "basil", true,
        "pepperoni", true,
        "bell peppers", true,
        "mushrooms", true,
        "onions", true
    );
    
    public CheckIngredientsAction() {
        super(
            "check_ingredients",
            "Check Ingredients",
            List.of(
                new SimpleCondition("orderTaken", true)
            ),
            List.of(
                new SimpleEffect("ingredientsChecked", true),
                new SimpleEffect("ingredientsAvailable", true) // Will be updated during execution
            ),
            1.0f,
            false
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        String pizzaType = (String) state.getProperty("pizzaType");
        
        if (pizzaType == null) {
            System.out.println("PizzaBot: Error - Pizza type not specified.");
            return false;
        }
        
        List<String> requiredIngredients = PIZZA_INGREDIENTS.get(pizzaType);
        
        if (requiredIngredients == null) {
            System.out.println("PizzaBot: Error - Unknown pizza type: " + pizzaType);
            return false;
        }
        
        System.out.println("PizzaBot: Checking ingredients for " + pizzaType + " pizza...");
        
        // Check if all required ingredients are available
        boolean allIngredientsAvailable = true;
        for (String ingredient : requiredIngredients) {
            Boolean available = AVAILABLE_INGREDIENTS.get(ingredient);
            if (available == null || !available) {
                System.out.println("PizzaBot: Missing ingredient: " + ingredient);
                allIngredientsAvailable = false;
            }
        }
        
        if (allIngredientsAvailable) {
            System.out.println("PizzaBot: All ingredients are available!");
            state.setProperty("ingredientsAvailable", true);
        } else {
            System.out.println("PizzaBot: Some ingredients are missing. Cannot make the pizza.");
            state.setProperty("ingredientsAvailable", false);
        }
        
        return true; // The action itself succeeded, even if ingredients are missing
    }
}
