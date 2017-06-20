package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.BaseVar_Inlined;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;

public class LengthOf extends BaseInstruct
{
	private static int instanceCounter_Instruct = 0;
	
	private int instanceCounter_Inline = 0;
	
	private final MystVar target;
	private final Variable source;
	
	private LengthOf(MystVar inTarget, Variable inSource)
	{
		super(IrCodes.LengthOf, instanceCounter_Instruct++);
		this.target = inTarget;
		this.source = inSource;
	}
	
	public static LengthOf getInstance(MystVar inTarget, Variable inSource)
	{
		return new LengthOf(inTarget, inSource);
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public Variable getInlinedForm()
	{
		return new InlinedLengthOfVar(this.instanceNumber, this.instanceCounter_Inline++, this, this.source);
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add(this.target.getMostRelevantIdValue());
		result.add(" is set to lengthOf(");
		result.add(this.source.getMostRelevantIdValue());
		result.add(");");
		
		return result;
	}
	
	public MystVar getTarget()
	{
		return this.target;
	}

	public Variable getSource()
	{
		return this.source;
	}

	@Override
	public void revVarTypes()
	{
		this.source.revVarType();
		this.target.revVarType();
	}
	
	private static class InlinedLengthOfVar extends BaseVar_Inlined
	{
		private final Variable source;

		public InlinedLengthOfVar(int inInstanceNumber, int inSubInstanceNumber, Instruction inAssignmentInstruction, Variable inSource)
		{
			super("InlinedLengthOf", VarTypes.Int, inInstanceNumber, inSubInstanceNumber, inAssignmentInstruction);
			this.source = inSource;
		}

		@Override
		public CharList getValueAsCharList()
		{
			final CharList result = new CharList();
			
			result.add("LengthOf(");
			result.add(this.source.getMangledName());
			result.add(')');
			
			return result;
		}

		@Override
		public CanBeInlinedTypes canBeInlined()
		{
			return CanBeInlinedTypes.Yes;
		}

		@Override
		public String valueToString()
		{
			return this.getValueAsCharList().toString();
		}

		@Override
		public boolean isConstant()
		{
			return true;
		}
	}
}
