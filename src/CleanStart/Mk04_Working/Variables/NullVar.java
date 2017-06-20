package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.SpecialInstanceNums;

public class NullVar extends BaseVar_Final
{
	private static final NullVar sharedInstance = new NullVar("<shared nullVar instance>", SpecialInstanceNums.staticInstanceNum.getValue(), null);
	
	private NullVar(String inNamePrefix, int inInstanceNumber, Instruction inAssignmentInstruction)
	{
		super(inNamePrefix, VarTypes.Null, inInstanceNumber, inAssignmentInstruction);
	}
	
	public static NullVar getInstance()
	{
		return sharedInstance;
	}
	
	public static NullVar getInstance(String inNamePrefix)
	{
		return getInstance(inNamePrefix, SpecialInstanceNums.tempInstanceNum.getValue());
	}
	
	public static NullVar getInstance(String inNamePrefix, int inInstanceNumber)
	{
		return getInstance(inNamePrefix, inInstanceNumber, null);
	}
	
	public static NullVar getInstance(String inNamePrefix, int inInstanceNumber, Instruction inAssignmentInstruction)
	{
		return new NullVar(inNamePrefix, inInstanceNumber, inAssignmentInstruction);
	}

	@Override
	public CharList getValueAsCharList()
	{
		return new CharList(this.valueToString());
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String valueToString()
	{
		return "<Null Value>";
	}
}
