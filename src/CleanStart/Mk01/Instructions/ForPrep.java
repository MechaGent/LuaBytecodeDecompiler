package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class ForPrep extends ForLoop
{
	private ForPrep(int line, int subLine, int startStep, Var_Ref inIndexVar, Var_Ref inLimitVar, Var_Ref inStepValue, Var_Ref inExternalIndexVar, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		super(line, subLine, startStep, IrCodes.PrepLoop_For, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar, inJumpIfTrue, inJumpIfFalse);
	}
	
	public static ForLoop getInstance(int line, int subLine, int startStep, Var_Ref inIndexVar, Var_Ref inLimitVar, Var_Ref inStepValue, Var_Ref inExternalIndexVar, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		final ForPrep result = new ForPrep(line, subLine, startStep, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar, inJumpIfTrue, inJumpIfFalse);
		inJumpIfTrue.addIncomingJump(result, 0);
		inJumpIfFalse.addIncomingJump(result, 1);
		result.handleInstructionIo();
		return result;
	}
}
