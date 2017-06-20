package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Labels.Label;
import CleanStart.Mk04_Working.Variables.MystVar;

public class ForLoop extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar indexVar;
	private final MystVar limitVar;
	private final MystVar stepValue;
	private final MystVar externalIndexVar;
	
	private final Label toAfterLoop;

	public ForLoop(int inInstanceNumber, MystVar inIndexVar, MystVar inLimitVar, MystVar inStepValue, MystVar inExternalIndexVar, Label inToAfterLoop)
	{
		super(IrCodes.EvalLoop_For, inInstanceNumber);
		this.indexVar = inIndexVar;
		this.limitVar = inLimitVar;
		this.stepValue = inStepValue;
		this.externalIndexVar = inExternalIndexVar;
		this.toAfterLoop = inToAfterLoop;
	}
	
	public static ForLoop getInstance(MystVar inIndexVar, MystVar inLimitVar, MystVar inStepValue, MystVar inExternalIndexVar, Label inToAfterLoop)
	{
		return new ForLoop(instanceCounter++, inIndexVar, inLimitVar, inStepValue, inExternalIndexVar, inToAfterLoop);
	}

	public MystVar getIndexVar()
	{
		return this.indexVar;
	}

	public MystVar getLimitVar()
	{
		return this.limitVar;
	}

	public MystVar getStepValue()
	{
		return this.stepValue;
	}

	public MystVar getExternalIndexVar()
	{
		return this.externalIndexVar;
	}

	public Label getToAfterLoop()
	{
		return this.toAfterLoop;
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
		
		result.add("if ((");
		result.add(this.indexVar.getMostRelevantIdValue());
		result.add(" += ");
		result.add(this.stepValue.getMostRelevantIdValue());
		result.add(") < ");
		result.add(this.limitVar.getMostRelevantIdValue());
		result.add("), then jump to ");
		result.add(this.toAfterLoop.getLabelValue());
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.indexVar.revVarType();
		this.limitVar.revVarType();
		this.stepValue.revVarType();
		this.externalIndexVar.revVarType();
	}
}
