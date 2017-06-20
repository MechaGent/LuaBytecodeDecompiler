package LuaBytecodeDecompiler.Mk01;

import DataStructures.Linkages.CharList.Mk03.CharList;
import HandyStuff.BitTwiddling;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public abstract class Instruction
{
	private static final EndianSettings SigEnd = EndianSettings.LeftBitLeftByte;
	private static final boolean isVerbose = false;
	
	protected final InstructionTypes type;
	protected final AbstractOpCodes opCode;
	protected final byte register_A;

	private Instruction(InstructionTypes inType, AbstractOpCodes inOpCode, byte inRegister_A)
	{
		this.type = inType;
		this.opCode = inOpCode;
		this.register_A = inRegister_A;
	}
	
	public byte getA()
	{
		return this.register_A;
	}
	
	public int getUnsignedA()
	{
		return Byte.toUnsignedInt(this.register_A);
	}
	
	@Override
	public abstract String toString();
	
	protected static int flipInt(int in)
	{
		final byte r0 = (byte) (in & 0x0f);
		final byte r1 = (byte) ((in >>> 8) & 0x0f);
		final byte r2 = (byte) ((in >>> 16) & 0x0f);
		final byte r3 = (byte) ((in >>> 24) & 0x0f);
		return BitTwiddling.parseInt(SigEnd, r0, r1, r2, r3);
	}
	
	public static Instruction parseNextInstruction(BytesStreamer in, OpCodeMapper mapper)
	{
		final int first = in.getNextInt();
		final int ByteCode = first & 0x3f;
		final AbstractOpCodes OpType = mapper.getOpCodeFor(ByteCode);
		final InstructionTypes instType = mapper.getTypeOf(OpType);
		final Instruction curInstruct;

		switch (instType)
		{
			case iABC:
			{
				curInstruct = RawInstruction_iABC.parseFromRaw(first, mapper);
				break;
			}
			case iABx:
			{
				curInstruct = RawInstruction_iABx.parseFromRaw(first, mapper);
				break;
			}
			case iAsBx:
			{
				curInstruct = RawInstruction_iAsBx.parseFromRaw(first, mapper);
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}
		
		return curInstruct;
	}
	
	public static Instruction[] parseNextInstructionArr(BytesStreamer in, OpCodeMapper mapper, int length)
	{
		final Instruction[] result = new Instruction[length];
		//System.out.println("ArrLength: " + length);
		
		for(int i = 0; i < length; i++)
		{
			result[i] = parseNextInstruction(in, mapper);
		}
		
		return result;
	}

	public static class RawInstruction_iABC extends Instruction
	{
		private final short register_B;
		private final short register_C;
		
		private RawInstruction_iABC(AbstractOpCodes inOpCode, byte inA, short inRegister_B, short inRegister_C)
		{
			super(InstructionTypes.iABC, inOpCode, inA);
			this.register_B = inRegister_B;
			this.register_C = inRegister_C;
		}
		
		public static RawInstruction_iABC parseFromRaw(int inRaw, OpCodeMapper inMapper)
		{
			//inRaw = flipInt(inRaw);
			final int rawCode = inRaw & 0x003f;
			final AbstractOpCodes code = inMapper.getOpCodeFor(rawCode);	//3f == 111111
			final byte regA = (byte) ((inRaw >>> 6) & 0xff);
			final short regC = (short) ((inRaw >>> 14) & 0x1ff);
			final short regB = (short) ((inRaw >>> 23) & 0x1ff);
			
			if(isVerbose)
			{
				System.out.println("inRaw: " + Integer.toBinaryString(inRaw) + ", OpCode: " + Integer.toHexString(rawCode) + ", regA: " + regA + " regB: " + regB + " regC " + regC);
				System.out.println(Integer.toBinaryString(0x1f));
			}
			
			return new RawInstruction_iABC(code, regA, regB, regC);
		}
		
		public short getB()
		{
			return this.register_B;
		}
		
		public int getUnsignedB()
		{
			return Short.toUnsignedInt(this.register_B);
		}
		
		public short getC()
		{
			return this.register_C;
		}
		
		public int getUnsignedC()
		{
			return Short.toUnsignedInt(this.register_C);
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
			result.add(Short.toString(this.register_A));
			result.add(", reg B = ");
			result.add(Short.toString(this.register_B));
			result.add(", reg C = ");
			result.add(Short.toString(this.register_C));
			result.add(')');
			
			return result;
		}
	}
	
	public static class RawInstruction_iABx extends Instruction
	{
		private final int Bx;

		public RawInstruction_iABx(AbstractOpCodes inOpCode, byte inA, int inBx)
		{
			super(InstructionTypes.iABx, inOpCode, inA);
			this.Bx = inBx;
		}
		
		public static RawInstruction_iABx parseFromRaw(int inRaw, OpCodeMapper inMapper)
		{
			//inRaw = flipInt(inRaw);
			final int rawCode = inRaw & 0x3f;
			final AbstractOpCodes code = inMapper.getOpCodeFor(rawCode);	//3f == 111111
			final byte regA = (byte) ((inRaw >>> 6) & 0xff);
			final int regBx = ((inRaw >>> 14) & 0x3ffff);
			
			if(isVerbose)
			{
				System.out.println("inRaw: " + Integer.toHexString(inRaw) + ", OpCode: " + code + ", regA: " + regA + " regBx: " + regBx);
			}
			
			return new RawInstruction_iABx(code, regA, regBx);
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
			result.add(Integer.toString(this.register_A));
			result.add(", reg Bx = ");
			result.add(Integer.toString(this.Bx));
			result.add(')');
			
			return result;
		}
	}
	
	public static class RawInstruction_iAsBx extends Instruction
	{
		private final int sBx;

		/**
		 * 
		 * @param inType
		 * @param inOpCode
		 * @param inA
		 * @param inSBx don't try to pre-format this
		 */
		public RawInstruction_iAsBx(AbstractOpCodes inOpCode, byte inA, int inSBx)
		{
			super(InstructionTypes.iAsBx, inOpCode, inA);
			this.sBx = (int) (Integer.toUnsignedLong(inSBx) - 131071);
		}
		
		public static RawInstruction_iAsBx parseFromRaw(int inRaw, OpCodeMapper inMapper)
		{
			//inRaw = flipInt(inRaw);
			final int rawCode = inRaw & 0x3f;
			final AbstractOpCodes code = inMapper.getOpCodeFor(rawCode);	//3f == 111111
			final byte regA = (byte) ((inRaw >>> 6) & 0xff);
			final int regsBx = ((inRaw >>> 14) & 0x3ffff);
			
			if(isVerbose)
			{
				System.out.println("inRaw: " + Integer.toHexString(inRaw) + ", OpCode: " + code + ", regA: " + regA + " regsBx: " + regsBx);
			}
			
			return new RawInstruction_iAsBx(code, regA, regsBx);
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
			result.add(Integer.toString(this.register_A));
			result.add(", reg sBx = ");
			result.add(Integer.toString(this.sBx));
			result.add(')');
			
			return result;
		}
	}
}
