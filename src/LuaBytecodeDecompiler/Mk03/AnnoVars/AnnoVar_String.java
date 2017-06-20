package LuaBytecodeDecompiler.Mk03.AnnoVars;

import LuaBytecodeDecompiler.Mk03.ScopeLevels;

public class AnnoVar_String extends AnnoVar
{
	private final String value;

	public AnnoVar_String(String inName, ScopeLevels inScope, String inValue)
	{
		super(inName, inScope);
		this.value = inValue;
	}

	public String getValue()
	{
		return this.value;
	}
	
	@Override
	public String getAdjustedName()
	{
		if(this.scope == ScopeLevels.Local_Constant)
		{
			return "\"" + this.value + "\"";
		}
		else
		{
			return super.getAdjustedName();
		}
	}
}
