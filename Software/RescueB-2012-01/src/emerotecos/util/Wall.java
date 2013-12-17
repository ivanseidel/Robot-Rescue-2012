package emerotecos.util;

public class Wall{
	public static final int wExist = 1;
	public static final int wUnknown = 0;
	public static final int wNotExist = 2;
	
	public boolean isDeleted = false;
	
	protected double probability = 0.5;
	protected double unprobability = 0.5;
	
	public int getStatus(){
		if(exist())
			return wExist;
		
		if(probability == 0.5)
			return wUnknown;
		
		return wNotExist;
	}
	
	public void resetWall()
	{
		setProb(0.5);
		isDeleted = false;
	}
	
	public void updateWall(double chance)
	{
		if (chance < 0 || chance > 1)
			return;
		probability *= chance;
		unprobability *= 1 - chance;
		normalize();
		if (isDeleted)
			setDeleted(false);
	}
	
	public void normalize()
	{
		probability = Statistics.normalize(probability, unprobability);
		unprobability = 1 - probability;
	}
	
	public boolean exist(){
		if (isDeleted)
			return false;
		return probability > 0.5;
	}
	
	public boolean exist(boolean truth){
		updateWall((truth? 1 : 0));
		return truth;
	}
	
	public double getAccuracy()
	{
		return Math.abs(getProb() - 0.5);
	}
	
	public double getProb()
	{
		return probability;
	}
	
	public boolean setDeleted(boolean b)
	{
		if (isDeleted == b)
			return false;
		isDeleted = b;
		return true;
	}
	
	public void setProb(double prob){
		if (prob < 0 || prob > 1)
			return;
		probability = prob;
		unprobability = 1 - prob;
	}
}