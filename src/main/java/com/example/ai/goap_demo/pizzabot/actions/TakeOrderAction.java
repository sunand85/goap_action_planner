package com.example.ai.goap_demo.pizzabot.actions;

import com.example.ai.goap_demo.goap.core.Condition;
import com.example.ai.goap_demo.goap.core.Effect;
import com.example.ai.goap_demo.goap.core.WorldState;
import com.example.ai.goap_demo.goap.impl.SimpleCondition;
import com.example.ai.goap_demo.goap.impl.SimpleEffect;
import com.example.ai.goap_demo.pizzabot.PizzaBotAction;

import java.util.List;
import java.util.Scanner;

/**
 * Action for taking a pizza order from a customer.
 */
public class TakeOrderAction extends PizzaBotAction {
    
    public TakeOrderAction() {
        super(
            "take_order",
            "Take Order",
            List.of(
                new SimpleCondition("customerPresent", true)
            ),
            List.of(
                new SimpleEffect("orderTaken", true),
                new SimpleEffect("pizzaType", "Margherita") // Set directly to Margherita for demo
            ),
            1.0f,
            false
        );
    }
    
    @Override
    protected boolean performAction(WorldState state) {
        System.out.println("PizzaBot: Hello! Welcome to PizzaBot. What kind of pizza would you like?");
        System.out.println("Available options: Margherita, Pepperoni, Vegetarian");
        
        // For demo purposes, automatically select a pizza type instead of waiting for input
        // This avoids issues when running as a JAR and prevents potential input blocking
        String pizzaType = "Margherita";
        
        System.out.println("Customer: I'd like a " + pizzaType + " pizza, please.");
        System.out.println("PizzaBot: Great! I'll prepare a " + pizzaType + " pizza for you.");
        
        return true;
    }
    
    @Override
    public WorldState applyEffects(WorldState state) {
        // Override the default applyEffects to ensure our pizzaType is set correctly
        WorldState newState = super.applyEffects(state);
        return newState;
    }
}
