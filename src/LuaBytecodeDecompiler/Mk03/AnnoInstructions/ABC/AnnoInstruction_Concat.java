package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Concat extends AnnoInstruction
{
	private final AnnoVar target;
	private final AnnoVar[] sources;

	public AnnoInstruction_Concat(int inRaw, int inI, AnnoVar inTarget, AnnoVar[] inSources)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Concatenation);
		this.target = inTarget;
		this.sources = inSources;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add("GlobalMethods.concat(");
		result.add(this.sources[0].getAdjustedName());

		for (int i = 1; i < this.sources.length; i++)
		{
			result.add(", ");
			result.add(this.sources[i].getAdjustedName());
		}

		result.add(')');

		return result;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar[] getSources()
	{
		return this.sources;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		int i = 0;

		for (; i < this.sources.length; i++)
		{
			in.addVarRead(this.sources[i], this, place, i);
		}

		in.addVarWrite(this.target, this, place, i);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add("GlobalMethods.concat(");
		result.add(this.sources[0].getAdjustedName());

		for (int i = 1; i < this.sources.length; i++)
		{
			result.add(", ");
			result.add(this.sources[i].getAdjustedName());
		}

		result.add(')');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		this.sources[inSubstep - 1] = inReplacement;

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add("GlobalMethods.concat(");
		result.add(this.sources[0].getAdjustedName());

		for (int i = 1; i < this.sources.length; i++)
		{
			result.add(", ");
			result.add(this.sources[i].getAdjustedName());
		}

		result.add(')');

		return result;
	}
}
