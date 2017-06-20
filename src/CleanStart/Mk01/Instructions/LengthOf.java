package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.Variables.Var_Ref;

public class LengthOf extends BaseInstruction
{
	private final Var_Ref target;
	private final Variable source;
	
	private LengthOf(int line, int subLine, int startStep, Var_Ref inTarget, Variable inSource)
	{
		super(IrCodes.LengthOf, line, subLine, startStep, 1);
		this.target = inTarget;
		this.source = inSource;
	}
	
	public static LengthOf getInstance(int line, int subLine, int startStep, Var_Ref inTarget, Variable inSource)
	{
		final LengthOf result = new LengthOf(line, subLine, startStep, inTarget, inSource);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref getTarget()
	{
		return this.target;
	}

	public Variable getSource()
	{
		return this.source;
	}

	@Override
	protected void handleInstructionIo()
	{
		this.target.logWriteAtTime(this.sigTimes[1], this.source.getEvaluatedValueAtTime(this.sigTimes[0]));
	}
}
