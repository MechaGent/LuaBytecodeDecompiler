package LuaBytecodeDecompiler.Mk06_Working.Vars;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

/**
 * 
 * this class is for array lookups only. Map lookups should use IrVar_Mapped
 *
 */
public class IrVar_Indexed extends IrVar
{
	private static int instanceCounter = 0;
	
	private final IrVar sourceTable;
	private final IrVar sourceIndex;
	
	/**
	 * @param inScope
	 * @param inSourceTable
	 * @param inSourceIndex
	 */
	public IrVar_Indexed(ScopeLevels inScope, IrVar inSourceTable, IrVar inSourceIndex)
	{
		super("IrVar_Indexed_" + instanceCounter++, VarTypes.Indexed, inScope, false);
		this.sourceTable = inSourceTable;
		this.sourceIndex = inSourceIndex;
	}
	
	public IrVar_Indexed(String oldLabel, ScopeLevels inScope, IrVar inSourceTable, IrVar inSourceIndex)
	{
		super(oldLabel, VarTypes.Indexed, inScope, false);
		this.sourceTable = inSourceTable;
		this.sourceIndex = inSourceIndex;
	}

	public IrVar getSourceTable()
	{
		return this.sourceTable;
	}

	public IrVar getSourceIndex()
	{
		return this.sourceIndex;
	}

	@Override
	public String valueToString()
	{
		final CharList result = new CharList();
		
		result.add(this.sourceTable.valueToString());
		result.add('[');
		result.add(this.sourceIndex.valueToString());
		result.add(']');
		
		return result.toString();
	}
}
