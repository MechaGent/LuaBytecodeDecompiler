package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.BaseVar_Inlined;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;

public class Concat extends BaseInstruct
{
	private static int instanceCounter_Instruct = 0;
	
	private int instanceCounter_Inline = 0;
	
	private final MystVar target;
	private final Variable[] sources;
	
	private Concat(MystVar inTarget, Variable[] inSources)
	{
		super(IrCodes.Concat, instanceCounter_Instruct++);
		this.target = inTarget;
		this.sources = inSources;
	}
	
	public static Concat getInstance(MystVar inTarget, Variable[] inSources)
	{
		return new Concat(inTarget, inSources);
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Once;
	}

	@Override
	public Variable getInlinedForm()
	{
		return new InlinedConcatVar(this.instanceNumber, this.instanceCounter_Inline++, this, this.sources);
	}
	
	public MystVar getTarget()
	{
		return this.target;
	}

	public Variable[] getSources()
	{
		return this.sources;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add(this.target.getMostRelevantIdValue());
		result.add(" is set to (");
		
		for(int i = 0; i < this.sources.length; i++)
		{
			if(i != 0)
			{
				result.add(" + ");
			}
			
			result.add(this.sources[i].getMostRelevantIdValue());
		}
		
		result.add(");");
		
		return result;
	}
	
	@Override
	public void revVarTypes()
	{
		this.target.revVarType();
		
		for(Variable curr: this.sources)
		{
			curr.revVarType();
		}
	}

	private static class InlinedConcatVar extends BaseVar_Inlined
	{
		private final Variable[] sources;

		public InlinedConcatVar(int inInstanceNumber, int inSubInstanceNumber, Instruction inAssignmentInstruction, Variable[] inSources)
		{
			super("InlinedConcat", VarTypes.String, inInstanceNumber, inSubInstanceNumber, inAssignmentInstruction);
			this.sources = inSources;
		}

		@Override
		public CharList getValueAsCharList()
		{
			final CharList result = new CharList();
			
			result.add('"');
			
			for(int i = 0; i < this.sources.length; i++)
			{
				if(i != 0)
				{
					result.add("\" + \"");
				}
				
				result.add(this.sources[i].valueToString());
			}
			
			result.add('"');
			
			return result;
		}
		
		@Override
		public String getMangledName()
		{
			return this.namePrefix + '_' + this.instanceNumber + '_' + this.subInstanceNumber + '(' + this.varType.toString() + ')';
		}

		@Override
		public CanBeInlinedTypes canBeInlined()
		{
			return CanBeInlinedTypes.Once;
		}

		@Override
		public boolean isOperator()
		{
			return false;
		}

		@Override
		public boolean isOperand()
		{
			return true;
		}

		@Override
		public boolean isConstant()
		{
			return true;
		}

		@Override
		public int getPrecedence()
		{
			return -1;
		}

		@Override
		public String valueToString()
		{
			return this.getValueAsCharList().toString();
		}
	}
}
