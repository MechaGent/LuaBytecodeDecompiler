package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_Bool extends BaseVariable
{
	private static final Var_Bool TRUE = new Var_Bool("Var_Bool:TRUE", StepStage.getNullStepStage(), true);
	private static final Var_Bool FALSE = new Var_Bool("Var_Bool:FALSE", StepStage.getNullStepStage(), false);
	
	private final boolean cargo;
	
	private Var_Bool(String inMangledName, StepStage inTimeOfCreation, boolean inCargo)
	{
		super(inMangledName, inTimeOfCreation);
		this.cargo = inCargo;
	}
	
	public static Var_Bool getInstance(boolean value)
	{
		if(value)
		{
			return TRUE;
		}
		else
		{
			return FALSE;
		}
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.valueToString();
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return this;
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.Constant_Bool;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		if(in.getType() == VariableTypes.Constant_Bool)
		{
			if(this.cargo == ((Var_Bool) in).cargo)
			{
				return Comparisons.SameType_SameValue;
			}
			else
			{
				return Comparisons.SameType_DifferentValue;
			}
		}
		else
		{
			return super.compareWith_base(in);
		}
	}

	@Override
	public String valueToString()
	{
		return Boolean.toString(this.cargo);
	}
	
	public boolean getValue()
	{
		return this.cargo;
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
