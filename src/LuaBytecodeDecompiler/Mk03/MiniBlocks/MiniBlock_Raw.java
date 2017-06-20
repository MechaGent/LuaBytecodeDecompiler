package LuaBytecodeDecompiler.Mk03.MiniBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public class MiniBlock_Raw extends MiniBlock
{
	private final AnnoInstruction[] core;

	public MiniBlock_Raw(AnnoInstruction[] inCore)
	{
		super();
		this.core = inCore;
	}

	@Override
	protected CharList toCharList_Internal(int inOffset)
	{
		final CharList result = new CharList();
		
		for(int i = 0; i < this.core.length; i++)
		{
			if(i != 0)
			{
				result.addNewLine();
			}
			
			result.add(this.core[i].toCharList(true), true);
		}
		
		return result;
	}
}
