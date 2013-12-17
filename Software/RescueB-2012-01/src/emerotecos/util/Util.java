package emerotecos.util;

import java.util.ArrayList;

public class Util {
	
	public enum Movement{
		LEFT, RIGHT, FRONT, BACK;
		
		public Movement rotate(int delta){
			Movement ret = this;
			delta %= 4;
			for (int i = 0; i < delta; i++)
			{
				switch(ret){
				case FRONT:
					ret = RIGHT;
					break;
				case RIGHT:
					ret = BACK;
					break;
				case BACK:
					ret = LEFT;
					break;
				case LEFT:
					ret = FRONT;
					break;
				}
			}
			return ret;
		}
	}
	
	public enum Direction{
		FORWARD, BACKWARD;
		
		public int getVal(){
			if(this == FORWARD)
				return 1;
			return -1;
		}
	}
	
	public enum Heading{
		North, East, South, West;
		
		static public ArrayList<Heading> getAllHeadings()
		{
			ArrayList<Heading> ret = new ArrayList<Heading>();
			ret.add(North);
			ret.add(East);
			ret.add(South);
			ret.add(West);
			return ret;
		}
		
		public Heading invert()
		{
			return this.left().left();
		}
		
		public int getValue(){
			if(this == North)
				return 0;
			if(this == East)
				return 1;
			if(this == South)
				return 2;
			return 3;
		}
		public Heading left(){
			switch(this){
			case North:
				return West;
			case East:
				return North;
			case South:
				return East;
			case West:
				return South;
			}
			return null;
		}
		public Heading right(){
			switch(this){
			case North:
				return East;
			case East:
				return South;
			case South:
				return West;
			case West:
				return North;
			}
			return null;
		}
	}
	
	public static boolean range(double min, double max, double value){
	    return (value <= max? (value >= min? true: false) : false);
	}
	
	public static double[] filterVariation(double[] reads, double minDif){
		double[] filter = new double[reads.length];
		double last = reads[1], now, filt;
		for(int p = 0; p < reads.length; p++){
			now = reads[p];
			filt = now;
			if(p > 0 && p < reads.length - 1 ){
				if(Math.abs(now - last) > minDif)
					filt = (last + reads[p+1])/2;
			}else{
				if(Math.abs(now - last) > minDif)
					filt = last;
			}
			filter[p] = filt;
			last = now;
		}
		
		return filter;
	}
	
	public static int whereIsMinimum(double[] in){
		int pos = 0;
		double val = in[0];
		for(int i = 0; i < in.length; i++){
			if(in[i] < val){
				val = in[i];
				pos = i;
			}
				
		}
		return pos;
	}
}
