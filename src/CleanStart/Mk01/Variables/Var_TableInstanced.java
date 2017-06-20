package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_TableInstanced extends BaseVariable
{
	private final Var_Table pointer;

	public Var_TableInstanced(String inMangledName, StepStage inTimeOfCreation, Var_Table inPointer)
	{
		super(inMangledName, inTimeOfCreation);
		this.pointer = inPointer;
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.mangledName + '@' + this.timeOfCreation.toCharList().toString();
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return this;
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.InstancedTableOfReferences;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		if (in.getType() == VariableTypes.InstancedTableOfReferences)
		{
			return this.compareWith((Var_TableInstanced) in, inTime);
		}
		else
		{
			return super.compareWith_base(in);
		}
	}

	public Comparisons compareWith(Var_TableInstanced in, StepStage inTime)
	{
		final Comparisons result;

		if (this == in)
		{
			result = Comparisons.SameType_SameValue;
		}
		else if (this.pointer == in.pointer)
		{
			result = this.pointer.compareWith(in.pointer, inTime);
		}
		else
		{
			result = Comparisons.SameType_DifferentValue;
		}

		return result;
	}

	@Override
	public String valueToString()
	{
		return super.getIndeterminantValue();
	}

	public Variable attemptToGet(Variable in)
	{
		final Var_Ref result = this.pointer.attemptToGet(in);

		if (result == null)
		{
			throw new NullPointerException();
		}

		return result.getEvaluatedValueAtTime(this.timeOfCreation);
	}

	public Variable attemptToGet(Var_String in)
	{
		final Var_Ref result = this.pointer.attemptToGet(in);

		if (result == null)
		{
			throw new NullPointerException();
		}

		return result.getEvaluatedValueAtTime(this.timeOfCreation);
	}

	public Variable attemptToGet(Var_Int in)
	{
		final Var_Ref result = this.pointer.attemptToGet(in);

		if (result == null)
		{
			throw new NullPointerException();
		}

		return result.getEvaluatedValueAtTime(this.timeOfCreation);
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
