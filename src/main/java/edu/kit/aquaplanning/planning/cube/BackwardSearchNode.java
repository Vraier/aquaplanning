package edu.kit.aquaplanning.planning.cube;

import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.Plan;

public class BackwardSearchNode {

	public AtomSet trueAtoms;
	public AtomSet falseAtoms;
	public BackwardSearchNode parent;
	public Action lastAction;

	public BackwardSearchNode(Goal goal) {
		trueAtoms = new AtomSet(goal.getAtoms(), true);
		falseAtoms = new AtomSet(goal.getAtoms(), false);
		parent = null;
		lastAction = null;
	}

	public BackwardSearchNode(AtomSet trueAtoms, AtomSet falseAtoms, BackwardSearchNode parent, Action lastAction) {
		this.trueAtoms = trueAtoms;
		this.falseAtoms = falseAtoms;
		this.parent = parent;
		this.lastAction = lastAction;
	}

	public Plan getPartialPlan() {
		
		Plan plan = new Plan();
		BackwardSearchNode node = this;
		while(node != null && node.lastAction != null) {
			plan.appendAtBack(node.lastAction);
			node = node.parent;
		}
		
		return plan;
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
}
