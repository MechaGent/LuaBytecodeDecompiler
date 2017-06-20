package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;

public class Return_Variably extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar[] sources;
	private final MystVar lastExpression;	//should be a function call
	private int numCloseBracesToAppend;
	
	private Return_Variably(MystVar[] inSources, MystVar inLastExpression)
	{
		super(IrCodes.Return_Variably, instanceCounter++);
		this.sources = inSources;
		this.lastExpression = inLastExpression;
		this.numCloseBracesToAppend = 0;
	}
	
	public static Return_Variably getInstance(MystVar[] inSources, MystVar inLastExpression)
	{
		return new Return_Variably(inSources, inLastExpression);
	}

	public MystVar[] getSources()
	{
		return this.sources;
	}

	public MystVar getLastExpression()
	{
		return this.lastExpression;
	}
	
	public void incrementNumCloseBracesToAppend()
	{
		this.numCloseBracesToAppend++;
	}
	
	public int getNumCloseBracesToAppend()
	{
		return this.numCloseBracesToAppend;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.NonApplicable;
	}

	@Override
	public Variable getInlinedForm()
	{
		return null;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("<> NeedToImplement: ");
		result.add(this.IrCode.toString());
		result.add(" - ");
		result.add(this.toString());
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		for(MystVar curr: this.sources)
		{
			curr.revVarType();
		}
		
		this.lastExpression.revVarType();
	}
}
