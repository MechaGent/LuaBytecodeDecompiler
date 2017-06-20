package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Labels.Label;

public class Jump_Straight extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final Label jumpTo;
	private final boolean isNegativeJump;
	
	private Jump_Straight(Label inJumpTo, boolean inIsNegativeJump)
	{
		super(IrCodes.Jump_Straight, instanceCounter++);
		this.jumpTo = inJumpTo;
		this.isNegativeJump = inIsNegativeJump;
	}
	
	public static Jump_Straight getInstance(Label inJumpTo)
	{
		return getInstance(inJumpTo, false);
	}
	
	public static Jump_Straight getInstance(Label inJumpTo, boolean inIsNegativeJump)
	{
		final Jump_Straight result = new Jump_Straight(inJumpTo, inIsNegativeJump);
		inJumpTo.setInstructionAsIncoming(inJumpTo, 0);
		return result;
	}

	public Label getJumpTo()
	{
		return this.jumpTo;
	}

	public boolean isNegativeJump()
	{
		return this.isNegativeJump;
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
		
		result.add("Proceed directly to Label '");
		result.add(this.jumpTo.getLabelValue());
		result.add("' (");
		
		if(this.isNegativeJump)
		{
			result.add("Negative");
		}
		else
		{
			result.add("Positive");
		}
		
		result.add("). Do not pass Go. Do not collect $200.");
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		
	}
}
