package LuaBytecodeDecompiler.Mk03.AnnoVars;

import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk03.ScopeLevels;

public class AnnoVar
{
	private static final AnnoVar NullAnnoVar = new AnnoVar("NullAnnoVar", ScopeLevels.Global);

	private final String name;
	protected final ScopeLevels scope;
	protected final boolean isMethodCall;

	public AnnoVar(String inName, ScopeLevels inScope)
	{
		this(inName, inScope, false);
	}
	
	public AnnoVar(String inName, ScopeLevels inScope, boolean inIsMethodCall)
	{
		this.name = inName;
		this.scope = inScope;
		this.isMethodCall = inIsMethodCall;
	}

	public String getName()
	{
		return this.name;
	}

	public ScopeLevels getScope()
	{
		return this.scope;
	}
	
	public boolean isMethodCall()
	{
		return this.isMethodCall;
	}

	public String getAdjustedName()
	{
		final String result;

		switch (this.scope)
		{
			case Global:
			{
				result = MiscAutobot.prependGlobalVarsPrefix(this.name);
				break;
			}
			default:
			{
				result = this.name;
				break;
			}
		}

		return result;
	}

	public static AnnoVar getNullAnnoVar()
	{
		return NullAnnoVar;
	}
}
