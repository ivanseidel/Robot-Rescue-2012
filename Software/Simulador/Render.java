package grid;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Render extends Applet implements Runnable {
	private int[][] layout =   {{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
								{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}}; 
	private int box_size = 10;
	
	
	private Grid grid;
	private int width, height;
	private Robot robot;
	
	public void init()
	{
		//layout = new int[301][121];
		robot = new Robot(10, 20);
		grid = new Grid(layout, robot);
		width = grid.getWidth();
		height = grid.getHeight();
		
		SLAM sl = robot.getSLAM();
		setSize((sl.getWidth() + grid.getWidth() + 2) * box_size, (sl.getHeight() + 1) * box_size);
		robot.turn(1);
		
		//addKeyListener(this);
	}
	
	/*public void keyTyped( KeyEvent e ) { }
	public void keyReleased( KeyEvent e ) { }
	public void keyPressed( KeyEvent e ) 
	{
	  int c = e.getKeyCode();
	  if (c == KeyEvent.VK_RIGHT)
		  robot.turn(Heading.RIGHT);
	  else if (c == KeyEvent.VK_LEFT)
		  robot.turn(Heading.LEFT);
	  else if (c == KeyEvent.VK_UP)
		  robot.move(1);
	  robot.sense();
	  repaint();
	}*/
	// Use isso pra fazer o robo andar sozinho
	public void start()
	{
		Thread th = new Thread(this);
		th.start();
	}
	
	public void run()
	{
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		SLAM sl;
		while (true)
		{	
			sl = robot.getSLAM();
			robot.sense();
			robot.turn(Heading.LEFT);
			while (!robot.move(1))
			{
				robot.turn(Heading.RIGHT);
			}
			if (sl.goalReached())
			{
				sl.setOutOfMap();
				sl.resetMap();
				sl.setGoal();
				repaint();
				break;
			}
			//Point p = robot.getPos();
			//int x = p.x;
			//int y = p.y;
			repaint();
			//repaint((x-1) * box_size, (y-1) * box_size, (x + 1) * box_size, (y + 1) * box_size);
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ex)
			{
				
			}
			
		}
	}
	
	public void drawSLAM(Graphics g)
	{
		SLAM sl = robot.getSLAM();
		
		for (int x = 0; x < sl.getWidth(); x++)
		{
			for (int y = 0; y < sl.getHeight(); y++)
			{
				if (sl.haveSeen(x, y))
				{
					g.setColor(Color.green);
					g.fillRect(x * box_size, y * box_size, box_size, box_size);
					g.setColor(Color.black);
					g.drawRect(x * box_size, y * box_size, box_size, box_size);
				}
				else if (sl.isObstacle(x, y))
				{
					g.fillRect(x * box_size, y * box_size, box_size, box_size);	
				}
				else if (sl.isOut(x, y))
				{
					g.setColor(Color.gray);
					g.fillRect(x * box_size, y * box_size, box_size, box_size);
					g.setColor(Color.black);
					g.drawRect(x * box_size, y * box_size, box_size, box_size);
				}
				else if (sl.isRed(x, y))
				{
					g.setColor(Color.red);
					g.fillRect(x * box_size, y * box_size, box_size, box_size);
					g.setColor(Color.black);
					g.drawRect(x * box_size, y * box_size, box_size, box_size);
				}
				else
				{
					g.drawRect(x * box_size, y * box_size, box_size, box_size);
				}
				
				if (sl.isRobot(x, y))
				{
					g.fillOval(x * box_size + box_size / 4, y * box_size + box_size / 4, box_size / 2, box_size / 2);
				}
			}
		}
	}
	
	public void drawRealMap(Graphics g, int offset)
	{
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (layout[y][x] != 0)
					g.fillRect(offset + x * box_size, y * box_size, box_size, box_size);	
				else
					g.drawRect(offset + x * box_size, y * box_size, box_size, box_size);
				
				if (grid.isRobot(x, y))
				{
					g.fillOval(offset + robot.getX() * box_size + box_size / 4, robot.getY() * box_size + box_size / 4, box_size / 2, box_size / 2);
				}
			}
		}
	}
	
	public void paint(Graphics g)
	{
		drawSLAM(g);
		SLAM sl = robot.getSLAM();
		int offset = sl.getWidth() * (box_size + 1);
		drawRealMap(g, offset);
	}
}
