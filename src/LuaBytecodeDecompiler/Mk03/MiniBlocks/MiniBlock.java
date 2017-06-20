package LuaBytecodeDecompiler.Mk03.MiniBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public abstract class MiniBlock
{
	protected static final MiniBlock NullMiniBlock = new MiniBlock_Null();
	
	private MiniBlock prev;
	private MiniBlock next;
	protected final AnnoInstruction[] spine;
	protected final int start;
	protected final int end;
	
	public MiniBlock(AnnoInstruction[] inSpine, int inStart, int inEnd)
	{
		this.prev = NullMiniBlock;
		this.next = NullMiniBlock;
		this.spine = inSpine;
		this.start = inStart;
		this.end = inEnd;
	}

	public void linkNext(MiniBlock next)
	{
		this.next = next;
		next.prev = this;
	}
	
	public void linkPrev(MiniBlock prev)
	{
		this.prev = prev;
		prev.next = this;
	}
	
	public static MiniBlock getNullMiniBlock()
	{
		return NullMiniBlock;
	}

	public MiniBlock getPrev()
	{
		return this.prev;
	}
	
	public boolean hasPrev()
	{
		return this.prev != NullMiniBlock;
	}

	public MiniBlock getNext()
	{
		return this.next;
	}
	
	public boolean hasNext()
	{
		return this.next != NullMiniBlock;
	}

	public CharList toCharList(int offset)
	{
		final CharList result = this.toCharList_Internal(offset);
		
		MiniBlock nextLoc = this.next;
		
		while(nextLoc != NullMiniBlock)
		{
			result.addNewLine();
			result.add(nextLoc.toCharList_Internal(offset), true);
			nextLoc = nextLoc.next;
		}
		
		return result;
	}
	
	protected abstract CharList toCharList_Internal(int offset);
	
	private static class MiniBlock_Null extends MiniBlock
	{
		@Override
		public CharList toCharList_Internal(int inOffset)
		{
			return new CharList("NullBlock");
		}
	}
}
