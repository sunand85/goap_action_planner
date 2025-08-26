package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.ActionResult;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.WorldState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AStarPathfinderTest {

    @Test
    void testFindPath() {
        // Create a simple world state
        WorldState initialState = new HashMapWorldState();
        initialState.setProperty("hasIngredients", true);
        initialState.setProperty("hasDough", false);
        initialState.setProperty("hasToppings", false);
        initialState.setProperty("isPizzaBaked", false);
        
        // Create actions
        List<Action> actions = new ArrayList<>();
        
        // Action 1: Prepare dough
        actions.add(new BaseAction(
            "prepare_dough",
            "Prepare Dough",
            List.of(new SimpleCondition("hasIngredients", true)),
            List.of(new SimpleEffect("hasDough", true)),
            1.0f,
            false
        ) {
            @Override
            public ActionResult execute(WorldState state) {
                return new DefaultActionResult(true, new HashMap<>());
            }
        });
        
        // Action 2: Add toppings
        actions.add(new BaseAction(
            "add_toppings",
            "Add Toppings",
            List.of(new SimpleCondition("hasDough", true)),
            List.of(new SimpleEffect("hasToppings", true)),
            1.0f,
            false
        ) {
            @Override
            public ActionResult execute(WorldState state) {
                return new DefaultActionResult(true, new HashMap<>());
            }
        });
        
        // Action 3: Bake pizza
        actions.add(new BaseAction(
            "bake_pizza",
            "Bake Pizza",
            List.of(
                new SimpleCondition("hasDough", true),
                new SimpleCondition("hasToppings", true)
            ),
            List.of(new SimpleEffect("isPizzaBaked", true)),
            1.0f,
            false
        ) {
            @Override
            public ActionResult execute(WorldState state) {
                return new DefaultActionResult(true, new HashMap<>());
            }
        });
        
        // Create goal
        Map<String, Object> goalState = new HashMap<>();
        goalState.put("isPizzaBaked", true);
        Goal goal = new SimpleGoal(goalState);
        
        // Create pathfinder
        AStarPathfinder pathfinder = new AStarPathfinder(new SimpleHeuristic());
        
        // Find path
        List<Action> path = pathfinder.findPath(initialState, goal, actions);
        
        // Verify path
        assertNotNull(path, "Path should not be null");
        assertEquals(3, path.size(), "Path should contain 3 actions");
        assertEquals("Prepare Dough", path.get(0).getName());
        assertEquals("Add Toppings", path.get(1).getName());
        assertEquals("Bake Pizza", path.get(2).getName());
    }
}
