package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;

public class StringVar extends Var
{
	private final String core;
	
	private StringVar(String inNamePrefix, int inInstanceNumber, String inCore)
	{
		super(inNamePrefix, inInstanceNumber);
		this.core = inCore;
	}

	public static StringVar getInstance(String inNamePrefix, int inInstanceNumber, String inCore)
	{
		return new StringVar(inNamePrefix, inInstanceNumber, inCore);
	}

	public String getCore()
	{
		return this.core;
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.String;
	}

	/**
	 * remember to use getCore() if no quotes are wanted
	 * @return
	 */
	@Override
	public String getValueAsString()
	{
		return '"' + this.core + '"';
	}

	@Override
	public int getValueAsInt()
	{
		return Integer.parseInt(this.core);
	}

	@Override
	public float getValueAsFloat()
	{
		return Float.parseFloat(this.core);
	}

	@Override
	public boolean getValueAsBoolean()
	{
		return this.core.length() != 0;
	}
}
