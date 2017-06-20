package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class CopyArray extends BaseInstruction
{
	private final int offsetOffset;
	private final Var_Ref targetTable;
	private final Var_Ref[] sourceArray;
	
	private CopyArray(int line, int subLine, int startStep, int inOffsetOffset, Var_Ref inTargetArray, Var_Ref[] inSourceArray)
	{
		//numSteps = (inSourceArray.length x S) * 2
		super(IrCodes.CopyArray, line, subLine, startStep, inSourceArray.length * 2);
		this.offsetOffset = inOffsetOffset;
		this.targetTable = inTargetArray;
		this.sourceArray = inSourceArray;
	}

	public static CopyArray getInstance(int line, int subLine, int startStep, int inOffsetOffset, Var_Ref inTargetArray, Var_Ref[] inSourceArray)
	{
		final CopyArray result = new CopyArray(line, subLine, startStep, inOffsetOffset, inTargetArray, inSourceArray);
		result.handleInstructionIo();
		return result;
	}
	
	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref source: this.sourceArray)
		{
			source.logReadAtTime(this.sigTimes[stepIndex++]);
			this.targetTable.logInternalStateChangeAtTime(this.sigTimes[stepIndex++]);
		}
	}

	public int getOffsetOffset()
	{
		return this.offsetOffset;
	}

	public Var_Ref getTargetTable()
	{
		return this.targetTable;
	}

	public Var_Ref[] getSourceArray()
	{
		return this.sourceArray;
	}
}
