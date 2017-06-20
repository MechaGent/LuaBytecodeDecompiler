package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;

public class Jump_Straight extends Instruction
{
	private static int InstanceCounter = 0;

	protected final Label jumpTo;
	protected final boolean isNegativeJump;

	private Jump_Straight(StepNum inStartTime, Label inJumpTo, boolean inIsNegative)
	{
		this(IrCodes.Jump_Straight, InstanceCounter++, inStartTime, inJumpTo, inIsNegative);
	}

	protected Jump_Straight(IrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump)
	{
		super(inIrCode, inInstanceNumber_Specific, inStartTime);
		this.jumpTo = inJumpTo;
		this.isNegativeJump = inIsNegativeJump;
	}

	public static Jump_Straight getInstance(StepNum inStartTime, Label inJumpTo, boolean inIsNegative)
	{
		final Jump_Straight result = new Jump_Straight(inStartTime, inJumpTo, inIsNegative);
		result.jumpTo.setAsIncoming(InstructionIndex.getInstance(result, 0));
		result.logAllReadsAndWrites();
		return result;
	}
	
	public boolean isNegativeJump()
	{
		return this.isNegativeJump;
	}
	
	public Label getJumpTo()
	{
		return this.jumpTo;
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{

	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.NonApplicable;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add("Proceed directly to Label '");
		result.add(this.jumpTo.getMangledName(inAppendMetadata));
		result.add("' (");

		if (this.isNegativeJump)
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
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("//\tGoTo ");
		result.add(this.jumpTo.getMangledName(false));
		result.add(" (");
		
		if(this.isNegativeJump)
		{
			result.add('-');
		}
		else
		{
			result.add('+');
		}
		
		result.add(')');

		return result;
	}
}
