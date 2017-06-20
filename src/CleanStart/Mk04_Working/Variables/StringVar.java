package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;

public class StringVar extends BaseVar_Final
{
	private final String cargo;
	
	private StringVar(String inNamePrefix, int inInstanceNumber, String inCargo)
	{
		super(inNamePrefix, VarTypes.String, inInstanceNumber, null);
		this.cargo = inCargo;
	}
	
	private StringVar(String inNamePrefix, int inInstanceNumber, Instruction inAssigner, String inCargo)
	{
		super(inNamePrefix, VarTypes.String, inInstanceNumber, inAssigner);
		this.cargo = inCargo;
	}
	
	public static StringVar getInstance(String inNamePrefix, int inInstanceNumber , String inCargo)
	{
		return new StringVar(inNamePrefix, inInstanceNumber, inCargo);
	}
	
	public static StringVar getInstance(String inNamePrefix, int inInstanceNumber, Instruction inAssigner, String inCargo)
	{
		return new StringVar(inNamePrefix, inInstanceNumber, inAssigner, inCargo);
	}
	
	public String getCargo()
	{
		return this.cargo;
	}

	@Override
	public CharList getValueAsCharList()
	{
		final CharList result = new CharList();
		
		result.add('"');
		result.add(this.cargo);
		result.add('"');
		
		return result;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String valueToString()
	{
		return '"' + this.cargo + '"';
	}
	
	@Override
	public String getMostRelevantIdValue(boolean showInferredType)
	{
		return '"' + this.cargo + '"';
	}
}
