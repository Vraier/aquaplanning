package edu.kit.aquaplanning.planning.cube;

import edu.kit.aquaplanning.model.ground.State;

public interface StateDistanceHeuristic {

	public boolean cutOff(State state1, State state2); 
}
