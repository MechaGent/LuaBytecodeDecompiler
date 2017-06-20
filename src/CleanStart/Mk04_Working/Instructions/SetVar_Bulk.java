package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;

public class SetVar_Bulk extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar[] targets;
	private final Variable source;
	
	private SetVar_Bulk(MystVar[] inTargets, Variable inSource)
	{
		super(IrCodes.SetVar_Bulk, instanceCounter++);
		this.targets = inTargets;
		this.source = inSource;
	}
	
	public static SetVar_Bulk getInstance(MystVar[] inTargets, Variable inSource)
	{
		return new SetVar_Bulk(inTargets, inSource);
	}

	public MystVar[] getTargets()
	{
		return this.targets;
	}

	public Variable getSource()
	{
		return this.source;
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
		
		result.add("Set the following to ");
		result.add(this.source.getMostRelevantIdValue());
		result.add(": {");
		
		for(int i = 0; i < this.targets.length; i++)
		{
			if(i!=0)
			{
				result.add(", ");
			}
			
			result.add(this.targets[i].getMostRelevantIdValue());
		}
		
		result.add('}');
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.source.revVarType();
		
		for(MystVar curr: this.targets)
		{
			curr.revVarType();
		}
	}
}
