package CleanStart.Mk04_Working.Variables;

import CleanStart.Mk04_Working.Instruction;

public abstract class BaseVar_Inlined extends BaseVar
{
	protected final int subInstanceNumber;

	public BaseVar_Inlined(String inNamePrefix, VarTypes inVarType, int inInstanceNumber, int inSubInstanceNumber, Instruction inAssignmentInstruction)
	{
		super(inNamePrefix, inVarType, inInstanceNumber, inAssignmentInstruction);
		this.subInstanceNumber = inSubInstanceNumber;
	}
	
	@Override
	public String getMangledName()
	{
		return this.namePrefix + '_' + this.instanceNumber + '_' + this.subInstanceNumber + '(' + this.varType.toString() + ')';
	}
}
