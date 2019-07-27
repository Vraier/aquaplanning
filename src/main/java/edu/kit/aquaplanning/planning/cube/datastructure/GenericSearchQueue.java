package edu.kit.aquaplanning.planning.cube.datastructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy.Mode;

public class GenericSearchQueue {

	private SearchStrategy strategy;
	private GenericHeuristic heuristic;

	// Different data structures used depending on the employed strategy
	private Queue<GenericSearchNode> queue;
	private Stack<GenericSearchNode> stack;
	private List<GenericSearchNode> list;
	private Random random;
	private Set<GenericSearchNode> visitedStates;

	public GenericSearchQueue(SearchStrategy strategy, GenericHeuristic heuristic) {
		this.strategy = strategy;
		this.heuristic = heuristic;
		initFrontier();
	}

	private void initFrontier() {

		switch (strategy.getMode()) {
		case breadthFirst:
			queue = new ArrayDeque<>();
			break;
		case depthFirst:
			stack = new Stack<>();
			break;
		case bestFirst:
			queue = new PriorityQueue<GenericSearchNode>((n1, n2) ->
			// Compare heuristic scores
			n1.heuristicValue - n2.heuristicValue);
			break;
		case aStar:
			queue = new PriorityQueue<GenericSearchNode>((n1, n2) ->
			// Compare (cost so far + heuristic scores)
			n1.depth + n1.heuristicValue - (n2.depth + n2.heuristicValue));
			break;
		case weightedAStar:
			int heuristicWeight = strategy.getHeuristicWeight();
			queue = new PriorityQueue<GenericSearchNode>((n1, n2) ->
			// Compare (cost so far + heuristic scores)
			n1.depth + heuristicWeight * n1.heuristicValue - (n2.depth + heuristicWeight * n2.heuristicValue));
			break;
		case randomChoice:
			list = new ArrayList<>();
			random = new Random(strategy.getSeed());
			break;
		}
		visitedStates = new HashSet<>();
	}

	public boolean isEmpty() {
		if (strategy.getMode() == Mode.depthFirst) {
			return stack.isEmpty();
		} else if (strategy.getMode() == Mode.randomChoice) {
			return list.isEmpty();
		} else {
			return queue.isEmpty();
		}
	}

	public int size() {
		int size = 0;
		if (strategy.getMode() == Mode.depthFirst) {
			size = stack.size();
		} else if (strategy.getMode() == Mode.randomChoice) {
			size = list.size();
		} else {
			size = queue.size();
		}
		return size;
	}
	
	public int visitedSize() {
		return visitedStates.size();
	}

	public GenericSearchNode get() {

		GenericSearchNode node;
		if (strategy.getMode() == Mode.depthFirst) {
			node = stack.pop();
		} else if (strategy.getMode() == Mode.randomChoice) {
			int r = random.nextInt(list.size());
			node = list.remove(r);
		} else {
			node = queue.poll();
		}

		// Or queue is empty now
		if (node == null) {
			return null;
		}

		// TODO: If the node was already visited we pull a new one?
		// if(visitedStates.contains(node)) {
		// return this.get();
		// }

		// Else we add it to the visited nodes
		visitedStates.add(node);

		return node;
	}

	public void add(GenericSearchNode child) {
		assert (child != null);

		if (visitedStates.contains(child)) {
			return;
		}

		if (strategy.isHeuristical()) {
			// Compute heuristic value for the node
			child.heuristicValue = heuristic.value(child);
			if (child.heuristicValue < Integer.MAX_VALUE) {
				// Only add node if heuristic does not return infinity
				queue.add(child);
			}
		} else if (strategy.getMode() == Mode.breadthFirst) {
			queue.add(child);
		} else if (strategy.getMode() == Mode.depthFirst) {
			stack.push(child);
		} else if (strategy.getMode() == Mode.randomChoice) {
			list.add(child);
		}
	}

	public List<Cube> getOpenCubes() {

		List<Cube> cubes = new ArrayList<Cube>();
		while (!this.isEmpty()) {
			cubes.add(this.get().getCube());
		}
		return cubes;
	}

	public List<Cube> getClosedCubes() {

		List<Cube> cubes = new ArrayList<Cube>();
		for (GenericSearchNode n : visitedStates) {
			cubes.add(n.getCube());
		}
		return cubes;
	}
}
