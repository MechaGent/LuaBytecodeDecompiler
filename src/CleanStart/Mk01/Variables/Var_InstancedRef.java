package CleanStart.Mk01.Variables;

import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.InstancedVariable;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_InstancedRef extends BaseVariable implements InstancedVariable
{
	protected final Var_Ref pointer;

	public Var_InstancedRef(StepStage inTime, Var_Ref inPointer)
	{
		this(inPointer.getMangledName() + "_instance_", inTime, inPointer);
	}
	
	public Var_InstancedRef(String inMangledName, StepStage inTime, Var_Ref inPointer)
	{
		super(inMangledName, inTime);
		this.pointer = inPointer;
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.getEvaluatedValueAsString();
	}
	
	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return this.getEvaluatedValue();
	}
	
	@Override
	public String getEvaluatedValueAsString()
	{
		return this.pointer.getEvaluatedValueAsStringAtTime(this.timeOfCreation);
	}
	
	@Override
	public Variable getEvaluatedValue()
	{
		return this.pointer.getEvaluatedValueAtTime(this.timeOfCreation);
	}
	
	@Override
	public String valueToString()
	{
		return this.getEvaluatedValueAsStringAtTime(this.timeOfCreation);
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.InstancedReference;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		return this.pointer.compareWith(in, this.timeOfCreation);
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
