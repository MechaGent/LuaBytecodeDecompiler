package LuaBytecodeDecompiler.Mk03;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler.VarTouchy;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public abstract class AnnoInstruction
{
	private static final int minOffset_ByteAddress = 4;
	private static final int minOffset_OpCodes = 24;
	private static final int minOffset_TerseByteCode = 35;

	private final int raw;

	private final int instructionAddress;
	private final InstructionTypes type;
	private final AbstractOpCodes opCode;
	private final boolean canManipulateProgramCounter;
	private boolean presentAsNoOp;
	private String label;

	public AnnoInstruction(int inRaw, int inInstructionAddress, InstructionTypes inType, AbstractOpCodes inOpCode)
	{
		this(inRaw, inInstructionAddress, inType, inOpCode, false);
	}

	public AnnoInstruction(int inRaw, int inInstructionAddress, InstructionTypes inType, AbstractOpCodes inOpCode, boolean inCanManipulateProgramCounter)
	{
		this.raw = inRaw;
		this.instructionAddress = inInstructionAddress;
		this.type = inType;
		this.opCode = inOpCode;
		this.canManipulateProgramCounter = inCanManipulateProgramCounter;
		this.presentAsNoOp = false;
		this.label = "";
	}

	public int getInstructionAddress()
	{
		return this.instructionAddress;
	}

	public InstructionTypes getType()
	{
		return this.type;
	}

	public int getRawCode()
	{
		return this.raw;
	}

	public AbstractOpCodes getOpCode()
	{
		return this.opCode;
	}

	public boolean canManipulateProgramCounter()
	{
		return this.canManipulateProgramCounter;
	}

	public void setAsNoOp()
	{
		this.presentAsNoOp = true;
	}

	public void setLabel(String in)
	{
		this.label = in;
	}

	@Override
	public String toString()
	{
		return this.toCharList(false).toString();
	}

	public CharList toCharList(boolean isVerbose)
	{
		final CharList result = new CharList();
		
		if (this.presentAsNoOp)
		{
			result.add("//");
		}
		
		result.add('\t');

		if (this.label.length() != 0)
		{
			result.add(label);
			result.add(' ');
		}
		
		//result.add('\t');

		MiscAutobot.prepend(Integer.toHexString(this.instructionAddress), result, '0', minOffset_ByteAddress);
		result.add('\t');
		MiscAutobot.append(this.opCode.toString(), result, ' ', minOffset_OpCodes);
		final int old = result.charSize();
		result.add('(');
		result.add(this.type.toString());
		result.add("){");
		final int[] values = splitInstructionValues(this.type, this.raw);

		switch (values.length)
		{
			case 1:
			{
				result.add(Integer.toString(values[0]));
				break;
			}
			case 2:
			{
				result.add(Integer.toString(values[0]));
				result.add(", ");
				result.add(Integer.toString(values[1]));
				break;
			}
			case 3:
			{
				result.add(Integer.toString(values[0]));
				result.add(", ");
				result.add(Integer.toString(values[2]));
				result.add(", ");
				result.add(Integer.toString(values[1]));
				break;
			}
			default:
			{
				break;
			}
		}

		result.add('}');

		final int dif = minOffset_TerseByteCode - (result.charSize() - old);

		if (dif > 0)
		{
			result.add(' ', dif);
		}

		result.add("|||\t");

		if (isVerbose)
		{
			if (this.presentAsNoOp)
			{
				result.add("//\t");
			}

			result.add(this.toCharList_internal(isVerbose), true);
		}

		return result;
	}

	public CharList toCharList_InlineForm(VarTouchy target)
	{
		return new CharList();
	}

	protected abstract CharList toCharList_internal(boolean isVerbose);

	public abstract void addAllReadsAndWrites(VarFlowHandler in);

	public abstract CharList toCharList_InlineForm();
	
	public abstract CharList toPseudoJava(int offset);
	
	protected CharList toPseudoJava_unhandled(int offset)
	{
		final CharList result = this.toCharList(true);
		result.push("<ERROR: currently unhandled!> ");
		result.push('\t', offset);
		return result;
	}

	public abstract AnnoInstruction replaceStep(int substep, AnnoVar replacement);

	private static int[] splitInstructionValues(InstructionTypes type, int raw)
	{
		final int[] result;

		switch (type)
		{
			case iABC:
			{
				result = new int[3];
				result[0] = (raw >>> 6) & 0xff;
				result[1] = (raw >>> 14) & 0x1ff;
				result[2] = (raw >>> 23) & 0x1ff;
				break;
			}
			case iABx:
			{
				result = new int[2];
				result[0] = (raw >>> 6) & 0xff;
				result[1] = (raw >>> 14) & 0x3ffff;
				break;
			}
			case iAsBx:
			{
				result = new int[2];
				result[0] = (raw >>> 6) & 0xff;
				result[1] = (int) (Integer.toUnsignedLong((raw >>> 14) & 0x3ffff) - 131071);
				break;
			}
			case HighLevel:
			{
				result = new int[0];
				break;
			}
			default:
			{
				throw new UnhandledEnumException(type);
			}
		}

		return result;
	}

	public static class NullAnnoInstruction extends AnnoInstruction
	{
		public NullAnnoInstruction(int inI)
		{
			super(-1, inI, InstructionTypes.HighLevel, AbstractOpCodes.NoOp);
		}

		@Override
		protected CharList toCharList_internal(boolean inIsVerbose)
		{
			return new CharList("Null Instruction");
		}

		@Override
		public void addAllReadsAndWrites(VarFlowHandler in)
		{

		}

		@Override
		public CharList toCharList_InlineForm()
		{
			return new CharList("Null Instruction");
		}

		@Override
		public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
		{
			return this;
		}

		@Override
		public CharList toPseudoJava(int inOffset)
		{
			return new CharList("Null Instruction");
		}

		/*
		@Override
		public ReadWriteState getReadWriteState(AnnoVar inIn)
		{
			return ReadWriteState.None;
		}
		*/
	}
}
