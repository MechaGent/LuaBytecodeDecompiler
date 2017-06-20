package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.FunctionObjects.FunctionObject;

public class FunctionVar extends Var
{
	private static int instanceCounter = 0;
	
	private final FunctionObject core;

	public FunctionVar(String inNamePrefix, FunctionObject inCore)
	{
		super(inNamePrefix, instanceCounter++);
		this.core = inCore;
	}
	
	public static FunctionVar getInstance(String inNamePrefix, FunctionObject inCore)
	{
		return new FunctionVar(inNamePrefix, inCore);
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.FunctionObject;
	}
	
	@Override
	public String getMangledName(boolean appendMetadata)
	{
		return this.namePrefix + this.core.getName() + '_' + this.instanceNumber_specific;
	}

	@Override
	public String getValueAsString()
	{
		return this.getMangledName(false);
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
