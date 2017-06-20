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

public class CreateNewMap extends IrInstruction
{
	private IrVar target;

	private CreateNewMap(StepStage inLineNum, IrVar inTarget)
	{
		super(inLineNum, IrCodes.CreateNewMap, 1);
		this.target = inTarget;
	}
	
	public static CreateNewMap getInstance(StepStage inLineNum, IrVar inTarget)
	{
		final CreateNewMap result = new CreateNewMap(inLineNum, inTarget);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar getTarget()
	{
		return this.target;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);
		
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
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		
		in.addVarWrite(this.target, this, new StepStage(lineNum, subLine, 2, "writing to target"), this.target);
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		if(StepStage.allowWriteSwaps() && inSwapOutStep.getStepNum() == 2)
		{
			this.target = inSwapIn;
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewWrite(this.target, new IrVar_Ref("new KeyValueMap()", ScopeLevels.Temporary, true), this, new StepStage(line, subline, count++, ""));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		if(inVarIndex == 2)
		{
			this.target = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
}
