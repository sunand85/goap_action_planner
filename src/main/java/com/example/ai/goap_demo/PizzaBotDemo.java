package com.example.ai.goap_demo;

import com.example.ai.goap_demo.pizzabot.PizzaBot;

/**
 * Main class to demonstrate the PizzaBot using GOAP.
 */
public class PizzaBotDemo {
    
    public static void main(String[] args) {
        System.out.println("Starting PizzaBot Demo...");
        System.out.println("This demo shows how Goal Oriented Action Planning (GOAP) works.");
        System.out.println("The PizzaBot will create a plan to serve a pizza and execute it.\n");
        
        // First run - will trigger replanning due to dough preparation failure
        System.out.println("======================================================");
        System.out.println("SCENARIO 1: WITH REPLANNING (DOUGH PREPARATION FAILURE)");
        System.out.println("======================================================\n");
        runPizzaBotDemo();
        
        // Second run - should succeed without replanning
        System.out.println("\n\n======================================================");
        System.out.println("SCENARIO 2: WITHOUT REPLANNING (SUCCESSFUL EXECUTION)");
        System.out.println("======================================================\n");
        runPizzaBotDemo();
    }
    
    private static void runPizzaBotDemo() {
        // Create the PizzaBot
        PizzaBot pizzaBot = new PizzaBot();
        
        // Create a plan
        System.out.println("=== Creating Plan ===");
        pizzaBot.createPlan();
        
        // Execute the plan
        System.out.println("\n=== Executing Plan ===");
        boolean success = pizzaBot.executePlan();
        
        // Print final status
        System.out.println("\n=== Final Status ===");
        if (success) {
            System.out.println("PizzaBot successfully served the pizza!");
        } else {
            System.out.println("PizzaBot failed to serve the pizza.");
        }
        
        System.out.println("\nFinal world state: " + pizzaBot.getWorldState());
    }
}
