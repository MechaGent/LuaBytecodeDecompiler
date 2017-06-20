package LuaBytecodeDecompiler.Mk06_Working.Vars;

import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_Formula extends IrVar
{
	private final Formula core;

	public IrVar_Formula(String inLabel, ScopeLevels inScope, Formula inCore)
	{
		super(inLabel, VarTypes.Formula, inScope, false);
		this.core = inCore;
	}

	@Override
	public String valueToString()
	{
		return this.core.toCharList().toString();
	}
}
