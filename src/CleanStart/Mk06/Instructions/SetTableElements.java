package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.StepNum.ForkBehaviors;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;

public class SetTableElements extends Instruction
{
	private static int instanceCounter = 0;
	
	private final VarRef target;
	private final int target_startIndex;
	private final VarRef[] sources;
	private final int source_startIndex;	//from the original array, not this one (this one starts at [0])
	private final String sourceName;
	
	private SetTableElements(StepNum inStartTime, VarRef inTarget, int inTarget_startIndex, VarRef[] inSources, int inSource_startIndex, String inSourceName)
	{
		super(IrCodes.CopyArray, instanceCounter++, inStartTime);
		this.target = inTarget;
		this.target_startIndex = inTarget_startIndex;
		this.sources = inSources;
		this.source_startIndex = inSource_startIndex;
		this.sourceName = inSourceName;
	}
	
	public static SetTableElements getInstance(StepNum inStartTime, VarRef inTarget, int inTarget_startIndex, VarRef[] inSources, int inSource_startIndex, String inSourceName)
	{
		final SetTableElements result = new SetTableElements(inStartTime, inTarget, inTarget_startIndex, inSources, inSource_startIndex, inSourceName);
		result.logAllReadsAndWrites();
		return result;
	}

	public int getSource_startIndex()
	{
		return this.source_startIndex;
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
		
		for(int i = 0; i < this.sources.length; i++)
		{
			if(i == 0)
			{
				current = this.startTime.fork(ForkBehaviors.ForkToSubstage);
			}
			else
			{
				current = current.fork(ForkBehaviors.IncrementLast);
			}
			
			this.sources[i].logReadAtTime(current, InferenceTypes.Mystery);
			
			current = current.fork(ForkBehaviors.IncrementLast);
			
			this.target.logStateChangeAtTime(InstructionIndex.getInstance(this, i + 1), current);
		}
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();
		
		result.add("The variables from ");
		
		final String targetName = this.target.getMangledName(inAppendMetadata);
		result.add(targetName);
		result.add('[');
		result.addAsString(this.target_startIndex);
		result.add("] to ");
		result.add(targetName);
		result.add('[');
		result.addAsString(this.target_startIndex + this.sources.length - 1);
		result.add("are set equal to the corresponding variables from ");
		result.add(this.sources[0].getMangledName(inAppendMetadata));
		result.add(" to ");
		result.add(this.sources[this.sources.length - 1].getMangledName(inAppendMetadata));
		
		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		
		result.add("for (int i = 0; i < ");
		result.addAsString(this.sources.length);
		result.add("; i++)\t// auto-generated from SetTableElements");
		result.addNewIndentedLine(offset);
		result.add('{');
		result.addNewIndentedLine(offset + 1);
		result.add(this.target.getValueAtTime(this.startTime).getValueAsString());
		result.add("[i + ");
		result.addAsString(this.target_startIndex);
		result.add("] = ");
		result.add(this.sourceName);
		result.add("[i + ");
		result.addAsString(this.source_startIndex);
		result.add("];");
		result.addNewIndentedLine(offset);
		result.add('}');

		return result;
	}
}
