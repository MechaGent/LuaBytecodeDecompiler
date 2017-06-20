package LuaBytecodeDecompiler.Mk03.AnnoVars;

import LuaBytecodeDecompiler.Mk03.ScopeLevels;

public class AnnoVar_Int extends AnnoVar
{
	private static AnnoVar_Int HardCodedBool_False = new AnnoVar_Int("sharedHardBool_False", ScopeLevels.Hardcoded_Bool, 0);
	private static AnnoVar_Int HardCodedBool_True = new AnnoVar_Int("sharedHardBool_True", ScopeLevels.Hardcoded_Bool, 1);
	
	private final int value;

	public AnnoVar_Int(String inName, ScopeLevels inScope, int inValue)
	{
		super(inName, inScope);
		this.value = inValue;
	}

	public int getValue()
	{
		return this.value;
	}
	
	@Override
	public String getAdjustedName()
	{
		if(this.scope == ScopeLevels.Local_Constant)
		{
			return Integer.toString(this.value);
		}
		else
		{
			return super.getAdjustedName();
		}
	}
	
	public static AnnoVar_Int getHardCodedBool(boolean value)
	{
		if(value)
		{
			return HardCodedBool_True;
		}
		else
		{
			return HardCodedBool_False;
		}
	}
}
