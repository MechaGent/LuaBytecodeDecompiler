package CleanStart.Mk04_Working.Variables;

import CleanStart.Mk04_Working.Instruction;

public abstract class BaseVar_Final extends BaseVar
{
	public BaseVar_Final(String inNamePrefix, VarTypes inVarType, Instruction inAssignmentInstruction)
	{
		super(inNamePrefix, inVarType, inAssignmentInstruction);
	}

	public BaseVar_Final(String inNamePrefix, VarTypes inVarType, int inInstanceNumber, Instruction inAssignmentInstruction)
	{
		super(inNamePrefix, inVarType, inInstanceNumber, inAssignmentInstruction);
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public String getMostRelevantIdValue()
	{
		return this.valueToString();
		//return this.getMangledName();
	}
}
