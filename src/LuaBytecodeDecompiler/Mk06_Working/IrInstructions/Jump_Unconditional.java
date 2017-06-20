package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class Jump_Unconditional extends IrInstruction
{
	protected Jump_Unconditional(StepStage inLineNum)
	{
		super(inLineNum, IrCodes.Jump_Unconditional, 1);
	}
	
	public static Jump_Unconditional getInstance(StepStage inLineNum, IrInstruction inJumpTo)
	{
		final Jump_Unconditional result = new Jump_Unconditional(inLineNum);
		result.setNext(inJumpTo, 0);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("jumpTarget: ");
		result.add(this.getNext().getMangledName());
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		// TODO Auto-generated method stub
		
	}
}
