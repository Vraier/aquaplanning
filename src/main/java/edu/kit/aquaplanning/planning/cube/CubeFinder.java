package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;

//TODO add computational Bounds
public interface CubeFinder {

	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes);
	
	public Plan getPlan();
}
