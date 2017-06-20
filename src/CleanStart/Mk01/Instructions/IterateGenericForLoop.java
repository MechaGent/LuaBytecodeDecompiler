package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.JumpingInstruction;
import CleanStart.Mk01.Variables.Var_Ref;

public class IterateGenericForLoop extends BaseInstruction implements JumpingInstruction
{
	private final Var_Ref iteratorFunction;
	private final Var_Ref state;
	private final Var_Ref enumIndex;
	private final Var_Ref[] loopVars;
	
	private JumpLabel toOtherEnd;
	private JumpLabel toAfterEnd;
	
	private IterateGenericForLoop(int line, int subLine, int startStep, Var_Ref inIteratorFunction, Var_Ref inState, Var_Ref inEnumIndex, Var_Ref[] inLoopVars, JumpLabel inToOtherEnd, JumpLabel inToAfterEnd)
	{
		super(IrCodes.EvalLoop_ForEach, line, subLine, startStep, 1 + 1 + 1 + inLoopVars.length + 1 + 1 + 1);
		this.iteratorFunction = inIteratorFunction;
		this.state = inState;
		this.enumIndex = inEnumIndex;
		this.loopVars = inLoopVars;
		this.toOtherEnd = inToOtherEnd;
		this.toAfterEnd = inToAfterEnd;
	}
	
	public static IterateGenericForLoop getInstance(int line, int subLine, int startStep, Var_Ref inIteratorFunction, Var_Ref inState, Var_Ref inEnumIndex, Var_Ref[] inLoopVars, JumpLabel inToOtherEnd, JumpLabel inToAfterEnd)
	{
		final IterateGenericForLoop result = new IterateGenericForLoop(line, subLine, startStep, inIteratorFunction, inState, inEnumIndex, inLoopVars, inToOtherEnd, inToAfterEnd);
		inToOtherEnd.addIncomingJump(result, 0);
		inToAfterEnd.addIncomingJump(result, 1);
		result.handleInstructionIo();
		return result;
	}
	
	@Override
	public void setIndexedJumpLabelTo(int inIndex, JumpLabel inIn)
	{
		switch(inIndex)
		{
			case 0:
			{
				this.toOtherEnd = inIn;
				break;
			}
			case 1:
			{
				this.toAfterEnd = inIn;
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		this.state.logReadAtTime(this.sigTimes[stepIndex++]);
		this.enumIndex.logReadAtTime(this.sigTimes[stepIndex++]);
		this.iteratorFunction.logInternalStateChangeAtTime(this.sigTimes[stepIndex++]);
		
		for(Var_Ref externVal: this.loopVars)
		{
			externVal.logIndeterminateWriteAtTime(this.sigTimes[stepIndex++]);
		}
		
		this.loopVars[0].logReadAtTime(this.sigTimes[stepIndex++]);
		
		this.enumIndex.logReadAtTime(this.sigTimes[stepIndex++]);
		this.state.logWriteAtTime(this.sigTimes[stepIndex++], this.enumIndex);
	}

	public Var_Ref getIteratorFunction()
	{
		return this.iteratorFunction;
	}

	public Var_Ref getState()
	{
		return this.state;
	}

	public Var_Ref getEnumIndex()
	{
		return this.enumIndex;
	}

	public Var_Ref[] getLoopVars()
	{
		return this.loopVars;
	}

	public JumpLabel getToOtherEnd()
	{
		return this.toOtherEnd;
	}

	public JumpLabel getToAfterEnd()
	{
		return this.toAfterEnd;
	}
}
