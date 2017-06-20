package LuaBytecodeDecompiler.Mk06_Working.Vars;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

/**
 * 
 * This class is for map lookups only. Array lookups should use IrVar_Indexed.
 *
 */
public class IrVar_Mapped extends IrVar
{
	private static int instanceCounter = 0;
	
	private final IrVar sourceMap;
	private final IrVar sourceKey;
	
	/**
	 * @param inScope
	 * @param inSourceMap
	 * @param inSourceKey
	 */
	public IrVar_Mapped(ScopeLevels inScope, IrVar inSourceMap, IrVar inSourceKey)
	{
		super("IrVar_Mapped_" + instanceCounter++, VarTypes.Mapped, inScope, false);
		this.sourceMap = inSourceMap;
		this.sourceKey = inSourceKey;
	}
	
	public IrVar_Mapped(String oldLabel, ScopeLevels inScope, IrVar inSourceMap, IrVar inSourceKey)
	{
		super(oldLabel, VarTypes.Mapped, inScope, false);
		this.sourceMap = inSourceMap;
		this.sourceKey = inSourceKey;
	}
	
	public IrVar getSourceMap()
	{
		return this.sourceMap;
	}

	public IrVar getSourceKey()
	{
		return this.sourceKey;
	}

	@Override
	public String valueToString()
	{
		final CharList result = new CharList();
		
		result.add(this.sourceMap.getLabel());
		result.add(".[\"");
		//result.add(".[");
		result.add(this.sourceKey.valueToString());
		result.add("\"]");
		//result.add("]");
		
		return result.toString();
	}
}
