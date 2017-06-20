package CleanStart.Mk06.SubVars;

import CharList.CharList;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Var;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.RawEnums;
import CleanStart.Mk06.StepNum.ForkBehaviors;

public class MethodVar extends Var
{
	private final StepNum startTime;
	private final VarRef subjectObject;
	private final String methodName;
	private final boolean ArgsAreVariable;
	private final VarRef[] Args;
	private final boolean ReturnsAreVariable;
	private final VarRef[] Returns;

	private MethodVar(String inNamePrefix, StepNum inStartTime, VarRef inSubjectObject, String inMethodName, boolean inArgsAreVariable, VarRef[] inArgs, boolean inReturnsAreVariable, VarRef[] inReturns)
	{
		super(inNamePrefix, RawEnums.WrapperVar.getValue());
		
		this.startTime = inStartTime;
		this.subjectObject = inSubjectObject;
		this.methodName = inMethodName;
		this.ArgsAreVariable = inArgsAreVariable;
		this.Args = inArgs;
		this.ReturnsAreVariable = inReturnsAreVariable;
		this.Returns = inReturns;
	}

	public static MethodVar getInstance(String inNamePrefix, StepNum inStartTime, VarRef inSubjectObject, String inMethodName, boolean inArgsAreVariable, VarRef[] inArgs, boolean inReturnsAreVariable, VarRef[] inReturns)
	{
		final MethodVar result = new MethodVar(inNamePrefix, inStartTime, inSubjectObject, inMethodName, inArgsAreVariable, inArgs, inReturnsAreVariable, inReturns);
		
		result.logAllReadsAndWrites();
		
		return result;
	}
	
	public String getMangledName(boolean appendMetadata)
	{
		return "Method" + super.getMangledName(false);
	}
	
	private void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);

		for (int i = 0; i < this.Args.length; i++)
		{
			//this.Args[i].logReadAtTime(current, InferenceTypes.Mystery);
			this.Args[i].logStateChangeAtTime(InstructionIndex.getInstance(null, i), current);
			current = current.fork(ForkBehaviors.IncrementLast);
		}
		
		if(this.subjectObject != null)
		{
			this.subjectObject.logStateChangeAtTime(InstructionIndex.getInstance(null, this.Args.length + 2), current);
		}
		
		current = current.fork(ForkBehaviors.IncrementLast);
		
		for(int i = 0; i < this.Returns.length; i++)
		{
		this.Returns[i].logStateChangeAtTime(InstructionIndex.getInstance(null, this.Args.length + 3 + i), current);
		}
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.Mystery;
	}

	@Override
	public String getValueAsString()
	{
		final CharList result = new CharList();

		result.add(this.namePrefix);

		if (this.subjectObject != null)
		{
			result.add('\'');
			result.add(this.subjectObject.getValueAtTime(this.startTime).getValueAsString());
			result.add("'.");
		}

		result.add(this.methodName);
		result.add('(');

		for (int i = 0; i < this.Args.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Args[i].getMangledName());
		}

		if (this.ArgsAreVariable)
		{
			result.add("...");
		}

		result.add(')');
		
		/*
		result.add("\t//\t");
		result.add('{');

		for (int i = 0; i < this.Returns.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Returns[i].getMangledName());
		}

		if (this.ReturnsAreVariable)
		{
			result.add("...");
		}

		result.add('}');
		*/

		return result.toString();
	}

	@Override
	public int getValueAsInt()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public float getValueAsFloat()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getValueAsBoolean()
	{
		throw new UnsupportedOperationException();
	}
}
