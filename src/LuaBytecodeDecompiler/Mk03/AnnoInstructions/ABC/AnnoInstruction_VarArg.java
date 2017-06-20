package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_VarArg extends AnnoInstruction
{
	private final AnnoVar[] additionalArgs;
	private final AnnoVar[] targets;

	public AnnoInstruction_VarArg(int inRaw, int inI, AnnoVar[] inAdditionalArgs, AnnoVar[] inTargets)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.VarArg);
		this.additionalArgs = inAdditionalArgs;
		this.targets = inTargets;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("Additional Args: ");
		result.add(this.additionalArgs[0].getAdjustedName());

		for (int i = 1; i < this.additionalArgs.length; i++)
		{
			result.add(", ");
			result.add(this.additionalArgs[i].getAdjustedName());
		}

		return result;
	}

	public AnnoVar[] getAdditionalArgs()
	{
		return this.additionalArgs;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		int stepCount = 0;

		for (int i = 0; i < this.additionalArgs.length; i++)
		{
			in.addVarRead(this.additionalArgs[i], this, place, stepCount++);
			in.addVarWrite(this.targets[i], this, place, stepCount++);
		}
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add("VarArg:[");

		if (this.additionalArgs.length != 0)
		{
			result.add(this.additionalArgs[0].getAdjustedName());

			for (int i = 1; i < this.additionalArgs.length; i++)
			{
				result.add(", ");
				result.add(this.additionalArgs[i].getAdjustedName());
			}
		}

		result.add(']');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		this.additionalArgs[inSubstep] = inReplacement;

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		return this.toPseudoJava_unhandled(inOffset);
	}
}
