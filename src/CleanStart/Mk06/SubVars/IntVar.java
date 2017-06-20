package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;

public class IntVar extends Var
{
	private final int core;

	private IntVar(String inNamePrefix, int inInstanceNumber, int inCore)
	{
		super(inNamePrefix, inInstanceNumber);
		this.core = inCore;
	}

	public static IntVar getInstance(String inNamePrefix, int inInstanceNumber, int inCore)
	{
		return new IntVar(inNamePrefix, inInstanceNumber, inCore);
	}

	public float getCore()
	{
		return this.core;
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.Float;
	}

	@Override
	public String getValueAsString()
	{
		return Float.toString(this.core);
	}

	@Override
	public int getValueAsInt()
	{
		return this.core;
	}

	@Override
	public float getValueAsFloat()
	{
		return this.core;
	}

	@Override
	public boolean getValueAsBoolean()
	{
		return this.core != 0.0f;
	}
}
