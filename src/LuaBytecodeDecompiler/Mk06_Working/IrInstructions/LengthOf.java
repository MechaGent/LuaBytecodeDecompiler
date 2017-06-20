package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;

public class LengthOf extends IrInstruction
{
	private IrVar target;
	private IrVar sourceLengthableThing;
	
	private LengthOf(StepStage inLineNum, IrVar inTarget, IrVar inSourceLengthableThing)
	{
		super(inLineNum, IrCodes.LengthOf, 1);
		this.target = inTarget;
		this.sourceLengthableThing = inSourceLengthableThing;
	}
	
	public static LengthOf getInstance(StepStage inLineNum, IrVar inTarget, IrVar inSourceLengthableThing)
	{
		final LengthOf result = new LengthOf(inLineNum, inTarget, inSourceLengthableThing);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar getTarget()
	{
		return this.target;
	}

	public IrVar getSourceLengthableThing()
	{
		return this.sourceLengthableThing;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);
		result.add(", source: ");
		result.add(this.sourceLengthableThing.toWordyVersion(), true);
		
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
		
		in.addVarRead(this.sourceLengthableThing, this, new StepStage(lineNum, subLine, currentStageNum++, "reading sourceLengthableThing"));
		in.addVarWrite(this.target, this, new StepStage(lineNum, subLine, currentStageNum++, "writing to target"));
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();
		
		switch(step)
		{
			case 2:
			{
				this.sourceLengthableThing = inSwapIn;
				break;
			}
			case 3:
			{
				if(StepStage.allowWriteSwaps())
				{
					this.target = inSwapIn;
					break;
				}
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		IrVar test = in.checkVarForReassign(this.sourceLengthableThing);
		
		if(test != this.sourceLengthableThing)
		{
			this.sourceLengthableThing = test;
		}
		
		final IrVar_Ref length = new IrVar_Ref("LengthOf:" + this.sourceLengthableThing.getLabel(), ScopeLevels.Temporary, false);
		in.registerNewWrite(this.target, length, this);
		*/
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.sourceLengthableThing, this, new StepStage(line, subline, count++, "reading from sourceLengthableThing"));
		
		final IrVar_Ref length = new IrVar_Ref("LengthOf:" + this.sourceLengthableThing.getLabel(), ScopeLevels.Temporary, true);
		in.registerNewWrite(this.target, length, this, new StepStage(line, subline, count++, "writing to target"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		switch(inVarIndex)
		{
			case 2:
			{
				this.sourceLengthableThing = inIn;
				break;
			}
			case 3:
			{
				this.target = inIn;
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}
}
