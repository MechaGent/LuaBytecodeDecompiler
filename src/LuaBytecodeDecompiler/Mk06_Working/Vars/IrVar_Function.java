package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_Function extends IrVar
{
	private final Function core;

	public IrVar_Function(String inLabel, ScopeLevels inScope, boolean inIsFromMethodReturn, Function inCore)
	{
		super(inLabel, VarTypes.Function, inScope, inIsFromMethodReturn);
		this.core = inCore;
	}

	@Override
	public String valueToString()
	{
		return this.core.getFunctionName();
	}

	public Function getCore()
	{
		return this.core;
	}
}
