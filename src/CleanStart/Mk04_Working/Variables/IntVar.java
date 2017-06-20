package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.SpecialInstanceNums;

public class IntVar extends BaseVar_Final
{
	private final int cargo;

	private IntVar(String inNamePrefix, int inInstanceNum, int inCargo)
	{
		super(inNamePrefix, VarTypes.Int, inInstanceNum, null);
		this.cargo = inCargo;
	}
	
	private IntVar(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, int inCargo)
	{
		super(inNamePrefix, VarTypes.Int, inInstanceNum, inAssignmentInstruction);
		this.cargo = inCargo;
	}
	
	public static IntVar getInstance(String inNamePrefix, int inCargo)
	{
		return new IntVar(inNamePrefix, SpecialInstanceNums.tempInstanceNum.getValue(), inCargo);
	}
	
	public static IntVar getInstance(String inNamePrefix, int inInstanceNum, int inCargo)
	{
		return new IntVar(inNamePrefix, inInstanceNum, inCargo);
	}
	
	public static IntVar getInstance(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, int inCargo)
	{
		return new IntVar(inNamePrefix, inInstanceNum, inAssignmentInstruction, inCargo);
	}
	
	public static IntVar getInstance(String inNamePrefix, Instruction inAssignmentInstruction, int inCargo)
	{
		return new IntVar(inNamePrefix, SpecialInstanceNums.tempInstanceNum.getValue(), inAssignmentInstruction, inCargo);
	}

	@Override
	public CharList getValueAsCharList()
	{
		return new CharList(Integer.toString(this.cargo));
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String valueToString()
	{
		return Integer.toString(this.cargo);
	}
}
