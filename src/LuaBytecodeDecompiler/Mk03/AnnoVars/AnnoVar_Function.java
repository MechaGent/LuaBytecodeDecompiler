package LuaBytecodeDecompiler.Mk03.AnnoVars;

import LuaBytecodeDecompiler.Mk03.ScopeLevels;

public class AnnoVar_Function extends AnnoVar
{
	private final AnnoVar[] Locals;
	private final int Params;

	public AnnoVar_Function(String inName, ScopeLevels inScope, AnnoVar[] inLocals, int inParams)
	{
		super(inName, inScope);
		this.Locals = inLocals;
		this.Params = inParams;
	}

	/*
	public AnnoVar[] getReturnBasket()
	{
		return this.ReturnBasket;
	}
	*/
	
	public AnnoVar[] getLocals()
	{
		return this.Locals;
	}
	
	public AnnoVar[] getReturns(int numReturns)
	{
		final AnnoVar[] result = new AnnoVar[numReturns];
		final int offset = this.Locals.length - numReturns - 1;
		
		for(int i = 0; i < result.length; i++)
		{
			result[i] = this.Locals[i + offset];
		}
		
		return result;
	}

	public int getNumParams()
	{
		return this.Params;
	}
}
