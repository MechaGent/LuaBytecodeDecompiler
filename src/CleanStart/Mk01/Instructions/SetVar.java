package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.Variables.Var_Ref;

public class SetVar extends BaseInstruction
{
	private final Variable target;
	private final Variable source;

	private SetVar(int line, int subLine, int startStep, Variable inTarget, Variable inSource)
	{
		super(IrCodes.SetVar, line, subLine, startStep, 1 + 1);
		this.target = inTarget;

		if (inSource.getType() == VariableTypes.Reference)
		{
			final Variable tempSource = ((Var_Ref) inSource).getEvaluatedValueAtTime(this.sigTimes[0]);
			
			if(tempSource == Var_Ref.getIndeterminateValue())
			{
				this.source = inSource;
			}
			else
			{
				this.source = tempSource;
			}
		}
		else
		{
			this.source = inSource;
		}
	}

	public static SetVar getInstance(int line, int subLine, int startStep, Variable inTarget, Variable inSource)
	{
		final SetVar result = new SetVar(line, subLine, startStep, inTarget, inSource);
		result.handleInstructionIo();
		return result;
	}

	public Variable getTarget()
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
		int stepIndex = 1;

		if (this.source.getType() == VariableTypes.Reference)
		{
			((Var_Ref) this.source).logReadAtTime(this.sigTimes[stepIndex++]);
		}

		if (this.target.getType() == VariableTypes.Reference)
		{
			final boolean wasNeeded = ((Var_Ref) this.target).logWriteAtTime(this.sigTimes[stepIndex++], this.source);

			if (!wasNeeded)
			{
				this.setExpiredState(true);
			}
		}
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList result)
	{
		result.add('\t', inOffset);
		result.add(this.OpCode.toString());
		result.add('\t');
		//result.add(this.target.getMangledName());
		//result.add('(');
		result.add(this.target.getEvaluatedValueAsStringAtTime(this.sigTimes[0]));
		//result.add(')');
		// result.add(this.target.getEvaluatedValueAsStringAtTime(this.timeOfCreation));

		result.add(" = ");
		// result.add(this.source.getMangledName());
		// result.add('(');
		result.add(this.source.getEvaluatedValueAsStringAtTime(this.sigTimes[this.sigTimes.length - 1]));
		// result.add(')');
		result.add("\t// via ");
		result.add(this.source.getMangledName());

		return result;
	}
}
