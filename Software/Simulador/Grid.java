package grid;

import java.awt.Point;

public class Grid {
	private int width, height;
	private int[][] grid;
	private Robot robot;
	
	public Grid(int [][]grid, Robot r)
	{
		this.grid = grid;
		width = grid[0].length;
		height = grid.length;
		robot = r;
		robot.setGrid(this);
		robot.setPosition(new Point(0, 0));
		//robot.setPosition(width / 2, height / 2);
	}
	
	public boolean isObstacle(int x, int y)
	{
		return x >= width || y >= height || x < 0 || y < 0 || grid[y][x] != 0;
	}
	
	public boolean isRobot(int x, int y)
	{
		return robot.getX() == x && robot.getY() == y;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
}
