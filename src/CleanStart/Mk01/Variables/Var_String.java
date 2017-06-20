package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_String extends BaseVariable
{
	private final String cargo;
	private final boolean isLabel;	//surround with quotes when toString()ing if false
	
	public Var_String(String inMangledName, StepStage inCreationTime, String inCargo, boolean inIsLabel)
	{
		super(inMangledName, inCreationTime);
		this.cargo = inCargo;
		this.isLabel = inIsLabel;
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
		return VariableTypes.Constant_String;
	}
	
	@Override
	public String valueToString()
	{
		if(this.isLabel)
		{
			return this.cargo;
		}
		else
		{
			return '"' + this.cargo + '"';
		}
	}

	public String getCargo()
	{
		return this.cargo;
	}

	public boolean isLabel()
	{
		return this.isLabel;
	}
	
	public boolean equals(BaseVariable in)
	{
		if(in.getType() == VariableTypes.Constant_String)
		{
			return this.equals((Var_String) in);
		}
		else
		{
			return super.equals(in);
		}
	}
	
	public boolean equals(Var_String in)
	{
		return !(this.isLabel ^ in.isLabel) && this.cargo.equals(in.cargo);
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		final Comparisons result;
		
		switch(in.getType())
		{
			case Constant_String:
			{
				if(((Var_String) in).equals(this))
				{
					result = Comparisons.SameType_SameValue;
				}
				else
				{
					result = Comparisons.SameType_DifferentValue;
				}
				
				break;
			}
			default:
			{
				result = super.compareWith_base(in);
				break;
			}
		}
		
		return result;
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
