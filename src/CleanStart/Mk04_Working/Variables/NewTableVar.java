package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;

public class NewTableVar extends BaseVar
{
	private final int arrLength;
	private final int hashSize;

	private NewTableVar(String inNamePrefix, Instruction inAssignmentInstruction, int inArrLength, int inHashSize)
	{
		super(inNamePrefix, VarTypes.TableObject, inAssignmentInstruction);
		this.arrLength = inArrLength;
		this.hashSize = inHashSize;
	}
	
	public static NewTableVar getInstance(String inNamePrefix, int inArrLength, int inHashSize)
	{
		return getInstance(inNamePrefix, null, inArrLength, inHashSize);
	}
	
	public static NewTableVar getInstance(String inNamePrefix, Instruction inAssignmentInstruction, int inArrLength, int inHashSize)
	{
		return new NewTableVar(inNamePrefix, inAssignmentInstruction, inArrLength, inHashSize);
	}

	public int getArrLength()
	{
		return this.arrLength;
	}

	public int getHashSize()
	{
		return this.hashSize;
	}

	@Override
	public CharList getValueAsCharList()
	{
		final CharList result = new CharList();
		
		result.add("<> <NewTableVar>");
		
		return result;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Once;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public String valueToString()
	{
		return this.getValueAsCharList().toString();
	}
}
