package edu.kit.aquaplanning.planning.cube;

import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.State;

public class ManhattanDistanceHeuristic implements StateDistanceHeuristic {

	private int difference;
	
	ManhattanDistanceHeuristic(int difference){
		this.difference = difference;
	}
	
	@Override
	public boolean cutOff(State state1, State state2) {
		AtomSet diff = state1.getAtomSet().xor(state2.getAtomSet());
		return (diff.numAtoms() > difference);
	}
}
