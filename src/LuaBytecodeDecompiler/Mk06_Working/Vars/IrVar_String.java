package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_String extends IrVar
{
	private static String blankString = "<EMPTY STRING>";
	private static int instanceCounter = 0;

	private final String value;

	public IrVar_String(ScopeLevels inScope, boolean inIsFromMethodReturn, String inValue)
	{
		this("IrVar_String_" + instanceCounter++, inScope, inIsFromMethodReturn, inValue);
	}

	public IrVar_String(String inLabel, ScopeLevels inScope, boolean inIsFromMethodReturn, String inValue)
	{
		super(inLabel, VarTypes.String, inScope, inIsFromMethodReturn);

		if (inValue.length() == 0)
		{
			this.value = blankString;
		}
		else
		{
			this.value = inValue;
		}
	}

	public String getValue()
	{
		return this.value;
	}

	@Override
	public String valueToString()
	{
		// return "\"" + this.value + "\"";
		return this.value;
	}
}
