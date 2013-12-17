package emerotecos.util;

import java.util.ArrayList;

import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;

public class MapBrain {
	public static Cell findNextGoalDexter(MapController mc)
	{
		if (!BFS.Dexter(mc))
			return mc.getThisCell();
		ArrayList<Cell> cells = mc.map.getAllCells();
		int least = -1;
		double leastWeight = BFS.MAX_WEIGHT;
		for (int i = 0; i < cells.size(); i++)
		{
			Cell c = cells.get(i);
			if (c != null && !c.isVisited && !c.isBlack && c != mc.getThisCell() && leastWeight > c.weight)
			{
				least = i;
				leastWeight = cells.get(i).weight;
			}
		}
		
		if (least == -1)
			return mc.getThisCell();
		return cells.get(least);
	}
	
	public static Cell findNextGoalFast(MapController mc)
	{
		return findNextGoalDexter(mc);
	}
	
	public static Cell findNextGoalNotSoFast(MapController mc)
	{
		Cell goal = null;
		int map_size = Math.max(mc.map.getMaxX(), mc.map.getMaxY());
		ArrayList<Cell> closed = new ArrayList<Cell>();
		for (int i = 2; i < map_size; i++)
		{
			ArrayList<Cell> square = mc.map.getCellSquare(mc.xPos, mc.yPos, i);
			boolean reachedOne = false;
			for (Cell c : square)
			{
				if (c != mc.getThisCell() && !closed.contains(c) && c != null && !c.isVisited && !c.isIsolated)
				{
					ArrayList<Heading> dirs = BFS.createHeadingsList(mc, c.x, c.y);
					if (!dirs.isEmpty())
						reachedOne = true;
					c.weight = BFS.calculateWeight(mc, dirs);
					if (goal == null || (c.weight < goal.weight && c.weight > 0))
						goal = c;
					closed.add(c);
				}
			}
			if (!reachedOne)
				return mc.getThisCell();
			if (goal != null)
				return goal;
		}
		if (goal == null)
			goal = mc.getThisCell();
		return goal;
	}
	
	public static Cell findNextGoalSlow(MapController mc)
	{
		Cell goal = null;
		ArrayList<Cell> all = mc.map.getAllCells();
		for (Cell c : all)
		{
			ArrayList<Heading> dirs = BFS.createHeadingsList(mc, c.x, c.y);
			c.weight = BFS.calculateWeight(mc, dirs);
			if (goal == null || (c.weight < goal.weight && c.weight > 0 && !c.isVisited && c.weight < BFS.MAX_WEIGHT))
				goal = c;
		}
		return goal;
	}
	
	public static ArrayList<Movement> getNextMovementList(MapController mc)
	{
		Cell goal = findNextGoalFast(mc);
		return BFS.createMovementList(mc, BFS.createHeadingsList(mc, goal.x, goal.y));
	}
	
	public static ArrayList<Movement> getMovementList(MapController mc, Cell goal)
	{
		return BFS.createMovementList(mc, BFS.createHeadingsList(mc, goal.x, goal.y));
	}
}
