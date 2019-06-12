package edu.kit.aquaplanning.planning.cube.datastructure;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.datastructures.ActionIndex;

public class ForwardSearchNode extends GenericSearchNode {

	private State state;
	private ActionIndex aIndex;
	
	public ForwardSearchNode(GroundPlanningProblem problem) {
		this.depth = 0;
		this.parent = null;
		this.problem = problem;
		this.state = problem.getInitialState();
		this.aIndex = new ActionIndex(problem);
		this.lastAction = null;
		this.heuristicValue = 0;
	}
	
	private ForwardSearchNode(ForwardSearchNode node, Action action, State state) {
		this.depth = node.depth + 1;
		this.parent = node;
		this.problem = node.problem;
		this.state = state;
		this.aIndex = node.aIndex;
		this.lastAction = action;
		this.heuristicValue = 0;
	}
	
	@Override
	public Goal getGoal() {
		return this.problem.getGoal();
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public Plan getPartialPlan() {
		Plan plan = new Plan();
		GenericSearchNode node = this;
		while(node != null && node.lastAction != null) {
			plan.appendAtFront(node.lastAction);
			node = node.parent;
		}
		return plan;
	}

	@Override
	public List<GenericSearchNode> getPredecessors() {
		
		ArrayList<GenericSearchNode> list = new ArrayList<GenericSearchNode>();
		for (Action action : aIndex.getApplicableActions(this.state)) {
			// Create new node by applying the operator
			State newState = action.apply(this.state);
			
			// Add new node to frontier
			ForwardSearchNode newNode = new ForwardSearchNode(this, action, newState);
			list.add(newNode);
		}
		return list;
	}

	@Override
	public boolean satisfiesProblem() {
		return problem.getGoal().isSatisfied(this.state);
	}

	@Override
	public Cube getCube() {
		return new Cube(problem, state, this.getPartialPlan());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForwardSearchNode other = (ForwardSearchNode) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
}
