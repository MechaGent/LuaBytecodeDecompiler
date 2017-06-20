package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;
import CleanStart.Mk06.VarFormula.Formula;
import CleanStart.Mk06.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk06.VarFormula.Operators;

public class Jump_ForLoop extends Jump_Branched
{
	private static int instanceCounter = 0;

	private final VarRef indexVar;
	private final VarRef limitVar;
	private final VarRef stepValue;
	private final VarRef externalIndexVar;

	private Jump_ForLoop(StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump, Formula inTest, Label inJumpTo_ifFalse, boolean inFalseJumpIsNegative, VarRef inIndexVar, VarRef inLimitVar, VarRef inStepValue, VarRef inExternalIndexVar)
	{
		super(IrCodes.EvalLoop_For, instanceCounter++, inStartTime, inJumpTo, inIsNegativeJump, inTest, inJumpTo_ifFalse, inFalseJumpIsNegative);
		this.indexVar = inIndexVar;
		this.limitVar = inLimitVar;
		this.stepValue = inStepValue;
		this.externalIndexVar = inExternalIndexVar;
	}

	public static Jump_ForLoop getInstance(StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump, Label inJumpTo_ifFalse, boolean inFalseJumpIsNegative, VarRef inIndexVar, VarRef inLimitVar, VarRef inStepValue, VarRef inExternalIndexVar)
	{
		final FormulaBuilder inTest = new FormulaBuilder();
		
		inTest.add(inIndexVar);
		inTest.add(Operators.ArithToBool_LessThan);
		inTest.add(inLimitVar);
		
		final Jump_ForLoop result = new Jump_ForLoop(inStartTime, inJumpTo, inIsNegativeJump, inTest.build("constructedFormula"), inJumpTo_ifFalse, inFalseJumpIsNegative, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar);
		result.setBranchType(BranchTypes.For);
		result.jumpTo.setAsIncoming(InstructionIndex.getInstance(result, 0));
		result.jumpTo_ifFalse.setAsIncoming(InstructionIndex.getInstance(result, 1));
		result.logAllReadsAndWrites();
		return result;
	}
	
	@Override
	public void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);
		
		//log test
		current = current.fork(ForkBehaviors.IncrementLast);
		
		this.indexVar.logReadAtTime(current, InferenceTypes.Math_Variable);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.limitVar.logReadAtTime(current, InferenceTypes.Math_Variable);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.stepValue.logReadAtTime(current, InferenceTypes.Math_Variable);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.externalIndexVar.logReadAtTime(current, InferenceTypes.Math_Variable);
		current = current.fork(ForkBehaviors.IncrementLast);
		
		this.indexVar.logStateChangeAtTime(InstructionIndex.getInstance(this, 6), current);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.externalIndexVar.logStateChangeAtTime(InstructionIndex.getInstance(this, 7), current);
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add("if ((");
		result.add(this.indexVar.getMangledName(inAppendMetadata));
		result.add(" += ");
		result.add(this.stepValue.getMangledName(inAppendMetadata));
		result.add(") < ");
		result.add(this.limitVar.getMangledName(inAppendMetadata));
		result.add("), then jump to ");
		result.add(this.jumpTo.getMangledName(inAppendMetadata));

		if (this.isNegativeJump)
		{
			result.add(" (Negative)");
		}
		else
		{
			result.add(" (Positive)");
		}

		result.add(". Otherwise, jump to ");
		result.add(this.jumpTo_ifFalse.getMangledName(inAppendMetadata));

		if (this.falseJumpIsNegative)
		{
			result.add(" (Negative)");
		}
		else
		{
			result.add(" (Positive)");
		}

		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("//\t<> NeedToImplement: ");
		result.add(this.irCode.toString());
		result.add(" - ");
		result.add(this.getMangledName(false));

		return result;
	}
}
