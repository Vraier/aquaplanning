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
import edu.kit.aquaplanning.planning.cube.cutoffHeuristic.CutOffHeuristic;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy.Mode;

public class GenericSearchQueue {

	private SearchStrategy strategy;
	private GenericHeuristic heuristic;
	private CutOffHeuristic cutOffHeuristic;

	private List<GenericSearchNode> anchors;
	private List<GenericSearchNode> cutOffs;

	// Different data structures used depending on the employed strategy
	private Queue<GenericSearchNode> queue;
	private Stack<GenericSearchNode> stack;
	private List<GenericSearchNode> list;
	private Random random;
	private Set<GenericSearchNode> visitedStates;

	public GenericSearchQueue(SearchStrategy strategy, GenericHeuristic heuristic, CutOffHeuristic cutOffHeuristic) {
		this.strategy = strategy;
		this.heuristic = heuristic;
		this.cutOffHeuristic = cutOffHeuristic;
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
		anchors = new ArrayList<GenericSearchNode>();
		cutOffs = new ArrayList<GenericSearchNode>();
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
		return size + cutOffs.size();
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

		//TODO what if we call ourself recursively many times?
		assert(node != null);
		
		visitedStates.add(node);
		if (cutOffHeuristic != null && cutOffHeuristic.isAnchor(node)) {
			anchors.add(node);
		}
		if (cutOffHeuristic != null && cutOffHeuristic.cutOff(anchors, node)) {
			cutOffs.add(node);
			return this.get();
		}

		return node;
	}

	public void add(GenericSearchNode child) {
		assert(child != null);
		
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

	public List<Cube> getCubes() {
		
		List<Cube> cubes = new ArrayList<Cube>();
		for(GenericSearchNode node: cutOffs) {
			cubes.add(node.getCube());
		}
		while(!this.isEmpty()) {
			cubes.add(this.get().getCube());
		}
		return cubes;
	}
}
