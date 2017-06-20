package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_GetGlobal extends AnnoInstruction
{
	private final AnnoVar target;

	/**
	 * will always be from Globals
	 */
	private AnnoVar sourceGlobal;

	public AnnoInstruction_GetGlobal(int inRaw, int inI, AnnoVar inVarA, AnnoVar inSourceGlobal)
	{
		super(inRaw, inI, InstructionTypes.iABx, AbstractOpCodes.GetGlobal);
		this.target = inVarA;
		this.sourceGlobal = inSourceGlobal;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getSourceGlobal()
	{
		return this.sourceGlobal;
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

		in.addVarRead(this.sourceGlobal, this, place, 0);
		in.addVarWrite(this.target, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList(this.sourceGlobal.getAdjustedName());
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.sourceGlobal = inReplacement;
		}
		else
		{
			throw new IllegalArgumentException();
		}

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add(this.sourceGlobal.getAdjustedName());

		return result;
	}
}
