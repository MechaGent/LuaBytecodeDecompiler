package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Math extends AnnoInstruction
{
	private final AnnoVar target;
	private AnnoVar operand1;
	private AnnoVar operand2;

	public AnnoInstruction_Math(int inRaw, int inI, AbstractOpCodes inOpCode, AnnoVar inTarget, AnnoVar inOperand1, AnnoVar inOperand2)
	{
		super(inRaw, inI, InstructionTypes.iABC, inOpCode);
		this.target = inTarget;
		this.operand1 = inOperand1;
		this.operand2 = inOperand2;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	private static String getSpacedOperator(AbstractOpCodes in)
	{
		final String result;

		switch (in)
		{
			case Add:
			{
				result = " + ";
				break;
			}
			case Subtract:
			{
				result = " - ";
				break;
			}
			case Multiply:
			{
				result = " * ";
				break;
			}
			case Divide:
			{
				result = " / ";
				break;
			}
			case Modulo:
			{
				result = " % ";
				break;
			}
			case Power:
			{
				result = " ^ ";
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in);
			}
		}

		return result;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getOperand1()
	{
		return this.operand1;
	}

	public AnnoVar getOperand2()
	{
		return this.operand2;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		in.addVarRead(this.operand1, this, place, 0);
		in.addVarRead(this.operand2, this, place, 1);
		in.addVarWrite(this.target, this, place, 2);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add(this.operand1.getAdjustedName());
		result.add(getSpacedOperator(this.getOpCode()));
		result.add(this.operand2.getAdjustedName());

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch (inSubstep)
		{
			case 0:
			{
				this.operand1 = inReplacement;
				break;
			}
			case 1:
			{
				this.operand2 = inReplacement;
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
		result.add(this.operand1.getAdjustedName());
		result.add(getSpacedOperator(this.getOpCode()));
		result.add(this.operand2.getAdjustedName());

		return result;
	}
}
