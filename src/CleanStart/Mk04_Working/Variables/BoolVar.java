package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.SpecialInstanceNums;

public class BoolVar extends BaseVar_Final
{
	private final boolean cargo;

	private BoolVar(String inNamePrefix, int inInstanceNum, boolean inCargo)
	{
		super(inNamePrefix, VarTypes.Bool, inInstanceNum, null);
		this.cargo = inCargo;
	}
	
	private BoolVar(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, boolean inCargo)
	{
		super(inNamePrefix, VarTypes.Bool, inInstanceNum, inAssignmentInstruction);
		this.cargo = inCargo;
	}
	
	public static BoolVar getInstance(String inNamePrefix, int inInstanceNum, boolean inCargo)
	{
		return new BoolVar(inNamePrefix, inInstanceNum, inCargo);
	}
	
	public static BoolVar getInstance(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, boolean inCargo)
	{
		return new BoolVar(inNamePrefix, inInstanceNum, inAssignmentInstruction, inCargo);
	}
	
	public static BoolVar getInstance(String inNamePrefix, Instruction inAssignmentInstruction, boolean inCargo)
	{
		return new BoolVar(inNamePrefix, SpecialInstanceNums.tempInstanceNum.getValue(), inAssignmentInstruction, inCargo);
	}

	@Override
	public CharList getValueAsCharList()
	{
		return new CharList(Boolean.toString(this.cargo));
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String valueToString()
	{
		return Boolean.toString(this.cargo);
	}
}
