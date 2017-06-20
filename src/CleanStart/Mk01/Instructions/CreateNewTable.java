package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;
import CleanStart.Mk01.Variables.Var_Table;

public class CreateNewTable extends BaseInstruction
{
	private final Var_Ref target;
	private final Var_Table source;

	private CreateNewTable(int line, int subLine, int startStep, Var_Ref inTarget, Var_Table inSource)
	{
		//numSteps = (1 xW)
		super(IrCodes.CreateNewTable, line, subLine, startStep, 1);
		this.target = inTarget;
		this.source = inSource;
	}
	
	public static CreateNewTable getInstance(int line, int subLine, int startStep, Var_Ref inTarget, Var_Table inSource)
	{
		final CreateNewTable result = new CreateNewTable(line, subLine, startStep, inTarget, inSource);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref getTarget()
	{
		return this.target;
	}

	public Var_Table getSource()
	{
		return this.source;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		this.target.logWriteAtTime(this.sigTimes[stepIndex++], this.source);
	}
}
