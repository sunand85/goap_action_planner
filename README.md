# GOAP Demo - Goal Oriented Action Planning

This project demonstrates the implementation of Goal Oriented Action Planning (GOAP) in Java. GOAP is an AI technique commonly used in game development and agent-based systems to create intelligent, goal-driven behaviors.

## Overview

GOAP is a planning system that allows agents to determine a sequence of actions to achieve a goal. It's particularly useful for creating non-player characters (NPCs) in games that can adapt to changing environments and make decisions based on the current state of the world.

The key components of GOAP are:

1. **Actions**: Things an agent can do, with preconditions and effects
2. **World State**: The current state of the world, represented as a set of key-value pairs
3. **Goals**: Desired states that the agent wants to achieve
4. **Planner**: The system that finds a sequence of actions to transform the current state into the goal state

## Project Structure

The project is organized into the following packages:

- `com.example.ai.goap_demo.goap.core`: Core interfaces for the GOAP system
- `com.example.ai.goap_demo.goap.impl`: Implementations of the core interfaces
- `com.example.ai.goap_demo.pizzabot`: A demo application that uses GOAP to simulate a pizza-making robot

## Key Classes

### Core Interfaces

- `WorldState`: Represents the state of the world as a collection of properties
- `Condition`: A condition that must be satisfied for an action to be performed
- `Effect`: A change to the world state that occurs when an action is performed
- `Action`: An action that can be performed by an agent
- `Goal`: A desired state that an agent wants to achieve
- `Planner`: Plans a sequence of actions to achieve a goal

### Implementations

- `HashMapWorldState`: Implementation of WorldState using a HashMap
- `SimpleCondition`: Simple implementation of Condition that checks if a property equals an expected value
- `SimpleEffect`: Simple implementation of Effect that sets a property to a value
- `BaseAction`: Base implementation of Action that handles common functionality
- `SimpleGoal`: Simple implementation of Goal that is satisfied when all conditions are met
- `AStarPathfinder`: Implementation of the A* algorithm for finding the optimal sequence of actions
- `GOAPPlanner`: Implementation of the GOAP planner using A* pathfinding

## PizzaBot Example

The project includes a PizzaBot example that demonstrates how GOAP can be used to create a pizza-making robot. The robot can:

1. Take an order from a customer
2. Check if all required ingredients are available
3. Prepare the dough
4. Add toppings
5. Bake the pizza
6. Serve the pizza to the customer

The PizzaBot uses GOAP to determine the sequence of actions needed to serve a pizza, and can adapt if conditions change (e.g., if ingredients are missing).

## Building and Running

To build the project:

```bash
mvn clean package
```

To run the PizzaBot demo:

```bash
java -jar target/goap-demo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Applications of GOAP

GOAP can be used in various applications, including:

- Game AI for NPCs
- Robotics and autonomous systems
- Workflow orchestration
- Agent-based simulations
- Task planning for virtual assistants

## References

- [Goal Oriented Action Planning for a Smarter AI](https://gamedevelopment.tutsplus.com/tutorials/goal-oriented-action-planning-for-a-smarter-ai--cms-20793)
- [Artificial Intelligence for Games](https://www.amazon.com/Artificial-Intelligence-Games-Ian-Millington/dp/0123747317) by Ian Millington and John Funge
