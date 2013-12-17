package grid;

import java.awt.Point;

public class Heading {
	public static int NORTH = 0;
	public static int EAST = 1;
	public static int SOUTH = 2;
	public static int WEST = 3;  

	public static int RIGHT = 1;
	public static int LEFT = 3;
	
	public static int rotate(int actual, int delta_o)
	{
		return (actual + delta_o) % 4;
	}
	
	public static Point movePoint(Point pos, int orientation, int distance)
	{
		int new_x = pos.x;
		int new_y = pos.y;
		
		if (orientation == Heading.NORTH)
			new_y -= distance;
		else if (orientation == Heading.SOUTH)
			new_y += distance;
		else if (orientation == Heading.EAST)
			new_x += distance;
		else if (orientation == Heading.WEST)
			new_x -= distance;
		return new Point(new_x, new_y);
	}
}
