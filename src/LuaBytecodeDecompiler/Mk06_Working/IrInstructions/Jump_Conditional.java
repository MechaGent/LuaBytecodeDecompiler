package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class Jump_Conditional extends IrInstruction
{
	private final Formula boolStatement;
	//private IrInstruction firstInstructionAfter;

	private Jump_Conditional(StepStage inLineNum, Formula inBoolStatement)
	{
		super(inLineNum, IrCodes.Jump_Conditional, 2);
		this.boolStatement = inBoolStatement;
		//this.firstInstructionAfter = IrInstruction.NullIrInstruction;
	}

	public static Jump_Conditional getInstance(StepStage inLineNum, Formula inBoolStatement, IrInstruction inJumpIfTrue, IrInstruction inJumpIfFalse)
	{
		final Jump_Conditional result = new Jump_Conditional(inLineNum, inBoolStatement);
		result.setNext(inJumpIfTrue, 0);
		result.setNext(inJumpIfFalse, 1);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public Formula getBoolStatement()
	{
		return this.boolStatement;
	}
	
	public IrInstruction getFirstCommonInstruction()
	{
		return this.getNext(0).getFirstCommonInstructionWith(this.getNext(1));
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("boolStatement: ");
		result.add(this.boolStatement.toCharList(), true);
		result.add(", jumpIfTrue: ");
		result.add(((JumpLabel) this.getNext(0)).getLabel());
		result.add(", jumpIfFalse: ");
		result.add(((JumpLabel) this.getNext(1)).getLabel());
		
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
		// TODO Auto-generated method stub
		int dummy = 0;
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		// TODO Auto-generated method stub
		
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
