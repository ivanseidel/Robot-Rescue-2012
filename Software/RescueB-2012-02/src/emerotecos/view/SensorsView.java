package emerotecos.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import emerotecos.rescue.b.Program1.State;
import emerotecos.rescue.b.R;
import emerotecos.rescue.b.RobotActivity;
import emerotecos.util.BaseRescueBLooper;
import emerotecos.util.Cell;
import emerotecos.util.IRDistanceSensor;
import emerotecos.util.Map;
import emerotecos.util.MapController;
import emerotecos.util.ServoController;
import emerotecos.util.Statable;
import emerotecos.util.Wall;

public class SensorsView extends View {
	
	/*
	 * FUNCTIONALITY OBJECTS
	 */
	BaseRescueBLooper Robot;
	ArrayList<IRDistanceSensor> DistanceSensors;
	double[] DistanceSensorsAngle = {0, 0, 0, 0};
	ServoController servo;	
	
	/*
	 * Maps
	 */
	MapController mapController = null;

	/*
	 * SCALES
	 */
	public final static double DistSCALE  = 4;
	public final static double WallDist	= 30;
	
	/*
	 * POSITIONING AND ANGLE ATRIBUTES
	 */
	int xPos = 0, yPos = 0;
	
	/*
	 * DRAWING OBJECTS
	 */
	Paint pDistanceSensor;
	Paint pRoute;
	Paint pText;
	Paint pTextError;
	Paint pTextVictim;
	Paint pFillRed;
	Paint pWallClear;
	Paint pWallExist;
	Paint pCellVisited;
	Paint pCellClear;
	Paint pCellVictim;
	Paint pTextVictimBg;
	Paint pCellBlack;

	public void setPosition(int x, int y){
		xPos = x;
		yPos = y;
	}
	
	public Statable robotState = null;
	
	public double getRobotAngleRad(){
		//int angle = mapController.dir.getValue()*90 + 180;
		int angle = mapController.dir.getValue()*90 + 180;
		return Math.toRadians(angle);
	}
	public double getHeadAngleRad(){
		int angle = (servo.getPosition())*-1 - 90;
		return Math.toRadians(angle)-getRobotAngleRad();
	}
	
	public ArrayList<Point> path = null;
	Path route = new Path();
	protected void drawPath(Canvas canvas, int xOffset, int yOffset){
		
		if(path != null){
			if(path.size() > 0){
				int x, y;
				boolean first = true;
				route.reset();
				for(Point a: path){
					x = (int) (a.x*WallDist*DistSCALE + WallDist*DistSCALE/2);
					y = (int) (a.y*WallDist*DistSCALE*-1 - WallDist*DistSCALE/2);
					 
					if(first){
						route.setLastPoint(x, y);
						first = false;
					}else{
						route.lineTo(x, y);
					}
				}
				//route.close();
				canvas.save();
				canvas.translate(xPos+xOffset, yPos+yOffset);
				canvas.drawPath(route, pRoute);
				canvas.restore();
				
				canvas.drawText("Path len.: "+path.size(),10, 220, pText);
			}else{
				canvas.drawText("Path is empty!",10, 220, pTextError);
			}
		}else{
			canvas.drawText("Path is not inited!",10, 220, pTextError);
		}
		
	}
	
	Bitmap rawRobot;
	Bitmap robot;
	protected void drawRobot(Canvas canvas, int xOffset, int yOffset){
		canvas.save();
		canvas.rotate(mapController.dir.getValue()*90 + 180, xPos+xOffset, yPos+yOffset);
		canvas.save();
		canvas.translate(xPos+xOffset, yPos+yOffset);
		canvas.drawPath(arrow, pFillRed);
		canvas.restore();
		canvas.restore();
	}
	protected void drawSensors(Canvas canvas, int xOffset, int yOffset){
		double tmp_angle = getHeadAngleRad();
		double tmp_distance;
		
		//Matrix robotM = new Matrix();
		//robot = Bitmap.createScaledBitmap(rawRobot, 86, 86, true);
		//robotM.setRotate(mapController.getHeading()+45);
		//canvas.drawBitmap(Bitmap.createBitmap(robot, 0, 0, robot.getWidth(), robot.getHeight(), robotM, true),xPos + xOffset -43,yPos + yOffset - 43, pText);
		for(int i = 0; i < 4; i++){
			tmp_distance = DistanceSensors.get(i).lastMeasure;
			//if(tmp_distance < 60){
				canvas.drawText("D"+(i+1)+": "+Math.round(tmp_distance) , 10+(i*100)%200, (i < 2? 40: 80), pText);
				
				canvas.drawLine(
					xOffset+xPos,
					yOffset+yPos,
					(int)(Math.sin(tmp_angle+DistanceSensorsAngle[i])*DistSCALE*tmp_distance)
						+xOffset+xPos,
					(int)(Math.cos(tmp_angle+DistanceSensorsAngle[i])*DistSCALE*tmp_distance)
						+yOffset+yPos,
					pDistanceSensor);
			//}
		}
		canvas.drawText("Servo angle: "+Robot.servo.getPosition(),10, 120, pText);
		canvas.drawText("RobotAngle: "+(mapController.dir.getValue()*90+180)%360,10, 160, pText);
	}
	
	float xS, xE, yS, yE;
	Cell cell_tmp;
	protected void drawMap(Canvas canvas, double xOffset, double yOffset){
		if(mapController != null){
			// Render Map
			Map map = mapController.map;
			
			for(int x = 0; x < 10; x++){
				for(int y = 0; y < 5; y++){
					xS = (float) (x*WallDist*DistSCALE + xOffset + xPos);
					xE = (float) ((x+1)*WallDist*DistSCALE + xOffset + xPos);
					yS = (float) (y*WallDist*DistSCALE*-1 + yOffset + yPos);
					yE = (float) ((y+1)*WallDist*DistSCALE*-1 + yOffset + yPos);
					cell_tmp = map.getCell(x, y);
					
					if(cell_tmp.hasVictim){
						canvas.drawRect(xS, yS, xE, yE, pCellVictim);
					}else if(cell_tmp.isBlack){
						canvas.drawRect(xS, yS, xE, yE, pCellBlack);
					}else if(cell_tmp.isVisited){
						canvas.drawRect(xS, yS, xE, yE, pCellVisited);
					}else{
						canvas.drawRect(xS, yS, xE, yE, pCellClear);
					}
					
					// North Wall
					if(cell_tmp.WallNorth.getStatus() == Wall.wUnknown)
						canvas.drawLine(xS, yE, xE, yE, pWallClear);
					else if(cell_tmp.WallNorth.getStatus() == Wall.wExist)
						canvas.drawLine(xS, yE, xE, yE, pWallExist);
					// South Wall
					if(cell_tmp.WallSouth.getStatus() == Wall.wUnknown)
						canvas.drawLine(xS, yS, xE, yS, pWallClear);
					else if(cell_tmp.WallSouth.getStatus() == Wall.wExist)
						canvas.drawLine(xS, yS, xE, yS, pWallExist);
					// West Wall
					if(cell_tmp.WallWest.getStatus() == Wall.wUnknown)
						canvas.drawLine(xS, yS, xS, yE, pWallClear);
					else if(cell_tmp.WallWest.getStatus() == Wall.wExist)
						canvas.drawLine(xS, yS, xS, yE, pWallExist);
					
					// East Wall
					if(cell_tmp.WallEast.getStatus() == Wall.wUnknown)
						canvas.drawLine(xE, yS, xE, yE, pWallClear);
					else if(cell_tmp.WallEast.getStatus() == Wall.wExist)
						canvas.drawLine(xE, yS, xE, yE, pWallExist);
					
					canvas.drawText("("+x+","+y+")", xS+30, yS-40, pText);
				}
			}
			if(mapController.getThisCell().hasVictim){
				canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), pTextVictimBg);
				canvas.drawText("VICTIM", 50, 300, pTextVictim);
			}
		}
	}
	
	/* Current x and y of the touch */
	private float mCurrentX = 0;
	private float mCurrentY = 0;
	 
	/* The touch distance change from the current touch */
	private float mDeltaX = 0;
	private float mDeltaY = 0;
	
	public boolean dragable = false;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(dragable){
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mCurrentX = event.getRawX();
				mCurrentY = event.getRawY();
				mDeltaX = 0;
				mDeltaY = 0;
			} 
			else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				float x = event.getRawX();
				float y = event.getRawY();
				
				mDeltaX = x - mCurrentX;
				mDeltaY = y - mCurrentY;
				mCurrentX = x;
				mCurrentY = y;
				invalidate();
			}else{
				RobotActivity.gyroIntVals[0] = 0;
				RobotActivity.gyroIntVals[1] = 0;
				RobotActivity.gyroIntVals[2] = 0;
				
				mDeltaX = 0;
				mDeltaY = 0;
			}
			
			xPos += (Math.abs(mDeltaX) < 1? 0: mDeltaX);
			yPos += (Math.abs(mDeltaY) < 1? 0: mDeltaY);
		}
		return true;
	}
	
	// Temporary variables
	int xMid, yMid, xEnd, yEnd;
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		xEnd = canvas.getWidth();
		yEnd = canvas.getHeight();
		xMid = xEnd/2;
		yMid = yEnd/2;
		
		if(isInited()){
					
			// Draws background
			if(!Robot.isConnected()){
				canvas.drawColor(Color.LTGRAY);//canvas.drawColor(Color.argb(255, 245, 61, 10));
				canvas.drawText("Connect to the robot! ",10 ,yEnd - 10, pTextError);
			}else if(Robot.getStatus() == BaseRescueBLooper.RUNNING){
				canvas.drawColor(Color.LTGRAY);
			}else{
				canvas.drawColor(Color.argb(255, 255, 204, 55));
			}
			// Draws the Map grid
			drawMap(canvas,
					xMid - WallDist*DistSCALE/2,
					yMid + WallDist*DistSCALE/2);
			
			// Draws the robot with it`s sensors headings and,
			drawRobot(canvas,
					(int)(xMid + mapController.xPos*WallDist*DistSCALE),
					(int)(yMid - mapController.yPos*WallDist*DistSCALE));
			drawSensors(canvas,
					(int)(xMid + mapController.xPos*WallDist*DistSCALE),
					(int)(yMid - mapController.yPos*WallDist*DistSCALE));
			
		}else{
			canvas.drawColor(Color.WHITE);
			canvas.drawText("IOIO Not initialized!",10 , yEnd - 10, pTextError);
			
			if(mapController != null){
				drawRobot(canvas,
						(int)(xMid + mapController.xPos*WallDist*DistSCALE),
						(int)(yMid - mapController.yPos*WallDist*DistSCALE));
				drawMap(canvas,
					xMid - WallDist*DistSCALE/2,
					yMid + WallDist*DistSCALE/2);
			}
		}
		double compAngle = RobotActivity.gyroIntVals[2];
		drawPath(canvas,
					(int)(xMid - WallDist*DistSCALE/2),
					(int)(yMid + WallDist*DistSCALE/2));
		
		canvas.drawCircle(xEnd-40, yEnd-40, 35, pDistanceSensor);
		canvas.drawLine(xEnd-40, yEnd-40,
				(int)(xEnd-40 + Math.cos(Math.toRadians(compAngle))*35),
				(int)(yEnd-40 + Math.sin(Math.toRadians(compAngle))*35),
				pDistanceSensor);
		canvas.drawText(""+Math.round(compAngle*-10.0)/10.0,xEnd-180, yEnd-20, pDistanceSensor);
		
		//canvas.drawText("Gyro0:"+Math.round(RobotActivity.gyroIntVals[0]),10, 280, pTextError);
		canvas.drawText("I: "+Math.round(RobotActivity.gyroIntVals[1]),10, 340, pTextError);
		canvas.drawText("H: "+Math.round(RobotActivity.gyroIntVals[2]),10, 400, pTextError);
		if(robotState != null)
			canvas.drawText("S: "+robotState.getState(),10, 460, pTextError);
	}
	
	public boolean isInited(){
		if(Robot == null)
			return false;
		return true;
	}
	
	
	public void setMapController(MapController mapController){
		this.mapController = mapController;
	}
	
	
	public void setup(BaseRescueBLooper RescueBLooper){
		Robot = RescueBLooper;
		this.servo = RescueBLooper.servo;
		
		this.DistanceSensors = new ArrayList<IRDistanceSensor>();
		DistanceSensors.add(RescueBLooper.dist1);
		DistanceSensors.add(RescueBLooper.dist2);
		DistanceSensors.add(RescueBLooper.dist3);
		DistanceSensors.add(RescueBLooper.dist4);
		
		DistanceSensorsAngle[0] = Math.PI/2;
		DistanceSensorsAngle[1] = 0;
		DistanceSensorsAngle[2] = -Math.PI/2;
		DistanceSensorsAngle[3] = Math.PI;
	}
	
	Path arrow;
	
	protected void init(){
		pDistanceSensor = new Paint(); 
		pDistanceSensor.setStrokeWidth(4);
		pDistanceSensor.setColor(Color.BLUE);
		pDistanceSensor.setStyle(Style.STROKE);
		pDistanceSensor.setTextSize(40);

		pRoute = new Paint();
		pRoute.setStrokeWidth(6);
		pRoute.setColor(Color.MAGENTA);
		pRoute.setStyle(Style.STROKE);
		pRoute.setTextSize(40);
		
		pWallClear = new Paint();
		pWallClear.setStrokeWidth(2);
		pWallClear.setColor(Color.GRAY);
		pWallClear.setStyle(Style.STROKE);

		pWallExist = new Paint();
		pWallExist.setStrokeWidth(6);
		pWallExist.setColor(Color.BLACK);
		pWallExist.setStyle(Style.STROKE);
		
		pCellVisited = new Paint();
		pCellVisited.setColor(Color.argb(200, 61, 245, 0));
		pCellVisited.setStyle(Style.FILL);
		
		pCellClear = new Paint();
		pCellClear.setColor(Color.argb(30, 200, 200, 200));
		pCellClear.setStyle(Style.FILL);
		
		pCellVictim = new Paint();
		pCellVictim.setColor(Color.argb(200, 204, 51, 255));
		pCellVictim.setStyle(Style.FILL);
		
		pCellBlack = new Paint();
		pCellBlack.setColor(Color.BLACK);
		pCellBlack.setStyle(Style.FILL);
		
		pText = new Paint();
		pText.setColor(Color.DKGRAY);
		pText.setTextSize(25);
		
		pTextError = new Paint();
		pTextError.setColor(Color.RED);
		pTextError.setTextSize(40);
		
		pTextVictim = new Paint();
		pTextVictim.setColor(Color.RED);
		pTextVictim.setTextSize(200);
		
		pTextVictimBg = new Paint();
		pTextVictimBg.setColor(Color.argb(100, 200, 20, 20));
		pTextVictimBg.setStyle(Style.FILL);
		
		pFillRed = new Paint();
		pFillRed.setColor(Color.RED);
		pFillRed.setStyle(Style.FILL);
		
		rawRobot = BitmapFactory.decodeResource(getResources(), R.drawable.robot);
		
		arrow = new Path();
		arrow.setLastPoint(0, 55);
		arrow.lineTo(-30, 5);
		arrow.lineTo(-10, 10);
		arrow.lineTo(-15, -50);
		arrow.lineTo(15, -50);
		arrow.lineTo(10, 10);
		arrow.lineTo(30, 5);

		arrow.close();
		
	}
	
	public SensorsView(Context context) {
		super(context);
		init();
	}
	public SensorsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public SensorsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	

}
