package com.example.ai.goap_demo.goap.impl;

import com.example.ai.goap_demo.goap.core.Action;
import com.example.ai.goap_demo.goap.core.Goal;
import com.example.ai.goap_demo.goap.core.HeuristicFunction;
import com.example.ai.goap_demo.goap.core.WorldState;

import java.util.*;

/**
 * Implementation of the A* algorithm for finding the optimal sequence of actions.
 */
public class AStarPathfinder {
    private final HeuristicFunction heuristicFunction;
    
    public AStarPathfinder(HeuristicFunction heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }
    
    /**
     * Finds the optimal path from start state to goal
     */
    public List<Action> findPath(WorldState startState, Goal goal, List<Action> availableActions) {
        // Open set contains nodes to be evaluated
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparing(node -> node.f));
        
        // Closed set contains already evaluated nodes
        Set<String> closedSet = new HashSet<>();
        
        // Map to track nodes by state hash for quick lookup
        Map<String, Node> openNodesByHash = new HashMap<>();
        
        // Start node
        Node startNode = new Node(startState, null, null, 0, 
                                 heuristicFunction.calculate(startState, goal, availableActions));
        openSet.add(startNode);
        openNodesByHash.put(getStateHash(startState), startNode);
        
        // Add a safety counter to prevent infinite loops
        int iterations = 0;
        final int MAX_ITERATIONS = 10000;
        
        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            
            // Get node with lowest f value
            Node current = openSet.poll();
            String currentStateHash = getStateHash(current.state);
            openNodesByHash.remove(currentStateHash);
            
            // Check if goal is reached
            if (goal.isSatisfied(current.state)) {
                System.out.println("Goal reached after " + iterations + " iterations");
                return reconstructPath(current);
            }
            
            // Add to closed set
            closedSet.add(currentStateHash);
            
            // Explore neighbors (states reachable by applying actions)
            for (Action action : availableActions) {
                // Check if action is applicable in current state
                if (!action.checkPreconditions(current.state)) {
                    continue;
                }
                
                // Apply action to get new state
                WorldState newState = action.applyEffects(current.state);
                String newStateHash = getStateHash(newState);
                
                // Skip if already evaluated
                if (closedSet.contains(newStateHash)) {
                    continue;
                }
                
                // Calculate costs
                float g = current.g + action.getCost();
                float h = heuristicFunction.calculate(newState, goal, availableActions);
                float f = g + h;
                
                // Check if already in open set
                Node existingNode = openNodesByHash.get(newStateHash);
                
                if (existingNode != null) {
                    // If we found a better path, update the existing node
                    if (g < existingNode.g) {
                        // Need to remove and re-add to update position in priority queue
                        openSet.remove(existingNode);
                        existingNode.parent = current;
                        existingNode.action = action;
                        existingNode.g = g;
                        existingNode.f = f;
                        openSet.add(existingNode);
                    }
                } else {
                    // Create new node and add to open set
                    Node neighbor = new Node(newState, current, action, g, f);
                    openSet.add(neighbor);
                    openNodesByHash.put(newStateHash, neighbor);
                }
            }
        }
        
        if (iterations >= MAX_ITERATIONS) {
            System.out.println("A* search exceeded maximum iterations (" + MAX_ITERATIONS + ")");
        }
        
        return null; // No path found
    }
    
    /**
     * Reconstructs the path from goal node to start node
     */
    private List<Action> reconstructPath(Node goalNode) {
        List<Action> path = new ArrayList<>();
        Node current = goalNode;
        
        while (current.parent != null) {
            path.add(0, current.action); // Add to front of list
            current = current.parent;
        }
        
        return path;
    }
    
    /**
     * Creates a hash representation of a world state
     */
    private String getStateHash(WorldState state) {
        // Create a more reliable hash based on the actual properties
        Map<String, Object> props = state.getAllProperties();
        StringBuilder sb = new StringBuilder();
        
        // Sort keys for consistent ordering
        List<String> keys = new ArrayList<>(props.keySet());
        Collections.sort(keys);
        
        for (String key : keys) {
            Object value = props.get(key);
            sb.append(key).append("=").append(value).append(";");
        }
        
        return sb.toString();
    }
    
    /**
     * Node class for A* search
     */
    private static class Node {
        WorldState state;
        Node parent;
        Action action;
        float g; // Cost from start to this node
        float f; // g + h (estimated total cost)
        
        Node(WorldState state, Node parent, Action action, float g, float f) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.g = g;
            this.f = f;
        }
    }
}
