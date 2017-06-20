package CleanStart.Mk01;

import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.Variables.Var_Ref;
import CleanStart.Mk01.Variables.Var_String;

public abstract class BaseVariable implements Variable
{
	protected static final Variable initialVar = new Var_String("InitialValueVar", StepStage.getNullStepStage(), "<default initial value>", true);
	protected static int instanceCounter = 0;
	
	protected final String mangledName;
	protected final StepStage timeOfCreation;
	
	public BaseVariable(String inMangledName, StepStage inTimeOfCreation)
	{
		this.mangledName = inMangledName;
		this.timeOfCreation = inTimeOfCreation;
	}

	@Override
	public String getMangledName()
	{
		return this.mangledName;
	}

	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Override
	public boolean isOperand()
	{
		return true;
	}

	@Override
	public int getPrecedence()
	{
		return -1;
	}

	protected Comparisons compareWith_base(Variable in)
	{
		if (this == in)
		{
			return Comparisons.SameType_SameValue;
		}
		else if (this.getType() == in.getType())
		{
			return Comparisons.SameType_DifferentValue;
		}
		else
		{
			return Comparisons.DifferentType;
		}
	}
	
	protected String getIndeterminantValue()
	{
		return this.mangledName + "@<indeterminant time!>";
	}
	
	@Override
	public String valueToStringAtTime(StepStage inTime)
	{
		final String result = this.getEvaluatedValueAsStringAtTime(inTime);
		
		if(result == null)
		{
			throw new IllegalArgumentException("null return from type: " + this.getType().toString());
		}
		
		return result;
	}

	public static final Variable[] getArray_NullInit(int length, StepStage time)
	{
		final Variable[] result = new Variable[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = new Var_Null(time);
		}

		return result;
	}

	public static final Var_Ref[] getRefArray_NullInit(String[] names, StepStage time)
	{
		final Var_Ref[] result = new Var_Ref[names.length];

		for (int i = 0; i < names.length; i++)
		{
			result[i] = new Var_Ref(names[i], time, new Var_Null(time));
		}

		return result;
	}

	public static final Var_Ref[] getRefArray_NullInit(int length, String namePrefix, int nameStartOffset, StepStage time)
	{
		final Var_Ref[] result = new Var_Ref[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = new Var_Ref(namePrefix + (nameStartOffset + i), time, new Var_Null(time));
		}

		return result;
	}

	public static class Var_Null extends BaseVariable
	{
		private static int instanceCounter = 0;
		
		public Var_Null(StepStage inTimeOfCreation)
		{
			super("NullVar_" + instanceCounter++, inTimeOfCreation);
		}

		@Override
		public String getEvaluatedValueAsStringAtTime(StepStage inTime)
		{
			return "<Null value>";
		}

		@Override
		public Variable getEvaluatedValueAtTime(StepStage inTime)
		{
			return this;
		}

		@Override
		public VariableTypes getType()
		{
			return VariableTypes.Constant_Null;
		}

		@Override
		public Comparisons compareWith(Variable in, StepStage inTime)
		{
			if (in == this)
			{
				return Comparisons.SameType_SameValue;
			}
			else
			{
				return Comparisons.DifferentType;
			}
		}

		@Override
		public String valueToString()
		{
			return "<Null value>";
		}

		@Override
		public boolean isConstant()
		{
			return true;
		}
	}
}
