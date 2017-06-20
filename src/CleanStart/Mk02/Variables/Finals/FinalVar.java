package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.BaseVar;
import CleanStart.Mk02.Variables.InlineStates;
import CleanStart.Mk02.Variables.VarTypes;

public abstract class FinalVar extends BaseVar
{
	public FinalVar(int inInstanceNumber, VarTypes inVarType)
	{
		super(inInstanceNumber, inVarType);
	}

	@Override
	public boolean isFinal()
	{
		return true;
	}
	
	@Override
	public InlineStates getReadState()
	{
		return InlineStates.Inline_Always;
	}

	@Override
	public InlineStates getWriteState()
	{
		return InlineStates.Inline_Error;
	}
	
	public abstract String getCargo_AsString();
}
