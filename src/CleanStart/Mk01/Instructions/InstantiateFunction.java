package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.FunctionObject;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class InstantiateFunction extends BaseInstruction
{
	private final FunctionObject functionPrototype;
	private final Var_Ref[] upvalues;
	private final Var_Ref functionInstance;
	
	private InstantiateFunction(int line, int subLine, int startStep, Var_Ref inFunctionInstance, FunctionObject inFunctionPrototype, Var_Ref[] inUpvalues)
	{
		super(IrCodes.CreateFunctionInstance, line, subLine, startStep, 1 + inUpvalues.length + inUpvalues.length);
		this.functionInstance = inFunctionInstance;
		this.functionPrototype = inFunctionPrototype;
		this.upvalues = inUpvalues;
	}
	
	public static InstantiateFunction getInstance(int line, int subLine, int startStep, Var_Ref inFunctionInstance, FunctionObject inFunctionPrototype, Var_Ref[] inUpvalues)
	{
		final InstantiateFunction result = new InstantiateFunction(line, subLine, startStep, inFunctionInstance, inFunctionPrototype, inUpvalues);
		result.handleInstructionIo();
		return result;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref ref: this.upvalues)
		{
			ref.logReadAtTime(this.sigTimes[stepIndex++]);
		}
		
		for(Var_Ref ref: this.upvalues)
		{
			ref.logInternalStateChangeAtTime(this.sigTimes[stepIndex++]);
		}
		
		this.functionInstance.logWriteAtTime(this.sigTimes[stepIndex++], this.functionPrototype);
	}
}
