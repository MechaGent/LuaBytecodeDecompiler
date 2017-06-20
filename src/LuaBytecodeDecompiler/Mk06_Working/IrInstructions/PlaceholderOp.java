package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class PlaceholderOp extends IrInstruction
{
	private final String placeholder;

	private PlaceholderOp(StepStage inLinePosData, String inPlaceholder)
	{
		super(inLinePosData, IrCodes.PlaceHolderOp, 1);
		this.placeholder = inPlaceholder;
	}
	
	public static PlaceholderOp getInstance(StepStage inLinePosData, String inPlaceholder)
	{
		return new PlaceholderOp(inLinePosData, inPlaceholder);
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("<placeholder> ");
		result.add(this.placeholder);
		
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
