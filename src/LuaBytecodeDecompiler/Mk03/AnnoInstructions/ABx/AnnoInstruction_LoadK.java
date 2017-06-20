package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_LoadK extends AnnoInstruction
{
	private final AnnoVar target;
	private AnnoVar sourceConstant;

	public AnnoInstruction_LoadK(int inRaw, int inI, AnnoVar inVarA, AnnoVar inVarBx)
	{
		super(inRaw, inI, InstructionTypes.iABx, AbstractOpCodes.LoadK);
		this.target = inVarA;
		this.sourceConstant = inVarBx;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getSourceConstant()
	{
		return this.sourceConstant;
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

		in.addVarRead(this.sourceConstant, this, place, 0);
		in.addVarWrite(this.target, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList(this.sourceConstant.getAdjustedName());
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.sourceConstant = inReplacement;
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
		result.add(this.sourceConstant.getAdjustedName());

		return result;
	}
}
