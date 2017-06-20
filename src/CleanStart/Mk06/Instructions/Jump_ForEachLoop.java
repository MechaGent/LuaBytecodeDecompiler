package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;
import CleanStart.Mk06.SubVars.MethodVar;
import CleanStart.Mk06.VarFormula.Formula;
import CleanStart.Mk06.VarFormula.Formula.FormulaBuilder;

public class Jump_ForEachLoop extends Jump_Branched
{
	private static int instanceCounter = 0;

	private final VarRef iteratorFunction;
	private final VarRef state;
	private final VarRef enumIndex;
	private final VarRef[] loopVars;

	public Jump_ForEachLoop(StepNum inStartTime, Label inJumpTo, Formula inTest, Label inJumpTo_ifFalse, VarRef inIteratorFunction, VarRef inState, VarRef inEnumIndex, VarRef[] inLoopVars)
	{
		super(IrCodes.EvalLoop_ForEach, instanceCounter++, inStartTime, inJumpTo, true, inTest, inJumpTo_ifFalse, false);
		this.iteratorFunction = inIteratorFunction;
		this.state = inState;
		this.enumIndex = inEnumIndex;
		this.loopVars = inLoopVars;
	}

	public static Jump_ForEachLoop getInstance(StepNum inStartTime, Label inJumpTo, Label inJumpTo_ifFalse, VarRef inIteratorFunction, VarRef inState, VarRef inEnumIndex, VarRef[] inLoopVars)
	{
		final FormulaBuilder inTest = new FormulaBuilder();

		final MethodVar hasRemainingMethod = MethodVar.getInstance("", inStartTime, inIteratorFunction, "hasRemaining", false, new VarRef[0], false, new VarRef[0]);

		inTest.add(hasRemainingMethod);

		final Jump_ForEachLoop result = new Jump_ForEachLoop(inStartTime, inJumpTo, inTest.build("constructedFormula"), inJumpTo_ifFalse, inIteratorFunction, inState, inEnumIndex, inLoopVars);
		result.setBranchType(BranchTypes.ForEach);
		result.jumpTo.setAsIncoming(InstructionIndex.getInstance(result, 0));
		result.jumpTo_ifFalse.setAsIncoming(InstructionIndex.getInstance(result, 1));
		result.logAllReadsAndWrites();
		return result;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		/*
		 * every loop:
		 * iterator function is called with 2 args, state and enumIndex
		 * results are returned in loopVars
		 * first loopVar is tested
		 * if null, loop is done
		 * else, enumIndex = first loopVar
		 */
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		this.state.logReadAtTime(current, InferenceTypes.Mystery);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.enumIndex.logReadAtTime(current, InferenceTypes.Mystery);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.iteratorFunction.logStateChangeAtTime(InstructionIndex.getInstance(this, current.getTailElement()), current);

		for (VarRef external : this.loopVars)
		{
			current = current.fork(ForkBehaviors.IncrementLast);
			external.logStateChangeAtTime(InstructionIndex.getInstance(this, current.getTailElement()), current);
		}

		current = current.fork(ForkBehaviors.IncrementLast);
		this.loopVars[0].logReadAtTime(current, InferenceTypes.Mystery);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.enumIndex.logReadAtTime(current, InferenceTypes.Mystery);
		current = current.fork(ForkBehaviors.IncrementLast);
		this.state.logStateChangeAtTime(InstructionIndex.getInstance(this, current.getTailElement()), current);
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add("<> NeedToImplement: ");
		result.add(this.irCode.toString());
		result.add(" - ");
		result.add(this.toString());

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
