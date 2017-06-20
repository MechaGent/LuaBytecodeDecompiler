package LuaBytecodeDecompiler.Mk02.Instructions;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk02.Instructions.VarCode.ScopeTypes;

public class Instruction_iABC extends Instruction
{
	private final int regA;
	private final short regB;
	private final short regC;

	public Instruction_iABC(int inInstructionAddress, AbstractOpCodes inOpCode, int inRaw, byte inRegA, short inRegB, short inRegC)
	{
		this(inInstructionAddress, inOpCode, inRaw, Byte.toUnsignedInt(inRegA), inRegB, inRegC);
	}
	
	public Instruction_iABC(int inInstructionAddress, AbstractOpCodes inOpCode, int inRaw, int inRegA, short inRegB, short inRegC)
	{
		super(inInstructionAddress, InstructionTypes.iABC, inOpCode, inRaw);
		this.regA = inRegA;
		this.regB = inRegB;
		this.regC = inRegC;
	}

	public static Instruction_iABC parseFromRaw(int address, int inRaw, OpCodeMapper inMapper)
	{
		return parseFromRaw(address, inRaw, inMapper, inMapper.getOpCodeFor(inRaw & 0x3f));
	}

	public static Instruction_iABC parseFromRaw(int address, int inRaw, OpCodeMapper inMapper, AbstractOpCodes code)
	{
		final byte regA = (byte) ((inRaw >>> 6) & 0xff);
		final short regC = (short) ((inRaw >>> 14) & 0x1ff);
		final short regB = (short) ((inRaw >>> 23) & 0x1ff);

		return new Instruction_iABC(address, code, inRaw, regA, regB, regC);
	}

	public int getA()
	{
		return this.regA;
	}

	public short getB()
	{
		return this.regB;
	}

	public short getC()
	{
		return this.regC;
	}

	@Override
	public String toString()
	{
		return this.toCharList().toString();
	}

	public CharList toCharList()
	{
		final CharList result = new CharList();

		result.add(this.opCode.toString());
		result.add('(');
		result.add("reg_A = ");
		result.add(Integer.toString(this.regA));
		result.add(", reg B = ");
		result.add(Short.toString(this.regB));
		result.add(", reg C = ");
		result.add(Short.toString(this.regC));
		result.add(')');

		return result;
	}

	@Override
	public CharList disAssemble()
	{
		final CharList result = this.disAssemble_start();

		result.add(Integer.toHexString(this.regA));
		result.add(", ");
		result.add(Integer.toHexString(this.regB));
		result.add(", ");
		result.add(Integer.toHexString(this.regC));
		result.add('}');

		return result;
	}

	/*
	@Override
	public VarCode getVarCode_Left()
	{
		final VarCode result;
		result = null;

		/*
		switch (this.opCode)
		{
			case Move:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);

				// right
				result = new VarCode(ScopeTypes.Local, this.regB);

				break;
			}
			case LoadNull:
			{
				// left
				VarCode prev = null;

				for (int i = this.regB; i > this.regA; i--)
				{
					prev = new VarCode(ScopeTypes.Local, i, prev);
				}

				result = new VarCode(ScopeTypes.Local, this.regA, prev);

				// right
				result = VarCode.getNullVarCode();

				break;
			}
			case LoadBool:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);

				// right
				result = new VarCode(ScopeTypes.Hardcoded_Bool, (this.regB != 0) ? 1 : 0);

				break;
			}
			case GetUpvalue:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);

				// right
				result = new VarCode(ScopeTypes.Upvalue, this.regB);

				break;
			}
			case SetUpvalue:
			{
				// left
				result = new VarCode(ScopeTypes.Upvalue, this.regB);

				// right
				result = new VarCode(ScopeTypes.Local, this.regA);

				break;
			}
			case GetTable:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);

				// right
				final VarCode next = getRK_VarCode(this.regC);
				result = new VarCode(ScopeTypes.Local, this.regB, true, next);

				break;
			}
			case SetTable:
			{
				// left
				final VarCode next = getRK_VarCode(this.regB);
				result = new VarCode(ScopeTypes.Local, this.regA, true, next);

				// right
				result = getRK_VarCode(this.regC);

				break;
			}
			case Add:
			case Subtract:
			case Multiply:
			case Divide:
			case Modulo:
			case Power:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);

				// right
				final VarCode next = getRK_VarCode(this.regC);
				result = getRK_VarCode(this.regB, next);

				break;
			}
			case ArithmeticNegation:
			case LogicalNegation:
			case Length:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);
				
				// right
				result = new VarCode(ScopeTypes.Local, this.regB);
				
				break;
			}
			case Concatenation:
			{
				// left
				result = new VarCode(ScopeTypes.Local, this.regA);
				
				//right
				VarCode prev = null;

				for (int i = this.regC; i > this.regB; i--)
				{
					prev = new VarCode(ScopeTypes.Local, i, prev);
				}

				result = new VarCode(ScopeTypes.Local, this.regB, prev);

				break;
			}
			case TailCall:	// holding off on this and CALL  and VarArg until clarification is obtained
			case VarArg:
			case Call:
			{
				/*
				 * how this unfolds depends upon whether the previous and next lines are also Calls.
				 *
				result = null;
				break;
			}
			case Return:
			{
				//left
				result = VarCode.getNullVarCode();
				
				//right
				final int B = this.regB;

				switch (B)
				{
					case 0:
					{
						final VarCode prev = new VarCode(ScopeTypes.Local, Integer.MAX_VALUE);

						result = new VarCode(ScopeTypes.Local, this.regA, prev);

						break;
					}
					case 1:
					{
						result = VarCode.getEmptyVarCode();
						break;
					}
					default:
					{
						VarCode prev = null;
						
						for(int i = this.regA + this.regB - 2; i > this.regA; i--)
						{
							prev = new VarCode(ScopeTypes.Local, i, prev);
						}
						
						result = new VarCode(ScopeTypes.Local, this.regA, prev);

						break;
					}
				}

				break;
			}
			case Self:
			{
				//left
				final VarCode APlusOne = new VarCode(ScopeTypes.Local, this.regA + 1);
				result = new VarCode(ScopeTypes.Local, this.regA, APlusOne);
				
				//right
				final VarCode codeC = getRK_VarCode(this.regC);
				final VarCode codeBsecond = new VarCode(ScopeTypes.Local, this.regB, true, codeC);
				result = new VarCode(ScopeTypes.Local, this.regB, codeBsecond);

				break;
			}
			case TestIfEqual:
			{
				
				
				result.add("if (");
				result.add(this.RK_toString(in.getB()));
				result.add(" == ");
				result.add(this.RK_toString(in.getC()));
				result.add(") != ");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(") then ");
				result.add(programCounter);
				result.add("++;");
				break;
			}
			case TestIfLessThan:
			{
				result.add("if (");
				result.add(this.RK_toString(in.getB()));
				result.add(" < ");
				result.add(this.RK_toString(in.getC()));
				result.add(") != ");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(") then ");
				result.add(programCounter);
				result.add("++;");
				break;
			}
			case TestIfLessThanOrEqual:
			{
				result.add("if (");
				result.add(this.RK_toString(in.getB()));
				result.add(" <= ");
				result.add(this.RK_toString(in.getC()));
				result.add(") != ");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(") then ");
				result.add(programCounter);
				result.add("++;");
				break;
			}
			case TestAND:
			{
				result.add("if !(");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" <=> ");
				result.add(Integer.toString(in.getC()));
				result.add(") then ");
				result.add(programCounter);
				result.add("++;");
				break;
			}
			case TestANDSave:
			{
				result.add("if (");
				final String nameB = this.localsDebugData[in.getB()].getName();
				result.add(nameB);
				result.add(" <=> ");
				result.add(Integer.toString(in.getC()));
				result.add(") then ");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(nameB);
				result.add(", else ");
				result.add(programCounter);
				result.add("++;");
				break;
			}
			case IterateGenericForLoop:
			{
				final int A = in.getA();
				final String nameA3 = this.localsDebugData[A + 3].getName();
				result.add(nameA3);

				for (int i = A + 4; i < A + 2 + in.getC() + 1; i++)
				{
					result.add(", ");
					result.add(this.localsDebugData[i].getName());
				}

				result.add(" = ");
				result.add(this.localsDebugData[A].getName());
				result.add('(');
				result.add(this.localsDebugData[A + 1].getName());
				result.add(", ");
				final String nameA2 = this.localsDebugData[A + 2].getName();
				result.add(nameA2);
				result.add("); if (");
				result.add(nameA3);
				result.add(" != null) then { ");
				result.add(nameA2);
				result.add(" = ");
				result.add(nameA3);
				result.add("; } else { ");
				result.add(programCounter);
				result.add("++; }");

				break;
			}
			case NewTable:
			{
				// TODO untested
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = new Table. length = ");
				result.add(Double.toString(MiscAutobot.parseAsFloatingPointByte(in.getB())));
				result.add(", hashSize = ");
				result.add(Double.toString(MiscAutobot.parseAsFloatingPointByte(in.getC())));
				result.add('.');
				break;
			}
			case SetList:
			case Close:
				break;
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}
	*/

	private static VarCode getRK_VarCode(int regValue)
	{
		return getRK_VarCode(regValue, null);
	}

	private static VarCode getRK_VarCode(int regValue, VarCode next)
	{
		final int compare = regValue & 0x100;

		if (compare == 0x100)
		{
			return new VarCode(ScopeTypes.Constant, regValue & ~0x100, next);
		}
		else
		{
			return new VarCode(ScopeTypes.Local, regValue, next);
		}
	}
}
