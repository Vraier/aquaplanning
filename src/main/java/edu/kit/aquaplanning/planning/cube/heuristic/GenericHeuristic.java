package edu.kit.aquaplanning.planning.cube.heuristic;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public class GenericHeuristic {

	public int value(GenericSearchNode child) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static GenericHeuristic getHeuristic(Configuration config) {
		switch (config.cubeFindHeuristic) {
		case relaxedPathLength:
			return new GenericRelaxedPathLength();
		case manhattanGoalDistance:
			return new GenericManhattanGoalDistance();
		/*
		case actionInterferenceRelaxation:
			return new SatAbstractionHeuristic(p, config);
		case ffTrautmann:
			return new TrautmannsHeuristic(p);
		case ffFroleyks:
			return new FroleyksHeuristic(p);
		case ffWilliams:
			return new WilliamsHeuristic(p); */
		default:
			break;
		}
		return null;
	}
}
