package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.FunctionObjects.FunctionObject;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.BaseVar_Inlined;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.VarTypes;

public class CreateFunctionObject extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final FunctionObject prototype;
	private final MystVar[] upvalues_initialArgs;
	private final MystVar[] upvalues_changedState;
	private final MystVar target;
	
	public CreateFunctionObject(int inInstanceNumber, FunctionObject inPrototype, MystVar[] inUpvalues_initialArgs, MystVar[] inUpvalues_changedState, MystVar inTarget)
	{
		super(IrCodes.CreateFunctionInstance, inInstanceNumber);
		this.prototype = inPrototype;
		this.upvalues_initialArgs = inUpvalues_initialArgs;
		this.upvalues_changedState = inUpvalues_changedState;
		this.target = inTarget;
	}

	public static CreateFunctionObject getInstance(FunctionObject inPrototype, MystVar[] inUpvalues_initialArgs, MystVar[] inUpvalues_changedState, MystVar inTarget)
	{
		return new CreateFunctionObject(instanceCounter++, inPrototype, inUpvalues_initialArgs, inUpvalues_changedState, inTarget);
	}

	public FunctionObject getPrototype()
	{
		return this.prototype;
	}

	public MystVar[] getUpvalues_initialArgs()
	{
		return this.upvalues_initialArgs;
	}

	public MystVar[] getUpvalues_changedState()
	{
		return this.upvalues_changedState;
	}

	public MystVar getTarget()
	{
		return this.target;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Once;
	}

	@Override
	public Variable getInlinedForm()
	{
		return new CreateFunctionObjectVar(this.instanceNumber, this, this.prototype, this.upvalues_initialArgs);
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("set ");
		result.add(this.target.getMostRelevantIdValue());
		result.add(" as a new instance of ");
		result.add(this.prototype.getName());
		result.add(", with upvalues: {");
		
		for(int i = 0; i < this.upvalues_initialArgs.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(this.upvalues_initialArgs[i].getMostRelevantIdValue());
			result.add('(');
			result.add(this.upvalues_changedState[i].getMostRelevantIdValue());
			result.add(')');
		}
		
		result.add('}');
		
		return result;
	}
	
	@Override
	public void revVarTypes()
	{
		for(MystVar curr: this.upvalues_initialArgs)
		{
			curr.revVarType();
		}
		
		this.target.revVarType();
	}
	
	private static CharList assignedValueToCharList(FunctionObject prototype, MystVar[] upvalues)
	{
		final CharList result = new CharList();
		
		result.add(prototype.getName());
		result.add(".createNewInstance(");
		
		for(int i = 0; i < upvalues.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(upvalues[i].getValueAsCharList(), true);
		}
		
		return result;
	}
	
	private static class CreateFunctionObjectVar extends BaseVar_Inlined
	{
		private static int subInstanceCounter = 0;
		
		private final FunctionObject prototype;
		private final MystVar[] upvalues;
		
		public CreateFunctionObjectVar(int inInstanceNumber, Instruction inAssignmentInstruction, FunctionObject inPrototype, MystVar[] inUpvalues)
		{
			super("CreateFunctionObjectVar_", VarTypes.FunctionObject, inInstanceNumber, subInstanceCounter++, inAssignmentInstruction);
			this.prototype = inPrototype;
			this.upvalues = inUpvalues;
		}

		@Override
		public CharList getValueAsCharList()
		{
			return assignedValueToCharList(this.prototype, this.upvalues);
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
