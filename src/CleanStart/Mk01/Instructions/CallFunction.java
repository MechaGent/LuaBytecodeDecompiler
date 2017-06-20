package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class CallFunction extends BaseInstruction
{
	private final Var_Ref startingFunction;
	private final boolean ArgsAreVariable;
	private final Var_Ref[] Args;
	private final boolean ReturnsAreVariable;
	private final Var_Ref[] Returns;
	
	private CallFunction(int line, int subLine, int startStep, Var_Ref inStartingFunction, boolean inArgsAreVariable, Var_Ref[] inArgs, boolean inReturnsAreVariable, Var_Ref[] inReturns)
	{
		//numSteps = (1 xR) + (inArgs.length xR) + (inReturns.length xWi)
		super(IrCodes.CallFunction, line, subLine, startStep, 1 + inArgs.length + inReturns.length);
		this.startingFunction = inStartingFunction;
		this.ArgsAreVariable = inArgsAreVariable;
		this.Args = inArgs;
		this.ReturnsAreVariable = inReturnsAreVariable;
		this.Returns = inReturns;
	}
	
	public static CallFunction getInstance(int line, int subLine, int startStep, Var_Ref inStartingFunction, boolean inArgsAreVariable, Var_Ref[] inArgs, boolean inReturnsAreVariable, Var_Ref[] inReturns)
	{
		final CallFunction result = new CallFunction(line, subLine, startStep, inStartingFunction, inArgsAreVariable, inArgs, inReturnsAreVariable, inReturns);
		result.handleInstructionIo();
		return result;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		this.startingFunction.logReadAtTime(this.sigTimes[stepIndex++]);
		
		for(Var_Ref arg: this.Args)
		{
			arg.logReadAtTime(this.sigTimes[stepIndex++]);
		}
		
		for(Var_Ref Return: this.Returns)
		{
			Return.logIndeterminateWriteAtTime(this.sigTimes[stepIndex++]);
		}
	}

	public Var_Ref getStartingFunction()
	{
		return this.startingFunction;
	}

	public boolean isArgsAreVariable()
	{
		return this.ArgsAreVariable;
	}

	public Var_Ref[] getArgs()
	{
		return this.Args;
	}

	public boolean isReturnsAreVariable()
	{
		return this.ReturnsAreVariable;
	}

	public Var_Ref[] getReturns()
	{
		return this.Returns;
	}
	
	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList in)
	{
		in.add('\t', inOffset);
		
		in.add("calling function '");
		in.add(this.startingFunction.getEvaluatedValueAsStringAtTime(this.sigTimes[0]));
		in.add("'(");
		
		for(int i = 0; i < this.Args.length; i++)
		{
			if(i != 0)
			{
				in.add(", ");
			}
			
			in.add(this.Args[i].getEvaluatedValueAsStringAtTime(this.sigTimes[0]));
		}
		
		in.add("), which returns {");
		
		for(int i = 0; i < this.Returns.length; i++)
		{
			if(i != 0)
			{
				in.add(", ");
			}
			
			in.add(this.Returns[i].getEvaluatedValueAsStringAtTime(this.sigTimes[this.sigTimes.length - 1]));
		}
		
		in.add('}');
		
		return in;
	}
}
