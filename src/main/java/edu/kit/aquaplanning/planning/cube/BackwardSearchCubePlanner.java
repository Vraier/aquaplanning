package edu.kit.aquaplanning.planning.cube;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;

public class BackwardSearchCubePlanner implements CubeFinder {

	Queue<SearchNode> queue;
	Set<SearchNode> visitedGoals;
	
	public BackwardSearchCubePlanner() {
		queue = new ArrayDeque<SearchNode>();
		visitedGoals = new HashSet<SearchNode>();
	}
	
	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		Goal goal = problem.getGoal();
		queue.add(new SearchNode(goal));
		List<Action> actions = problem.getActions();

		while (!queue.isEmpty() && queue.size() <= numCubes) {

			SearchNode node = queue.poll();

			for (Action a : actions) {
				
				// We found a new possible goal. Add it to the queue
				if (canApplyTo(a, node)) {
					SearchNode newNode = getPredecessor(a, node);
					newNode.predecessor = node;
					newNode.action = a;
					add(newNode);
				}
			}
		}
		
		if(queue.size() < numCubes) {
			System.out.println("Something went terribly worng. Backwardsearch didnt find enough goals!");
			return null;
		} else {
			List<Cube> cubes = new ArrayList<Cube>();
			for(SearchNode n: queue) {
				//TODO: generate the cubes and return them
			}
			return null;
		}
	}

	@Override
	public Plan getPlan() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks if applying the given action to a state can result in the given goal.
	 * 
	 * 
	 * Returns true iff the effects of the given action are a subset of the given
	 * goal. This means that all true atoms in the effects of the action are set in
	 * the goal and all negative atoms are not set.
	 */
	private boolean canApplyTo(Action a, SearchNode n) {

		AtomSet pSet = a.getEffectsPos();
		AtomSet nSet = a.getEffectsNeg();

		return n.trueAtoms.all(pSet) && n.falseAtoms.all(nSet);
	}

	private SearchNode getPredecessor(Action a, SearchNode n) {

		assert (canApplyTo(a, n));

		AtomSet tAtoms = (AtomSet) n.trueAtoms.clone();
		tAtoms.applyTrueAtomsAsFalse(a.getEffectsPos());
		tAtoms.applyTrueAtoms(a.getPreconditionsPos());

		AtomSet fAtoms = (AtomSet) n.falseAtoms.clone();
		fAtoms.applyTrueAtomsAsFalse(a.getEffectsNeg());
		fAtoms.applyTrueAtoms(a.getPreconditionsNeg());

		return new SearchNode(tAtoms, fAtoms);
	}
	
	private void add(SearchNode node) {
		
		if(!visitedGoals.contains(node)) {
			queue.add(node);
			visitedGoals.add(node);
		}
	}

	private class SearchNode {

		private AtomSet trueAtoms;
		private AtomSet falseAtoms;
		private SearchNode predecessor;
		private Action action;

		public SearchNode(Goal goal) {
			trueAtoms = new AtomSet(goal.getAtoms(), true);
			falseAtoms = new AtomSet(goal.getAtoms(), false);
			predecessor = null;
			action = null;
		}

		public SearchNode(AtomSet trueAtoms, AtomSet falseAtoms) {
			this.trueAtoms = trueAtoms;
			this.falseAtoms = falseAtoms;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			SearchNode other = (SearchNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private BackwardSearchCubePlanner getOuterType() {
			return BackwardSearchCubePlanner.this;
		}		
	}
}
