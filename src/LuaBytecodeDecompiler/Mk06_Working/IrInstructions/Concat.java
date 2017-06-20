package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class Concat extends IrInstruction
{
	@SuppressWarnings("unused")
	private static final int inlineLimit = 5;

	private IrVar target;
	private final IrVar[] sourceTable;

	private Concat(StepStage inLineNum, IrVar inTarget, IrVar[] inSourceTable)
	{
		super(inLineNum, IrCodes.Concat, 1);
		this.target = inTarget;
		this.sourceTable = inSourceTable;
		
		/*
		for(int i = 0; i < inSourceTable.length; i++)
		{
			if(inSourceTable[i] == null)
			{
				throw new NullPointerException(i + " of " + inSourceTable.length);
			}
		}
		*/
	}

	public static Concat getInstance(StepStage inLineNum, IrVar inTarget, IrVar[] inSourceTable)
	{
		final Concat result = new Concat(inLineNum, inTarget, inSourceTable);
		IrInstruction.LabelsMap.put(result.mangledName, result);

		// result.logAllReadsAndWrites();

		return result;
	}

	public IrVar getTarget()
	{
		return this.target;
	}

	public IrVar[] getSourceTable()
	{
		return this.sourceTable;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);

		if (this.sourceTable.length > 0)
		{
			result.add(", startVar: ");
			result.add(this.sourceTable[0].toWordyVersion(), true);
			result.add(", endVar: ");
			result.add(this.sourceTable[this.sourceTable.length - 1].toWordyVersion(), true);
		}
		else
		{
			result.add("<empty array>");
		}

		return result;
	}

	@Override
	protected CharList toCharList_internal_getNaturalLanguageVersion()
	{
		final CharList result = new CharList();

		return result;
	}

	@Override
	public void registerAllReadsAndWrites(VarCleaner in)
	{
		int currentStageNum = 2;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();

		for (int i = 0; i <= this.sourceTable.length; i++)
		{
			in.addVarRead(this.sourceTable[i], this, new StepStage(lineNum, subLine, currentStageNum++, "reading source register_" + i));
		}

		in.addVarWrite(this.target, this, new StepStage(lineNum, subLine, currentStageNum++, "writing to target register"), this.target);
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();
		final int test = step - 2;

		if (test <= 0)
		{
			// this.sourceTable[step - 2 + this.startIndex] = inSwapIn;
			this.sourceTable[step - 2].logWriteToAtTime(inSwapOutStep, inSwapIn);

		}
		else if (test == 1 && StepStage.allowWriteSwaps())
		{
			// this.target = inSwapIn;
			this.target.logWriteToAtTime(inSwapOutStep, inSwapIn);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		for(int i = 0; i < this.sourceTable.length; i++)
		{
			final IrVar swap = in.checkVarForReassign(this.sourceTable[i]);
			
			if(swap != this.sourceTable[i])
			{
				this.sourceTable[i] = swap;
			}
		}
		
		final IrVar swap = in.checkWriteTargetVarForPartialReassign(this.target);
		
		if(swap != this.target)
		{
			this.target = swap;
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		for(int i = 0; i < this.sourceTable.length; i++)
		{
			in.registerNewRead(this.sourceTable[i], this, new StepStage(line, subline, count++, "reading from table"));
		}
		
		in.registerNewWrite(this.target, null, this, new StepStage(line, subline, count++, "placeholder write"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		if((inVarIndex -= 2) < this.sourceTable.length)
		{
			this.sourceTable[inVarIndex] = inIn;
		}
		else if((inVarIndex -= this.sourceTable.length) == 0)
		{
			this.target = inIn;
		}
	}

	/*
	@Override
	protected void logAllReadsAndWrites()
	{
		/*
		 * all sourceRegisters are read
		 * target is written
		 *
		
		int currentStageNum = 1;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		
		for(int i = this.startIndex; i <= this.endIndex; i++)
		{
			final StepStage stage = new StepStage(lineNum, subLine, currentStageNum++, "reading source register_" + (i - this.startIndex));
			this.logRead(stage, this.sourceTable[i]);
		}
		
		final StepStage stage = new StepStage(lineNum, subLine, currentStageNum++, "writing to target register");
		this.logWrite(stage, this.target);
	}
	*/
}
