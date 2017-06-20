package LuaBytecodeDecompiler.Mk01.Abstract;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.DisAssemblable;
import LuaBytecodeDecompiler.Mk01.Instruction;
import LuaBytecodeDecompiler.Mk01.MiscAutobot;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class FunctionBlock implements DisAssemblable
{
	private static final NullConstant sharedNullConstant = new NullConstant();
	private static final NullConstant sharedUnknownConstant = new NullConstant();
	
	private static final int DisAssemblyMinPosWidth = 4;
	private static final int DisAssemblyMinValueWidth = 16;

	/**
	 * these lineNums are for the source file, not the binary
	 */
	private final String sourceFile;
	private final int lineDefined;
	private final int lastLineDefined;
	private final int numUpvalues;
	private final int numParams;
	private final int isVarArgFlag;
	private final int maxStackSize; // number of registers used
	private final Instruction[] instructions;
	private final Constant[] constants;
	private final FunctionBlock[] functions;

	/*
	 * Optional
	 * positions are encoded with the assumption that the starting index is 1
	 */
	private final int[] sourceLinePositions;
	private final LocalVarDebugInfo[] localVarsInfo;
	private final String[] Upvalues;

	private FunctionBlock(BytesStreamer in, byte sizeTLength, int luaNumSize, OpCodeMapper mapper, String sourceFileName)
	{
		this.sourceFile = sourceFileName;
		this.lineDefined = in.getNextInt();
		this.lastLineDefined = in.getNextInt();
		this.numUpvalues = in.getNextByte();
		this.numParams = in.getNextByte();
		this.isVarArgFlag = in.getNextByte();
		this.maxStackSize = in.getNextByte();
		
		final int instructionListSize = in.getNextInt();
		//System.out.println("numInstructs: " + instructionListSize + ", read directly before: " + Integer.toHexString(in.getPosition()));
		this.instructions = Instruction.parseNextInstructionArr(in, mapper, instructionListSize);
		//this.instructions = new Instruction[0];
		
		//in.setToPosition(0x101);
		//System.out.println("pos: " + Integer.toHexString(in.getPosition()));
		
		this.constants = Constant.parseNextConstantArr(in, sizeTLength, luaNumSize);

		this.functions = parseNextFunctionArr(in, sizeTLength, luaNumSize, mapper);

		final int sourceLinePosListSize = in.getNextInt();
		this.sourceLinePositions = in.getNextIntArr(sourceLinePosListSize);

		this.localVarsInfo = LocalVarDebugInfo.parseNextArr(in, sizeTLength);

		final int UpvaluesListSize = in.getNextInt();
		this.Upvalues = MiscAutobot.parseNextLuaStringArr(in, sizeTLength, UpvaluesListSize);
	}

	public static FunctionBlock[] parseNextFunctionArr(BytesStreamer in, byte sizeTLength, int luaNumSize, OpCodeMapper mapper)
	{
		final int ArrLength = in.getNextInt();
		//System.out.println("here: " + ArrLength);
		//System.out.println("numFunctions: " + ArrLength + ", read directly before: " + Integer.toHexString(in.getPosition()));
		final FunctionBlock[] result = new FunctionBlock[ArrLength];

		for (int i = 0; i < ArrLength; i++)
		{
			final String nextName = MiscAutobot.parseNextLuaString(in, sizeTLength);
			result[i] = new FunctionBlock(in, sizeTLength, luaNumSize, mapper, nextName);
		}

		return result;
	}

	public static NullConstant getSharednullconstant()
	{
		return sharedNullConstant;
	}

	public static int getDisassemblyminposwidth()
	{
		return DisAssemblyMinPosWidth;
	}

	public int getLineDefined()
	{
		return this.lineDefined;
	}

	public int getLastLineDefined()
	{
		return this.lastLineDefined;
	}

	public int getNumUpvalues()
	{
		return this.numUpvalues;
	}

	public int getNumParams()
	{
		return this.numParams;
	}

	public int getIsVarArgFlag()
	{
		return this.isVarArgFlag;
	}

	public int getMaxStackSize()
	{
		return this.maxStackSize;
	}

	public Instruction[] getInstructions()
	{
		return this.instructions;
	}

	public Constant[] getConstants()
	{
		return this.constants;
	}

	public FunctionBlock[] getFunctions()
	{
		return this.functions;
	}

	public int[] getSourceLinePositions()
	{
		return this.sourceLinePositions;
	}

	public LocalVarDebugInfo[] getLocalVarsInfo()
	{
		return this.localVarsInfo;
	}

	public String[] getUpvalues()
	{
		return this.Upvalues;
	}

	public CharList disAssemble()
	{
		return this.disAssemble(0, "");
	}

	public CharList disAssemble(int offset)
	{
		return this.disAssemble(offset, "");
	}

	public CharList disAssemble(String label)
	{
		return this.disAssemble(0, label);
	}

	public CharList disAssemble(int offset, String label)
	{
		final CharList result = new CharList('\t', offset);

		result.add("START: ");
		result.add(label);
		result.addNewLine();
		int pos = this.lineDefined + 12;

		/*
		 * Source name
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += this.sourceFile.length() + 1 + 8;
		result.add('\t');
		prepend(this.sourceFile, result, ' ', DisAssemblyMinValueWidth);
		result.add(": function name");
		result.addNewLine();
		
		/*
		 * lineDefined (4 bytes)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 4;
		result.add('\t');
		prepend(Integer.toHexString(this.lineDefined), result, ' ', DisAssemblyMinValueWidth);
		//result.add('\t');
		result.add(":\tfirst line wherein this function is written.");
		result.addNewLine();

		/*
		 * lastLineDefined (4 bytes)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 4;
		result.add('\t');
		prepend(Integer.toHexString(this.lastLineDefined), result, ' ', DisAssemblyMinValueWidth);
		result.add(":\tlast line wherein this function is written (Also noted at that line).");
		result.addNewLine();

		/*
		 * numUpvalues (1 byte)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 1;
		result.add('\t');
		prepend(Integer.toString(this.numUpvalues), result, ' ', DisAssemblyMinValueWidth);
		result.add(":\tthe (base10) quantity of upValues for this function");
		result.addNewLine();

		/*
		 * numParams (1 byte)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 1;
		result.add('\t');
		prepend(Integer.toString(this.numParams), result, ' ', DisAssemblyMinValueWidth);
		result.add(":\tthe (base10) quantity of parameters for this function");
		result.addNewLine();

		/*
		 * isVarArgFlag (1 byte)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 1;
		result.add('\t');
		prepend(Integer.toHexString(this.isVarArgFlag), result, ' ', DisAssemblyMinValueWidth);
		result.add(":\tindicates VarArg presence");
		result.addNewLine();

		/*
		 * maxStackSize (1 byte)
		 */
		result.add('\t', offset);
		prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
		pos += 1;
		result.add('\t');
		prepend(Integer.toString(this.maxStackSize), result, ' ', DisAssemblyMinValueWidth);
		result.add(":\tthe maximum stack size (base10)");
		result.addNewLine();

		/*
		 * instructions[] (4 + (.length * 4) bytes)
		 */
		for (int i = 0; i < this.instructions.length; i++)
		{
			result.add('\t', offset);
			prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
			pos += 4;
			result.add('\t');
			result.add(this.instructions[i].toString());
			result.addNewLine();
		}
		
		/*
		 * constants[]
		 */
		for(int i = 0; i < this.constants.length; i++)
		{
			result.add('\t', offset);
			prepend(Integer.toHexString(pos), result, '0', DisAssemblyMinPosWidth);
			pos += 4;
			result.add('\t');
			result.add(this.constants[i].toString());
			result.addNewLine();
		}
		
		/*
		 * functions[]
		 */
		for(int i = 0; i < this.functions.length; i++)
		{
			result.add(this.functions[i].disAssemble(offset + 1, ""), true);
		}
		
		result.add('\t', offset);
		result.add("END: ");
		result.add(label);
		result.addNewLine();

		return result;
	}

	/*
	private static void append(String append, CharList result, char delim, int minOffset)
	{
		final int dif = minOffset - append.length();
	
		result.add(append);
	
		if (dif > 0)
		{
			result.add(delim, dif);
		}
	}
	*/

	private static void prepend(String append, CharList result, char delim, int minOffset)
	{
		final int dif = minOffset - append.length();

		if (dif > 0)
		{
			result.add(delim, dif);
		}

		result.add(append);
	}

	public static class RootFunction extends FunctionBlock
	{
		private final String sourceName;

		private RootFunction(BytesStreamer in, byte sizeTLength, int luaNumSize, OpCodeMapper mapper, String inSourceName)
		{
			super(in, sizeTLength, luaNumSize, mapper, inSourceName);
			this.sourceName = inSourceName;
		}

		public static RootFunction parseNextRootFunction(BytesStreamer in, byte sizeTLength, int luaNumSize, OpCodeMapper mapper)
		{
			final String inSourceName = MiscAutobot.parseNextLuaString(in, sizeTLength);
			
			return new RootFunction(in, sizeTLength, luaNumSize, mapper, inSourceName);
		}

		public String getSourceName()
		{
			return this.sourceName;
		}
	}

	public static abstract class Constant
	{
		private final ConstantTypes type;

		public Constant(ConstantTypes inType)
		{
			this.type = inType;
		}

		public ConstantTypes getType()
		{
			return this.type;
		}

		@Override
		public abstract String toString();
		
		@SuppressWarnings("unused")
		private static int mask(int in)
		{
			return in & 0xff;
		}

		public static Constant parseNextConstant(BytesStreamer in, int sizeTLength, int luaNumSize)
		{
			final Constant result;
			
			//System.out.println("on line: " + Integer.toHexString(in.getPosition()));
			
			final int constFlagPos = in.getPosition();
			//final int veryRawType = in.getNextByte();
			//final int almostRawType = veryRawType & 0xff;
			final int rawType = in.getNextByte() & 0xff;
			
			final ConstantTypes type = ConstantTypes.parse(rawType & 0xff, constFlagPos);

			switch (type)
			{
				case Null:
				{
					result = sharedNullConstant;
					break;
				}
				case Bool:
				{
					result = new BoolConstant(in);
					break;
				}
				case Num:
				{
					result = new NumConstant(in, luaNumSize);
					break;
				}
				case String:
				{
					result = new StringConstant(in, sizeTLength);
					break;
				}
				case Unknown:
				{
					result = sharedUnknownConstant;
					break;
				}
				case Unknown_length9:
				{
					result = sharedUnknownConstant;
					in.getNextByteArr(9);
					break;
				}
				case Unknown_length8:
				{
					result = sharedUnknownConstant;
					in.getNextByteArr(8);
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Unhandled Enum: " + type);
				}
			}

			return result;
		}

		public static Constant[] parseNextConstantArr(BytesStreamer in, int sizeTLength, int luaNumSize)
		{
			final int ArrLength = in.getNextInt(EndianSettings.LeftBitRightByte);
			//System.out.println("ConstantArr length: " + ArrLength);
			//System.out.println("numConstants: " + ArrLength + ", read directly before " + Integer.toHexString(in.getPosition()));
			
			if(ArrLength < 0)
			{
				throw new ArrayIndexOutOfBoundsException(ArrLength + " attempted");
			}
			
			final Constant[] result = new Constant[ArrLength];

			for (int i = 0; i < ArrLength; i++)
			{
				result[i] = parseNextConstant(in, sizeTLength, luaNumSize);
			}

			return result;
		}
	}

	public static enum ConstantTypes
	{
		Unknown,
		Unknown_length8,
		Unknown_length9,
		Null,
		Bool,
		Num,
		String;

		public static ConstantTypes parse(int flag, int position)
		{
			final ConstantTypes result;

			switch (flag)
			{
				case 0:
				{
					result = Null;
					break;
				}
				case 1:
				{
					result = Bool;
					break;
				}
				case 3:
				{
					result = Num;
					break;
				}
				case 4:
				{
					result = String;
					break;
				}
				/*
				case 0x40:
				{
					result = Unknown_length9;
					break;
				}
				case 0x7:
				{
					result = Unknown_length8;
					break;
				}
				*/
				default:
				{
					//throw new IllegalArgumentException("Unhandled ConstantFlag: " + flag);
					result = Unknown;
					System.out.println("unknown ConstantFlag: " + Integer.toHexString(flag) + " at position: " + Integer.toHexString(position));
					break;
				}
			}

			return result;
		}
	}

	public static class NullConstant extends Constant
	{
		public NullConstant()
		{
			super(ConstantTypes.Null);
		}

		@Override
		public String toString()
		{
			return "NULL";
		}
	}

	public static class BoolConstant extends Constant
	{
		private final boolean value;

		public BoolConstant(BytesStreamer in)
		{
			super(ConstantTypes.Bool);
			this.value = in.getNextByte() == 1;
		}

		public boolean getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<Boolean> ");
			result.add(Boolean.toString(this.value));

			return result.toString();
		}
	}

	public static class NumConstant extends Constant
	{
		private final double value;

		public NumConstant(BytesStreamer in, int numSize)
		{
			super(ConstantTypes.Num);
			
			if(numSize == 8)
			{
			this.value = in.getNextDouble();
			}
			else if(numSize == 4)
			{
				this.value = in.getNextFloat();
			}
			else
			{
				throw new IllegalArgumentException("bad luaNum byteLength: " + numSize);
			}
		}

		public double getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<Number> ");
			result.add(Double.toString(this.value));

			return result.toString();
		}
	}

	public static class StringConstant extends Constant
	{
		private final String value;

		public StringConstant(BytesStreamer in, int sizeTLength)
		{
			super(ConstantTypes.String);
			this.value = MiscAutobot.parseNextLuaString(in, sizeTLength);
		}

		public String getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<String> ");
			result.add(this.value);

			return result.toString();
		}
	}

	public static class LocalVarDebugInfo
	{
		private final String name;
		private final int scopeStart;
		private final int scopeEnd;

		public LocalVarDebugInfo(BytesStreamer in, int sizeTLength)
		{
			this.name = MiscAutobot.parseNextLuaString(in, sizeTLength);
			this.scopeStart = in.getNextInt();
			this.scopeEnd = in.getNextInt();
		}

		public static LocalVarDebugInfo[] parseNextArr(BytesStreamer in, int sizeTLength)
		{
			//System.out.println("Locals place: " + Integer.toHexString(in.getPosition()));
			
			final int length = in.getNextInt();
			
			//System.out.println("numLocals: " + length + ", read directly before " + Integer.toHexString(in.getPosition()));
			
			final LocalVarDebugInfo[] result;

			if (length != 0)
			{
				result = new LocalVarDebugInfo[length];

				for (int i = 0; i < length; i++)
				{
					result[i] = new LocalVarDebugInfo(in, sizeTLength);
				}
			}
			else
			{
				result = new LocalVarDebugInfo[0];
			}

			return result;
		}

		public String getName()
		{
			return this.name;
		}

		public int getScopeStart()
		{
			return this.scopeStart;
		}

		public int getScopeEnd()
		{
			return this.scopeEnd;
		}
	}
}
