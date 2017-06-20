package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Negation extends AnnoInstruction
{
	private final AnnoVar target;
	private AnnoVar source;

	public AnnoInstruction_Negation(int inRaw, int inI, AbstractOpCodes inOpCode, AnnoVar inTarget, AnnoVar inSource)
	{
		super(inRaw, inI, InstructionTypes.iABC, inOpCode);
		this.target = inTarget;
		this.source = inSource;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getSource()
	{
		return this.source;
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
		
		switch (this.getOpCode())
		{
			case ArithmeticNegation:
			{
				result.add('-');
				break;
			}
			case LogicalNegation:
			{
				result.add('!');
				break;
			}
			default:
			{
				throw new UnhandledEnumException(this.getOpCode());
			}
		}
		
		result.add(this.source.getAdjustedName());

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if(inSubstep == 0)
		{
			this.source = inReplacement;
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
		result.add(this.target.getAdjustedName());
		result.add(" = ");

		switch (this.getOpCode())
		{
			case ArithmeticNegation:
			{
				result.add('-');
				break;
			}
			case LogicalNegation:
			{
				result.add('!');
				break;
			}
			default:
			{
				throw new UnhandledEnumException(this.getOpCode());
			}
		}
		
		result.add(this.source.getAdjustedName());

		return result;
	}
}
