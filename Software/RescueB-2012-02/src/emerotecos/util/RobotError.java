package emerotecos.util;

public class RobotError {
	public static int E_OK = 0;
	public static int E_INFO = 1;
	public static int E_ERROR = 2;
	
	public String error = "";
	public int code = 0;
	public String description = "";
	public int type = 0;
	public RobotFixer fixer;
	
	
	public RobotError(String error, int code, String description,
			RobotFixer fixer) {
		
		this.error = error;
		this.code = code;
		this.description = description;
		this.fixer = fixer;
	}
	public RobotError(String error, int code, String description, int type, 
			RobotFixer fixer){
		this.error = error;
		this.code = code;
		this.description = description;
		this.fixer = fixer;
		this.type = type;
	}
	
	
	
}
