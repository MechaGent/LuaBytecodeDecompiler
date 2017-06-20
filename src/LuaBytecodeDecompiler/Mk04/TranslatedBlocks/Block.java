package LuaBytecodeDecompiler.Mk04.TranslatedBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;

public abstract class Block
{
	protected static final Block NullBlock = new NullBlockClass();
	
	protected Block nextBlock;
	
	public abstract BlockTypes getBlockType();
	
	public CharList toCharList(int offset)
	{
		final CharList result = this.toCharList_internal(offset);
		result.addNewLine();
		
		result.add(this.nextBlock.toCharList(offset), true);
		
		return result;
	}
	
	protected abstract CharList toCharList_internal(int offset);
	
	public boolean isNull()
	{
		return false;
	}
	
	public Block getNextBlock()
	{
		return this.nextBlock;
	}

	public void setNextBlock(Block inNextBlock)
	{
		this.nextBlock = inNextBlock;
	}
	
	private static class NullBlockClass extends Block
	{
		private NullBlockClass()
		{
			super();
		}
		
		@Override
		public BlockTypes getBlockType()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CharList toCharList_internal(int inOffset)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean isNull()
		{
			return true;
		}
	}
}
