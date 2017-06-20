package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_SetGlobal extends AnnoInstruction
{
	private final AnnoVar targetVar;
	private AnnoVar sourceVar;

	public AnnoInstruction_SetGlobal(int inRaw, int inI, AnnoVar inTargetVar, AnnoVar inSourceVar)
	{
		super(inRaw, inI, InstructionTypes.iABx, AbstractOpCodes.SetGlobal);
		this.targetVar = inTargetVar;
		this.sourceVar = inSourceVar;
	}

	public AnnoVar getTargetVar()
	{
		return this.targetVar;
	}

	public AnnoVar getSourceVar()
	{
		return this.sourceVar;
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

		in.addVarRead(this.sourceVar, this, place, 0);
		in.addVarWrite(this.targetVar, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList(this.sourceVar.getAdjustedName());
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.sourceVar = inReplacement;
		}
		else
		{
			throw new IllegalArgumentException("bad index: " + inSubstep);
		}

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.targetVar.getAdjustedName());
		result.add(" = ");
		result.add(this.sourceVar.getAdjustedName());

		return result;
	}
}
