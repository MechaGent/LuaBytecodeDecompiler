package LuaBytecodeDecompiler.Mk01.Enums;

import java.util.EnumSet;

public class OpCodeVersionInfo
{
	private static final OpCodeVersionInfo NullInfoObject = new OpCodeVersionInfo(LuaVersions.getFirst(), -1);
	
	private final LuaVersions version;
	private final int opCode;
	private final EnumSet<OpCodeVars> vars;
	
	public OpCodeVersionInfo(LuaVersions inVersion, int inOpCode, OpCodeVars... inVars)
	{
		this.version = inVersion;
		this.opCode = inOpCode;
		this.vars = (inVars.length > 0)? EnumSet.of(inVars[0], inVars) : EnumSet.noneOf(OpCodeVars.class);
	}

	public LuaVersions getVersion()
	{
		return this.version;
	}

	public int getOpCode()
	{
		return this.opCode;
	}

	public EnumSet<OpCodeVars> getVars()
	{
		return this.vars;
	}
	
	public static OpCodeVersionInfo getNullInfoObject()
	{
		return NullInfoObject;
	}
}
