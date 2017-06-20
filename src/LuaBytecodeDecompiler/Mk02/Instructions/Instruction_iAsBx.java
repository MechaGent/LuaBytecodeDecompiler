package LuaBytecodeDecompiler.Mk02.Instructions;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;

public class Instruction_iAsBx extends Instruction
{
	private final int regA;
	private final int sBx;

	/**
	 * 
	 * @param inType
	 * @param inOpCode
	 * @param inA
	 * @param inSBx
	 *            don't try to pre-format this
	 */
	public Instruction_iAsBx(int instructionAddress, AbstractOpCodes inOpCode, int inRaw, int inA, int inSBx)
	{
		super(instructionAddress, InstructionTypes.iAsBx, inOpCode, inRaw);
		this.regA = inA;
		this.sBx = (int) (Integer.toUnsignedLong(inSBx) - 131071);
	}

	public static Instruction_iAsBx parseFromRaw(int address, int inRaw, OpCodeMapper inMapper)
	{
		return parseFromRaw(address, inRaw, inMapper, inMapper.getOpCodeFor(inRaw & 0x3f));
	}
	
	public static Instruction_iAsBx parseFromRaw(int instructionAddress, int inRaw, OpCodeMapper inMapper, AbstractOpCodes code)
	{
		final int regA = ((inRaw >>> 6) & 0xff);
		final int regsBx = ((inRaw >>> 14) & 0x3ffff);

		return new Instruction_iAsBx(instructionAddress, code, inRaw, regA, regsBx);
	}
	
	public int getA()
	{
		return this.regA;
	}

	public int get_sBx()
	{
		return this.sBx;
	}

	public long getUnsigned_sBx()
	{
		return Integer.toUnsignedLong(this.sBx);
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
		result.add(", reg sBx = ");
		result.add(Integer.toString(this.sBx));
		result.add(')');

		return result;
	}

	@Override
	public CharList disAssemble()
	{
		final CharList result = this.disAssemble_start();

		result.add(Integer.toHexString(this.regA));
		result.add(", ");
		result.add(Integer.toHexString(this.sBx));
		result.add('}');

		return result;
	}
}
