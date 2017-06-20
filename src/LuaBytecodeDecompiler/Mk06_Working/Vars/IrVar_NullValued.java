package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_NullValued extends IrVar
{
	private static final IrVar_NullValued sharedInstance = new IrVar_NullValued();
	
	private IrVar_NullValued()
	{
		super("IrVar_NullValued", VarTypes.NullValued, ScopeLevels.Hardcoded_Null, false, true);
	}
	
	public static IrVar_NullValued getInstance()
	{
		return sharedInstance;
	}

	@Override
	public String valueToString()
	{
		return "NULL VALUED";
	}
}
