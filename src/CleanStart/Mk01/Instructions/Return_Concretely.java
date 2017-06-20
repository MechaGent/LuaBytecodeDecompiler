package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class Return_Concretely extends BaseInstruction
{
	private final Var_Ref[] sources;

	private Return_Concretely(int line, int subLine, int startStep, Var_Ref[] inSources)
	{
		super(IrCodes.Return_Concretely, line, subLine, startStep, inSources.length);
		this.sources = inSources;
	}
	
	public static Return_Concretely getInstance(int line, int subLine, int startStep, Var_Ref[] inSources)
	{
		final Return_Concretely result = new Return_Concretely(line, subLine, startStep, inSources);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref[] getSources()
	{
		return this.sources;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref ref: this.sources)
		{
			ref.logReadAtTime(this.sigTimes[stepIndex++]);
		}
	}
	
	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList in)
	{
		in.add('\t', inOffset);
		in.add("return {");
		
		for(int i = 0; i < this.sources.length; i++)
		{
			if(i != 0)
			{
				in.add(", ");
			}
			
			in.add(this.sources[i].getEvaluatedValueAsStringAtTime(this.sigTimes[this.sigTimes.length - 1]));
		}
		
		in.add("} to calling function");
		
		return in;
	}
}
