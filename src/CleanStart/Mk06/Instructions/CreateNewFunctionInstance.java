package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.FunctionObjects.FunctionObject;
import CleanStart.Mk06.SubVars.FunctionVar;
import CleanStart.Mk06.SubVars.MethodVar;
import CleanStart.Mk06.StepNum.ForkBehaviors;

public class CreateNewFunctionInstance extends Instruction
{
	private static int instanceCounter = 0;

	private final FunctionObject prototype;
	private final VarRef[] upvalues;
	private final VarRef target;
	private final MethodVar methodVarForm;

	private CreateNewFunctionInstance(StepNum inStartTime, FunctionObject inPrototype, VarRef[] inUpvalues, VarRef inTarget)
	{
		super(IrCodes.CreateFunctionInstance, instanceCounter++, inStartTime);
		this.prototype = inPrototype;
		this.upvalues = inUpvalues;
		this.target = inTarget;
		
		final FunctionVar coreWrapper = FunctionVar.getInstance("", inPrototype);
		final VarRef inReturns = VarRef.getInstance_forWrapper(coreWrapper, InferenceTypes.FunctionObject, InlineSchemes.Inline_IfOnlyReadOnce);
		this.methodVarForm = MethodVar.getInstance("", inStartTime, null, inPrototype.getName() + ".createInstance", false, VarRef.getEmptyArr(), false, new VarRef[]{inReturns});
	}

	public static CreateNewFunctionInstance getInstance(StepNum inStartTime, FunctionObject inPrototype, VarRef[] inUpvalues, VarRef inTarget)
	{
		final CreateNewFunctionInstance result = new CreateNewFunctionInstance(inStartTime, inPrototype, inUpvalues, inTarget);
		result.logAllReadsAndWrites();
		return result;
	}

	public FunctionObject getPrototype()
	{
		return this.prototype;
	}

	public VarRef[] getUpvalues()
	{
		return this.upvalues;
	}

	public VarRef getTarget()
	{
		return this.target;
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{
		this.setExpiredState(inNewState);
		//System.out.println("dingaling");
		
		/*
		if (inIndex == 0)
		{
			this.setExpiredState(inNewState);
		}
		else
		{
			// throw new IndexOutOfBoundsException(inIndex + " of length " + "1");
		}
		*/
	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.Inline_IfOnlyReadOnce;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		
		StepNum readFork = this.startTime.fork(ForkBehaviors.ForkToSubstage);
		StepNum writeFork = readFork.createOffsetStep(this.upvalues.length + 1);

		for (int i = 0; i < this.upvalues.length; i++)
		{
			this.upvalues[i].logReadAtTime(readFork, InferenceTypes.Mystery);
			this.upvalues[i].logStateChangeAtTime(InstructionIndex.getInstance(this, writeFork.getTailElement()), writeFork);

			readFork = readFork.fork(ForkBehaviors.IncrementLast);
			writeFork = writeFork.fork(ForkBehaviors.IncrementLast);
		}
		
		//final VarRef wrapper = VarRef.getInstance_forWrapper(this.methodVarForm, InferenceTypes.FunctionObject, InlineSchemes.Inline_IfOnlyReadOnce);
		
		//System.out.println("readFork: " + readFork.getInDiagForm().toString() + ", writeFork: " + writeFork.getInDiagForm().toString());
		this.target.logWriteAtTime(this.methodVarForm, InstructionIndex.getInstance(this, writeFork.getTailElement()), readFork, writeFork, InferenceTypes.FunctionObject);
		//this.target.logStateChangeAtTime(InstructionIndex.getInstance(this, writeFork.getTailElement()), writeFork);
		
		if(this.upvalues.length > 0)
		{
			writeFork = writeFork.fork(ForkBehaviors.IncrementLast);
			//this.target.logReadAtTime(writeFork, InferenceTypes.Mystery);
			this.target.logStateChangeAtTime(InstructionIndex.getInstance(this, writeFork.getTailElement()), writeFork);
		}
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add(this.target.getMangledName(inAppendMetadata));
		result.add(" is set to a new instance of ");
		result.add(this.prototype.getName());
		result.add(", with upvalues: {");

		for (int i = 0; i < this.upvalues.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.upvalues[i].getMangledName(inAppendMetadata));
		}

		result.add('}');

		return result;
	}

	@Override
	protected CharList toCodeForm_internal(boolean inAppendMetadata, int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getMangledName(inAppendMetadata));
		result.add(" = new ");
		result.add(this.prototype.getName());
		result.add(", with upvalues: {");

		for (int i = 0; i < this.upvalues.length; i++)
		{
			if (i != 0)
			{
				result.add(", ");
			}

			result.add(this.upvalues[i].getMangledName(inAppendMetadata));
		}

		result.add('}');

		return result;
	}

	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		
		if(this.isExpired)
		{
			result.add("//\t");
		}
		
		final String targetName = this.target.getMangledName(false);
		result.add(targetName);
		result.add(" = ");
		result.add(this.methodVarForm.getValueAsString());
		result.add(';');

		for (int i = 0; i < this.upvalues.length; i++)
		{
			result.addNewIndentedLine(offset);
			result.add(targetName);
			result.add(".allocateUpvalue(");
			result.add(this.upvalues[i].getValueAtTime(this.startTime).getValueAsString());
			result.add(");");
		}

		return result;
	}
}
