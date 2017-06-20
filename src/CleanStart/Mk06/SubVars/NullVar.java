package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;

public class NullVar extends Var
{
	private static int instanceCounter = 0;
	
	private NullVar(String inNamePrefix, int inInstanceNumber)
	{
		super(inNamePrefix, inInstanceNumber);
	}
	
	public static NullVar getInstance(String inNamePrefix)
	{
		return getInstance(inNamePrefix, instanceCounter++);
	}

	public static NullVar getInstance(String inNamePrefix, int inInstanceNumber)
	{
		return new NullVar(inNamePrefix, inInstanceNumber);
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.Null;
	}

	@Override
	public String getValueAsString()
	{
		return "<Null Value>";
	}

	@Override
	public int getValueAsInt()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public float getValueAsFloat()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getValueAsBoolean()
	{
		throw new UnsupportedOperationException();
	}
}
