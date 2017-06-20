package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;

public abstract class Return extends Instruction
{
	protected final VarRef[] sources;

	protected Return(IrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime, VarRef[] inSources)
	{
		super(inIrCode, inInstanceNumber_Specific, inStartTime);
		this.sources = inSources;
	}

	public static Return getInstance(StepNum inStartTime, VarRef[] inSources, boolean isVariable)
	{
		final Return result;

		if (isVariable)
		{
			result = new Return_Variably(inStartTime, inSources);
		}
		else
		{
			result = new Return_Concretely(inStartTime, inSources);
		}

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
		return InlineSchemes.NonApplicable;
	}

	private static class Return_Concretely extends Return
	{
		private static int instanceCounter = 0;

		protected Return_Concretely(StepNum inStartTime, VarRef[] inSources)
		{
			super(IrCodes.Return_Concretely, instanceCounter++, inStartTime, inSources);
		}

		@Override
		public void logAllReadsAndWrites()
		{
			StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

			for (int i = 0; i < this.sources.length; i++)
			{
				this.sources[i].logReadAtTime(current, InferenceTypes.Mystery);
				current = current.fork(ForkBehaviors.IncrementLast);
			}
		}

		@Override
		protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
		{
			final CharList result = new CharList();

			result.add("return the following to the calling function: {");

			for (int i = 0; i < this.sources.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(this.sources[i].getMangledName(inAppendMetadata));
			}

			result.add('}');

			return result;
		}

		@Override
		protected CharList toDecompiledForm_internal(int offset)
		{
			final CharList result = new CharList();

			result.add('\t', offset);

			result.add("return");

			switch (this.sources.length)
			{
				case 0:
				{
					break;
				}
				case 1:
				{
					result.add(' ');
					result.add(this.sources[0].getValueAtTime(this.startTime).getValueAsString());
					break;
				}
				default:
				{
					result.add(" {");

					for (int i = 0; i < this.sources.length; i++)
					{
						if (i != 0)
						{
							result.add(", ");
						}

						result.add(this.sources[i].getValueAtTime(this.startTime).getValueAsString());
					}

					result.add('}');
					break;
				}
			}

			result.add(';');

			return result;
		}
	}

	private static class Return_Variably extends Return
	{
		private static int instanceCounter = 0;

		protected Return_Variably(StepNum inStartTime, VarRef[] inSources)
		{
			super(IrCodes.Return_Variably, instanceCounter++, inStartTime, inSources);
		}

		@Override
		public void logAllReadsAndWrites()
		{
			StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);
			final int lastIndex = this.sources.length - 1;

			for (int i = 0; i < lastIndex; i++)
			{
				this.sources[i].logReadAtTime(current, InferenceTypes.Mystery);
				current = current.fork(ForkBehaviors.IncrementLast);
			}

			this.sources[lastIndex].logReadAtTime(current, InferenceTypes.FunctionObject);
		}

		@Override
		protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
		{
			final CharList result = new CharList();

			result.add("return the following to the calling function: {");

			final int lastIndex = this.sources.length - 1;

			for (int i = 0; i < lastIndex; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(this.sources[i].getMangledName(inAppendMetadata));
			}

			result.add("and the return values of ");
			result.add(this.sources[lastIndex].getMangledName(inAppendMetadata));

			result.add('}');

			return result;
		}

		@Override
		protected CharList toDecompiledForm_internal(int offset)
		{
			final CharList result = new CharList();

			result.add('\t', offset);
			
			result.add("return {");
			
			final int lastIndex = this.sources.length - 1;
			
			for (int i = 0; i < lastIndex; i++)
			{
				if (i != 0)
				{
					result.add(' ');
					
					if(i % 3 == 0)
					{
						result.addNewLine();
						result.add('\t', offset);
					}
					else
					{
						result.add(' ');
					}
				}

				result.add(this.sources[i].getValueAtTime(this.startTime).getValueAsString());
			}
			
			result.add("...");
			result.add(this.sources[lastIndex].getValueAtTime(this.startTime).getValueAsString());
			result.add("};");

			return result;
		}
	}
}
