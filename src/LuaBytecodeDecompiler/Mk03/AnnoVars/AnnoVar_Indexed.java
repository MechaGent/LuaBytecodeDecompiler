package LuaBytecodeDecompiler.Mk03.AnnoVars;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk03.ScopeLevels;

public class AnnoVar_Indexed extends AnnoVar
{
	private final AnnoVar table;
	private final AnnoVar index;
	
	public AnnoVar_Indexed(String inName, ScopeLevels inScope, AnnoVar inTable, AnnoVar inIndex)
	{
		super(inName, inScope);
		this.table = inTable;
		this.index = inIndex;
	}

	public AnnoVar getTable()
	{
		return this.table;
	}

	public AnnoVar getIndex()
	{
		return this.index;
	}
	
	@Override
	public String getAdjustedName()
	{
		final CharList result = new CharList();
		MiscAutobot.prettify_TableGetMethod(result, this.table.getAdjustedName(), this.index.getAdjustedName());
		return result.toString();
	}
}
