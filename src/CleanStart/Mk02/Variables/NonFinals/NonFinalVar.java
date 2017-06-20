package CleanStart.Mk02.Variables.NonFinals;

import CleanStart.Mk02.Variables.BaseVar;
import CleanStart.Mk02.Variables.InlineStates;
import CleanStart.Mk02.Variables.VarTypes;

public abstract class NonFinalVar extends BaseVar
{
	public NonFinalVar(int inInstanceNumber, VarTypes inVarType)
	{
		super(inInstanceNumber, inVarType, InlineStates.Inline_Always, InlineStates.Inline_Always);
	}
	
	@Override
	public boolean isFinal()
	{
		return false;
	}
}
