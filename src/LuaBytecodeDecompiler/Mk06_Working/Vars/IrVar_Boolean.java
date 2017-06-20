package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_Boolean extends IrVar
{
	private static final IrVar_Boolean instance_True = new IrVar_Boolean(ScopeLevels.Global, false, true, true);
	private static final IrVar_Boolean instance_False = new IrVar_Boolean(ScopeLevels.Global, false, true, false);
	private static int instanceCounter = 0;
	private final boolean value;

	public IrVar_Boolean(ScopeLevels inScope, boolean inIsFromMethodReturn, boolean isFinal, boolean inValue)
	{
		super("IrVar_Boolean_" + instanceCounter++, VarTypes.Boolean, inScope, inIsFromMethodReturn, isFinal);
		this.value = inValue;
	}

	public boolean getValue()
	{
		return this.value;
	}

	@Override
	public String valueToString()
	{
		return Boolean.toString(this.value);
	}

	public boolean isSymbolic()
	{
		return false;
	}
	
	public static IrVar_Boolean getInstance_True()
	{
		return instance_True;
	}
	
	public static IrVar_Boolean getInstance_False()
	{
		return instance_False;
	}
}
