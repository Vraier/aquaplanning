package edu.kit.aquaplanning.planning.cube.datastructure;

import java.util.List;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;

public abstract class GenericSearchNode {

	public int depth;
	public GenericSearchNode parent;
	public GroundPlanningProblem problem;
	public Action lastAction;
	public int heuristicValue;

	public abstract Plan getPartialPlan();

	public abstract List<GenericSearchNode> getPredecessors();

	public abstract boolean satisfiesProblem();

	public abstract Cube getCube();

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

	public abstract Goal getGoal();

	public abstract State getState();
	
}
