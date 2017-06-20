package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.NonFinals.RefVar;

public class IndetRefVar extends FinalVar
{
	public IndetRefVar(RefVar in)
	{
		super(in.getInstanceNumber(), in.getVarType());
	}
	
	public static IndetRefVar getInstance(RefVar in)
	{
		return new IndetRefVar(in);
	}

	@Override
	public String getCargo_AsString()
	{
		return this.getMangledName();
	}
}
