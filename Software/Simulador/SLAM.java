package grid;

import java.awt.Point;

public class SLAM {
	private int[][] map;
	private int width, height, orientation;
	private Point initial_pos, pos, goal;
	
	static int OUT = -1;
	static int FREE = 0;
	static int OBSTACLE = 1;
	static int SEEN = 2;
	static int RED = 255;
	
	public SLAM(int w, int h)
	{
		int measure = (w > h) ? w : h; 
		
		width = measure * 2 + 15;
		height = measure * 2 + 15;
	
		map = new int[height][width];
		
		pos = new Point(w + 10, h + 10);
		initial_pos = new Point(w + 10, h + 10);
		goal = new Point(w + 10, h + 10);
		
		orientation = Heading.WEST;
	}
	
	public boolean isInitial(int x, int y)
	{
		return x == initial_pos.x && y == initial_pos.y;
	}
	
	public boolean goalReached()
	{
		return pos.x == goal.x && pos.y == goal.y;
	}
	
	public boolean moveRobot(int distance)
	{
		int new_y = pos.y, new_x = pos.x;
		if (orientation == Heading.NORTH)
			new_y -= distance;
		else if (orientation == Heading.SOUTH)
			new_y += distance;
		else if (orientation == Heading.EAST)
			new_x += distance;
		else if (orientation == Heading.WEST)
			new_x -= distance;
		
		return setPosition(new_x, new_y);
	}
	
	public void setOutOfMap()
	{
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (!isFree(x, y))
					break;
				map[y][x] = OUT;
			}
			
			for (int x = width - 1; x >= 0; x--)
			{
				if (!isFree(x, y))
					break;
				map[y][x] = OUT;
			}
		}
	}
	
	public void resetMap()
	{
		int new_x0 = 0, new_y0 = 0, new_xmax = width - 1, new_ymax = height - 1;
		boolean breaking = false;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (!isOut(x, y))
				{
					new_y0 = y;
					breaking = true;;
					break;
				}
			}
			if (breaking)
			{
				breaking = false;
				break;
			}
		}
		
		for (int y = height - 1; y >= 0; y--)
		{
			for (int x = 0; x < width; x++)
			{
				if (!isOut(x, y))
				{
					new_ymax = y;
					breaking = true;
					break;
				}
			}
			if (breaking)
			{
				breaking = false;
				break;
			}
		}
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!isOut(x, y))
				{
					new_x0 = x;
					breaking = true;
					break;
				}
			}
			if (breaking)
			{
				breaking = false;
				break;
			}
		}
		
		for (int x = width - 1; x >= 0; x--)
		{
			for (int y = 0; y < height; y++)
			{
				if (!isOut(x, y))
				{
					new_xmax = x;
					breaking = true;
					break;
				}
			}
			if (breaking)
			{
				breaking = false;
				break;
			}
		}
		int new_width = new_xmax - new_x0 + 1;
		int new_height = new_ymax - new_y0 + 1;
		int[][] new_map = new int[new_height][new_width];
		for (int i = 0; i < new_height; i++)
		{
			for (int j = 0; j < new_width; j++)
			{
				new_map[i][j] = map[new_y0 + i][new_x0 + j];
				if (new_map[i][j] == OUT)
					new_map[i][j] = OBSTACLE;
				if (isRobot(new_x0 + j, new_y0 + i))
				{
					pos.x = j;
					pos.y = i;
				}
				if (isInitial(new_x0 + j, new_y0 + i))
				{
					initial_pos.x = j;
					initial_pos.y = i;
				}
			}
		}
		map = new_map;
		width = new_width;
		height = new_height;
	}
	
	public void setGoal()
	{
		int sum_x = 0, sum_y = 0;
		int counter = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (isFree(x, y))
				{
					sum_x += x;
					sum_y += y;
					counter++;
				}
			}
		}
		
		goal.x = sum_x / counter;
		goal.y = sum_y / counter;
		System.out.printf("%d %d\n", goal.x, goal.y);
		map[goal.y][goal.x] = RED;
	}
	
	public boolean setPosition(int new_x, int new_y)
	{
		if (new_x < 0 || new_x >= width)
			return false;
		if (new_y < 0 || new_y >= height)
			return false;
		if (isObstacle(new_x, new_y))
			return false;
		
		pos.move(new_x, new_y);
		return true;
	}
	
	public void turnTo(int heading)
	{
		orientation = heading;
	}
	
	public void turn(int delta)
	{
		orientation = Heading.rotate(orientation, delta);
	}
	
	public void addAdjacent(int delta_o, int what)
	{
		Point p = Heading.movePoint(pos, Heading.rotate(orientation, delta_o), 1);
		addToMap(p.x, p.y, what);
	}
	
	public void addToMap(int x, int y, int what)
	{
		if (x >= 0 && x < width && y >= 0 && y < height)
			map[y][x] = what;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getX()
	{
		return pos.x;
	}
	
	public int getY()
	{
		return pos.y;
	}
	
	public boolean isRobot(int x, int y)
	{
		return pos.x == x && pos.y == y;
	}
	
	public boolean isObstacle(int x, int y)
	{
		return map[y][x] == OBSTACLE;
	}
	
	public boolean haveSeen(int x, int y)
	{
		return map[y][x] == SEEN;
	}
	
	public boolean isFree(int x, int y)
	{
		return map[y][x] == FREE;
	}
	
	public boolean isOut(int x, int y)
	{
		return map[y][x] == OUT;
	}
	
	public boolean isRed(int x, int y)
	{
		return map[y][x] == RED;
	}
}
