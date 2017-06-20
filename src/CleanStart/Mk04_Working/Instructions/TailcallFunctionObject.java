package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.BaseVar_Inlined;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;

public class TailcallFunctionObject extends BaseInstruct
{
	private static int instanceCounter = 0;

	private final MystVar functionObject;
	private final boolean ArgsAreVariable;
	private final MystVar[] Args;

	private TailcallFunctionObject(int inInstanceNumber, MystVar inFunctionObject, boolean inArgsAreVariable, MystVar[] inArgs)
	{
		super(IrCodes.TailcallFunctionObject, inInstanceNumber);
		this.functionObject = inFunctionObject;
		this.ArgsAreVariable = inArgsAreVariable;
		this.Args = inArgs;
	}

	public static TailcallFunctionObject getInstance(MystVar inFunctionObject, boolean inArgsAreVariable, MystVar[] inArgs)
	{
		return new TailcallFunctionObject(instanceCounter++, inFunctionObject, inArgsAreVariable, inArgs);
	}

	public MystVar getFunctionObject()
	{
		return this.functionObject;
	}

	public boolean isArgsAreVariable()
	{
		return this.ArgsAreVariable;
	}

	public MystVar[] getArgs()
	{
		return this.Args;
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
		
		result.add("<> NeedToImplement: ");
		result.add(this.IrCode.toString());
		result.add(" - ");
		result.add(this.toString());
		
		return result;
	}
	
	@Override
	public void revVarTypes()
	{
		this.functionObject.revVarType();
		
		for(MystVar curr: this.Args)
		{
			curr.revVarType();
		}
	}
	
	private static class FunctionObjectVar extends BaseVar_Inlined
	{
		private static int instanceCounter = 0;
		private final TailcallFunctionObject core;

		public FunctionObjectVar(TailcallFunctionObject inAssignmentInstruction)
		{
			super("FunctionObjectVar_", VarTypes.FunctionObject, inAssignmentInstruction.instanceNumber, instanceCounter++, inAssignmentInstruction);
			this.core = inAssignmentInstruction;
		}

		@Override
		public CharList getValueAsCharList()
		{
			final CharList result = new CharList();

			result.add('{');
			result.add("... to TopOfStack");
			result.add("} = ");

			result.add(this.core.functionObject.getMangledName());
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
