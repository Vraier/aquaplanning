package edu.kit.aquaplanning.planning.cube.datastructure;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;

public class BackwardSearchNode extends GenericSearchNode {

	public AtomSet trueAtoms;
	public AtomSet falseAtoms;

	public BackwardSearchNode(GroundPlanningProblem problem) {
		this.depth = 0;
		this.parent = null;
		this.problem = problem;
		this.lastAction = null;
		this.heuristicValue = 0;
		this.trueAtoms = new AtomSet(problem.getGoal().getAtoms(), true);
		this.falseAtoms = new AtomSet(problem.getGoal().getAtoms(), false);
		assert (!this.trueAtoms.intersects(this.falseAtoms));
	}

	/**
	 * Constructs the predecessor of a backward search node given the action that
	 * leads to this node. Since a backward search node represents a goal (a set of
	 * states) only one predecessor is enough to represent all possible states that
	 * lead to this goal.
	 * 
	 * @param action
	 *            the action that will fulfill the goal provided by the child
	 * @param predecessor
	 *            the node that should be fulfilled by the action
	 * @return a goal from which the action will fulfill the node
	 */
	private BackwardSearchNode(BackwardSearchNode predecessor, Action action) {

		assert (predecessor.canResultByApplying(action));

		this.trueAtoms = (AtomSet) predecessor.trueAtoms.clone();
		this.trueAtoms.applyTrueAtomsAsFalse(action.getEffectsPos());
		this.trueAtoms.applyTrueAtoms(action.getPreconditionsPos());

		this.falseAtoms = (AtomSet) predecessor.falseAtoms.clone();
		this.falseAtoms.applyTrueAtomsAsFalse(action.getEffectsNeg());
		this.falseAtoms.applyTrueAtoms(action.getPreconditionsNeg());

		assert (!this.falseAtoms.intersects(this.trueAtoms));

		this.depth = predecessor.depth + 1;
		this.parent = predecessor;
		this.problem = predecessor.problem;
		this.lastAction = action;
		this.heuristicValue = 0;
	}

	@Override
	public Goal getGoal() {
		List<Atom> atomList = new ArrayList<Atom>();

		for (int i = 0; i < problem.getAtomNames().size(); i++) {
			if (this.trueAtoms.get(i)) {
				atomList.add(new Atom(i, problem.getAtomNames().get(i), true));
			}
			if (this.falseAtoms.get(i)) {
				atomList.add(new Atom(i, problem.getAtomNames().get(i), false));
			}
		}
		return new Goal(atomList);
	}

	@Override
	public State getState() {
		return this.problem.getInitialState();
	}

	@Override
	public Plan getPartialPlan() {
		Plan plan = new Plan();
		GenericSearchNode node = this;
		while (node != null && node.lastAction != null) {
			plan.appendAtBack(node.lastAction);
			node = node.parent;
		}

		return plan;
	}

	@Override
	public List<GenericSearchNode> getPredecessors() {
		List<GenericSearchNode> list = new ArrayList<GenericSearchNode>();
		for (Action a : problem.getActions()) {
			if (canResultByApplying(a)) {
				list.add(new BackwardSearchNode(this, a));
			}
		}
		return list;
	}

	/**
	 * state trueAtoms = t, node trueAtoms = t', node falseAtoms = f'
	 * 
	 * @return (t' subset t and f' not intersects t)<=>(t' subset t and f' subset f)
	 */
	@Override
	public boolean satisfiesProblem() {
		return problem.getInitialState().getAtomSet().all(this.trueAtoms)
				&& problem.getInitialState().getAtomSet().none(this.falseAtoms);
	}

	@Override
	public Cube getCube() {
		return new Cube(this.problem, this.getGoal(), this.getPartialPlan());
	}

	/**
	 * Checks if applying the given action can result in this goal.
	 * 
	 * Returns true iff the effects of the given action are a subset of this goal.
	 * This means that all true atoms in the effects of the action are set in the
	 * goal and all negative atoms are not set.
	 */
	private boolean canResultByApplying(Action action) {

		AtomSet tSet = action.getEffectsPos();
		AtomSet fSet = action.getEffectsNeg();

		return (this.trueAtoms.intersects(tSet) || this.falseAtoms.intersects(fSet)) && !this.trueAtoms.intersects(fSet)
				&& !this.falseAtoms.intersects(tSet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((falseAtoms == null) ? 0 : falseAtoms.hashCode());
		result = prime * result + ((trueAtoms == null) ? 0 : trueAtoms.hashCode());
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
		BackwardSearchNode other = (BackwardSearchNode) obj;
		if (falseAtoms == null) {
			if (other.falseAtoms != null)
				return false;
		} else if (!falseAtoms.equals(other.falseAtoms))
			return false;
		if (trueAtoms == null) {
			if (other.trueAtoms != null)
				return false;
		} else if (!trueAtoms.equals(other.trueAtoms))
			return false;
		return true;
	}

	@Override
	public AtomSet getAtomSet() {
		throw new UnsupportedOperationException("Backward Cube finding does no support cut off heuristics yet.");
		// return this.trueAtoms;
	}
}
