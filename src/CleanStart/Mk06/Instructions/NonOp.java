package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;

public class NonOp extends Instruction
{
	private static int instanceCounter = 0;

	protected NonOp(StepNum inStartTime)
	{
		super(IrCodes.NonOp, instanceCounter++, inStartTime);
	}

	public static NonOp getInstance(StepNum inStartTime)
	{
		return new NonOp(inStartTime);
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{
		this.setExpiredState(inNewState);
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

		result.add("Non Op");

		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("Non Op (instance ");
		result.addAsString(this.instanceNumber_specific);
		result.add(", if you like that sort of thing)");

		return result;
	}
}
