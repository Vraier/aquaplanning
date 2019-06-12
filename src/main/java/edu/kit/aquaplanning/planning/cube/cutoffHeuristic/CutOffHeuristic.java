package edu.kit.aquaplanning.planning.cube.cutoffHeuristic;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public abstract class CutOffHeuristic {

	public abstract boolean cutOff(List<GenericSearchNode> anchors, GenericSearchNode state); 
	
	public abstract boolean isAnchor(GenericSearchNode node);
	
	public static CutOffHeuristic getCutOffHeuristic(Configuration config) {
		//TODO
		return null;
	}
}
