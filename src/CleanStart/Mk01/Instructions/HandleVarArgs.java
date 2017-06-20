package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class HandleVarArgs extends BaseInstruction
{
	private final Var_Ref[] targets;	//actual sources are indeterminate
	private final boolean copyAllPossibles;

	private HandleVarArgs(int line, int subLine, int startStep, Var_Ref[] inTargets, boolean inCopyAllPossibles)
	{
		super(IrCodes.VarArgsHandler, line, subLine, startStep, inTargets.length);
		this.targets = inTargets;
		this.copyAllPossibles = inCopyAllPossibles;
	}

	public static HandleVarArgs getInstance(int line, int subLine, int startStep, Var_Ref[] inTargets, boolean inCopyAllPossibles)
	{
		final HandleVarArgs result = new HandleVarArgs(line, subLine, startStep, inTargets, inCopyAllPossibles);
		result.handleInstructionIo();
		return result;
	}
	
	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref ref: this.targets)
		{
			ref.logIndeterminateWriteAtTime(this.sigTimes[stepIndex++]);
		}
	}

	public Var_Ref[] getTargets()
	{
		return this.targets;
	}

	public boolean isCopyAllPossibles()
	{
		return this.copyAllPossibles;
	}
}
