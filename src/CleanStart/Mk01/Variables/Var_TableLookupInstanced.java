package CleanStart.Mk01.Variables;

import CharList.CharList;
import CleanStart.Mk01.BaseVariable;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;

public class Var_TableLookupInstanced extends BaseVariable
{
	private final Var_InstancedRef tableInstance;
	private final Variable indexerInstance;
	
	public Var_TableLookupInstanced(String inMangledName, StepStage inTimeOfCreation, Var_Ref inTableInstance, Variable inIndexerInstance)
	{
		this(inMangledName, inTimeOfCreation, new Var_InstancedRef(inTimeOfCreation, inTableInstance), inIndexerInstance);
	}
	
	public Var_TableLookupInstanced(String inMangledName, StepStage inTimeOfCreation, Var_InstancedRef inTableInstance, Variable inIndexerInstance)
	{
		super(inMangledName, inTimeOfCreation);
		this.tableInstance = inTableInstance;
		this.indexerInstance = inIndexerInstance;
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
		return VariableTypes.InstancedReference_Indexed;
	}

	@Override
	public Comparisons compareWith(Variable in, StepStage inTime)
	{
		return super.compareWith_base(in);
	}

	@Override
	public String valueToString()
	{
		final CharList result = new CharList();
		
		result.add(this.tableInstance.valueToString());
		result.add(".getAt(");
		result.add(this.indexerInstance.valueToString());
		result.add(')');
		
		return result.toString();
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
}
