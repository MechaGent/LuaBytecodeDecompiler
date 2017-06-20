package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.ScopeLevels;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Indexed;

public class AnnoInstruction_Self extends AnnoInstruction
{
	private final AnnoVar firstTarget;
	private AnnoVar firstSource;
	private final AnnoVar secondTarget;
	private AnnoVar_Indexed secondSource;
	private boolean hasPseudoInstruction1Deployed;
	private boolean hasPseudoInstruction2Deployed;

	public AnnoInstruction_Self(int inRaw, int inI, AnnoVar inFirstTarget, AnnoVar inFirstSource, AnnoVar inSecondTarget, AnnoVar inIndex)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Self);
		this.firstTarget = inFirstTarget;
		this.firstSource = inFirstSource;
		this.secondTarget = inSecondTarget;
		this.secondSource = new AnnoVar_Indexed("<ShouldNotSeeThis_SELF>", ScopeLevels.Temporary, this.firstSource, inIndex);
		this.hasPseudoInstruction1Deployed = false;
		this.hasPseudoInstruction2Deployed = false;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add(this.firstTarget.getAdjustedName());
		result.add(" = ");
		final String firstSourceName = this.firstSource.getAdjustedName();
		result.add(firstSourceName);
		result.add("; ");
		result.add(this.secondTarget.getAdjustedName());
		result.add(" = ");
		result.add(this.secondSource.getAdjustedName());

		// MiscAutobot.prettify_TableGetMethod(result, firstSourceName, this.index.getAdjustedName());

		return result;
	}

	public AnnoVar getFirstTarget()
	{
		return this.firstTarget;
	}

	public AnnoVar getFirstSource()
	{
		return this.firstSource;
	}

	public AnnoVar getSecondTarget()
	{
		return this.secondTarget;
	}

	public AnnoVar_Indexed getSecondSource()
	{
		return this.secondSource;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		in.addVarRead(this.firstSource, this, place, 0);
		in.addVarWrite(this.firstTarget, this, place, 1);
		in.addVarRead(this.secondSource, this, place, 2);
		in.addVarWrite(this.secondTarget, this, place, 3);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}

	public AnnoInstruction getPseudoInstructionFor(int substep)
	{
		final AnnoInstruction result;

		switch (substep)
		{
			case 1:
			{
				result = new AnnoInstruction_Move(this.getRawCode(), this.getInstructionAddress(), this.firstTarget, this.firstSource);
				this.hasPseudoInstruction1Deployed = true;
				break;
			}
			case 3:
			{
				result = new AnnoInstruction_Move(this.getRawCode(), this.getInstructionAddress(), this.secondTarget, this.secondSource);
				this.hasPseudoInstruction2Deployed = true;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad index: " + substep);
			}
		}

		if (this.hasPseudoInstruction1Deployed && this.hasPseudoInstruction2Deployed)
		{
			this.setAsNoOp();
		}

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch (inSubstep)
		{
			case 0:
			{
				// System.out.println("line: " + Integer.toHexString(this.getInstructionAddress()) + " triggering on trying to replace: " + this.firstSource.getAdjustedName() + " with: " + inReplacement.getAdjustedName());
				this.firstSource = inReplacement;
				this.secondSource = new AnnoVar_Indexed("<ShouldNotSeeThis_SELF>", ScopeLevels.Temporary, inReplacement, this.secondSource.getIndex());
				break;
			}
			case 2:
			{
				this.secondSource = new AnnoVar_Indexed("<ShouldNotSeeThis_SELF>", ScopeLevels.Temporary, this.firstSource, inReplacement);
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
		result.add(this.firstTarget.getAdjustedName());
		result.add(" = ");
		final String firstSourceName = this.firstSource.getAdjustedName();
		result.add(firstSourceName);
		result.add(';');
		result.addNewLine();
		result.add('\t', inOffset);
		result.add(this.secondTarget.getAdjustedName());
		result.add(" = ");
		result.add(this.secondSource.getAdjustedName());

		// MiscAutobot.prettify_TableGetMethod(result, firstSourceName, this.index.getAdjustedName());

		return result;
	}
}
