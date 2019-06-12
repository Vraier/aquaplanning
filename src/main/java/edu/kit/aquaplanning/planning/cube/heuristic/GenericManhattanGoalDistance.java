package edu.kit.aquaplanning.planning.cube.heuristic;

import java.util.Random;

import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public class GenericManhattanGoalDistance extends GenericHeuristic{

	private Random rnd;
	
	public GenericManhattanGoalDistance() {
		rnd = new Random();
	}
	
	@Override
	public int value(GenericSearchNode node) {
		
		Goal g = node.getGoal();
		State s = node.getState();
		int unsatisfiedGoals = 0;
		for (Atom goal : g.getAtoms()) {
			if (!s.holds(goal)) {
				unsatisfiedGoals++;
			}
		}
		return 10*(node.depth + unsatisfiedGoals)+rnd.nextInt(10);
	}
}
