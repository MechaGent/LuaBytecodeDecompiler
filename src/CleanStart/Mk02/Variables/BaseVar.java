package CleanStart.Mk02.Variables;

import CleanStart.Mk02.Variables.Finals.ExceptionalVar;

public abstract class BaseVar implements Var
{
	protected static final ExceptionalVar NullVar = ExceptionalVar.getInstance("Null", "<null>");
	protected static final ExceptionalVar IndetVar = ExceptionalVar.getInstance("Indeterminate", "<indeterminate>");
	
	private final int instanceNumber;
	private final VarTypes varType;
	
	private int numReads;
	protected int numWrites;

	public BaseVar(int inInstanceNumber, VarTypes inVarType)
	{
		this.instanceNumber = inInstanceNumber;
		this.varType = inVarType;
		this.numReads = 0;
		this.numWrites = 0;
	}

	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}

	public VarTypes getVarType()
	{
		return varType;
	}

	public abstract InlineStates getReadState();

	public abstract InlineStates getWriteState();

	@Override
	public String getMangledName()
	{
		return this.varType.toString() + this.instanceNumber;
	}
	
	public boolean isNull()
	{
		return this == NullVar;
	}
	
	public boolean isIndeterminate()
	{
		return this == IndetVar;
	}

	public int getNumReads()
	{
		return numReads;
	}

	public void incrementNumReads(int amount)
	{
		this.numReads += amount;
	}
}
