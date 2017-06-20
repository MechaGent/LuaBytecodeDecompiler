package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.JumpingInstruction;
import CleanStart.Mk01.VarFormula.Formula;
import CleanStart.Mk01.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk01.VarFormula.Operators;
import CleanStart.Mk01.Variables.Var_Ref;

public class ForLoop extends BaseInstruction implements JumpingInstruction
{
	protected final Var_Ref indexVar;
	protected final Var_Ref limitVar;
	protected final Var_Ref stepValue;
	protected final Var_Ref externalIndexVar;
	
	protected JumpLabel jumpIfTrue;
	protected JumpLabel jumpIfFalse;
	
	protected ForLoop otherEnd;
	
	protected ForLoop(int line, int subLine, int startStep, Var_Ref inIndexVar, Var_Ref inLimitVar, Var_Ref inStepValue, Var_Ref inExternalIndexVar, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		this(line, subLine, startStep, IrCodes.EvalLoop_For, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar, inJumpIfTrue, inJumpIfFalse);
	}

	protected ForLoop(int line, int subLine, int startStep, IrCodes inOpCode, Var_Ref inIndexVar, Var_Ref inLimitVar, Var_Ref inStepValue, Var_Ref inExternalIndexVar, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		//numSteps = (1 xR) + (1 xR) + (1 xW) + (1 xR) + (1 xR) + (1 xW)
		super(inOpCode, line, subLine, startStep, 6);
		this.indexVar = inIndexVar;
		this.limitVar = inLimitVar;
		this.stepValue = inStepValue;
		this.externalIndexVar = inExternalIndexVar;
		this.jumpIfTrue = inJumpIfTrue;
		this.jumpIfFalse = inJumpIfFalse;
		this.otherEnd = null;
	}
	
	public static ForLoop getInstance(int line, int subLine, int startStep, Var_Ref inIndexVar, Var_Ref inLimitVar, Var_Ref inStepValue, Var_Ref inExternalIndexVar, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		final ForLoop result = new ForLoop(line, subLine, startStep, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar, inJumpIfTrue, inJumpIfFalse);
		inJumpIfTrue.addIncomingJump(result, 0);
		inJumpIfFalse.addIncomingJump(result, 1);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref getIndexVar()
	{
		return this.indexVar;
	}

	public Var_Ref getLimitVar()
	{
		return this.limitVar;
	}

	public Var_Ref getStepValue()
	{
		return this.stepValue;
	}

	public Var_Ref getExternalIndexVar()
	{
		return this.externalIndexVar;
	}

	public ForLoop getOtherEnd()
	{
		return this.otherEnd;
	}

	public void setOtherEnd(ForLoop inOtherEnd)
	{
		this.otherEnd = inOtherEnd;
	}

	public JumpLabel getJumpIfTrue()
	{
		return this.jumpIfTrue;
	}

	public JumpLabel getJumpIfFalse()
	{
		return this.jumpIfFalse;
	}

	@Override
	public void setIndexedJumpLabelTo(int inIndex, JumpLabel inIn)
	{
		switch(inIndex)
		{
			case 0:
			{
				this.jumpIfTrue = inIn;
				break;
			}
			case 1:
			{
				this.jumpIfFalse = inIn;
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
		final FormulaBuilder buildy = new FormulaBuilder();
		buildy.add(this.indexVar);
		buildy.add(Operators.Arith_Add);
		buildy.add(this.stepValue);
		final Formula nextValue = buildy.build();
		this.indexVar.logReadAtTime(this.sigTimes[stepIndex++]);
		this.stepValue.logReadAtTime(this.sigTimes[stepIndex++]);
		this.indexVar.logWriteAtTime(this.sigTimes[stepIndex++], nextValue);
		
		this.indexVar.logReadAtTime(this.sigTimes[stepIndex++]);
		this.limitVar.logReadAtTime(this.sigTimes[stepIndex++]);
		this.externalIndexVar.logWriteAtTime(this.sigTimes[stepIndex++], this.indexVar);
	}
}
