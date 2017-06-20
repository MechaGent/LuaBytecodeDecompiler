package LuaBytecodeDecompiler.Mk04.TranslatedBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public class Block_Raw extends Block
{
	AnnoInstruction[] lines;

	public Block_Raw(AnnoInstruction[] inLines)
	{
		super();
		this.lines = inLines;
	}

	@Override
	public BlockTypes getBlockType()
	{
		return BlockTypes.Raw;
	}

	@Override
	protected CharList toCharList_internal(int inOffset)
	{
		final CharList result = new CharList();
		
		for(int i = 0; i < this.lines.length; i++)
		{
			result.addNewLine();
			result.add(this.lines[i].toCharList(true), true);
		}
		
		return result;
	}
}
