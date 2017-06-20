package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;

public class HandleVarArgs extends Instruction
{
	private static int instanceCounter = 0;
	
	private final boolean copyAllPossible;
	private final VarRef[] targets;
	private final int targets_startIndex;	//from the old array, not this one
	
	private HandleVarArgs(StepNum inStartTime, boolean inCopyAllPossible, VarRef[] inTargets, int inTargets_startIndex)
	{
		super(IrCodes.VarArgsHandler, instanceCounter++, inStartTime);
		this.copyAllPossible = inCopyAllPossible;
		this.targets = inTargets;
		this.targets_startIndex = inTargets_startIndex;
	}
	
	public static HandleVarArgs getInstance(StepNum inStartTime, boolean inCopyAllPossible, VarRef[] inTargets, int inTargets_startIndex)
	{
		final HandleVarArgs result = new HandleVarArgs(inStartTime, inCopyAllPossible, inTargets, inTargets_startIndex);
		result.logAllReadsAndWrites();
		return result;
	}

	public int getTargets_startIndex()
	{
		return this.targets_startIndex;
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
		StepNum current = null;
		
		for(int i = 0; i < this.targets.length; i++)
		{
			if(i == 0)
			{
				current = this.startTime.fork(ForkBehaviors.ForkToSubstage);
			}
			else
			{
				current = this.startTime.fork(ForkBehaviors.IncrementLast);
			}
			
			this.targets[i].logReadAtTime(current, InferenceTypes.Mystery);
		}
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();
		
		result.add("The following varArgs are handled: {");
		
		for(int i = 0; i < this.targets.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(this.targets[i].getMangledName(inAppendMetadata));
		}
		
		if(this.copyAllPossible)
		{
			result.add("...");
		}
		
		result.add('}');
		
		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = this.toVerboseCommentForm_internal(false);

		result.push("//\t");
		result.push('\t', offset);

		return result;
	}
}
