package CleanStart.Mk06;

import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.VarFormula.FormulaToken;

public abstract class Var implements Mangleable, FormulaToken
{
	private static int instanceCounter = 0;
	
	protected final String namePrefix;
	protected final int instanceNumber_overall;
	protected final int instanceNumber_specific;
	private int numReads;
	
	/*
	protected Var(String inNamePrefix)
	{
		this(inNamePrefix, -1);
	}
	*/
	
	protected Var(String inNamePrefix, int inInstanceNumber)
	{
		this.namePrefix = inNamePrefix;
		this.instanceNumber_overall = instanceCounter++;
		this.instanceNumber_specific = inInstanceNumber;
		this.numReads = 0;
	}
	
	@Override
	public String getMangledName()
	{
		return this.getMangledName(false);
	}
	
	@Override
	public String getMangledName(boolean appendMetadata)
	{
		//System.out.println(this.namePrefix);
		return "Var_" + this.namePrefix + ((this.instanceNumber_specific == -1)? "" : '_' + this.instanceNumber_specific) + (appendMetadata? this.getMetadata() : "");
	}
	
	public String getMetadata()
	{
		return '<' + this.getBestGuess().toString() + '>';
	}
	
	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Override
	public boolean isOperand()
	{
		return true;
	}
	
	@Override
	public int getPrecedence()
	{
		return -1;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}

	public int getNumReads()
	{
		return this.numReads;
	}
	
	public void incrementNumReads()
	{
		this.numReads++;
		//System.out.println("incrementing numReads of " + this.getMangledName() + " to " + this.numReads);
	}
	
	protected final void incrementNumReads_forced()
	{
		this.numReads++;
	}
	
	protected final void resetNumReads()
	{
		this.numReads = 0;
	}
	
	public void decrementNumReads()
	{
		this.numReads--;
	}
	
	public InferenceTypes getBestGuess()
	{
		throw new UnsupportedOperationException();
	}
	
	public abstract InferenceTypes getInitialType();
	public abstract String getValueAsString();
	public abstract int getValueAsInt();
	public abstract float getValueAsFloat();
	public abstract boolean getValueAsBoolean();
}
