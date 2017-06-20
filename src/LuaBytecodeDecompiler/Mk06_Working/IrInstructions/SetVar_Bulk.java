package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class SetVar_Bulk extends IrInstruction
{
	private final IrVar[] targetArray;
	private final int startIndex;
	private final int endIndex;
	private IrVar source;
	
	protected SetVar_Bulk(StepStage inLineNum, IrVar[] inTargetArray, int inStartIndex, int inEndIndex, IrVar inSource)
	{
		super(inLineNum, IrCodes.SetVar_Bulk, 1);
		this.targetArray = inTargetArray;
		this.startIndex = inStartIndex;
		this.endIndex = inEndIndex;
		this.source = inSource;
	}
	
	public static SetVar_Bulk getInstance(StepStage inLineNum, IrVar[] inTargetArray, int inStartIndex, int inEndIndex, IrVar inSource)
	{
		final SetVar_Bulk result = new SetVar_Bulk(inLineNum, inTargetArray, inStartIndex, inEndIndex, inSource);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar[] getTargetArray()
	{
		return this.targetArray;
	}

	public int getStartIndex()
	{
		return this.startIndex;
	}

	public int getEndIndex()
	{
		return this.endIndex;
	}

	public IrVar getSource()
	{
		return this.source;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("startIndex: ");
		result.addAsString(this.startIndex);
		result.add(", endIndex: ");
		result.addAsString(this.endIndex);
		result.add(", source: ");
		result.add(this.source.toWordyVersion(), true);
		
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
		
		in.addVarRead(this.source, this, new StepStage(lineNum, subLine, currentStageNum++, "reading source"));
		
		for(int i = this.startIndex; i <= this.endIndex; i++)
		{
			in.addVarWrite(this.targetArray[i], this, new StepStage(lineNum, subLine, currentStageNum++, "writing to table"), this.source);
		}
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();
		
		if(step == 2)
		{
			this.source = inSwapIn;
		}
		else
		{
			if(StepStage.allowWriteSwaps() && step - 3 < this.endIndex - this.startIndex + 1)
			{
				this.targetArray[this.startIndex + step - 3] = inSwapIn;
			}
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		IrVar test = in.checkVarForReassign(this.source);
		
		if(test != this.source)
		{
			this.source = test;
		}
		
		for(int i = this.startIndex; i <= this.endIndex; i++)
		{
			in.registerNewWrite(this.targetArray[i], this.source, this);
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.source, this, new StepStage(line, subline, count++, "reading from source"));
		
		for(int i = this.startIndex; i <= this.endIndex; i++)
		{
			in.registerNewWrite(this.targetArray[i], this.source, this, new StepStage(line, subline, count++, "reading from source"));
		}
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		if((inVarIndex -= 2) == 0)
		{
			this.source = inIn;
		}
		else if(--inVarIndex >= this.startIndex && inVarIndex <= this.endIndex)
		{
			this.targetArray[inVarIndex] = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
}
