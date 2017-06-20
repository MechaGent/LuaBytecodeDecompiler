package LuaBytecodeDecompiler.Mk03.AnnoInstructions;

import java.util.PriorityQueue;
import java.util.TreeMap;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class VarFlowHandler
{
	private final PriorityQueue<VarTouchy> reads;
	private final HalfByteRadixMap<StateSlice> history;
	private final HalfByteRadixMap<IntContainer> readsPerBlock;

	public VarFlowHandler()
	{
		this.reads = new PriorityQueue<VarTouchy>();
		this.history = new HalfByteRadixMap<StateSlice>();
		this.readsPerBlock = new HalfByteRadixMap<IntContainer>();
	}

	public void addVarRead(AnnoVar inVar, AnnoInstruction inInstruction, int instructionNumber, int substep)
	{
		if (instructionNumber < 0)
		{
			throw new IllegalArgumentException("bad instruction address: " + instructionNumber);
		}
		
		IntContainer test = this.readsPerBlock.get(inVar.getAdjustedName());
		
		if(test == null)
		{
			test = new IntContainer();
			this.readsPerBlock.add(inVar.getAdjustedName(), test);
		}
		
		if (test.isGoodForReplacement())
		{
			this.reads.add(new VarTouchy(inVar, inInstruction, instructionNumber, substep));
			test.incrementCargo();
		}
	}

	public void addVarWrite(AnnoVar inVar, AnnoInstruction inInstruction, int instructionNumber, int substep)
	{
		if (instructionNumber < 0)
		{
			throw new IllegalArgumentException("bad instruction address: " + instructionNumber);
		}

		StateSlice dest = this.history.get(inVar.getAdjustedName());

		if (dest == null)
		{
			dest = new StateSlice();
			this.history.add(inVar.getAdjustedName(), dest);
		}

		dest.addWrite(new VarTouchy(inVar, inInstruction, instructionNumber, substep));


		final IntContainer test = this.readsPerBlock.get(inVar.getAdjustedName());
		
		if(test != null)
		{
			test.resetCargo();
		}
	}

	public boolean hasMoreReads()
	{
		return !this.reads.isEmpty();
	}

	public VarTouchy getNextRead()
	{
		return this.reads.poll();
	}

	public boolean hasMoreWrites(AnnoVar in)
	{
		final StateSlice result = this.history.get(in.getAdjustedName());

		if (result != null)
		{
			return result.hasMoreWrites();
		}
		else
		{
			return false;
		}
	}

	/*
	public VarTouchy getNextWrite(AnnoVar in)
	{
		final StateSlice result = this.history.get(in.getAdjustedName());
	
		if (result != null)
		{
			return result.getNextVarWrite();
		}
		else
		{
			return null;
		}
	}
	*/

	public StateSlice getWriteHistory(AnnoVar in)
	{
		return this.history.get(in.getAdjustedName());
	}

	public VarTouchy getLastWrite(AnnoVar in)
	{
		final StateSlice result = this.history.get(in.getAdjustedName());

		if (result != null)
		{
			return result.getLastVarWrite();
		}
		else
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		final CharList result = new CharList();

		result.add("history keys:");

		for (CharList key : this.history.getKeySet())
		{
			result.add(' ');
			result.add(key, false);
		}

		return result.toString();
	}

	private static class IntContainer
	{
		private int cargo;
		
		public IntContainer()
		{
			this.cargo = 0;
		}
		
		public void incrementCargo()
		{
			this.cargo++;
		}
		
		public boolean isGoodForReplacement()
		{
			return this.cargo < 3;
		}
		
		public void resetCargo()
		{
			this.cargo = 0;
		}
	}
	
	public static class VarTouchy implements Comparable<VarTouchy>
	{
		private final AnnoVar var;
		private final AnnoInstruction instruction;
		private final int line;
		private final int subline;

		public VarTouchy(AnnoVar inVar, AnnoInstruction inInstruction, int inLine, int inSubline)
		{
			this.var = inVar;
			this.instruction = inInstruction;
			this.line = inLine;
			this.subline = inSubline;
		}

		public AnnoVar getVar()
		{
			return this.var;
		}

		public AnnoInstruction getInstruction()
		{
			return this.instruction;
		}

		public int getLine()
		{
			return this.line;
		}

		public int getSubline()
		{
			return this.subline;
		}

		@Override
		public int compareTo(VarTouchy in)
		{
			if (this.line < 0)
			{
				throw new IndexOutOfBoundsException("bad instruction address. Var: " + this.var.getAdjustedName() + "\r\nInstruction: " + this.instruction.toString());
			}
			else if (in.line < 0)
			{
				throw new IndexOutOfBoundsException("bad instruction address. Var: " + in.var.getAdjustedName() + "\r\nInstruction: " + in.instruction.toString());
			}
			else
			{
				final int result = this.line - in.line;

				if (result != 0)
				{
					return result;
				}
				else
				{
					return this.subline - in.subline;
				}
			}
		}
	}

	public static class StateSlice
	{
		// private final PriorityQueue<VarTouchy> writes;
		private final TreeMap<VarTouchy, VarTouchy> writes;
		private final VarTouchy[] lastWrite;
		private int lastWriteIndex;

		public StateSlice()
		{
			// this.writes = new PriorityQueue<VarTouchy>();
			this.writes = new TreeMap<VarTouchy, VarTouchy>();
			this.lastWrite = new VarTouchy[2];
			this.lastWriteIndex = 0;
		}

		public final void addWrite(VarTouchy in)
		{
			this.writes.put(in, in);
		}

		public boolean hasMoreWrites()
		{
			return !this.writes.isEmpty();
		}

		/*
		public VarTouchy getNextVarWrite()
		{
			final VarTouchy result = this.writes.poll();
		
			if (this.firstIsNewer)
			{
				this.lastWrite[1] = result;
				this.firstIsNewer = false;
			}
			else
			{
				this.lastWrite[0] = result;
				this.firstIsNewer = true;
			}
		
			return result;
		}
		*/

		private final VarTouchy getLastVarWrite()
		{
			return this.lastWrite[(this.lastWriteIndex ^= 1)];
		}

		public VarTouchy getLastVarWriteBefore(VarTouchy read)
		{
			return this.writes.floorKey(read);
		}
	}
}
