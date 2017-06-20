package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class Stasisify extends IrInstruction
{
	private final IrVar[] arrayToCheck;
	private final int firstVarIndex;

	private Stasisify(StepStage inLineNum, IrVar[] inArrayToCheck, int inFirstVarIndex)
	{
		super(inLineNum, IrCodes.Stasisify, 1);
		this.arrayToCheck = inArrayToCheck;
		this.firstVarIndex = inFirstVarIndex;
	}

	public static Stasisify getInstance(StepStage inLineNum, IrVar[] inArrayToCheck, int inFirstVarIndex)
	{
		final Stasisify result = new Stasisify(inLineNum, inArrayToCheck, inFirstVarIndex);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar[] getArrayToCheck()
	{
		return this.arrayToCheck;
	}

	public int getFirstVarIndex()
	{
		return this.firstVarIndex;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("firstVarIndex: ");
		result.addAsString(this.firstVarIndex);
		
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
		
		for(int i = this.firstVarIndex; i < this.arrayToCheck.length; i++)
		{
			in.addVarRead(this.arrayToCheck[i], this, new StepStage(lineNum, subLine, currentStageNum++, "reading variable"));
			in.addVarWrite(this.arrayToCheck[i], this, new StepStage(lineNum, subLine, currentStageNum++, "writing variable (potentially)"));
		}
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();
		final int index = step / 2;
		
		if((step & 1) == 0)
		{
			//is read
			this.arrayToCheck[index] = inSwapIn;
		}
		else
		{
			//is write
			if(StepStage.allowWriteSwaps())
			{
				this.arrayToCheck[index] = inSwapIn;
			}
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap inLatestValues)
	{
		
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		
	}
}
