package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_LoadBool extends AnnoInstruction
{
	private final AnnoVar target;
	private final boolean source;

	public AnnoInstruction_LoadBool(int inRaw, int inI, AnnoVar inVarA, boolean inVarB, boolean inVarC)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.LoadBool, inVarC);
		this.target = inVarA;
		this.source = inVarB;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public boolean getSource()
	{
		return this.source;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		in.addVarWrite(this.target, this, this.getInstructionAddress(), 0);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList(Boolean.toString(this.source));
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		throw new UnsupportedOperationException("bad index: " + inSubstep);
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add(Boolean.toString(this.source));

		return result;
	}
}
