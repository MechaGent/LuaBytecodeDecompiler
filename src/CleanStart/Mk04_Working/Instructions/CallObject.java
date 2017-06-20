package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.BaseVar_Inlined;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;

public class CallObject extends BaseInstruct
{
	private static int instanceCounter = 0;

	private final MystVar targetObject;
	private final ObjectTypes objectType;
	private final boolean ArgsAreVariable;
	private final MystVar[] Args;
	private final boolean ReturnsAreVariable;
	private final MystVar[] Returns;

	private CallObject(int inInstanceNumber, MystVar inFunctionObject, boolean inArgsAreVariable, MystVar[] inArgs, boolean inReturnsAreVariable, MystVar[] inReturns)
	{
		super(IrCodes.CallObject, inInstanceNumber);
		this.targetObject = inFunctionObject;

		switch (inFunctionObject.getBestGuess())
		{
			case TableObject:
			{
				this.objectType = ObjectTypes.Table;
				break;
			}
			case FunctionObject:
			{
				this.objectType = ObjectTypes.Function;
				break;
			}
			case Mystery:
			{
				this.objectType = ObjectTypes.Unknown;
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}

		this.ArgsAreVariable = inArgsAreVariable;
		this.Args = inArgs;
		this.ReturnsAreVariable = inReturnsAreVariable;
		this.Returns = inReturns;
	}

	public static CallObject getInstance(MystVar inObject, boolean inArgsAreVariable, MystVar[] inArgs, boolean inReturnsAreVariable, MystVar[] inReturns)
	{
		return new CallObject(instanceCounter++, inObject, inArgsAreVariable, inArgs, inReturnsAreVariable, inReturns);
	}

	public MystVar getTargetObject()
	{
		return this.targetObject;
	}

	public ObjectTypes getObjectType()
	{
		return this.objectType;
	}

	public boolean isArgsAreVariable()
	{
		return this.ArgsAreVariable;
	}

	public MystVar[] getArgs()
	{
		return this.Args;
	}

	public boolean isReturnsAreVariable()
	{
		return this.ReturnsAreVariable;
	}

	public MystVar[] getReturns()
	{
		return this.Returns;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Once;
	}

	@Override
	public Variable getInlinedForm()
	{
		return new FunctionObjectVar(this);
	}

	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();

		result.add("Call function '");
		result.add(this.targetObject.getMostRelevantIdValue());
		// result.add(this.functionObject.getValueAsCharList(), true);
		result.add("' with args (");

		for (int i = 0; i < this.Args.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Args[i].getMostRelevantIdValue());
			// result.add(this.Args[i].getValueAsCharList(), true);
		}

		if (this.ArgsAreVariable)
		{
			if (this.Args.length != 0)
			{
				result.add(", ");
			}

			result.add("... <TopOfStack>");
		}

		result.add("), which returns {");

		for (int i = 0; i < this.Returns.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.Returns[i].getMostRelevantIdValue());
			// result.add(this.Returns[i].getValueAsCharList(), true);
		}

		if (this.ReturnsAreVariable)
		{
			if (this.Returns.length != 0)
			{
				result.add(", ");
			}

			result.add("... <TopOfStack>");
		}

		result.add('}');

		return result;
	}
	
	@Override
	public void revVarTypes()
	{
		this.targetObject.revVarType();
		
		for(MystVar curr: this.Args)
		{
			curr.revVarType();
		}
		
		for(MystVar curr: this.Returns)
		{
			curr.revVarType();
		}
	}

	public static enum ObjectTypes
	{
		Table,
		Function,
		Unknown;
	}
	
	private static class FunctionObjectVar extends BaseVar_Inlined
	{
		private static int instanceCounter = 0;
		private final CallObject core;

		public FunctionObjectVar(CallObject inAssignmentInstruction)
		{
			super("FunctionObjectVar_", VarTypes.FunctionObject, inAssignmentInstruction.instanceNumber, instanceCounter++, inAssignmentInstruction);
			this.core = inAssignmentInstruction;
		}

		@Override
		public CharList getValueAsCharList()
		{
			final CharList result = new CharList();

			result.add('{');

			for (int i = 0; i < this.core.Returns.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				result.add(this.core.Returns[i].getValueAsCharList(), true);
			}

			if (this.core.ReturnsAreVariable)
			{
				result.add("... to TopOfStack");
			}

			result.add("} = ");

			result.add(this.core.targetObject.getMangledName());
			result.add(".call(");

			if (this.core.ArgsAreVariable)
			{
				result.add("...");
			}
			else
			{
				for (int i = 0; i < this.core.Args.length; i++)
				{
					if (i != 0)
					{
						result.add(", ");
					}

					result.add(this.core.Args[i].getValueAsCharList(), true);
				}
			}

			result.add(')');

			return result;
		}

		@Override
		public CanBeInlinedTypes canBeInlined()
		{
			return CanBeInlinedTypes.Once;
		}

		@Override
		public boolean isConstant()
		{
			return false;
		}

		@Override
		public String valueToString()
		{
			return this.getValueAsCharList().toString();
		}
	}
}
