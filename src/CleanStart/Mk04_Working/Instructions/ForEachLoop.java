package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Labels.Label;
import CleanStart.Mk04_Working.Variables.MystVar;

public class ForEachLoop extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar iteratorFunction;
	private final MystVar state;
	private final MystVar enumIndex;
	private final MystVar[] loopVars;
	
	private final Label toOtherEnd;
	
	public ForEachLoop(int inInstanceNumber, MystVar inIteratorFunction, MystVar inState, MystVar inEnumIndex, MystVar[] inLoopVars, Label inToOtherEnd)
	{
		super(IrCodes.EvalLoop_ForEach, inInstanceNumber);
		this.iteratorFunction = inIteratorFunction;
		this.state = inState;
		this.enumIndex = inEnumIndex;
		this.loopVars = inLoopVars;
		this.toOtherEnd = inToOtherEnd;
	}
	
	public static ForEachLoop getInstance(MystVar inIteratorFunction, MystVar inState, MystVar inEnumIndex, MystVar[] inLoopVars, Label inToOtherEnd)
	{
		return new ForEachLoop(instanceCounter++, inIteratorFunction, inState, inEnumIndex, inLoopVars, inToOtherEnd);
	}

	public MystVar getIteratorFunction()
	{
		return this.iteratorFunction;
	}

	public MystVar getState()
	{
		return this.state;
	}

	public MystVar getEnumIndex()
	{
		return this.enumIndex;
	}

	public MystVar[] getLoopVars()
	{
		return this.loopVars;
	}

	public Label getToOtherEnd()
	{
		return this.toOtherEnd;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.NonApplicable;
	}

	@Override
	public Variable getInlinedForm()
	{
		return null;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("<> NeedToImplement: ");
		result.add(this.IrCode.toString());
		result.add(" - ");
		result.add(this.toString());
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.iteratorFunction.revVarType();
		this.state.revVarType();
		this.enumIndex.revVarType();
		
		for(MystVar curr: this.loopVars)
		{
			curr.revVarType();
		}
	}
}
