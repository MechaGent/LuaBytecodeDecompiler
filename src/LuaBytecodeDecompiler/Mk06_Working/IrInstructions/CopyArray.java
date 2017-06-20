package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class CopyArray extends IrInstruction
{
	private IrVar length;
	private IrVar targetArray;
	private IrVar targetArrayStartOffset;
	private IrVar sourceArray;
	private IrVar sourceArrayStartOffset;

	private CopyArray(StepStage inLineNum, IrVar inLength, IrVar inTargetArray, IrVar inTargetArrayStartOffset, IrVar inSourceArray, IrVar inSourceArrayStartOffset)
	{
		super(inLineNum, IrCodes.CopyArray, 1);
		this.length = inLength;
		this.targetArray = inTargetArray;
		this.targetArrayStartOffset = inTargetArrayStartOffset;
		this.sourceArray = inSourceArray;
		this.sourceArrayStartOffset = inSourceArrayStartOffset;
	}

	public static CopyArray getInstance(StepStage inLineNum, IrVar inLength, IrVar inTargetArray, IrVar inTargetArrayStartOffset, IrVar inSourceArray, IrVar inSourceArrayStartOffset)
	{
		final CopyArray result = new CopyArray(inLineNum, inLength, inTargetArray, inTargetArrayStartOffset, inSourceArray, inSourceArrayStartOffset);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		// result.logAllReadsAndWrites();
		return result;
	}

	public IrVar getLength()
	{
		return this.length;
	}

	public IrVar getTargetArray()
	{
		return this.targetArray;
	}

	public IrVar getTargetArrayStartOffset()
	{
		return this.targetArrayStartOffset;
	}

	public IrVar getSourceArray()
	{
		return this.sourceArray;
	}

	public IrVar getSourceArrayStartOffset()
	{
		return this.sourceArrayStartOffset;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("length: ");
		result.add(this.length.toWordyVersion(), true);
		result.add(", targetArray: ");
		result.add(this.targetArray.toWordyVersion(), true);
		result.add(", targetArrayStartOffset: ");
		result.add(this.targetArrayStartOffset.toWordyVersion(), true);
		result.add(", sourceArray: ");
		result.add(this.sourceArray.toWordyVersion(), true);
		result.add(", sourceArrayStartOffset: ");
		result.add(this.sourceArrayStartOffset.toWordyVersion(), true);

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

		in.addVarRead(this.sourceArray, this, new StepStage(lineNum, subLine, currentStageNum++, "reading source array"));
		in.addVarWrite(this.targetArray, this, new StepStage(lineNum, subLine, currentStageNum++, "writing source array"), this.targetArray);
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();

		switch (step)
		{
			case 3:
			{
				this.sourceArray = inSwapIn;
				break;
			}
			case 4:
			{
				if (StepStage.allowWriteSwaps())
				{
					this.targetArray = inSwapIn;
				}
				break;
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
		IrVar test = in.checkVarForReassign(this.length);
		
		if (test != this.length)
		{
			this.length = test;
		}
		
		test = in.checkVarForReassign(this.targetArray);
		
		if (test != this.targetArray)
		{
			this.targetArray = test;
		}
		
		test = in.checkVarForReassign(this.targetArrayStartOffset);
		
		if (test != this.targetArrayStartOffset)
		{
			this.targetArrayStartOffset = test;
		}
		
		test = in.checkVarForReassign(this.sourceArray);
		
		if (test != this.sourceArray)
		{
			this.sourceArray = test;
		}
		
		test = in.checkVarForReassign(this.sourceArrayStartOffset);
		
		if(test != this.sourceArrayStartOffset)
		{
			this.sourceArrayStartOffset = test;
		}
		*/

		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;

		in.registerNewRead(this.length, this, new StepStage(line, subline, count++, "reading length"));
		in.registerNewRead(this.sourceArray, this, new StepStage(line, subline, count++, "reading source array"));
		in.registerNewRead(this.sourceArrayStartOffset, this, new StepStage(line, subline, count++, "reading sourceArrayStartOffset"));
		in.registerNewRead(this.targetArray, this, new StepStage(line, subline, count++, "reading targetArray"));
		in.registerNewRead(this.targetArrayStartOffset, this, new StepStage(line, subline, count++, "reading targetArrayStartOffset"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		switch ((inVarIndex -= 2))
		{
			case 0:
			{
				this.length = inIn;
			}
			case 1:
			{
				this.sourceArray = inIn;
			}
			case 2:
			{
				this.sourceArrayStartOffset = inIn;
			}
			case 3:
			{
				this.targetArray = inIn;
			}
			case 4:
			{
				this.targetArrayStartOffset = inIn;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}

	/*
	@Override
	protected void logAllReadsAndWrites()
	{
		/*
		 * for(length), read sourceArray, then write to targetArray
		 * 
		 * implementing as 'read sourceArray, then write targetArray' due to being unable to parse length at compiletime
		 *
		
		int currentStageNum = 1;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		StepStage currentStage = new StepStage(lineNum, subLine, currentStageNum++, "reading source registers");
		this.logRead(currentStage, this.sourceArray);
		currentStage = new StepStage(lineNum, subLine, currentStageNum++, "writing source registers");
		this.logWrite(currentStage, this.targetArray);
	}
	*/
}
