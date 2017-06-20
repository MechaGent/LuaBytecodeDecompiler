package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.TableElementVar;

public abstract class SetVar extends BaseInstruct
{
	private static int instanceCounter_Instruct = 0;
	
	protected final Variable source;
	
	private SetVar(Variable inSource)
	{
		super(IrCodes.SetVar, instanceCounter_Instruct++);
		this.source = inSource;
	}
	
	public static SetVar getInstance(MystVar inTarget, Variable inSource)
	{
		if(inTarget == inSource)
		{
			throw new IllegalArgumentException();
		}
		
		return new SetVar_MystVarTarget(inTarget, inSource);
	}
	
	public static SetVar getInstance(TableElementVar inTarget, Variable inSource)
	{
		if(inTarget == inSource)
		{
			throw new IllegalArgumentException();
		}
		return new SetVar_TableElementVarTarget(inTarget, inSource);
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public Variable getInlinedForm()
	{
		return this.source;
	}

	public Variable getSource()
	{
		return this.source;
	}
	
	public abstract Variable getTarget_AsVariable();
	
	public static class SetVar_MystVarTarget extends SetVar
	{
		private final MystVar target;
		
		private SetVar_MystVarTarget(MystVar inTarget, Variable inSource)
		{
			super(inSource);
			this.target = inTarget;
		}

		public MystVar getTarget()
		{
			return this.target;
		}
		
		@Override
		public CharList getVerboseCommentForm()
		{
			final CharList result = new CharList();
			
			result.add(this.target.getMostRelevantIdValue());
			result.add(" is set to ");
			result.add(this.source.getMostRelevantIdValue());
			
			return result;
		}

		@Override
		public void revVarTypes()
		{
			this.source.revVarType();
			this.target.revVarType();
		}

		@Override
		public Variable getTarget_AsVariable()
		{
			return this.target;
		}
	}
	
	public static class SetVar_TableElementVarTarget extends SetVar
	{
		private final TableElementVar target;

		private SetVar_TableElementVarTarget(TableElementVar inTarget, Variable inSource)
		{
			super(inSource);
			this.target = inTarget;
		}

		public TableElementVar getTarget()
		{
			return this.target;
		}
		
		@Override
		public CharList getVerboseCommentForm()
		{
			final CharList result = new CharList();
			
			result.add(this.target.getMostRelevantIdValue());
			result.add("<TablelementVar> is set to ");
			result.add(this.source.getMostRelevantIdValue());
			
			return result;
		}

		@Override
		public void revVarTypes()
		{
			this.source.revVarType();
			this.target.revVarType();
		}

		@Override
		public Variable getTarget_AsVariable()
		{
			return this.target;
		}
	}
}
