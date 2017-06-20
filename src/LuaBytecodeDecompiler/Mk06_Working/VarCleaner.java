package LuaBytecodeDecompiler.Mk06_Working;

import java.util.PriorityQueue;
import java.util.TreeMap;

import HalfByteRadixMap.HalfByteRadixMap;

public class VarCleaner
{
	private final PriorityQueue<IoAction> reads;
	private final HalfByteRadixMap<VarIoTimeline> history;
	private final HalfByteRadixMap<IntContainer> numReadsPerWrite;
	
	public VarCleaner()
	{
		this.reads = new PriorityQueue<IoAction>();
		this.history = new HalfByteRadixMap<VarIoTimeline>();
		this.numReadsPerWrite = new HalfByteRadixMap<IntContainer>();
	}
	
	public void addVarRead(IrVar inVar, IrInstruction inInstruction, StepStage step)
	{
		IntContainer test = this.numReadsPerWrite.get(inVar.label);
		
		if(test == null)
		{
			test = new IntContainer();
			this.numReadsPerWrite.put(inVar.label, test);
		}
		
		if(test.isGoodForReplacement())
		{
			//System.out.println("read logged: " + step.toString() + ": " + inVar.label);
			this.reads.add(new IoAction(step, inInstruction, inVar));
			test.incrementCargo();
		}
	}
	
	public void addVarWrite(IrVar inVarWrite, IrInstruction inInstruction, StepStage step)
	{
		this.addVarWrite(inVarWrite, inInstruction, step, inVarWrite);
	}
	
	public void addVarWrite(IrVar inVarWrite, IrInstruction inInstruction, StepStage step, IrVar inVarSetTo)
	{
		VarIoTimeline dest = this.history.get(inVarWrite.label);
		
		if(dest == null)
		{
			dest = new VarIoTimeline();
			this.history.put(inVarWrite.label, dest);
		}
		
		//System.out.println("write logged: " + step.toString() + ": " + inVar.label);
		dest.addWrite(new IoAction(step, inInstruction, inVarSetTo));
		
		final IntContainer test = this.numReadsPerWrite.get(inVarWrite.label);
		
		if(test != null)
		{
			test.resetCargo();
		}
	}
	
	public VarIoTimeline getWriteHistory(IoAction in)
	{
		return this.history.get(in.getVar().label);
	}
	
	public boolean hasMoreReads()
	{
		return !this.reads.isEmpty();
	}
	
	public IoAction getNextRead()
	{
		return this.reads.poll();
	}
	
	public boolean hasMoreWrites(IrVar in)
	{
		final VarIoTimeline result = this.history.get(in.label);
		
		if(result != null)
		{
			return result.hasMoreWrites();
		}
		else
		{
			return false;
		}
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
	
	public static class IoAction implements Comparable<IoAction>
	{
		private final StepStage coord;
		private final IrInstruction line;
		private final IrVar var;
		
		public IoAction(StepStage inCoord, IrInstruction inLine, IrVar inVar)
		{
			this.coord = inCoord;
			this.line = inLine;
			this.var = inVar;
		}

		public StepStage getCoord()
		{
			return this.coord;
		}

		public IrInstruction getLine()
		{
			return this.line;
		}

		public IrVar getVar()
		{
			return this.var;
		}

		@Override
		public int compareTo(IoAction inO)
		{
			return this.coord.compareTo(inO.coord);
		}
	}
	
	public static class VarIoTimeline
	{
		private final TreeMap<IoAction, IoAction> writes;
		
		public VarIoTimeline()
		{
			this.writes = new TreeMap<IoAction, IoAction>();
		}
		
		public void addWrite(IoAction in)
		{
			this.writes.put(in, in);
		}
		
		public boolean hasMoreWrites()
		{
			return !this.writes.isEmpty();
		}
		
		public IoAction getLastWriteBefore(IoAction in)
		{
			return this.writes.floorKey(in);
		}
	}
}
