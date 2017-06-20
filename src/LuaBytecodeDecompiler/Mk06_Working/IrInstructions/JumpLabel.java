package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class JumpLabel extends IrInstruction
{
	private static int instanceCounter = 0;
	
	private final String label;

	private JumpLabel(StepStage inLineNum, String inLabel)
	{
		super(inLineNum, IrCodes.Jump_Label, 1);
		this.label = inLabel;
	}
	
	public static JumpLabel getInstance(StepStage inLineNum)
	{
		return getInstance(inLineNum, "JumpLabel_" + instanceCounter++);
	}
	
	public static JumpLabel getInstance(StepStage inLineNum, String inLabel)
	{
		final JumpLabel result = new JumpLabel(inLineNum, inLabel);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public String getLabel()
	{
		return this.label;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add(this.label);
		
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
		// TODO Auto-generated method stub
		
	}
}
