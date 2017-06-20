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
import CleanStart.Mk06.Var;

public class SetVar extends Instruction
{
	private static int instanceCounter = 0;

	protected final VarRef target;
	private int expiredCount;
	protected final VarRef source;

	private SetVar(StepNum inStartTime, VarRef inTarget, VarRef inSource)
	{
		this(IrCodes.SetVar, instanceCounter++, inStartTime, inTarget, inSource);
	}

	protected SetVar(IrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime, VarRef inTarget, VarRef inSource)
	{
		super(inIrCode, inInstanceNumber_Specific, inStartTime);
		this.target = inTarget;
		this.expiredCount = 0;
		this.source = inSource;
	}

	public static SetVar getInstance(StepNum inStartTime, VarRef inTarget, VarRef inSource)
	{
		final SetVar result = new SetVar(inStartTime, inTarget, inSource);
		result.logAllReadsAndWrites();
		return result;
	}

	public static SetVar getInstance(StepNum inStartTime, VarRef inTarget, VarRef inSource, InlineSchemes newScheme)
	{
		final SetVar result = new SetVar_ChangedScheme(inStartTime, inTarget, inSource, newScheme);
		result.logAllReadsAndWrites();
		return result;
	}
	
	public static SetVar getInstance(StepNum inStartTime, VarRef inTarget, Var inSource, InlineSchemes newScheme)
	{
		final VarRef wrap2 = VarRef.getInstance("<temp>");
		
		final StepNum writeAt = inStartTime.fork(ForkBehaviors.ForkToSubstage).fork(ForkBehaviors.DecrementLast).fork(ForkBehaviors.DecrementLast);
		final StepNum readAt = writeAt.fork(ForkBehaviors.DecrementLast);
		
		//System.out.println("compare: " + writeAt.compareTo(inStartTime));
		
		final SetVar result = new SetVar_ChangedScheme(inStartTime, inTarget, wrap2, newScheme);
		wrap2.logWriteAtTime(inSource, InstructionIndex.getInstance(result, -1), readAt, writeAt, InferenceTypes.Mystery);
		wrap2.decrementNumReads();
		result.logAllReadsAndWrites();
		
		return result;
	}

	public static SetVar getInstance_fromTableElement(StepNum inStartTime, VarRef inTarget, VarRef inSourceTable, VarRef inSourceIndex)
	{
		final SetVar result = new SetVar_FromTable(inStartTime, inTarget, inSourceTable, inSourceIndex);
		result.logAllReadsAndWrites();
		return result;
	}

	public static SetVar getInstance_toTableElement(StepNum inStartTime, VarRef inTargetTable, VarRef inTargetIndex, VarRef inSource)
	{
		final SetVar result = new SetVar_ToTable(inStartTime, inTargetTable, inTargetIndex, inSource);
		result.logAllReadsAndWrites();
		return result;
	}
	
	public VarRef getTarget()
	{
		return this.target;
	}
	
	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{
		this.setExpiredState(inNewState);
	}
	
	@Override
	public void setExpiredState(boolean newState)
	{
		super.setExpiredState(newState);
		this.expiredCount++;
	}
	
	@Override
	public boolean isExpired()
	{
		//return this.isExpired;
		return this.expiredCount >= this.target.getNumReads();
	}

	public VarRef getSource()
	{
		return this.source;
	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.Inline_EveryTime;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		this.logAllReadsAndWrites(InferenceTypes.Mystery);
	}
	
	protected void logAllReadsAndWrites(InferenceTypes sourceType)
	{
		/*
		 * Steps:
		 * [1] == read core
		 * [2] == write core to target
		 */
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		this.source.logReadAtTime(current, sourceType);
		
		final InstructionIndex index = InstructionIndex.getInstance(this, 2);

		this.target.logWriteAtTime(this.source, index, current, current.fork(ForkBehaviors.IncrementLast), sourceType);
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean appendMetadata)
	{
		final CharList result = new CharList();

		/*
		result.add(this.target.getMangledName(appendMetadata));
		result.add(" is set equal to ");
		result.add(this.source.getMangledName(appendMetadata));
		*/
		
		result.add(this.target.getValueAsString());
		result.add(" is set equal to ");
		result.add(this.source.getValueAtTime(this.startTime).getValueAsString());

		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();
		
		result.add(this.target.getInitialValue().getValueAsString());
		result.add(" = ");
		result.add(this.source.getValueAtTime(this.startTime).getValueAsString());
		result.add(';');
		//result.add("\t//\ttarget was read: " + this.target.getValueAtTime(this.startTime).getNumReads());
		
		if(this.isExpired())
		{
			result.push("//\t");
		}
		
		result.push('\t', offset);
		
		return result;
	}

	private static class SetVar_ChangedScheme extends SetVar
	{
		private final InlineSchemes newScheme;

		public SetVar_ChangedScheme(StepNum inStartTime, VarRef inTarget, VarRef inSource, InlineSchemes inNewScheme)
		{
			super(inStartTime, inTarget, inSource);
			this.newScheme = inNewScheme;
		}

		@Override
		public InlineSchemes getRelevantSchemeForIndex(int inIndex)
		{
			return this.newScheme;
		}
	}

	private static class SetVar_FromTable extends SetVar
	{
		private final VarRef sourceIndex;

		public SetVar_FromTable(StepNum inStartTime, VarRef inTarget, VarRef inSource, VarRef inSourceIndex)
		{
			super(inStartTime, inTarget, inSource);
			this.sourceIndex = inSourceIndex;
		}

		@Override
		protected void logAllReadsAndWrites(InferenceTypes sourceType)
		{
			/*
			 * Steps:
			 * [1] == read core
			 * [2] == write core to target
			 */
			final StepNum tableRead = this.startTime.fork(ForkBehaviors.ForkToSubstage);
			final StepNum indexRead = tableRead.fork(ForkBehaviors.IncrementLast);

			this.source.logReadAtTime(tableRead, InferenceTypes.TableObject);
			this.sourceIndex.logReadAtTime(indexRead, InferenceTypes.Mystery);

			final InstructionIndex index = InstructionIndex.getInstance(this, 2);
			
			//final TableElementVar temp = TableElementVar.getInstance("<rawTableRef>", this.source, tableRead, this.sourceIndex, indexRead, InferenceTypes.TableElement);
			//final VarRef tempWrap = VarRef.getInstance_forWrapper(temp, InferenceTypes.TableElement);

			//this.target.logStateChangeAtTime(index, current.fork(ForkBehaviors.IncrementLast));
			//this.target.logWriteAtTime(this.source, index, current, current.fork(ForkBehaviors.IncrementLast), sourceType);
			this.target.logWriteAtTime(this.source, this.sourceIndex, index, indexRead, indexRead.fork(ForkBehaviors.IncrementLast), InferenceTypes.TableElement);
		}

		@Override
		protected CharList toVerboseCommentForm_internal(boolean appendMetadata)
		{
			final CharList result = new CharList();

			/*
			result.add(this.target.getMangledName(appendMetadata));
			result.add(" is set equal to ");
			result.add(this.source.getMangledName(appendMetadata));
			result.add(".[");
			result.add(this.sourceIndex.getMangledName(appendMetadata));
			result.add(']');
			*/
			
			result.add(this.target.getValueAsString());
			result.add(" is set equal to ");
			result.add(this.source.getValueAsString());
			result.add(".[");
			result.add(this.sourceIndex.getValueAsString());
			result.add(']');

			return result;
		}
		
		@Override
		protected CharList toDecompiledForm_internal(int offset)
		{
			final CharList result = new CharList();
			
			result.add(this.target.getInitialValue().getValueAsString());
			result.add(" = ");
			result.add(this.source.getValueAtTime(this.startTime).getValueAsString());
			result.add(".[");
			result.add(this.sourceIndex.getValueAtTime(this.startTime).getValueAsString());
			result.add(']');
			result.add(';');
			//result.add("\t//\ttarget was read: " + this.target.getValueAtTime(this.startTime).getNumReads());
			
			if(this.isExpired())
			{
				result.push("//\t");
			}
			
			result.push('\t', offset);
			
			return result;
		}
	}

	private static class SetVar_ToTable extends SetVar
	{
		private final VarRef targetIndex;

		public SetVar_ToTable(StepNum inStartTime, VarRef inTargetTable, VarRef inTargetIndex, VarRef inSource)
		{
			super(inStartTime, inTargetTable, inSource);
			this.targetIndex = inTargetIndex;
		}

		@Override
		protected void logAllReadsAndWrites(InferenceTypes sourceType)
		{
			StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

			this.source.logReadAtTime(current, InferenceTypes.TableObject);
			
			current = current.fork(ForkBehaviors.IncrementLast);
			
			this.targetIndex.logReadAtTime(current, InferenceTypes.Mystery);
			
			current = current.fork(ForkBehaviors.IncrementLast);
			
			this.target.logStateChangeAtTime(InstructionIndex.getInstance(this, current.getTailElement()), current);
		}

		@Override
		protected CharList toVerboseCommentForm_internal(boolean appendMetadata)
		{
			final CharList result = new CharList();

			/*
			result.add(this.target.getMangledName(appendMetadata));
			result.add(".[");
			result.add(this.targetIndex.getMangledName(appendMetadata));
			result.add("] is set equal to ");
			result.add(this.source.getMangledName(appendMetadata));
			*/
			
			result.add(this.target.getValueAsString());
			result.add(".[");
			result.add(this.targetIndex.getValueAsString());
			result.add("] is set equal to ");
			result.add(this.source.getValueAsString());

			return result;
		}
		
		@Override
		protected CharList toDecompiledForm_internal(int offset)
		{
			final CharList result = new CharList();
			
			result.add(this.target.getInitialValue().getValueAsString());
			result.add(".[");
			result.add(this.targetIndex.getValueAtTime(this.startTime).getValueAsString());
			result.add("] = ");
			result.add(this.source.getValueAtTime(this.startTime).getValueAsString());
			result.add(';');
			//result.add("\t//\ttarget was read: " + this.target.getValueAtTime(this.startTime).getNumReads());
			
			if(this.isExpired())
			{
				result.push("//\t");
			}
			
			result.push('\t', offset);
			
			return result;
		}
	}

	@Override
	protected CharList toTimestampedVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add(this.target.getMangledName(inAppendMetadata));
		result.add(" is set equal to ");
		result.add(this.source.getValueAtTime(this.startTime).getValueAsString());

		return result;
	}
}
