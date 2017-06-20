package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.Variables.Var_Ref;

public class SetVar_Bulk extends BaseInstruction
{
	private final Var_Ref[] targets;
	private final Variable source;
	
	private SetVar_Bulk(int line, int subLine, int startStep, Var_Ref[] inTargets, Variable inSource)
	{
		super(IrCodes.SetVar_Bulk, line, subLine, startStep, 1 + inTargets.length);
		this.targets = inTargets;
		this.source = inSource;
	}
	
	public static SetVar_Bulk getInstance(int line, int subLine, int startStep, Var_Ref[] inTargets, Variable inSource)
	{
		final SetVar_Bulk result = new SetVar_Bulk(line, subLine, startStep, inTargets, inSource);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref[] getTargets()
	{
		return this.targets;
	}

	public Variable getSource()
	{
		return this.source;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		if(this.source.getType() == VariableTypes.Reference)
		{
			((Var_Ref) this.source).logReadAtTime(this.sigTimes[stepIndex]);
		}
		
		stepIndex++;
		
		for(Var_Ref target: targets)
		{
			target.logWriteAtTime(this.sigTimes[stepIndex++], this.source);
		}
	}
}
