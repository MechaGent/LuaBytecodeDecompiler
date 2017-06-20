package CleanStart.Mk01.Instructions;

import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Variables.Var_Ref;

public class Return_Variably extends BaseInstruction
{
	private final Var_Ref[] sources;
	private final Var_Ref lastExpression;	//should be a function call

	private Return_Variably(int line, int subLine, int startStep, Var_Ref[] inSources, Var_Ref inLastExpression)
	{
		super(IrCodes.Return_Variably, line, subLine, startStep, inSources.length + 1);
		this.sources = inSources;
		this.lastExpression = inLastExpression;
	}
	
	public static Return_Variably getInstance(int line, int subLine, int startStep, Var_Ref[] inSources, Var_Ref inLastExpression)
	{
		final Return_Variably result = new Return_Variably(line, subLine, startStep, inSources, inLastExpression);
		result.handleInstructionIo();
		return result;
	}

	public Var_Ref[] getSources()
	{
		return this.sources;
	}

	public Var_Ref getLastExpression()
	{
		return this.lastExpression;
	}

	@Override
	protected void handleInstructionIo()
	{
		int stepIndex = 1;
		
		for(Var_Ref source: this.sources)
		{
			source.logReadAtTime(this.sigTimes[stepIndex++]);
		}
		
		this.lastExpression.logReadAtTime(this.sigTimes[stepIndex++]);
	}
}
