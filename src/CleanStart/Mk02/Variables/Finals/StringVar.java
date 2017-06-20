package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.VarTypes;

public class StringVar extends FinalVar
{
	private static int instanceCounter = 0;

	private final String cargo;

	private StringVar(VarTypes inVarType, String inCargo)
	{
		super(instanceCounter++, inVarType);
		this.cargo = inCargo;
	}

	public static StringVar getInstance(VarTypes inVarType, String inCargo)
	{
		return new StringVar(inVarType, inCargo);
	}

	public String getCargo()
	{
		return this.cargo;
	}

	@Override
	public String getCargo_AsString()
	{
		return this.cargo;
	}
}
