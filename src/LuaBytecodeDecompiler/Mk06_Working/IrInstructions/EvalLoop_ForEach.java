package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class EvalLoop_ForEach extends IrInstruction
{
	private IrVar iteratorFunction;
	private IrVar state;
	private IrVar enumIndex;
	private final IrVar[] internalLoopVars;

	private EvalLoop_ForEach(StepStage inLineNum, IrVar inIteratorFunction, IrVar inState, IrVar inEnumIndex, IrVar[] inInternalLoopVars)
	{
		super(inLineNum, IrCodes.EvalLoop_ForEach, 2);
		this.iteratorFunction = inIteratorFunction;
		this.state = inState;
		this.enumIndex = inEnumIndex;
		this.internalLoopVars = inInternalLoopVars;
	}

	public static EvalLoop_ForEach getInstance(StepStage inLineNum, IrVar inIteratorFunction, IrVar inState, IrVar inEnumIndex, IrVar[] inInternalLoopVars)
	{
		final EvalLoop_ForEach result = new EvalLoop_ForEach(inLineNum, inIteratorFunction, inState, inEnumIndex, inInternalLoopVars);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		// result.setNext(LoopStart, 0);
		// result.setNext(AfterLoop, 1);
		return result;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("iterator function: ");
		result.add(this.iteratorFunction.toWordyVersion(), true);
		result.add(", state: ");
		result.add(this.state.toWordyVersion(), true);
		result.add(", enumIndex: ");
		result.add(this.enumIndex.toWordyVersion(), true);
		result.add(", numInternalLoopVars: ");
		result.addAsString(this.internalLoopVars.length);

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

		in.addVarRead(this.state, this, new StepStage(lineNum, subLine, currentStageNum++, "reading state to pass as arg"));
		in.addVarRead(this.enumIndex, this, new StepStage(lineNum, subLine, currentStageNum++, "reading enumIndex to pass as arg"));
		in.addVarRead(this.iteratorFunction, this, new StepStage(lineNum, subLine, currentStageNum++, "calling iterator function"));
		in.addVarWrite(this.enumIndex, this, new StepStage(lineNum, subLine, currentStageNum++, "writing enumIndex via iteratorFunction"));

		for (int i = 0; i < this.internalLoopVars.length; i++)
		{
			in.addVarWrite(this.internalLoopVars[i], this, new StepStage(lineNum, subLine, currentStageNum++, "writing loop-local var_" + i));
		}
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();

		switch (step)
		{
			case 2:
			{
				this.state = inSwapIn;
				break;
			}
			case 3:
			{
				this.enumIndex = inSwapIn;
				break;
			}
			case 4:
			{
				this.iteratorFunction = inSwapIn;
				break;
			}
			case 5:
			{
				if (StepStage.allowWriteSwaps())
				{
					this.enumIndex = inSwapIn;
					break;
				}
			}
			default:
			{
				if (StepStage.allowWriteSwaps())
				{
					this.internalLoopVars[step - 6] = inSwapIn;
					break;
				}
				
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap inLatestValues)
	{
		//TODO
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		// TODO Auto-generated method stub
		
	}
}
