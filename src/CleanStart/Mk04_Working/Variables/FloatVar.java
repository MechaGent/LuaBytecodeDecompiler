package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.SpecialInstanceNums;

public class FloatVar extends BaseVar_Final
{
	private final float cargo;

	private FloatVar(String inNamePrefix, int inInstanceNum, float inCargo)
	{
		super(inNamePrefix, VarTypes.Float, inInstanceNum, null);
		this.cargo = inCargo;
	}
	
	private FloatVar(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, float inCargo)
	{
		super(inNamePrefix, VarTypes.Float, inInstanceNum, inAssignmentInstruction);
		this.cargo = inCargo;
	}
	
	public static FloatVar getInstance(String inNamePrefix, int inInstanceNum, float inCargo)
	{
		return new FloatVar(inNamePrefix, inInstanceNum, inCargo);
	}
	
	public static FloatVar getInstance(String inNamePrefix, int inInstanceNum, Instruction inAssignmentInstruction, float inCargo)
	{
		return new FloatVar(inNamePrefix, inInstanceNum, inAssignmentInstruction, inCargo);
	}
	
	public static FloatVar getInstance(String inNamePrefix, Instruction inAssignmentInstruction, float inCargo)
	{
		return new FloatVar(inNamePrefix, SpecialInstanceNums.tempInstanceNum.getValue(), inAssignmentInstruction, inCargo);
	}

	@Override
	public CharList getValueAsCharList()
	{
		return new CharList(Float.toString(this.cargo));
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String valueToString()
	{
		return Float.toString(this.cargo);
	}
}
