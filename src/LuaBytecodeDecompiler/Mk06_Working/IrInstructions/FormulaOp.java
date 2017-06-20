package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import ExpressionParser.Formula;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Formula;

public class FormulaOp extends IrInstruction
{
	private IrVar target;
	private final Formula formula;

	public FormulaOp(StepStage inLineNum, IrVar inTarget, Formula inFormula)
	{
		super(inLineNum, IrCodes.FormulaOp, 1);
		this.target = inTarget;
		this.formula = inFormula;
	}

	public IrVar getTarget()
	{
		return this.target;
	}
	
	public Formula getFormula()
	{
		return this.formula;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);
		
		result.add(", formula: {");
		result.add(this.formula.toCharList(), true);
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
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		IrVar test = in.checkWriteTargetVarForPartialReassign(this.target);
		
		if(test != this.target)
		{
			this.target = test;
		}
		
		in.registerNewWrite(this.target, new IrVar_Formula("formulaVar", ScopeLevels.Temporary, this.formula), this);
		*/
		
		in.registerNewWrite(this.target, new IrVar_Formula("formulaVar", ScopeLevels.Temporary, this.formula), this, new StepStage(this.linePosData.getLineNum(), this.linePosData.getSubLineNum(), 2, ""));
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
