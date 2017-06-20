package LuaBytecodeDecompiler.Mk02.Instructions;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk02.ChunkHeader;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public abstract class Instruction
{
	private static final int minOffset_OpCodes = 16;

	protected final int instructionAddress;
	protected final InstructionTypes type;
	protected final AbstractOpCodes opCode;
	protected final int raw;
	
	public Instruction(int inInstructionAddress, InstructionTypes inType, AbstractOpCodes inOpCode, int inRaw)
	{
		this.instructionAddress = inInstructionAddress;
		//System.out.println("instruction constructor address: " + this.instructionAddress);
		this.type = inType;
		this.opCode = inOpCode;
		this.raw = inRaw;
	}
	
	public static Instruction parseNextInstruction(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper)
	{
		final int address = in.getPosition();
		final int first = in.getNextInt();
		final int ByteCode = first & 0x3f;
		final AbstractOpCodes OpType = mapper.getOpCodeFor(ByteCode);
		final InstructionTypes instType = mapper.getTypeOf(OpType);
		final Instruction curInstruct;

		switch (instType)
		{
			case iABC:
			{
				curInstruct = Instruction_iABC.parseFromRaw(address, first, mapper, OpType);
				break;
			}
			case iABx:
			{
				curInstruct = Instruction_iABx.parseFromRaw(address, first, mapper, OpType);
				break;
			}
			case iAsBx:
			{
				curInstruct = Instruction_iAsBx.parseFromRaw(address, first, mapper, OpType);
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}

		return curInstruct;
	}
	
	//public abstract VarCode getVarCode_Left();
	//public abstract VarCode getVarCode_Right();

	public int getInstructionAddress()
	{
		return this.instructionAddress;
	}

	public InstructionTypes getType()
	{
		return this.type;
	}

	public AbstractOpCodes getOpCode()
	{
		return this.opCode;
	}

	public int getRaw()
	{
		return this.raw;
	}

	public abstract CharList disAssemble();
	
	protected CharList disAssemble_start()
	{
		final CharList result = new CharList();

		MiscAutobot.append(this.opCode.toString(), result, ' ', minOffset_OpCodes);
		result.add('(');
		result.add(this.type.toString());
		result.add("){");

		return result;
	}
}
