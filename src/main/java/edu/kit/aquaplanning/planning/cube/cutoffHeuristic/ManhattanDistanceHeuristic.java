package edu.kit.aquaplanning.planning.cube.cutoffHeuristic;

import java.util.List;

import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public class ManhattanDistanceHeuristic extends CutOffHeuristic {

	private int difference;
	
	public ManhattanDistanceHeuristic(int difference){
		this.difference = difference;
	}
	
	@Override
	public boolean cutOff(List<GenericSearchNode> anchors, GenericSearchNode state) {
		for(GenericSearchNode node: anchors) {
			//TODO
		}
		return false;
	}

	@Override
	public boolean isAnchor(GenericSearchNode node) {
		// TODO Auto-generated method stub
		return false;
	}
}
