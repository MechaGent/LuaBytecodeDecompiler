package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class TailCall extends BaseInstruction
{
	private final Var_Ref functionObject;
	private final Var_Ref[] Args;
	private final boolean argsAreVariable;
	
	public TailCall(int line, int subLine, int startStep, Var_Ref inFunctionObject, Var_Ref[] inArgs, boolean inArgsAreVariable)
	{
		super(IrCodes.Tailcall, line, subLine, startStep, 1 + inArgs.length);
		this.functionObject = inFunctionObject;
		this.Args = inArgs;
		this.argsAreVariable = inArgsAreVariable;
	}
	
	public static TailCall getInstance(int line, int subLine, int startStep, Var_Ref inFunctionObject, Var_Ref[] inArgs, boolean inArgsAreVariable)
	{
		final TailCall result = new TailCall(line, subLine, startStep, inFunctionObject, inArgs, inArgsAreVariable);
		result.handleInstructionIo();
		return result;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		this.functionObject.logReadAtTime(this.sigTimes[stepIndex++]);
		
		for(Var_Ref ref: this.Args)
		{
			ref.logReadAtTime(this.sigTimes[stepIndex++]);
		}
	}

	public Var_Ref getFunctionObject()
	{
		return this.functionObject;
	}

	public Var_Ref[] getArgs()
	{
		return this.Args;
	}

	public boolean isArgsAreVariable()
	{
		return this.argsAreVariable;
	}
}
