package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_Int extends IrVar
{
	private static int instanceCounter = 0;
	
	private final int value;

	public IrVar_Int(ScopeLevels inScope, boolean inIsFromMethodReturn, int inValue)
	{
		super("IrVar_Int_" + instanceCounter++, VarTypes.Int, inScope, inIsFromMethodReturn);
		this.value = inValue;
	}

	public int getValue()
	{
		return this.value;
	}

	@Override
	public String valueToString()
	{
		return Integer.toString(this.value);
	}
}
