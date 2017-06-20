package LuaBytecodeDecompiler.Mk02.Instructions;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;

public class Instruction_iABx extends Instruction
{
	private final int regA;
	private final int Bx;

	public Instruction_iABx(int instructionAddress, AbstractOpCodes inOpCode, int inRaw, byte inA, int inBx)
	{
		this(instructionAddress, inOpCode, inRaw, Byte.toUnsignedInt(inA), inBx);
	}
	
	public Instruction_iABx(int instructionAddress, AbstractOpCodes inOpCode, int inRaw, int inA, int inBx)
	{
		super(instructionAddress, InstructionTypes.iABx, inOpCode, inRaw);
		this.regA = inA;
		this.Bx = inBx;
	}

	public static Instruction_iABx parseFromRaw(int address, int inRaw, OpCodeMapper inMapper)
	{
		return parseFromRaw(address, inRaw, inMapper, inMapper.getOpCodeFor(inRaw & 0x3f));
	}
	
	public static Instruction_iABx parseFromRaw(int instructionAddress, int inRaw, OpCodeMapper inMapper, AbstractOpCodes code)
	{
		final int regA = (inRaw >>> 6) & 0xff;
		final int regBx = ((inRaw >>> 14) & 0x3ffff);

		return new Instruction_iABx(instructionAddress, code, inRaw, regA, regBx);
	}
	
	public int getA()
	{
		return this.regA;
	}

	public int getBx()
	{
		return this.Bx;
	}

	public long getUnsignedBx()
	{
		return Integer.toUnsignedLong(this.Bx);
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
		result.add(", reg Bx = ");
		result.add(Integer.toString(this.Bx));
		result.add(')');

		return result;
	}

	@Override
	public CharList disAssemble()
	{
		final CharList result = this.disAssemble_start();

		result.add(Integer.toHexString(this.regA));
		result.add(", ");
		result.add(Integer.toHexString(this.Bx));
		result.add('}');

		return result;
	}
}
