package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.InstancedVariable;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.Formula;

public class Var_InstancedFormula extends BaseVariable implements InstancedVariable
{
	private final Formula core;

	public Var_InstancedFormula(String inMangledName, StepStage inTimeOfCreation, Formula inCore)
	{
		super(inMangledName, inTimeOfCreation);
		this.core = inCore;
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.getEvaluatedValueAsString();
	}
	
	@Override
	public String getEvaluatedValueAsString()
	{
		return this.core.toCharList(this.timeOfCreation).toString();
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return this.getEvaluatedValue();
	}
	
	@Override
	public Variable getEvaluatedValue()
	{
		return this;
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.InstancedFormula;
	}

	@Override
	public Comparisons compareWith(Variable inIn, StepStage inTime)
	{
		return super.compareWith_base(inIn);
	}

	@Override
	public String valueToString()
	{
		return this.core.toCharList(this.timeOfCreation).toString();
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
