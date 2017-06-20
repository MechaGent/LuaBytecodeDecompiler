package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_LoadNull extends AnnoInstruction
{
	private final AnnoVar[] targetRange;

	public AnnoInstruction_LoadNull(int inRaw, int inI, AnnoVar... inTargetRange)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.LoadNull);
		this.targetRange = inTargetRange;
	}

	public AnnoVar[] getTargetRange()
	{
		return this.targetRange;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		for (int i = 0; i < this.targetRange.length; i++)
		{
			in.addVarWrite(this.targetRange[i], this, place, i);
		}
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList("null");
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.targetRange[0].getAdjustedName());

		for (int i = 1; i < this.targetRange.length; i++)
		{
			result.add(", ");
			result.add(this.targetRange[i].getAdjustedName());
		}

		result.add(" = null");

		return result;
	}
}
