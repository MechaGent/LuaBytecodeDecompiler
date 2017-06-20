package LuaBytecodeDecompiler.Mk03.MiniBlocks;

import java.util.Iterator;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public class Brickhouse
{
	private final MiniBlock firstBlock;

	public Brickhouse(MiniBlock inFirstBlock)
	{
		this.firstBlock = inFirstBlock;
	}

	public static Brickhouse parseFromInstructions(AnnoInstruction[] in)
	{
		final Itsy itsy = new Itsy(in);

		final MiniBlock inFirst = parseNext(itsy);
		MiniBlock current = inFirst;

		while (itsy.hasNext())
		{
			final MiniBlock next = parseNext(itsy);
			current.linkNext(next);
			current = next;
		}

		return new Brickhouse(inFirst);
	}

	/*
	 * patterns:
	 * 
	 */
	private static MiniBlock parseNext(Itsy in)
	{
		final SingleLinkedList<AnnoInstruction> core = new SingleLinkedList<AnnoInstruction>();
		boolean isDone = false;

		while (!isDone && in.hasNext())
		{
			final AnnoInstruction first = in.next();

			switch (first.getOpCode())
			{
				default:
				{
					core.add(first);
					break;
				}
			}
		}
	}
	
	private static MiniBlock parseAll(Itsy in)
	{
		
	}

	public CharList toCharList()
	{
		return this.firstBlock.toCharList(0);
	}

	private static class Itsy implements Iterator<AnnoInstruction>
	{
		private final AnnoInstruction[] core;
		private int place;

		public Itsy(AnnoInstruction[] inCore)
		{
			this.core = inCore;
			this.place = 0;
		}

		@Override
		public boolean hasNext()
		{
			return this.place < this.core.length;
		}

		@Override
		public AnnoInstruction next()
		{
			return this.core[this.place++];
		}

		public AnnoInstruction peekNext()
		{
			return this.core[this.place];
		}
	}
}
