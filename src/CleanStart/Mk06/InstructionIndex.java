package CleanStart.Mk06;

import CleanStart.Mk06.Enums.InlineSchemes;

public class InstructionIndex implements Mangleable
{
	private final Instruction target;
	private final int targetIndex;

	private InstructionIndex(Instruction inTarget, int inTargetIndex)
	{
		this.target = inTarget;
		this.targetIndex = inTargetIndex;
	}

	public static InstructionIndex getInstance(Instruction inTarget, int inTargetIndex)
	{
		return new InstructionIndex(inTarget, inTargetIndex);
	}

	public Instruction getTarget()
	{
		return this.target;
	}

	public int getTargetIndex()
	{
		return this.targetIndex;
	}

	public InlineSchemes getRelevantScheme()
	{
		if (this.target != null)
		{
			return this.target.getRelevantSchemeForIndex(this.targetIndex);
		}
		else
		{
			return InlineSchemes.Inline_Never;
		}
	}

	public void setPartialExpirationState(boolean newState)
	{
		this.target.setPartialExpiredState(newState, this.targetIndex);
	}

	@Override
	public String getMangledName()
	{
		return this.target.getMangledName(false) + '$' + this.targetIndex;
	}

	@Override
	public String getMangledName(boolean appendMetadata)
	{
		return this.target.getMangledName(false) + '$' + this.targetIndex + (appendMetadata ? "<implement InstructionIndex metadata>" : "");
	}
}
