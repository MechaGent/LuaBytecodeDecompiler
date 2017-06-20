package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class NoOp extends IrInstruction
{
	protected NoOp(StepStage inLineNum)
	{
		super(inLineNum, IrCodes.NoOp, 1);
	}
	
	public static NoOp getInstance(StepStage lineNum)
	{
		final NoOp result = new NoOp(lineNum);
		LabelsMap.put(result.mangledName, result);
		return result;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("No Op");
		
		return result;
	}

	@Override
	protected CharList toCharList_internal_getNaturalLanguageVersion()
	{
		final CharList result = new CharList();

		return result;
	}

	@Override
	public void registerAllReadsAndWrites(VarCleaner inIn)
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
	}
}
