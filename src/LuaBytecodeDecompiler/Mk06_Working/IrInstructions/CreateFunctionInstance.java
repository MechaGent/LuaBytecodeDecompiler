package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Function;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_FunctionInstance;

public class CreateFunctionInstance extends IrInstruction
{
	@SuppressWarnings("unused")
	private static final String builderStub = "buildy.set";

	private IrVar target;
	// private final IrVar_String dummyFunctionName;
	private IrVar_Function sourceFunction;
	private final IrVar[] params;
	private final IrVar[] upvals;

	private CreateFunctionInstance(StepStage inLineNum, IrVar inTarget, IrVar_Function inSourceFunction, IrVar[] inParams, IrVar[] inUpvals)
	{
		super(inLineNum, IrCodes.CreateFunctionInstance, 1);
		this.target = inTarget;
		// this.dummyFunctionName = new IrVar_String("dummyFunctionName", ScopeLevels.Local_Variable, false, inSourceFunction.getFunctionName());
		this.sourceFunction = inSourceFunction;
		this.params = inParams;
		this.upvals = inUpvals;
	}

	public static CreateFunctionInstance getInstance(StepStage inLineNum, IrVar inTarget, IrVar_Function inSourceFunction, IrVar[] inParams, IrVar[] inUpvals)
	{
		final CreateFunctionInstance result = new CreateFunctionInstance(inLineNum, inTarget, inSourceFunction, inParams, inUpvals);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		// result.logAllReadsAndWrites();
		return result;
	}

	public IrVar getTarget()
	{
		return this.target;
	}

	public IrVar_Function getSourceFunction()
	{
		return this.sourceFunction;
	}

	public IrVar[] getUpvals()
	{
		return this.upvals;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);
		result.add(", sourceFunction: ");
		result.add(this.sourceFunction.valueToString());
		result.add(", numUpValues: ");
		result.addAsString(this.upvals.length);

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

		in.addVarRead(this.sourceFunction, this, new StepStage(lineNum, subLine, currentStageNum++, "reading sourceFunction"));

		for (int i = 0; i < this.upvals.length; i++)
		{
			in.addVarRead(this.upvals[i], this, new StepStage(lineNum, subLine, currentStageNum++, "reading upvalue_" + i));
		}

		in.addVarWrite(this.target, this, new StepStage(lineNum, subLine, currentStageNum++, "writing to target"), this.target);
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();

		if (step == 2)
		{
			this.sourceFunction = (IrVar_Function) inSwapIn;
		}
		else if (step - 3 < this.upvals.length)
		{
			this.upvals[step - 3] = inSwapIn;
		}
		else
		{
			throw new IndexOutOfBoundsException(inSwapOutStep.toString());
		}
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
		
		for(int i = 0; i < this.upvals.length; i++)
		{
			test = in.checkVarForReassign(this.upvals[i]);
			
			if(test != this.upvals[i])
			{
				this.upvals[i] = test;
			}
		}
		
		final IrVar_FunctionInstance instance = new IrVar_FunctionInstance("instanceOf:" + this.sourceFunction.getLabel(), ScopeLevels.Temporary, this.sourceFunction.getCore(), this.params, this.upvals);
		in.registerNewWrite(this.target, instance, this);
		*/

		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;

		in.registerNewRead(this.sourceFunction, this, new StepStage(line, subline, count++, "reading source function"));

		for (int i = 0; i < this.params.length; i++)
		{
			in.registerNewRead(this.params[i], this, new StepStage(line, subline, count++, "reading param"));
		}

		for (int i = 0; i < this.upvals.length; i++)
		{
			in.registerNewRead(this.upvals[i], this, new StepStage(line, subline, count++, "reading upval"));
		}

		in.registerNewWrite(this.target, new IrVar_FunctionInstance("", ScopeLevels.Temporary, this.sourceFunction.getCore(), this.params, this.upvals), this, new StepStage(line, subline, count++, "writing to target"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		if(inVarIndex == 2)
		{
			this.sourceFunction = (IrVar_Function) inIn;
		}
		else if((inVarIndex -= 2) < this.params.length)
		{
			this.params[inVarIndex] = inIn;
		}
		else if((inVarIndex -= this.params.length) < this.upvals.length)
		{
			this.upvals[inVarIndex] = inIn;
		}
		else if((inVarIndex -= this.upvals.length) == 0)
		{
			this.target = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException(inVarIndex + "");
		}
	}

	/*
	@Override
	protected void logAllReadsAndWrites()
	{
		/*
		 * read sourceFunction
		 * read upvals
		 * write target
		 *
		
		int currentStageNum = 1;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		StepStage currentStage = new StepStage(lineNum, subLine, currentStageNum++, "reading source function");
		this.logRead(currentStage, this.dummyFunctionName);
	}
	*/
}
