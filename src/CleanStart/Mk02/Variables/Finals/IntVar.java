package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.VarTypes;

public class IntVar extends FinalVar
{
private static int instanceCounter = 0;
	
	private final int cargo;

	private IntVar(VarTypes inVarType, int inCargo)
	{
		super(instanceCounter++, inVarType);
		this.cargo = inCargo;
	}
	
	public static IntVar getInstance(VarTypes inVarType, int inCargo)
	{
		return new IntVar(inVarType, inCargo);
	}

	public int getCargo()
	{
		return this.cargo;
	}

	@Override
	public String getCargo_AsString()
	{
		return Integer.toString(this.cargo);
	}
}
