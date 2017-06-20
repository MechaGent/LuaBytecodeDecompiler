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

public class Concat extends Instruction
{
	private static int InstanceCounter = 0;

	private final VarRef target;
	private final VarRef[] sources;

	private Concat(StepNum inStartTime, VarRef inTarget, VarRef[] inSources)
	{
		super(IrCodes.Concat, InstanceCounter++, inStartTime);
		this.target = inTarget;
		this.sources = inSources;
	}

	public static Concat getInstance(StepNum inStartTime, VarRef inTarget, VarRef[] inSources)
	{
		final Concat result = new Concat(inStartTime, inTarget, inSources);
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
		return InlineSchemes.Inline_IfOnlyReadOnce;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		for (int i = 0; i < this.sources.length; i++)
		{
			this.sources[i].logReadAtTime(current, InferenceTypes.String);
			current = current.fork(ForkBehaviors.IncrementLast);
		}

		this.target.logStateChangeAtTime(InstructionIndex.getInstance(this, current.getTailElement()), current);
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add(this.target.getMangledName(inAppendMetadata));
		result.add(" is set equal to ");

		for (int i = 0; i < this.sources.length; i++)
		{
			if (i != 0)
			{
				result.add(" + ");
			}

			result.add(this.sources[i].getMangledName(inAppendMetadata));
		}

		return result;
	}

	@Override
	protected CharList toCodeForm_internal(boolean inAppendMetadata, int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getMangledName(inAppendMetadata));
		result.add(" = ");

		for (int i = 0; i < this.sources.length; i++)
		{
			if (i != 0)
			{
				result.add(" + ");
			}

			result.add(this.sources[i].getMangledName(inAppendMetadata));
		}
		
		result.add(';');

		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		
		result.add(this.target.getMangledName(false));
		result.add(" = ");
		
		for(int i = 0; i < this.sources.length; i++)
		{
			if(i != 0)
			{
				result.addNewIndentedLine(offset + 1);
				result.add("+ ");
			}
			
			result.add(this.sources[i].getValueAtTime(this.startTime).getValueAsString());
		}
		
		result.add(';');

		return result;
	}
}
