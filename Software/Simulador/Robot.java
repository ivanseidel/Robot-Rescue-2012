package grid;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Robot {
	private Point pos;
	private int size;
	private double forward_noise, turn_noise, sense_noise;
	private Random random;
	private Grid grid;
	private double maximum_distance;
	private int orientation;
	private SLAM slam;
	
	public Robot(int x, int y)
	{
		random = new Random();
		pos = new Point(x, y);
		forward_noise = turn_noise = sense_noise = 0.0;
		maximum_distance = 5;
		orientation = Heading.EAST;
		size = 5;
	}
	/*
	public void BFS()
	{
		Queue<Point> open = new LinkedList<Point>();
		HashSet<Point> closed = new HashSet<Point>();
		Point actual = new Point(slam.getX(), slam.getY());
		open.add(actual);
		
		while (!open.isEmpty())
		{
			actual = open.poll();
			if (slam.isFree(actual.x + 1, actual.y))
			{
				if ()
			}
			
		}
	}*/
	
	public void sense()
	{
		int x = pos.x;
		int y = pos.y;
		if (grid.isObstacle(x+1, y))
			slam.addAdjacent(Heading.EAST - orientation, SLAM.OBSTACLE);
		if (grid.isObstacle(x-1, y))
			slam.addAdjacent(Heading.WEST - orientation, SLAM.OBSTACLE);
		if (grid.isObstacle(x, y+1))
			slam.addAdjacent(Heading.SOUTH - orientation, SLAM.OBSTACLE);
		if (grid.isObstacle(x, y-1))
			slam.addAdjacent(Heading.NORTH - orientation, SLAM.OBSTACLE);
	}
	
	public void setGrid(Grid g)
	{
		grid = g;
		slam = new SLAM(grid.getWidth(), grid.getHeight());
	}
	
	public boolean move(int distance)
	{
		Point new_pos = Heading.movePoint(pos, orientation, distance);
		if (setPosition(new_pos))
		{
			slam.moveRobot(distance);
			slam.addAdjacent(2, SLAM.SEEN);
			return true; 
		}
		else
		{
			slam.addAdjacent(0, SLAM.OBSTACLE);
			return false;
		}
	}

	public boolean setPosition(Point p)
	{
		if (p.x < 0 || p.x >= grid.getWidth())
			return false;
		if (p.y < 0 || p.y >= grid.getHeight())
			return false;
		if (grid.isObstacle(p.x, p.y))
			return false;
		
		pos.move(p.x, p.y);
		return true;
	}
	
	public void turnTo(int heading)
	{
		orientation = heading;
		slam.turnTo(heading);
	}
	
	public void turn(int delta)
	{
		orientation = Heading.rotate(orientation, delta);
		slam.turn(delta);
	}
	
	public SLAM getSLAM()
	{
		return slam;
	}
	
	/*
	public double Distance(int degrees)
	{
		
	}
	
	public double[] sense()
	{
		double[] Z = new double[4];
		for (int i = 0; i < 4; i++)
		{
			Z[i] = 
		}
		return Z;
	}
	*/
	public void setNoise(double new_f_noise, double new_t_noise, double new_s_noise)
	{
		forward_noise = new_f_noise;
		turn_noise = new_t_noise;
		sense_noise = new_s_noise;
	}
	
	public int getX()
	{
		return pos.x;
	}
	
	public int getY()
	{
		return pos.y;
	}
}
