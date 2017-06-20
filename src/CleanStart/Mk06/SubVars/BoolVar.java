package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;

public class BoolVar extends Var
{
	private final boolean core;

	private BoolVar(String inNamePrefix, int inInstanceNumber, boolean inCore)
	{
		super(inNamePrefix, inInstanceNumber);
		this.core = inCore;
	}

	public static BoolVar getInstance(String inNamePrefix, int inInstanceNumber, boolean inCore)
	{
		return new BoolVar(inNamePrefix, inInstanceNumber, inCore);
	}

	public boolean getCore()
	{
		return this.core;
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.Bool;
	}

	@Override
	public String getValueAsString()
	{
		return Boolean.toString(this.core);
	}

	@Override
	public int getValueAsInt()
	{
		return this.core? 1 : 0;
	}

	@Override
	public float getValueAsFloat()
	{
		return this.core? 1.0f : 0.0f;
	}

	@Override
	public boolean getValueAsBoolean()
	{
		return this.core;
	}
}
