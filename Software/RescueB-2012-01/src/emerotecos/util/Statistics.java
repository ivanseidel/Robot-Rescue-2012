package emerotecos.util;

public class Statistics {
	static public double normalize(double probability, double unprobability)
	{
		double sum = probability + unprobability;
		return probability / sum;
	}
}
