package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Length extends AnnoInstruction
{
	private AnnoVar target;
	private AnnoVar source;

	public AnnoInstruction_Length(int inRaw, int inI, AnnoVar inTarget, AnnoVar inSource)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Length);
		this.target = inTarget;
		this.source = inSource;
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

		in.addVarRead(this.source, this, place, 0);
		in.addVarWrite(this.target, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add("GlobalMethods.lengthOf(");
		result.add(this.source.getAdjustedName());
		result.add(')');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch (inSubstep)
		{
			case 0:
			{
				this.source = inReplacement;
				break;
			}
			case 1:
			{
				this.target = inReplacement;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad index: " + inSubstep);
			}
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
		result.add("GlobalMethods.lengthOf(");
		result.add(this.source.getAdjustedName());
		result.add(')');

		return result;
	}
}
