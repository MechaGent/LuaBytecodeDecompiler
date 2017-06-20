package LuaBytecodeDecompiler.Mk06_Working;

import java.util.EnumMap;
import java.util.Map.Entry;

import CharList.CharList;

import java.util.PriorityQueue;
import java.util.TreeMap;

import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.JumpTypes;

public abstract class IrInstruction implements Comparable<IrInstruction>
{
	protected static final String NullIrInstructionName = "NullIrInstruction";
	protected static final IrInstruction NullIrInstruction = new NullIrInstructionClass();
	protected static final HalfByteRadixMap<IrInstruction> LabelsMap = initLabelsMap(NullIrInstructionName, NullIrInstruction);
	protected static int nameCounter = 0;

	private IrInstruction directPrev;
	private final HalfByteRadixMap<JumpTypes> incomingJumps;
	private final EnumMap<JumpTypes, TreeMap<StepStage, IrInstruction>> reversedIncomingJumps;
	// private final int lineNum;
	protected final StepStage linePosData;
	private final IrCodes opCode;
	protected final String mangledName;
	// private final TreeMap<StepStage, IoJob> AllIos;

	private final IrInstruction[] next;

	private boolean hasExpired;
	private String expiredBecause;

	protected IrInstruction(StepStage inLinePosData, IrCodes inOpCode, int numNextSlots)
	{
		this.directPrev = NullIrInstruction;
		this.incomingJumps = new HalfByteRadixMap<JumpTypes>();
		this.reversedIncomingJumps = new EnumMap<JumpTypes, TreeMap<StepStage, IrInstruction>>(JumpTypes.class);
		// this.lineNum = inLineNum;
		this.linePosData = inLinePosData;
		this.opCode = inOpCode;
		this.mangledName = inOpCode.toString() + nameCounter++;
		// this.AllIos = new TreeMap<StepStage, IoJob>();
		this.next = initNewNextArray(numNextSlots);
		this.hasExpired = false;
		this.expiredBecause = "";
	}

	protected IrInstruction(StepStage inLinePosData, IrCodes inOpCode, IrInstruction[] inNext)
	{
		this.directPrev = NullIrInstruction;
		this.incomingJumps = new HalfByteRadixMap<JumpTypes>();
		this.reversedIncomingJumps = new EnumMap<JumpTypes, TreeMap<StepStage, IrInstruction>>(JumpTypes.class);
		// this.lineNum = inLineNum;
		this.linePosData = inLinePosData;
		this.opCode = inOpCode;
		this.mangledName = inOpCode.toString() + nameCounter++;
		// this.AllIos = new TreeMap<StepStage, IoJob>();
		this.next = inNext;
		this.hasExpired = false;
		this.expiredBecause = "";
	}

	private static HalfByteRadixMap<IrInstruction> initLabelsMap(String NullName, IrInstruction Nully)
	{
		final HalfByteRadixMap<IrInstruction> result = new HalfByteRadixMap<IrInstruction>();
		result.put(NullName, Nully);
		return result;
	}

	private static IrInstruction[] initNewNextArray(int numNextSlots)
	{
		final IrInstruction[] result = new IrInstruction[numNextSlots];

		for (int i = 0; i < numNextSlots; i++)
		{
			result[i] = NullIrInstruction;
		}

		return result;
	}

	public boolean isNull()
	{
		return false;
	}

	public boolean wasDirectlyPrevious(IrInstruction in)
	{
		return this.directPrev.equals(in.mangledName);
	}

	public void setNext(IrInstruction next, int index)
	{
		this.next[index] = next;
	}

	public void addIncomingJump(IrInstruction in, JumpTypes value)
	{
		this.incomingJumps.put(in.mangledName, value);

		TreeMap<StepStage, IrInstruction> tree = this.reversedIncomingJumps.get(value);

		if (tree == null)
		{
			tree = new TreeMap<StepStage, IrInstruction>();
			this.reversedIncomingJumps.put(value, tree);
		}

		tree.put(in.getLinePosData(), in);

		// System.out.println("added: " + in.mangledName);
	}

	public IrInstruction getFurthestNegJump()
	{
		TreeMap<StepStage, IrInstruction> tree = this.reversedIncomingJumps.get(JumpTypes.Jump_Neg);

		// System.out.println("result: ");

		if (tree != null)
		{
			final IrInstruction result = tree.firstEntry().getValue();

			System.out.println("result: " + result.mangledName);

			return result;
		}
		else
		{
			return null;
		}
	}

	public int getNumNexts()
	{
		return this.next.length;
	}

	public IrInstruction getNext()
	{
		// return LabelsMap.get(this.next[0]);
		return this.next[0];
	}

	public IrInstruction getNext(int index)
	{
		// return LabelsMap.get(this.next[index]);
		return this.next[index];
	}

	public IrInstruction getDirectPrev()
	{
		return this.directPrev;
	}

	public void setDirectPrev(IrInstruction inLinearPrev)
	{
		this.directPrev = inLinearPrev;
	}

	public static IrInstruction getNullIrInstruction()
	{
		return NullIrInstruction;
	}

	/*
	public int getLineNum()
	{
		return this.lineNum;
	}
	*/

	public StepStage getLinePosData()
	{
		return this.linePosData;
	}

	public String getMangledName()
	{
		return this.mangledName;
	}

	public IrCodes getOpCode()
	{
		return this.opCode;
	}

	@Override
	public String toString()
	{
		return this.getMangledName();
	}
	
	public boolean hasExpired()
	{
		return this.hasExpired;
	}
	
	public void setExpiredState(boolean isExpiredNow, String reason)
	{
		this.hasExpired = isExpiredNow;
		this.expiredBecause = reason;
	}
	
	public String getExpiredStateReason()
	{
		return this.expiredBecause;
	}

	public CharList toCharList(boolean showParsedByteCode, boolean showNaturalLanguage)
	{
		return this.toCharList(0, showParsedByteCode, showNaturalLanguage);
	}

	public CharList toCharList(int offset, boolean showParsedByteCode, boolean showNaturalLanguage)
	{
		final CharList result = new CharList();

		// result.addAsString(this.lineNum);
		result.add(this.linePosData.toCharList(), true);
		result.add('\t', 2);
		result.add(this.mangledName);

		if (showParsedByteCode)
		{
			// result.add(this.opCode.toString());
			result.add(" - {");
			result.add(this.toCharList_internal_getByteCodeVersion(), true);
			result.add('}');
			result.add('\t', 2);
		}

		if (showNaturalLanguage)
		{
			result.add(" || ");
			result.add(this.toCharList_internal_getNaturalLanguageVersion(), true);
		}

		if (this.directPrev.equals(NullIrInstructionName))
		{
			result.addNewLine();
			result.add('\t', 4);
			result.add("COMES_FROM: ");

			boolean isFirst = true;

			result.add('{');

			for (Entry<HalfByteArray, JumpTypes> entry : this.incomingJumps)
			{
				if (isFirst)
				{
					isFirst = false;
				}
				else
				{
					result.add(" | ");
				}

				result.add(entry.getKey().interpretAsChars(), true);
				result.add('(');
				result.add(entry.getValue().toString());
				result.add(')');
			}

			result.add('}');
		}

		// result.add(" || ");

		if ((this.next.length > 1 && !this.linePosData.isFirstStepStage()) || this.opCode == IrCodes.Jump_Unconditional)
		{
			result.addNewLine();
			result.add('\t', 4);
			result.add("GOES_TO: ");

			for (int i = 0; i < this.next.length; i++)
			{
				if (i != 0)
				{
					result.add(" | ");
				}

				result.add(this.next[i].mangledName);
			}
		}

		return result;
	}

	@Override
	public int compareTo(IrInstruction in)
	{
		return this.linePosData.compareTo(in.linePosData);
	}

	protected abstract CharList toCharList_internal_getByteCodeVersion();

	protected abstract CharList toCharList_internal_getNaturalLanguageVersion();
	
	public abstract void registerAllReadsAndWrites(VarCleaner in);
	
	public abstract void evalSwapStatus(VarAssignMap latestValues);
	public abstract void SwapOutVar(int varIndex, IrVar in);
	
	//public abstract IrVar getInlinedVersionOfStep(StepStage in);
	
	/**
	 * make swapOutStep be the time of reading, not writing
	 */
	public abstract void swapSteps(StepStage swapOutStep, IrVar swapIn);

	/*
	public IrInstruction getFirstCommonInstructionWith(IrInstruction in)
	{
		if (this != in)
		{
			final HalfByteRadixMap<IrInstruction> checked = new HalfByteRadixMap<IrInstruction>();
	
			final PriorityQueue<IrInstruction> inQueue = new PriorityQueue<IrInstruction>();
			final PriorityQueue<IrInstruction> thisQueue = new PriorityQueue<IrInstruction>();
			// final StepStage floor = StepStage.min(in.linePosData, this.linePosData);
			IrInstruction result = null;
	
			inQueue.add(in);
			thisQueue.add(this);
	
			IrInstruction check1;
			IrInstruction check2;
			boolean isWorking = true;
	
			while (!inQueue.isEmpty() && !thisQueue.isEmpty())
			{
				check1 = inQueue.poll();
	
				result = checked.get(check1.linePosData.getHalfByteHash());
	
				if (result != null)
				{
					return result;
				}
				else
				{
					checked.put(check1.linePosData.getHalfByteHash(), check1);
	
					for (IrInstruction nextChoice : check1.next)
					{
						if (nextChoice.linePosData.compareTo(check1.linePosData) >= 0) // if !(nextChoice.linePosData < check1.linePosData)
						{
							inQueue.add(nextChoice);
						}
					}
				}
	
				check2 = thisQueue.poll();
	
				result = checked.get(check2.linePosData.getHalfByteHash());
	
				if (result != null)
				{
					return result;
				}
				else
				{
					checked.put(check2.linePosData.getHalfByteHash(), check2);
	
					for (IrInstruction nextChoice : check2.next)
					{
						if (nextChoice.linePosData.compareTo(check2.linePosData) >= 0) // if !(nextChoice.linePosData < check2.linePosData)
						{
							thisQueue.add(nextChoice);
						}
					}
				}
			}
	
			return result;
		}
		else
		{
			return this;
		}
	}
	*/

	public IrInstruction getFirstCommonInstructionWith(IrInstruction in)
	{
		if (this == in)
		{
			return this;
		}

		final HalfByteRadixMap<IrInstruction> checked = new HalfByteRadixMap<IrInstruction>();

		final PriorityQueue<IrInstruction> queue = new PriorityQueue<IrInstruction>();
		final StepStage mustBeAfter = StepStage.max(in.linePosData, this.linePosData);
		queue.add(in);
		queue.add(this);

		while (!queue.isEmpty())
		{
			final IrInstruction check = queue.poll();
			
			//System.out.println("checking check for pathing: " + check.mangledName);
			
			final IrInstruction result = checked.get(check.linePosData.getHalfByteHash());

			if (result != null)
			{
				//if (result == check)
				//{
					if (mustBeAfter.compareTo(check.linePosData) <= 0)
					{
						return result;
					}
				//}
			}
			else
			{
				checked.put(check.linePosData.getHalfByteHash(), check);
			}

			for (IrInstruction nextChoice : check.next)
			{
				if (check.linePosData.compareTo(nextChoice.linePosData) <= 0 && !checked.containsKey(check.linePosData.getHalfByteHash()))
				{
					queue.add(nextChoice);
				}
			}
		}

		return null;
	}

	// protected abstract CharList toCharList_internal_codeVersion(int offset);

	// protected abstract void logAllReadsAndWrites();

	/*
	protected void logRead(int stepNum, int stageNum, String stageLabel, IrVar target)
	{
		this.logRead(new StepStage(stepNum, stageNum, stageLabel), target);
	}
	*
	
	
	protected void logRead(StepStage step, IrVar source)
	{
		this.AllIos.put(step, new IoJob(source));
	}
	
	protected void logRead(StepStage step, IrVar_Ref source)
	{
		this.AllIos.put(step, new IoJob(source));
		source.addRead(step, this);
	}
	
	/*
	protected void logWrite(int stepNum, int stageNum, String stageLabel, IrVar target)
	{
		this.logWrite(new StepStage(stepNum, stageNum, stageLabel), target);
	}
	*
	
	protected void logWrite(StepStage step, IrVar_Ref target, IrVar source)
	{
		this.AllIos.put(step, new IoJob(target, source));
		target.addWrite(step, this);
	}
	
	protected StepStage generateStepStage(int stageNum, String stageLabel)
	{
		return new StepStage(this.linePosData.getLineNum(), this.linePosData.getSubLineNum(), stageNum, stageLabel);
	}
	
	public IrVar getTargetValueFromStep(StepStage step)
	{
		return this.AllIos.get(step).getTarget();
	}
	*/

	private static class NullIrInstructionClass extends IrInstruction
	{
		private NullIrInstructionClass()
		{
			super(new StepStage(-1, -1, -1, "NullStep"), IrCodes.NullOp, 0);
		}

		@Override
		public boolean isNull()
		{
			return true;
		}

		@Override
		protected CharList toCharList_internal_getByteCodeVersion()
		{
			return new CharList("NULL INSTRUCTION");
		}

		@Override
		protected CharList toCharList_internal_getNaturalLanguageVersion()
		{
			return new CharList("NULL INSTRUCTION");
		}

		@Override
		public void registerAllReadsAndWrites(VarCleaner in)
		{
			
		}

		@Override
		public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
		{
			
		}

		@Override
		public void evalSwapStatus(VarAssignMap inLatestValues)
		{
			
		}

		@Override
		public void SwapOutVar(int inVarIndex, IrVar inIn)
		{
			// TODO Auto-generated method stub
			
		}
	}

	/*
	private static class IoJob
	{
		private final IrVar_Ref target;
		@SuppressWarnings("unused")
		private final IrVar source;
		
		public IoJob(IrVar inSource)
		{
			this.target = null;
			this.source = inSource;
		}
		
		public IoJob(IrVar_Ref inTarget, IrVar inSource)
		{
			this.target = inTarget;
			this.source = inSource;
		}
		
		public IrVar getTarget()
		{
			return this.target;
		}
		
		public boolean isRead()
		{
			return this.target == null;
		}
		
		@SuppressWarnings("unused")
		public boolean isWrite()
		{
			return !this.isRead();
		}
	}
	*/
}
