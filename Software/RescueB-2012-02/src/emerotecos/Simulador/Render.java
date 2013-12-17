package emerotecos.simulador;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import emerotecos.util.BFS;
import emerotecos.util.Cell;
import emerotecos.util.Map;
import emerotecos.util.MapBrain;
import emerotecos.util.MapController;
import emerotecos.util.Wall;
import emerotecos.util.Util.Heading;
import emerotecos.util.Util.Movement;

public class Render extends Applet implements Runnable {

	private int width = 13, height = 6;
	private int box_size = 30, wall_thickness = 2;
	private int offset = 6;
	
	MapController floor1;
	MapController realController;
	
	Cell goal;
	
	public void init()
	{
		realController = new MapController(width, height, width / 2, height / 2);
		Map real = realController.map;
		
		real.getCell(4, 1).WallEast.setProb(0.9);
		real.getCell(4, 0).WallEast.setProb(0.9);
		real.getCell(4, -1).WallEast.setProb(0.9);
		
		real.getCell(5, -2).WallEast.setProb(0.9);
		real.getCell(5, -2).WallSouth.setProb(0.9);
		real.getCell(5, -2).WallNorth.setProb(0.9);
		
		real.getCell(5, -2).setRamp(true);
		real.getCell(3, -2).setBlack(true);
		
		real.getCell(-5, 1).WallWest.setProb(0.9);
		real.getCell(-5, 0).WallWest.setProb(0.9);
		real.getCell(-5, -1).WallWest.setProb(0.9);
		real.getCell(-5, -2).WallWest.setProb(0.9);
		
		real.getCell(4, -2).WallSouth.setProb(0.9);
		real.getCell(3, -2).WallSouth.setProb(0.9);
		real.getCell(2, -2).WallSouth.setProb(0.9);
		real.getCell(1, -2).WallSouth.setProb(0.9);
		real.getCell(0, -2).WallSouth.setProb(0.9);
		real.getCell(-1, -2).WallSouth.setProb(0.9);
		real.getCell(-2, -2).WallSouth.setProb(0.9);
		real.getCell(-3, -2).WallSouth.setProb(0.9);
		real.getCell(-4, -2).WallSouth.setProb(0.9);
		real.getCell(-5, -2).WallSouth.setProb(0.9);
		
		real.getCell(4, 1).WallNorth.setProb(0.9);
		real.getCell(3, 1).WallNorth.setProb(0.9);
		real.getCell(2, 1).WallNorth.setProb(0.9);
		real.getCell(1, 1).WallNorth.setProb(0.9);
		real.getCell(0, 1).WallNorth.setProb(0.9);
		real.getCell(-1, 1).WallNorth.setProb(0.9);
		real.getCell(-2, 1).WallNorth.setProb(0.9);
		real.getCell(-3, 1).WallNorth.setProb(0.9);
		real.getCell(-4, 1).WallNorth.setProb(0.9);
		real.getCell(-5, 1).WallNorth.setProb(0.9);
		
		real.getCell(3, 1).WallSouth.setProb(0.9);
		real.getCell(2, 1).WallSouth.setProb(0.9);
		real.getCell(1, 1).WallSouth.setProb(0.9);
		real.getCell(0, 1).WallSouth.setProb(0.9);
		real.getCell(-1, 1).WallSouth.setProb(0.9);
		real.getCell(-2, 1).WallSouth.setProb(0.9);
		real.getCell(-3, 1).WallSouth.setProb(0.9);
		real.getCell(-4, 1).WallSouth.setProb(0.9);
		
		real.getCell(-3, 1).setBlack(true);
		
		
		real.getCell(0, 0).WallNorth.setProb(0.9);
		real.getCell(0, 0).WallEast.setProb(0.7);
		
		real.getCell(0, -2).WallNorth.setProb(0.6);
		real.getCell(0, -1).WallEast.setProb(0.75);
		real.getCell(-1, -1).WallSouth.setProb(0.8);
		
		real.getCell(-2, -1).setBlack(true);
		
		realController.xPos = -2;
		realController.yPos = -2;
		realController.dir = Heading.East;
		
		floor1 = new MapController(width, height, width / 2, height / 2);
		goal = floor1.getThisCell();
		
		floor1.xPos = realController.xPos;
		floor1.yPos = realController.yPos;
		floor1.dir = realController.dir;
		/*
		Map read = new Map(2 , 2, 0, 0);
		read.getCell(0, 0).WallEast.updateWall(0.8);
		read.getCell(0, 0).WallNorth.updateWall(0.9);
		read.getCell(0, 0).WallSouth.updateWall(0.1);
		read.getCell(0, 0).WallWest.updateWall(0.5);
		
		floor1.updateMap(read, 0, 0);
		*/
		
		setSize((width * 4 + 2 * offset) * box_size,  (offset + height * 3) * box_size);
	}
	
	public void start()
	{
		Thread th = new Thread(this);
		th.start();
	}
	
	private void updateMC(MapController mc, MapController r, int radius)
	{
		double prob;
		ArrayList<Wall> closed = new ArrayList<Wall>();
		ArrayList<Cell> real_cells = r.map.getCellSquare(r.xPos, r.yPos, radius);
		ArrayList<Cell> imaginary_cells = mc.map.getCellSquare(mc.xPos, mc.yPos, radius);
		for (int i = 0; i < real_cells.size(); i++)
		{
			if (real_cells.get(i) != null && imaginary_cells.get(i) != null)
				for (int j = 0; j < 4; j++)
				{
					Wall w = real_cells.get(i).getWall(j);
					if (!closed.contains(w))
					{
						prob = real_cells.get(i).getWall(j).getProb();
						imaginary_cells.get(i).getWall(j).updateWall(prob);
						closed.add(w);
					}
				}
		}
		
		//particle.getThisCell().WallWest.updateWall(prob); */
	}
	
	@Override
	public void run() {
		while (true) {
			// Main actions of robot
			try {

				/*
				 * Creates a Map instance of the current position
				 */
				floor1.getThisCell().setVisited(true);
				updateMC(floor1, realController, 1);
				
				if (realController.getThisCell().isRamp)
				{
					floor1.getThisCell().setRamp(true);
				}
				
				if (realController.getThisCell().isBlack)
				{
					floor1.getThisCell().setBlack(true);
					realController.move(floor1.reverseLastMovement());
					floor1.move(floor1.reverseLastMovement());
					
				}
				
				goal = MapBrain.findNextGoalFast(floor1);
				repaint();
				
				Thread.sleep(200);

				ArrayList<Movement> moves = MapBrain.getMovementList(floor1, goal);
				if (!moves.isEmpty())
				{
					floor1.move(moves.get(0));
				
					realController.move(moves.get(0));
				}
				
				
				repaint();
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				// End of actions
			}
		}
	}

	public void drawMap(Graphics g, MapController mc, int offset_x, int offset_y, boolean printProb) {
		Map m = mc.map;
		for (int x = m.getFirstX(); x < m.getLastX(); x++) 
		{
			for (int y = m.getFirstY() ; y < m.getLastY(); y++) 
			{
				Cell cell = m.getCell(x, y);
				int pixel_x = (offset_x + x) * box_size;
				int pixel_y = (offset_y - y) * box_size;
				
				if (cell.isVisited)
				{
					g.setColor(Color.green);
					g.fillRect(pixel_x + 1, pixel_y - box_size -1, box_size - 1, box_size);
					g.setColor(Color.black);
				}
				if (cell.isBlack)
				{
					g.setColor(Color.gray);
					g.fillRect(pixel_x + 1, pixel_y - box_size, box_size - 1, box_size);
					g.setColor(Color.black);
				}
				if (cell.isRamp)
				{
					g.setColor(Color.blue);
					g.fillRect(pixel_x + 1, pixel_y - box_size, box_size - 1, box_size);
					g.setColor(Color.black);
				}
				
				if (mc.isRobot(x, y))
				{
					int px = pixel_x + box_size / 4;
					int py = pixel_y - 3 * box_size / 4;
					g.fillOval(px, py, box_size / 2, box_size / 2);
					double rads = Math.toRadians(floor1.dir.getValue() * 90 - 90);
					px += box_size / 4;
					py += box_size / 4;
					g.drawLine(px, py, (int)(px + Math.cos(rads) * box_size), (int)(py + Math.sin(rads) * box_size));
				}
				
				if (printProb)
					g.drawString("" + cell.WallEast.getProb(), 3 *box_size / 4 + pixel_x, pixel_y - box_size / 4);
				
				if (cell.WallEast.exist())
				{
					g.fillRect(box_size + pixel_x - wall_thickness, pixel_y - box_size, wall_thickness * 2, box_size);
				}
				else
				{
					g.drawLine(box_size + pixel_x, pixel_y, box_size + pixel_x, pixel_y - box_size);
				}
				
				if (printProb)
					g.drawString("" + cell.WallNorth.getProb(), pixel_x + box_size / 4, pixel_y - 3 * box_size / 4);
				
			    if (cell.WallNorth.exist())
				{
					g.fillRect(pixel_x, pixel_y - wall_thickness - box_size, box_size, 2 * wall_thickness);
				}
				else 
				{
					g.drawLine(pixel_x, pixel_y - box_size, pixel_x + box_size, pixel_y - box_size);
				}
				
				if (y == m.getFirstY())
				{
					if (cell.WallSouth.exist())
					{
						g.fillRect(pixel_x, pixel_y - wall_thickness, box_size, 2 * wall_thickness);
					}
					else
					{
						g.drawLine(pixel_x, pixel_y, pixel_x + box_size, pixel_y);
					}
					
				}
				
				if (x == m.getFirstX())
				{
					if (cell.WallWest.exist())
					{
						g.fillRect(pixel_x - wall_thickness, pixel_y - box_size, wall_thickness * 2, box_size);
					}
					else
					{
						g.drawLine(pixel_x, pixel_y, pixel_x, pixel_y - box_size);
					}
				}
				
				
				g.drawString( " " + cell.weight, pixel_x, pixel_y);
				//g.drawString(x + " " + y, pixel_x, pixel_y);
			}
			
		}
	}

	public void drawBFS(Graphics g, MapController mc, ArrayList<Heading> directions, int offset_x, int offset_y)
	{
		ArrayList<Point> points = BFS.createPointList(mc, directions);//BFS.createPointList(new Point(start.x, start.y), mc.map, directions);
		
		g.setColor(Color.red);
		
		int pixel_x, pixel_y, pixel_x2, pixel_y2;

		for (int i = 0; i < points.size() - 1; i++)
		{
			Point p1 = points.get(i);
			Point p2 = points.get(i + 1);
			
			pixel_x = (offset_x + p1.x) * box_size + box_size / 2;
			pixel_y = (offset_y - p1.y) * box_size - box_size / 2;
			pixel_x2 = (offset_x + p2.x) * box_size + box_size / 2;
			pixel_y2 = (offset_y - p2.y) * box_size - box_size / 2;
			
			g.drawLine(pixel_x, pixel_y, pixel_x2, pixel_y2);
		}
		
		g.setColor(Color.black);
	}
	
	public void paint(Graphics g)
	{	
		ArrayList<Heading> array = BFS.createHeadingsList(floor1, goal.x, goal.y);
		
		drawMap(g, realController, offset, offset - 2, false);
		drawMap(g, floor1, width + 2 * offset, offset - 2, false);
		drawBFS(g, floor1, array, width + 2 * offset, offset - 2);
		
		//drawMap(g, particle, width + 2 * offset, offset + height + 2, false);
	}
}