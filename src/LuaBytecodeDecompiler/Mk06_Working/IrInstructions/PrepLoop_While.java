package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class PrepLoop_While extends IrInstruction
{
	private final Formula boolStatement;

	private PrepLoop_While(StepStage inLineNum, Formula inBoolStatement)
	{
		super(inLineNum, IrCodes.PrepLoop_While, 2);
		this.boolStatement = inBoolStatement;
	}

	public static PrepLoop_While getInstance(StepStage inLineNum, Formula inBoolStatement)
	{
		final PrepLoop_While result = new PrepLoop_While(inLineNum, inBoolStatement);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public Formula getBoolStatement()
	{
		return this.boolStatement;
	}

	@Override
	public void setNext(IrInstruction in, int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IrInstruction getNext()
	{
		throw new UnsupportedOperationException();
	}

	public void setNext_EndOfLoop(IrInstruction in)
	{
		super.setNext(in, 1);
	}

	public void setNext_FirstInstructionInLoop(IrInstruction in)
	{
		super.setNext(in, 0);
	}

	public IrInstruction getNext_EndOfLoop()
	{
		return IrInstruction.LabelsMap.get(1);
	}

	public IrInstruction getNext_FirstInstructionInLoop()
	{
		return IrInstruction.LabelsMap.get(0);
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("test: {");

		result.add(this.boolStatement.toCharList(), true);

		result.add('}');

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
		
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		
	}
}
