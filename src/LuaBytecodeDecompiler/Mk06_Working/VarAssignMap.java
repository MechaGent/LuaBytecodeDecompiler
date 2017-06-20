package LuaBytecodeDecompiler.Mk06_Working;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Indexed;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Mapped;
import SingleLinkedList.Mk01.SingleLinkedList;

public class VarAssignMap
{
	private static final boolean turnOnFolding = true;

	private final HalfByteRadixMap<VarWriteData> assigns;

	private final HalfByteRadixMap<TreeMap<StepStage, WriteBlock>> fancyAssigns;

	public VarAssignMap()
	{
		this.assigns = new HalfByteRadixMap<VarWriteData>();
		this.fancyAssigns = new HalfByteRadixMap<TreeMap<StepStage, WriteBlock>>();
	}

	public void registerNewWrite(IrVar target, IrVar source, IrInstruction in, StepStage writeTime)
	{
		switch (target.getType())
		{
			case Indexed:
			{
				final IrVar_Indexed cast = (IrVar_Indexed) target;
				this.registerNewRead(cast.getSourceTable().getLabel(), target, in, writeTime);

				break;
			}
			case Mapped:
			{
				final IrVar_Mapped cast = (IrVar_Mapped) target;
				this.registerNewRead(cast.getSourceMap().getLabel(), target, in, writeTime);
				break;
			}
			default:
			{
				break;
			}
		}

		//this.assigns.put(target.getLabel(), new VarWriteData(source, in));

		TreeMap<StepStage, WriteBlock> writes = this.fancyAssigns.get(target.getLabel());

		if (writes == null)
		{
			writes = new TreeMap<StepStage, WriteBlock>();
			this.fancyAssigns.put(target.getLabel(), writes);
			writes.put(writeTime, new WriteBlock(target, source, writeTime, in, null));
		}
		else
		{
			final Entry<StepStage, WriteBlock> entry = writes.floorEntry(writeTime);
			
			writes.put(writeTime, new WriteBlock(target, source, writeTime, in, ((entry != null)? entry.getValue() : null)));
		}
	}

	public void registerNewRead(IrVar read, IrInstruction in, StepStage readTime)
	{
		switch (read.getType())
		{
			case Indexed:
			{
				// System.out.println("wants to register indexed var!");
				final IrVar_Indexed cast = (IrVar_Indexed) read;
				this.registerNewRead(cast.getSourceTable().getLabel(), read, in, readTime);

				break;
			}
			case Mapped:
			{
				// System.out.println("wants to register mapped var!");
				final IrVar_Mapped cast = (IrVar_Mapped) read;
				this.registerNewRead(cast.getSourceMap().getLabel(), read, in, readTime);

				break;
			}
			default:
			{
				this.registerNewRead(read.getLabel(), read, in, readTime);
				break;
			}
		}
	}

	private void registerNewRead(String label, IrVar read, IrInstruction in, StepStage readTime)
	{
		TreeMap<StepStage, WriteBlock> writes = this.fancyAssigns.get(label);

		if (writes == null)
		{
			writes = new TreeMap<StepStage, WriteBlock>();
			this.fancyAssigns.put(label, writes);
		}

		Entry<StepStage, WriteBlock> entry = writes.floorEntry(readTime);

		if (entry != null)
		{
			WriteBlock block = entry.getValue();

			if (block != null)
			{
				block.addNewRead(read, in, readTime);
			}
		}
	}

	/*
	public IrVar checkVarForReassign(IrVar in, StepStage time)
	{
		final TreeMap<StepStage, WriteBlock> tree = this.fancyAssigns.get(in.getLabel());
		
		if(tree != null)
		{
			final Entry<StepStage, WriteBlock> entry = tree.floorEntry(time);
			
			if(entry != null)
			{
				final WriteBlock block = entry.getValue();
				
				if(block != null)
				{
					
				}
			}
		}
		
		return in;
	}
	
	public IrVar checkWriteTargetVarForPartialReassign(IrVar in, StepStage time)
	{
		//don't forget to handle indexed and mapped
	}
	*/

	public void foldAll()
	{
		for (Entry<HalfByteArray, TreeMap<StepStage, WriteBlock>> entry : this.fancyAssigns)
		{
			// System.out.println("here");

			for (Entry<StepStage, WriteBlock> treeEntry : entry.getValue().entrySet())
			{
				final WriteBlock blocky = treeEntry.getValue();

				switch (blocky.writeInstruction.getOpCode())
				{
					case Concat:
					case EvalLoop_For:
					case EvalLoop_While:
					case PrepLoop_For:
					case PrepLoop_While:
					case CallMethod:
					{
						break;
					}
					default:
					{
						if (blocky.reads.getSize() != 0)
						{
							blocky.writeInstruction.setExpiredState(true, "inlined out");
						}
						break;
					}
				}

				// System.out.println("here, with block: " + blocky.toString());

				for (VarReadData data : blocky.reads)
				{
					this.foldCurrentVar(blocky, data);
				}
			}
		}
	}

	private void foldCurrentVar(WriteBlock block, VarReadData data)
	{
		if (data.var != null)
		{
			if (block.writeVar != null)
			{
				if (data.var == block.writeTarget)
				{
					data.instruction.SwapOutVar(data.time.getStepNum(), block.writeVar);
				}
				else
				{
					switch (data.var.getType())
					{
						case Indexed:
						{
							final IrVar_Indexed cast = (IrVar_Indexed) data.var;

							data.instruction.SwapOutVar(data.time.getStepNum(), new IrVar_Indexed(cast.getScope(), block.writeVar, cast.getSourceIndex()));
							break;
						}
						case Mapped:
						{
							final IrVar_Mapped cast = (IrVar_Mapped) data.var;

							data.instruction.SwapOutVar(data.time.getStepNum(), new IrVar_Mapped(cast.getScope(), block.writeVar, cast.getSourceKey()));
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}
				}
			}
			if (data.var == block.writeTarget && block.writeVar != null)
			{
				// System.out.println("swapBlock: " + block.toString());
				data.instruction.SwapOutVar(data.time.getStepNum(), block.writeVar);
			}
		}
	}

	/**
	 * note to self: maybe come back later, make this cyclic, see what happens
	 * 
	 * @param in
	 * @return
	 */
	public IrVar checkVarForReassign(IrVar in)
	{
		return this.checkVarForReassign_internal(in, true);
	}

	private IrVar checkVarForReassign_internal(IrVar in, boolean shouldExpireWriter)
	{
		if (turnOnFolding)
		{
			IrVar result;

			switch (in.getType())
			{
				case Indexed:
				{
					result = this.checkVarForReassign((IrVar_Indexed) in);
					break;
				}
				case Mapped:
				{
					result = this.checkVarForReassign((IrVar_Mapped) in);
					break;
				}
				default:
				{
					final VarWriteData temp = this.assigns.get(in.getLabel());

					if (temp != null)// && !temp.isFromMethodReturn())
					{
						// this.assigns.put(in.getLabel(), temp);
						result = temp.var;

						if (!temp.var.getLabel().equals("new KeyValueMap()"))
						{
							temp.instruction.setExpiredState(shouldExpireWriter, "inlined out");
						}
					}
					else
					{
						result = in;
					}

					break;
				}
			}

			// System.out.println("checked: " + in.getLabel() + ", with result: " + result.getLabel());

			return result;
		}
		else
		{
			return in;
		}
	}

	private IrVar_Indexed checkVarForReassign(IrVar_Indexed in)
	{
		final IrVar table = this.checkVarForReassign(in.getSourceTable());

		if (table != null)
		{
			final IrVar_Indexed next = new IrVar_Indexed(in.getLabel(), in.getScope(), table, in.getSourceIndex());
			// this.assigns.put(in.getLabel(), next);
			return next;
		}
		else
		{
			return in;
		}
	}

	private IrVar_Mapped checkVarForReassign(IrVar_Mapped in)
	{
		final IrVar table = this.checkVarForReassign(in.getSourceMap());

		if (table != null)
		{
			if (!table.getLabel().equals("new KeyValueMap()"))
			{
				final IrVar_Mapped next = new IrVar_Mapped(in.getLabel(), in.getScope(), table, in.getSourceKey());
				// this.assigns.put(in.getLabel(), next);
				return next;
			}
			else
			{

				return in;
			}
		}
		else
		{
			return in;
		}
	}

	public IrVar checkWriteTargetVarForPartialReassign(IrVar in)
	{
		if (turnOnFolding)
		{
			IrVar result;

			switch (in.getType())
			{
				case Indexed:
				{
					result = this.checkVarForReassign((IrVar_Indexed) in);
					break;
				}
				case Mapped:
				{
					result = this.checkVarForReassign((IrVar_Mapped) in);
					break;
				}
				default:
				{
					result = in;

					break;
				}
			}

			return result;
		}
		else
		{
			return in;
		}
	}

	private static class VarWriteData
	{
		private final IrVar var;
		private final IrInstruction instruction;

		public VarWriteData(IrVar inVar, IrInstruction inInstruction)
		{
			this.var = inVar;
			this.instruction = inInstruction;
		}
	}

	private static class VarReadData
	{
		private final IrVar var;
		private final IrInstruction instruction;
		private final StepStage time;

		public VarReadData(IrVar inVar, IrInstruction inInstruction, StepStage inTime)
		{
			this.var = inVar;
			this.instruction = inInstruction;
			this.time = inTime;
		}

		@Override
		public String toString()
		{
			return "VarReadData [var=" + this.var + ", instruction=" + this.instruction + ", time=" + this.time + "]";
		}
	}

	private static class WriteBlock
	{
		private final IrVar writeTarget;
		private final IrVar writeVar;
		private final StepStage writeTime;
		private final IrInstruction writeInstruction;
		private final WriteBlock prevBlock;
		private final SingleLinkedList<VarReadData> reads;

		public WriteBlock(IrVar inWriteTarget, IrVar inWriteVar, StepStage inWriteTime, IrInstruction inWriteInstruction, WriteBlock inPrevBlock)
		{
			this.writeTarget = inWriteTarget;
			this.writeVar = inWriteVar;
			this.writeTime = inWriteTime;
			this.writeInstruction = inWriteInstruction;
			this.prevBlock = inPrevBlock;
			this.reads = new SingleLinkedList<VarReadData>();
		}

		public boolean isSingleWriteSingleRead()
		{
			return this.reads.getSize() == 1;
		}

		public void addNewRead(IrVar inVar, IrInstruction inInstruction, StepStage inTime)
		{
			this.reads.add(new VarReadData(inVar, inInstruction, inTime));
		}

		@Override
		public String toString()
		{
			return "WriteBlock [writeTarget=" + this.writeTarget.getLabel() + ", writeVar=" + ((this.writeVar != null) ? this.writeVar.getLabel() : "null")
			// + ", writeTime=" + this.writeTime + ", writeInstruction=" + this.writeInstruction + ", reads=" + this.reads
					+ "]";
		}
	}
}
