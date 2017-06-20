package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.BoolBrancher;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Relational extends AnnoInstruction implements BoolBrancher
{
	private final boolean A;
	private AnnoVar operand1;
	private AnnoVar operand2;

	public AnnoInstruction_Relational(int inRaw, int inI, AbstractOpCodes inOpCode, boolean inA, AnnoVar inOperand1, AnnoVar inOperand2)
	{
		super(inRaw, inI, InstructionTypes.iABC, inOpCode, true);
		this.A = inA;
		this.operand1 = inOperand1;
		this.operand2 = inOperand2;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("if (");
		result.add(this.operand1.getAdjustedName());
		result.add(' ');

		switch (Operator.parseFromOpCode(this.getOpCode()))
		{
			case Equals:
			{
				if (!this.A)
				{
					result.add("==");
				}
				else
				{
					result.add("!=");
				}

				break;
			}
			case LessThan:
			{
				if (!this.A)
				{
					result.add('<');
				}
				else
				{
					result.add(">=");
				}
				break;
			}
			case LessThanEquals:
			{
				if (!this.A)
				{
					result.add("<=");
				}
				else
				{
					result.add('>');
				}
				break;
			}
			default:
			{
				throw new UnhandledEnumException(Operator.parseFromOpCode(this.getOpCode()));
			}
		}

		result.add(' ');
		result.add(this.operand2.getAdjustedName());
		//result.add(") == ");
		//result.add(Boolean.toString(!this.A));
		result.add("), then ");
		result.add(MiscAutobot.programCounter);
		result.add("++");

		return result;
	}

	public boolean getA()
	{
		return this.A;
	}

	public AnnoVar getOperand1()
	{
		return this.operand1;
	}

	public AnnoVar getOperand2()
	{
		return this.operand2;
	}

	private static enum Operator
	{
		Equals,
		LessThan,
		LessThanEquals;

		public static Operator parseFromOpCode(AbstractOpCodes in)
		{
			final Operator result;

			switch (in)
			{
				case TestIfEqual:
				{
					result = Equals;
					break;
				}
				case TestIfLessThan:
				{
					result = LessThan;
					break;
				}
				case TestIfLessThanOrEqual:
				{
					result = LessThanEquals;
					break;
				}
				default:
				{
					throw new UnhandledEnumException(in);
				}
			}

			return result;
		}
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		in.addVarRead(this.operand1, this, place, 0);
		in.addVarRead(this.operand2, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
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
	public CharList toBooleanStatement(boolean invertStatement)
	{
		final CharList result = new CharList();
		
		result.add(this.operand1.getAdjustedName());
		result.add(' ');
		
		final boolean critBool = invertStatement? !this.A : this.A;

		switch (Operator.parseFromOpCode(this.getOpCode()))
		{
			case Equals:
			{
				if (critBool)
				{
					result.add("==");
				}
				else
				{
					result.add("!=");
				}

				break;
			}
			case LessThan:
			{
				if (critBool)
				{
					result.add('<');
				}
				else
				{
					result.add(">=");
				}
				break;
			}
			case LessThanEquals:
			{
				if (critBool)
				{
					result.add("<=");
				}
				else
				{
					result.add('>');
				}
				break;
			}
			default:
			{
				throw new UnhandledEnumException(Operator.parseFromOpCode(this.getOpCode()));
			}
		}

		result.add(' ');
		result.add(this.operand2.getAdjustedName());
		
		return result;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		throw new UnsupportedOperationException();		//because this is handled elsewhere
	}
}
