package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class EvalLoop_While extends IrInstruction
{
	private final Formula boolStatement;
	
	private EvalLoop_While(StepStage inLineNum, Formula inBoolStatement)
	{
		super(inLineNum, IrCodes.EvalLoop_While, 2);
		this.boolStatement = inBoolStatement;
	}
	
	public static EvalLoop_While getInstance(StepStage inLineNum, Formula inBoolStatement)
	{
		final EvalLoop_While result = new EvalLoop_While(inLineNum, inBoolStatement);
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
	
	public void setNext_StartOfLoop(IrInstruction in)
	{
		super.setNext(in, 0);
	}
	
	public void setNext_FirstInstructionAfterLoop(IrInstruction in)
	{
		super.setNext(in, 1);
	}
	
	public IrInstruction getNext_StartOfLoop()
	{
		return IrInstruction.LabelsMap.get(0);
	}
	
	public IrInstruction getNext_FirstInstructionAfterLoop()
	{
		return IrInstruction.LabelsMap.get(1);
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
	public void registerAllReadsAndWrites(VarCleaner in)
	{
		/*
		int currentStageNum = 2;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		
		in.addVarRead(this.boolStatement, this, new StepStage(lineNum, subLine, currentStageNum++, "reading boolStatement"));
		
		*/
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
