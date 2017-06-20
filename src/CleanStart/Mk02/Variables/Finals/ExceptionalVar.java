package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.VarTypes;

/**
 * 
 * this class is to be used for exceptional singletons, ie Null, Indeterminate, newTable()
 *
 */
public class ExceptionalVar extends FinalVar
{
	private static int instanceCounter = 0;
	
	private final String exceptionalType;
	private final String exceptionalCargo;

	private ExceptionalVar(String inExceptionalType, String inExceptionalCargo)
	{
		super(instanceCounter++, VarTypes.Exceptional);
		this.exceptionalType = inExceptionalType;
		this.exceptionalCargo = inExceptionalCargo;
	}

	public static ExceptionalVar getInstance(String inExceptionalType, String inExceptionalCargo)
	{
		return new ExceptionalVar(inExceptionalType, inExceptionalCargo);
	}

	public String getExceptionalType()
	{
		return this.exceptionalType;
	}

	@Override
	public String getCargo_AsString()
	{
		return this.exceptionalCargo;
	}
}
