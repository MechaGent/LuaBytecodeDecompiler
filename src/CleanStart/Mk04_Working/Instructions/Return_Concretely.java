package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;

public class Return_Concretely extends BaseInstruct
{
	private static int instanceCounter = 0;
	private final MystVar[] sources;
	private int numCloseBracesToAppend;

	private Return_Concretely(MystVar[] inSources)
	{
		super(IrCodes.Return_Concretely, instanceCounter++);
		this.sources = inSources;
		this.numCloseBracesToAppend = 0;
	}
	
	public static Return_Concretely getInstance(MystVar[] inSources)
	{
		return new Return_Concretely(inSources);
	}

	public MystVar[] getSources()
	{
		return this.sources;
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
	
	public void incrementNumCloseBracesToAppend()
	{
		this.numCloseBracesToAppend++;
	}
	
	public int getNumCloseBracesToAppend()
	{
		return this.numCloseBracesToAppend;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("return the following to the calling function: {");
		
		for(int i = 0; i < this.sources.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(this.sources[i].getMostRelevantIdValue());
		}
		
		result.add('}');
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		for(MystVar curr: this.sources)
		{
			curr.revVarType();
		}
	}
}
