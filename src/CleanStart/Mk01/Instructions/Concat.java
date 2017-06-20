package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class Concat extends BaseInstruction
{
	private final Var_Ref target;
	private final Var_Ref[] sources;
	
	private Concat(int line, int subLine, int startStep, Var_Ref inTarget, Var_Ref[] inSources)
	{
		//numSteps = (inSources.length xR) + (1 xWi)
		super(IrCodes.Concat, line, subLine, startStep, inSources.length + 1);
		this.target = inTarget;
		this.sources = inSources;
	}
	
	public static Concat getInstance(int line, int subLine, int startStep, Var_Ref inTarget, Var_Ref[] inSources)
	{
		final Concat result = new Concat(line, subLine, startStep, inTarget, inSources);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref getTarget()
	{
		return this.target;
	}

	public Var_Ref[] getSources()
	{
		return this.sources;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref source: this.sources)
		{
			source.logReadAtTime(this.sigTimes[stepIndex++]);
		}
		
		this.target.logIndeterminateWriteAtTime(this.sigTimes[stepIndex++]);
	}
}
