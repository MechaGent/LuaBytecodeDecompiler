package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;

public class FloatVar extends Var
{
	private final float core;

	private FloatVar(String inNamePrefix, int inInstanceNumber, float inCore)
	{
		super(inNamePrefix, inInstanceNumber);
		this.core = inCore;
	}

	public static FloatVar getInstance(String inNamePrefix, int inInstanceNumber, float inCore)
	{
		return new FloatVar(inNamePrefix, inInstanceNumber, inCore);
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
		return (int) this.core;
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
