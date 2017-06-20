package LuaBytecodeDecompiler.Mk06_Working.Vars;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_FunctionInstance extends IrVar
{
	private final Function core;
	private final IrVar[] params;
	private final IrVar[] upvals;
	
	public IrVar_FunctionInstance(String inLabel, ScopeLevels inScope, Function inCore, IrVar[] inParams, IrVar[] inUpvals)
	{
		super(inLabel, VarTypes.FunctionInstance, inScope, true);
		this.core = inCore;
		this.params = inParams;
		this.upvals = inUpvals;
	}

	@Override
	public String valueToString()
	{
		final CharList result = new CharList();
		
		result.add("InstanceOf:");
		result.add(this.core.getFunctionName());
		result.add(", with upvalues {");
		
		for(int i = 0; i < this.upvals.length; i++)
		{
			result.add(' ');
			
			if(this.upvals[i].isFromMethodReturn())
			{
				result.add(this.upvals[i].getLabel());
			}
			else
			{
				result.add(this.upvals[i].valueToString());
			}
		}
		
		result.add('}');
		
		return result.toString();
	}
}
