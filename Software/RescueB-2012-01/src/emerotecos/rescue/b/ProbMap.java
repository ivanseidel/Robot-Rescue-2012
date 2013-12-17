package emerotecos.rescue.b;

public class ProbMap{
	double[][] probMap;
	int xSize, ySize;
	public ProbMap(int x, int y){
		probMap = new double[x][y];
		
		double normalize = 1.0/(x*y);
		for(int pX = 0; pX < x; pX++){
			for(int pY = 0; pY < y; pY++){
				probMap[pX][pY] = normalize;
			}
		}
		this.xSize = x;
		this.ySize = y;
	}
	
	public void normalize(){
		double sum = 0;
		for(int pX = 0; pX < xSize; pX++){
			for(int pY = 0; pY < ySize; pY++){
				sum += probMap[pX][pY];
			}
		}
		
		for(int pX = 0; pX < xSize; pX++){
			for(int pY = 0; pY < ySize; pY++){
				probMap[pX][pY] /= sum;
			}
		}
	}
}