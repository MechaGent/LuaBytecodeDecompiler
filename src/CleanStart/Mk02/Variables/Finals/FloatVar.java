package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.VarTypes;

public class FloatVar extends FinalVar
{
	private static int instanceCounter = 0;
	
	private final float cargo;

	private FloatVar(VarTypes inVarType, float inCargo)
	{
		super(instanceCounter++, inVarType);
		this.cargo = inCargo;
	}
	
	public static FloatVar getInstance(VarTypes inVarType, float inCargo)
	{
		return new FloatVar(inVarType, inCargo);
	}

	public float getCargo()
	{
		return this.cargo;
	}

	@Override
	public String getCargo_AsString()
	{
		return Float.toString(this.cargo);
	}
}
