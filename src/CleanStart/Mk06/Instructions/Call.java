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
import CleanStart.Mk06.SubVars.MethodVar;

public class Call extends Instruction
{
	private static int instanceCounter = 0;

	private final VarRef subjectObject;
	private final boolean ArgsAreVariable;
	private final VarRef[] Args;
	private final boolean ReturnsAreVariable;
	private final VarRef[] Returns;
	private final InlineSchemes scheme;

	private Call(StepNum inStartTime, VarRef inSubjectObject, boolean inArgsAreVariable, VarRef[] inArgs, boolean inReturnsAreVariable, VarRef[] inReturns)
	{
		super(IrCodes.CallObject, instanceCounter++, inStartTime);
		this.subjectObject = inSubjectObject;
		this.ArgsAreVariable = inArgsAreVariable;
		this.Args = inArgs;
		this.ReturnsAreVariable = inReturnsAreVariable;
		this.Returns = inReturns;

		switch (this.Returns.length)
		{
			case 0:
			{
				this.scheme = InlineSchemes.NonApplicable;
				break;
			}
			case 1:
			{
				if (this.ReturnsAreVariable)
				{
					this.scheme = InlineSchemes.Inline_Never;
				}
				else
				{
					this.scheme = InlineSchemes.Inline_EveryTime;
				}
				break;
			}
			default:
			{
				if (this.ReturnsAreVariable || this.Returns.length != 1)
				{
					this.scheme = InlineSchemes.Inline_Never;
				}
				else
				{
					this.scheme = InlineSchemes.Inline_IfOnlyReadOnce;
				}

				break;
			}
		}
	}

	public static enum ObjectTypes
	{
		Table,
		Function,
		Unknown;
	}

	public static Call getInstance(StepNum inStartTime, VarRef inSubjectObject, boolean inArgsAreVariable, VarRef[] inArgs, boolean inReturnsAreVariable, VarRef[] inReturns)
	{
		final Call result = new Call(inStartTime, inSubjectObject, inArgsAreVariable, inArgs, inReturnsAreVariable, inReturns);
		
		result.logAllReadsAndWrites();
		
		return result;
	}

	public ObjectTypes getObjectType()
	{
		switch (this.subjectObject.getValueAtTime(this.startTime).getBestGuess())
		{
			case TableObject:
			{
				return ObjectTypes.Table;
			}
			case FunctionObject:
			{
				return ObjectTypes.Function;
			}
			case Mystery:
			{
				return ObjectTypes.Unknown;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{

	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return this.scheme;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		for (int i = 0; i < this.Args.length; i++)
		{
			//this.Args[i].logReadAtTime(current, InferenceTypes.Mystery);
			this.Args[i].logStateChangeAtTime(InstructionIndex.getInstance(this, i), current);
			current = current.fork(ForkBehaviors.IncrementLast);
		}

		switch(this.Returns.length)
		{
			case 1:
			{
				final StepNum readTime = current;
				final MethodVar inline = MethodVar.getInstance("", readTime, this.subjectObject, "call", this.ArgsAreVariable, this.Args, this.ReturnsAreVariable, this.Returns);
				final VarRef wrap = VarRef.getInstance_forWrapper(inline, InferenceTypes.Mystery, InlineSchemes.Inline_IfOnlyReadOnce);
				current = current.fork(ForkBehaviors.IncrementLast);
				this.Returns[0].logWriteAtTime(wrap, InstructionIndex.getInstance(this, this.Args.length + 3), readTime, current, InferenceTypes.Mystery);
				break;
			}
			default:
			{
				this.subjectObject.logStateChangeAtTime(InstructionIndex.getInstance(this, this.Args.length + 2), current);
				
				for (int i = 0; i < this.Returns.length; i++)
				{
					current = current.fork(ForkBehaviors.IncrementLast);
					this.Returns[i].logStateChangeAtTime(InstructionIndex.getInstance(this, this.Args.length + 3 + i), current);
				}
				
				break;
			}
		}
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add('{');

		for (int i = 0; i < this.Returns.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Returns[i].getMangledName(inAppendMetadata));
		}

		if (this.ReturnsAreVariable)
		{
			result.add("...");
		}

		result.add("} = '");
		result.add(this.subjectObject.getMangledName(inAppendMetadata));
		result.add("'.call(");

		for (int i = 0; i < this.Args.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Args[i].getMangledName(inAppendMetadata));
		}

		if (this.ArgsAreVariable)
		{
			result.add("...");
		}

		result.add(')');

		return result;
	}

	@Override
	protected CharList toCodeForm_internal(boolean inAppendMetadata, int inOffset)
	{
		final CharList result = new CharList();
		
		result.add('\t', inOffset);

		result.add('{');

		for (int i = 0; i < this.Returns.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Returns[i].getMangledName(inAppendMetadata));
		}

		if (this.ReturnsAreVariable)
		{
			result.add("...");
		}

		result.add("} = '");
		result.add(this.subjectObject.getMangledName(inAppendMetadata));
		result.add("'.call(");

		for (int i = 0; i < this.Args.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Args[i].getMangledName(inAppendMetadata));
		}

		if (this.ArgsAreVariable)
		{
			result.add("...");
		}

		result.add(')');

		return result;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		
		final boolean isExpired = this.isExpired();
		
		if(isExpired)
		{
			result.add("//\t");
		}
		
		if(this.ReturnsAreVariable)
		{
			result.add('{');
			
			for(int i = 0; i < this.Returns.length; i++)
			{
				result.add(this.Returns[i].getValueAtTime(this.startTime).getValueAsString());
				result.add(',');
				
				if(!isExpired && i % 3 == 2)	//using i%3==2 instead of ...==0 to offset the fact that the var is before this statement
				{
					result.addNewLine();
					result.add('\t', offset);
				}
				else
				{
					result.add(' ');
				}
			}
			
			result.add("...} = ");
		}
		else if(this.Returns.length > 0)
		{
			result.add('{');
			
			for(int i = 0; i < this.Returns.length; i++)
			{
				if(i != 0)
				{
					result.add(',');
					
					if(!isExpired && i % 3 == 0)
					{
						result.addNewLine();
						result.add('\t', offset);
					}
					else
					{
						result.add(' ');
					}
				}
				
				result.add(this.Returns[i].getValueAtTime(this.startTime).getValueAsString());
			}
			
			result.add("} = ");
		}
		
		result.add('\'');
		result.add(this.subjectObject.getValueAtTime(this.startTime).getValueAsString());
		result.add("'.call( ");
		
		final int currentOffset = result.size();
		
		for(int i = 0; i < this.Args.length; i++)
		{
			if(i != 0)
			{
				result.add(',');
				
				if(i % 3 == 0)
				{
					//result.addNewIndentedLine(offset + 2);
					result.addNewLine();
					result.add(' ', currentOffset);
				}
				else
				{
					result.add(' ');
				}
			}
			
			result.add(this.Args[i].getValueAtTime(this.startTime).getValueAsString());
		}
		
		result.add(");");
		
		if(this.ArgsAreVariable)
		{
			result.add("\t//\t NOTE TO SELF: if this call's last argument does not show as a function call, fix that");
		}
		
		if(this.isExpired())
		{
			result.push("/*");
			result.add("*/");
		}

		return result;
	}
}
