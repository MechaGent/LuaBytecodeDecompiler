package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;

public class SetVar_Bulk extends Instruction
{
	private static int instanceCounter = 0;

	private final VarRef source;
	private final VarRef[] targets;
	private final boolean targetsAreSequential;

	private SetVar_Bulk(StepNum inStartTime, VarRef inSource, VarRef[] inTargets, boolean inTargetsAreSequential)
	{
		super(IrCodes.SetVar_Bulk, instanceCounter++, inStartTime);
		this.source = inSource;
		this.targets = inTargets;
		this.targetsAreSequential = inTargetsAreSequential;
	}

	public static SetVar_Bulk getInstance(StepNum inStartTime, VarRef inSource, VarRef[] inTargets, boolean inTargetsAreSequential)
	{
		final SetVar_Bulk result = new SetVar_Bulk(inStartTime, inSource, inTargets, inTargetsAreSequential);
		result.logAllReadsAndWrites();
		return result;
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{

	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.Inline_Never;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		this.source.logReadAtTime(current, InferenceTypes.Mystery);

		final StepNum timeOfRead = current;

		for (int i = 0; i < this.targets.length; i++)
		{
			current = current.fork(ForkBehaviors.IncrementLast);
			final InstructionIndex index = InstructionIndex.getInstance(this, i + 1);
			this.targets[i].logWriteAtTime(this.source, index, timeOfRead, current, InferenceTypes.Mystery);
		}
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();
		final int lastIndex = this.targets.length - 1;

		if (this.targetsAreSequential)
		{
			result.add(this.targets[0].getMangledName(inAppendMetadata));

			switch (this.targets.length)
			{
				case 2:
				{
					result.add(" and ");
					break;
				}
				default:
				{
					result.add(" through ");
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < lastIndex; i++)
			{
				result.add(this.targets[i].getMangledName(inAppendMetadata));
				result.add(", ");
			}

			result.add("and ");
		}

		result.add(this.targets[lastIndex].getMangledName(inAppendMetadata));
		result.add("are set equal to ");
		result.add(this.source.getValueAsString());

		return result;
	}

	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		
		for (int i = 0; i < this.targets.length; i++)
		{
			result.add(this.targets[i].getMangledName(false));
			result.add(" = ");
		}
		
		result.add(this.source.getValueAtTime(this.startTime).getValueAsString());

		result.add(';');

		return result;
	}
}
