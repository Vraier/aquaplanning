package edu.kit.aquaplanning.planning.cube.heuristic;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public abstract class GenericHeuristic {

	public abstract int value(GenericSearchNode node);

	public static GenericHeuristic getHeuristic(Configuration config) {
		switch (config.cubeFindHeuristic) {
		case relaxedPathLength:
			return new GenericRelaxedPathLength();
		case manhattanGoalDistance:
			return new GenericManhattanGoalDistance();
		case ffTrautmann:
			return new GenericTrautmann();
		case ffFroleyks:
			return new GenericFroleyks();
		case ffWilliams:
			return new GenericWilliams();
		default:
			break;
		}
		return null;
	}
}
