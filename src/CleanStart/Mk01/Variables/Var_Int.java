package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.FormulaToken;

public class Var_Int extends BaseVariable implements FormulaToken
{
	private final int cargo;
	
	public Var_Int(String inMangledName, StepStage inTimeOfCreation, int inCargo)
	{
		super(inMangledName, inTimeOfCreation);
		this.cargo = inCargo;
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
		return VariableTypes.Constant_Int;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		switch(in.getType())
		{
			case Constant_Float:
			{
				final Var_Float cast = (Var_Float) in;
				
				if(this.cargo == cast.getValue())
				{
					return Comparisons.SameType_SameValue;
				}
				else
				{
					return Comparisons.SameType_DifferentValue;
				}
			}
			case Constant_Int:
			{
				final Var_Int cast = (Var_Int) in;
				
				if(this.cargo == cast.cargo)
				{
					return Comparisons.SameType_SameValue;
				}
				else
				{
					return Comparisons.SameType_DifferentValue;
				}
			}
			case Constant_Bool:
			{
				final Var_Bool cast = (Var_Bool) in;
				
				if(cast.getValue())
				{
					if(this.cargo == 1.0f)
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
					if(this.cargo == 0.0f)
					{
						return Comparisons.SameType_SameValue;
					}
					else
					{
						return Comparisons.SameType_DifferentValue;
					}
				}
			}
			default:
			{
				return super.compareWith_base(in);
			}
		}
	}

	@Override
	public String valueToString()
	{
		return Integer.toString(this.cargo);
	}
	
	public int getValue()
	{
		return this.cargo;
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}

	@Override
	public String valueToStringAtTime(StepStage inTime)
	{
		return this.getEvaluatedValueAsStringAtTime(inTime);
	}
}
