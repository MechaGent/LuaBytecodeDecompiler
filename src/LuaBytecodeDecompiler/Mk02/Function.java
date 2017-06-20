package LuaBytecodeDecompiler.Mk02;

import java.util.Iterator;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk01.SingleLinkedList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABC;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABx;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iAsBx;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class Function implements Iterable<Function>
{
	private static int dummyFunctionInstanceCounter = 0;
	
	private static final int minOffset_ByteAddress = 4;
	private static final int minOffset_TerseByteCode = 35;
	private static final String GlobalVarsPrefix = "GlobalVariables.";

	private static final String GlobalMethodsPrefix = "GlobalMethods.";

	/**
	 * lengthOf(&ltdataType&gt in): if it's a string, returns string length; if it's a table, returns table length; otherwise, calls 'the metamethod'
	 */
	private static final String GlobalMethods_lengthOf = GlobalMethodsPrefix + "lengthOf(";

	/**
	 * concat(String ... in): meant for String concatenation, but didn't want to use '+'s so as to avoid patternSkimming confusion
	 */
	private static final String GlobalMethods_Concatenate = GlobalMethodsPrefix + "concat(";

	private static final String programCounter = "Internal.ProgramCounter";

	private final String parentName;
	private final String functionName;
	private final int lineDefined;
	private final int lastLineDefined;
	private final int numUpvalues;
	private final int numParameters;
	private final int isVarArgFlag;
	private final int maxStackSize;

	private final int instructionStartByteOffset;
	private final Instruction[] instructions;

	private final InstructionCounter instructionCounts;

	private final int constantsStartByteOffset;
	private final Constant[] constants;

	private final int functionsStartByteOffset;
	private final Function[] functionPrototypes;

	private final int localsDebugDataStartByteOffset;
	private final Local[] localsDebugData;

	private final int upValuesDebugDataStartByteOffset;
	private final String[] upValueNames;

	private Function(String inParent, String inFunctionName, int inLineDefined, int inLastLineDefined, int inNumUpvalues, int inNumParameters, int inIsVarArgFlag, int inMaxStackSize, int inInstructionStartByteOffset, Instruction[] inInstructions, InstructionCounter inInstructionCounts, int inConstantsStartByteOffset, Constant[] inConstants, int inFunctionsStartByteOffset, Function[] inFunctionPrototypes, int inLocalsDebugDataStartByteOffset, Local[] inLocalsDebugData, int inUpValuesDebugDataStartByteOffset, String[] inUpValueNames)
	{
		this.parentName = inParent;
		this.functionName = inFunctionName;
		this.lineDefined = inLineDefined;
		this.lastLineDefined = inLastLineDefined;
		this.numUpvalues = inNumUpvalues;
		this.numParameters = inNumParameters;
		this.isVarArgFlag = inIsVarArgFlag;
		this.maxStackSize = inMaxStackSize;
		this.instructionStartByteOffset = inInstructionStartByteOffset;
		this.instructions = inInstructions;
		this.instructionCounts = inInstructionCounts;
		this.constantsStartByteOffset = inConstantsStartByteOffset;
		this.constants = inConstants;
		this.functionsStartByteOffset = inFunctionsStartByteOffset;
		this.functionPrototypes = inFunctionPrototypes;
		this.localsDebugDataStartByteOffset = inLocalsDebugDataStartByteOffset;
		this.localsDebugData = inLocalsDebugData;
		this.upValuesDebugDataStartByteOffset = inUpValuesDebugDataStartByteOffset;
		this.upValueNames = inUpValueNames;
	}
	
	public static Function generateDummyFunction()
	{
		return generateDummyFunction("DummyFunction_" + dummyFunctionInstanceCounter++);
	}
	
	public static Function generateDummyFunction(String dummyName)
	{
		return new Function(dummyName + "_Parent", dummyName, -1, -1,(byte) -1,(byte) -1,(byte) -1,(byte) 2, -1, new Instruction[0], new InstructionCounter(), -1, new Constant[0], -1, new Function[0], -1, new Local[0], -1, new String[0]);
	}

	@Deprecated
	public static Function parseNextFunction_Alt(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper, String inParent, Accountant tracker)
	{
		final String functionOldName = MiscAutobot.parseNextLuaStringDefensively(in, header.getSize_t(), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		// System.out.println(functionOldName);
		
		if(functionOldName == null)
		{
			return null;
		}
		
		final String inFunctionName = tracker.getAlteredName(functionOldName);
		// System.out.println("starting parse of: " + inFunctionName);
		final int inLineDefined = in.getNextInt();
		final int inLastLineDefined = in.getNextInt();
		final byte inNumUpvalues = (byte) in.getNextByte();
		final byte inNumParameters = (byte) in.getNextByte();
		final byte inIsVarArgFlag = (byte) in.getNextByte();
		final byte inMaxStackSize = (byte) in.getNextByte();
		// System.out.println("maxStackSize: " + inMaxStackSize);
		final int inInstructionStartByteOffset = in.getPosition();
		final InstructionCounter inInstructionCounts = new InstructionCounter();
		final Instruction[] inInstructions = parseInstructionsArr(in, header, mapper, inInstructionCounts);
		final int inConstantsStartByteOffset = in.getPosition();
		// System.out.println("constants starting at pos: 0x" + Integer.toHexString(inConstantsStartByteOffset));
		final int AllegedConstantsArrLength = in.getNextInt(in.getDefaultInputEndianness());
		int Readiness = 0;
		Constant[] inConstants = parseConstantsArr(in, header, mapper, AllegedConstantsArrLength);
		final int inFunctionsStartByteOffset;
		final Function[] inFunctionPrototypes;
		final int sourceLinePosListSize;
		// System.out.println("sourceLinePosList length: " + Integer.toHexString(sourceLinePosListSize) + " at pos: " + Integer.toHexString(in.getPosition()));
		final int inLocalsDebugDataStartByteOffset;
		final Local[] inLocalsDebugData;
		final int inUpValuesDebugDataStartByteOffset;
		final String[] inUpValueNames;

		if (inConstants.length == AllegedConstantsArrLength) // see 'parseConstantsArr' for explanation
		{
			Readiness++;
		}

		if (Readiness < 1)
		{
			inFunctionsStartByteOffset = -1;
		}
		else
		{
			inFunctionsStartByteOffset = in.getPosition();
			Readiness++;
		}

		//System.out.println("here, with Readiness: " + Readiness);
		final int numFunctions;
		
		if (Readiness < 2 || in.canSafelyConsume(4))
		{
			numFunctions = -1;
		}
		else
		{
			numFunctions = in.getNextInt(in.getDefaultInputEndianness());
			
			Readiness++;
		}
		
		if(numFunctions == -1)
		{
			inFunctionPrototypes = new Function[0];
		}
		else
		{
			final SingleLinkedList<Function> temp = parseFunctionsArr(in, header, mapper, inFunctionName, tracker, numFunctions);
			
			if(temp.size() == numFunctions)
			{
				inFunctionPrototypes = temp.toArray(new Function[temp.size()]);
			}
			else
			{
				final Function[] hybrid = new Function[numFunctions];
				
				for(int i = 0; i < numFunctions; i++)
				{
					if(!temp.isEmpty())
					{
						hybrid[i] = temp.pop();
					}
					else
					{
						hybrid[i] = null;
					}
				}
				
				inFunctionPrototypes = hybrid;
			}
		}

		/*
		if (Readiness < 3)
		{
			sourceLinePosListSize = -1;
		}
		else
		{
		*/
			sourceLinePosListSize = in.getNextInt(in.getDefaultInputEndianness());
			Readiness++;
			System.out.println("sourceLinePosListSize: " + Integer.toHexString(sourceLinePosListSize) + ", at " + (in.getPosition()-4));
		//}

		if (Readiness < 4 || !in.canSafelyConsume(sourceLinePosListSize * 4))
		{

		}
		else
		{
			in.getNextIntArr(sourceLinePosListSize);
			Readiness++;
		}

		if (Readiness < 5)
		{
			inLocalsDebugDataStartByteOffset = -1;
		}
		else
		{
			inLocalsDebugDataStartByteOffset = in.getPosition();
			Readiness++;
		}

		if (Readiness < 6 || !in.canSafelyConsume((sourceLinePosListSize * 4) + 4))
		{
			inLocalsDebugData = Local.getDummyLocalsArr(inMaxStackSize);
		}
		else
		{
			inLocalsDebugData = parseLocalsArr(in, header.getSize_t(), inMaxStackSize);
			Readiness++;
		}

		if (Readiness < 6)
		{
			inUpValuesDebugDataStartByteOffset = -1;
		}
		else
		{
			inUpValuesDebugDataStartByteOffset = in.getPosition();
			Readiness++;
		}

		if (Readiness < 7 || !in.canSafelyConsume(4))
		{
			inUpValueNames = generateDummyUpvaluesArr(inNumUpvalues);
		}
		else
		{
			inUpValueNames = parseUpvaluesArr(in, header, inNumUpvalues);
			Readiness++;
		}

		final Function result = new Function(inParent, inFunctionName, inLineDefined, inLastLineDefined, inNumUpvalues, inNumParameters, inIsVarArgFlag, inMaxStackSize, inInstructionStartByteOffset, inInstructions, inInstructionCounts, inConstantsStartByteOffset, inConstants, inFunctionsStartByteOffset, inFunctionPrototypes, inLocalsDebugDataStartByteOffset, inLocalsDebugData, inUpValuesDebugDataStartByteOffset, inUpValueNames);

		tracker.register_Function(result);

		// System.out.println("yay");
		//System.out.println("****new function: " + inFunctionName + "!\r\nStream pos: " + in.getPosition() + "\r\n" + result.disAssemble());
		return result;
	}
	
	public static Function parseNextFunction(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper, String inParent, Accountant tracker)
	{
		final String functionOldName = MiscAutobot.parseNextLuaStringDefensively(in, header.getSize_t(), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		
		if(functionOldName == null)
		{
			return null;
		}
		
		final String inFunctionName = tracker.getAlteredName(functionOldName);
		//System.out.println("Function name: " + inFunctionName);
		final int inLineDefined = in.getNextInt();
		final int inLastLineDefined = in.getNextInt();
		final int inNumUpvalues = in.getNextByte();
		final int inNumParameters = in.getNextByte();
		final int inIsVarArgFlag = in.getNextByte();
		final int inMaxStackSize = in.getNextByte();
		final int inInstructionStartByteOffset = in.getPosition();
		final InstructionCounter inInstructionCounts = new InstructionCounter();
		final Instruction[] inInstructions = parseInstructionsArr(in, header, mapper, inInstructionCounts);
		final int inConstantsStartByteOffset = in.getPosition();
		final int AllegedConstantsArrLength = in.getNextInt(in.getDefaultInputEndianness());
		final Constant[] inConstants = parseConstantsArr(in, header, mapper, AllegedConstantsArrLength);
		final int inFunctionsStartByteOffset = in.getPosition();
		final int numFunctions = in.getNextInt(in.getDefaultInputEndianness());
		final SingleLinkedList<Function> temp = parseFunctionsArr(in, header, mapper, inFunctionName, tracker, numFunctions);
		final Function[] inFunctionPrototypes = temp.toArray(new Function[temp.size()]);
		final int sourceLinePosListSize = in.getNextInt(in.getDefaultInputEndianness());
		//System.out.println("sourceLineList is size " + sourceLinePosListSize);
		in.getNextByteArr(sourceLinePosListSize * 4);
		
		final int inLocalsDebugDataStartByteOffset = in.getPosition();
		final Local[] inLocalsDebugData = parseLocalsArr(in, header.getSize_t(), inMaxStackSize);
		final int inUpValuesDebugDataStartByteOffset = in.getPosition();
		final String[] inUpValueNames = parseUpvaluesArr(in, header, inNumUpvalues);
		
		final Function result = new Function(inParent, inFunctionName, inLineDefined, inLastLineDefined, inNumUpvalues, inNumParameters, inIsVarArgFlag, inMaxStackSize, inInstructionStartByteOffset, inInstructions, inInstructionCounts, inConstantsStartByteOffset, inConstants, inFunctionsStartByteOffset, inFunctionPrototypes, inLocalsDebugDataStartByteOffset, inLocalsDebugData, inUpValuesDebugDataStartByteOffset, inUpValueNames);

		tracker.register_Function(result);
		
		return result;
	}

	private static Instruction[] parseInstructionsArr(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper, InstructionCounter inInstructionCounts)
	{
		final int length = in.getNextInt(in.getDefaultInputEndianness());
		//System.out.println("instructionListLength: " + length + " read directly before: " + Integer.toHexString(in.getPosition()));
		final Instruction[] result = new Instruction[length];

		for (int i = 0; i < length; i++)
		{
			final Instruction current = Instruction.parseNextInstruction(in, header, mapper);
			result[i] = current;
			inInstructionCounts.countInstruction(current);
		}

		return result;
	}

	/**
	 * have to do this defensively, because apparently it's alright to just stop a lua file halfway through the constants table
	 * 
	 * @param in
	 * @param header
	 * @param mapper
	 * @return
	 */
	private static Constant[] parseConstantsArr(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper, int length)
	{
		final SingleLinkedList<Constant> result2 = new SingleLinkedList<Constant>();
		boolean isWorking = true;

		for (int i = 0; i < length && isWorking && in.canSafelyConsume(header.getSize_t()); i++)
		{
			final Constant temp = Constant.parseNextConstant(in, header, mapper);
			//System.out.println("Constant: " + temp);

			if (temp == null)
			{
				isWorking = false;
			}
			else
			{
				result2.add(temp);
			}
		}

		//System.out.println("leaving constants at pos " + Integer.toHexString(in.getPosition()));
		return result2.toArray(new Constant[result2.size()]);
	}

	private static SingleLinkedList<Function> parseFunctionsArr(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper, String parentName, Accountant tracker, int length)
	{
		//System.out.println("entering functions at pos " + Integer.toHexString(in.getPosition()));
		// System.out.println("should be ending at position: " + Integer.toHexString(in.getPosition() + length));
		final SingleLinkedList<Function> result = new SingleLinkedList<Function>();
		//final Function[] result = new Function[length];

		for (int i = 0; i < length; i++)
		{
			final Function temp = Function.parseNextFunction(in, header, mapper, parentName, tracker);
			
			if(temp == null)
			{
				break;
			}
			else
			{
				result.add(temp);
			}
		}

		// System.out.println("ending at position: " + Integer.toHexString(in.getPosition()));
		//System.out.println("leaving functions at pos " + Integer.toHexString(in.getPosition()));
		return result;
	}

	private static Local[] parseLocalsArr(BytesStreamer in, int size_tLength, int maxStackSize)
	{
		Local[] result = Local.parseNextLocalArr(in, size_tLength);
		// Local[] result = new Local[0];

		if (result.length < maxStackSize)
		{
			final Local[] redux = Local.getDummyLocalsArr(maxStackSize);

			for (int i = 0; i < result.length; i++)
			{
				redux[i] = result[i];
			}

			result = redux;
		}

		return result;
	}

	private static String[] parseUpvaluesArr(BytesStreamer in, ChunkHeader header, int numUpvaluesExpected)
	{
		String[] result = MiscAutobot.parseNextLuaStringArr(in, header.getSize_t(), EndianSettings.LeftBitLeftByte, EndianSettings.LeftBitLeftByte);

		if (result.length == 0)
		{
			result = generateDummyUpvaluesArr(numUpvaluesExpected);
		}

		return result;
	}

	private static String[] generateDummyUpvaluesArr(int numUpvaluesExpected)
	{
		String[] result = new String[numUpvaluesExpected];

		for (int i = 0; i < numUpvaluesExpected; i++)
		{
			result[i] = "UpValue_" + i;
		}

		return result;
	}

	/**
	 * the iterator this returns follows breadth-first ordering
	 */
	@Override
	public Iterator<Function> iterator()
	{
		return new FunctionsIterator(this);
	}

	public String getParentName()
	{
		return this.parentName;
	}

	public String getFunctionName()
	{
		return this.functionName;
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

	public int getNumParameters()
	{
		return this.numParameters;
	}

	public int getIsVarArgFlag()
	{
		return this.isVarArgFlag;
	}

	public int getMaxStackSize()
	{
		return this.maxStackSize;
	}

	public int getInstructionStartByteOffset()
	{
		return this.instructionStartByteOffset;
	}

	public Instruction[] getInstructions()
	{
		return this.instructions;
	}

	public InstructionCounter getInstructionCounts()
	{
		return this.instructionCounts;
	}

	public int getConstantsStartByteOffset()
	{
		return this.constantsStartByteOffset;
	}

	public Constant[] getConstants()
	{
		return this.constants;
	}

	public int getFunctionsStartByteOffset()
	{
		return this.functionsStartByteOffset;
	}

	public Function[] getFunctionPrototypes()
	{
		return this.functionPrototypes;
	}

	public int getLocalsDebugDataStartByteOffset()
	{
		return this.localsDebugDataStartByteOffset;
	}

	public Local[] getLocalsDebugData()
	{
		return this.localsDebugData;
	}

	public int getUpValuesDebugDataStartByteOffset()
	{
		return this.upValuesDebugDataStartByteOffset;
	}

	public String[] getUpValueNames()
	{
		return this.upValueNames;
	}

	public CharList disAssemble()
	{
		final CharList result = new CharList();

		result.add("functionName: ");
		result.add(this.functionName);
		result.addNewLine();

		result.add("firstLineDefinedInSource: ");
		result.add(Integer.toHexString(this.lineDefined));
		result.addNewLine();

		result.add("lastLineDefinedInSource: ");
		result.add(Integer.toHexString(this.lastLineDefined));
		result.addNewLine();

		result.add("numUpvalues: ");
		result.add(Integer.toString(this.numUpvalues));
		result.addNewLine();

		result.add("numParameters: ");
		result.add(Integer.toString(this.numParameters));
		result.addNewLine();

		result.add("VarArg flag: ");
		result.add(Integer.toHexString(this.isVarArgFlag));
		result.addNewLine();

		result.add("maxStackSize: ");
		result.add(Integer.toString(this.maxStackSize));
		result.addNewLine();

		result.add("***instructions start***");
		result.addNewLine();

		int pos = this.instructionStartByteOffset;

		for (int i = 0; i < this.instructions.length; i++)
		{
			MiscAutobot.prepend(Integer.toHexString(pos), result, '0', minOffset_ByteAddress);
			result.add('\t');

			final Instruction cur = this.instructions[i];
			final CharList disAssemble = cur.disAssemble();
			final CharList natLang = this.instructionToNaturalLanguage(cur);

			result.add(disAssemble, true);
			final int dif = minOffset_TerseByteCode - disAssemble.charSize();

			if (dif > 0)
			{
				result.add(' ', dif);
			}

			result.add("\t|||\t");
			result.add(natLang, true);

			/*
			if (natLang.charSize() == 0)
			{
				result.add(cur.disAssemble(), true);
			}
			else
			{
				result.add(natLang, true);
			}
			*/

			pos += 4;
			result.addNewLine();
		}

		result.add("***instructions end***");
		result.addNewLine();

		result.add("***constants start***");
		result.addNewLine();

		for (int i = 0; i < this.constants.length; i++)
		{
			MiscAutobot.prepend(Integer.toHexString(pos), result, '0', minOffset_ByteAddress);
			result.add('\t');
			result.add(this.constants[i].disAssemble(), true);
			pos += 4;
			result.addNewLine();
		}

		result.add("***constants end***");
		result.addNewLine();

		for (Function curFunction : this.functionPrototypes)
		{
			result.add(curFunction.disAssemble(), true);
			result.addNewLine();
		}

		return result;
	}

	public CharList disAssemble(int offset)
	{
		final CharList result = new CharList();
		result.add('\t', offset);

		result.add("functionName: ");
		result.add(this.functionName);
		result.addNewLine();
		result.add('\t', offset);

		result.add("firstLineDefinedInSource: ");
		result.add(Integer.toHexString(this.lineDefined));
		result.addNewLine();
		result.add('\t', offset);

		result.add("lastLineDefinedInSource: ");
		result.add(Integer.toHexString(this.lastLineDefined));
		result.addNewLine();
		result.add('\t', offset);

		result.add("numUpvalues: ");
		result.add(Integer.toString(this.numUpvalues));
		result.addNewLine();
		result.add('\t', offset);

		result.add("numParameters: ");
		result.add(Integer.toString(this.numParameters));
		result.addNewLine();
		result.add('\t', offset);

		result.add("VarArg flag: ");
		result.add(Integer.toHexString(this.isVarArgFlag));
		result.addNewLine();
		result.add('\t', offset);

		result.add("maxStackSize: ");
		result.add(Integer.toString(this.maxStackSize));
		result.addNewLine();
		result.add('\t', offset);

		result.add("***instructions start***");
		result.addNewLine();
		result.add('\t', offset);

		int pos = this.instructionStartByteOffset;

		for (int i = 0; i < this.instructions.length; i++)
		{
			MiscAutobot.prepend(Integer.toHexString(pos), result, '0', minOffset_ByteAddress);
			result.add('\t');

			final Instruction cur = this.instructions[i];
			final CharList disAssemble = cur.disAssemble();
			final CharList natLang = this.instructionToNaturalLanguage(cur);

			result.add(cur.disAssemble(), true);
			final int dif = minOffset_TerseByteCode - disAssemble.charSize();

			if (dif > 0)
			{
				result.add(' ', dif);
			}

			result.add("\t|||\t");
			result.add(natLang, true);

			/*
			if (natLang.charSize() == 0)
			{
				result.add(cur.disAssemble(), true);
			}
			else
			{
				result.add(natLang, true);
			}
			*/

			pos += 4;
			result.addNewLine();
			result.add('\t', offset);
		}

		result.add("***instructions end***");
		result.addNewLine();
		result.add('\t', offset);

		result.add("***constants start***");
		result.addNewLine();
		result.add('\t', offset);

		for (int i = 0; i < this.constants.length; i++)
		{
			MiscAutobot.prepend(Integer.toHexString(pos), result, '0', minOffset_ByteAddress);
			result.add('\t');
			result.add(this.constants[i].disAssemble(), true);
			pos += 4;
			result.addNewLine();
			result.add('\t', offset);
		}

		result.add("***constants end***");
		result.addNewLine();

		for (Function curFunction : this.functionPrototypes)
		{
			result.add(curFunction.disAssemble(offset + 1), true);
			result.addNewLine();
		}

		return result;
	}

	private CharList instructionToNaturalLanguage(Instruction in)
	{
		final CharList result;

		switch (in.getType())
		{
			case iABC:
			{
				result = this.instructionToNaturalLanguage_iABC((Instruction_iABC) in);
				break;
			}
			case iABx:
			{
				result = this.instructionToNaturalLanguage_iABx((Instruction_iABx) in);
				break;
			}
			case iAsBx:
			{
				result = this.instructionToNaturalLanguage_iAsBx((Instruction_iAsBx) in);
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getType());
			}
		}

		return result;
	}

	private CharList instructionToNaturalLanguage_iABC(Instruction_iABC in)
	{
		// TODO
		final CharList result = new CharList();

		switch (in.getOpCode())
		{
			case Move:
			{
				final String Name_regA = this.localsDebugData[in.getA()].getName();
				final String Name_regB = this.localsDebugData[in.getB()].getName();

				result.add(Name_regA);
				result.add(" = ");
				result.add(Name_regB);
				result.add(';');

				break;
			}
			case LoadNull:
			{
				final int regA = in.getA();
				final int regB = in.getB();

				result.add(this.localsDebugData[regA].getName());

				for (int i = regA + 1; i < regB + 1; i++)
				{
					result.add(", ");
					result.add(this.localsDebugData[i].getName());
				}

				result.add(" = null;");

				break;
			}
			case LoadBool:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(Boolean.toString(in.getB() != 0));
				result.add(';');

				if (in.getC() != 0)
				{
					result.add(" GOTO addr: ");
					result.add(Integer.toHexString(in.getInstructionAddress() + 2));
				}
				result.add(';');

				break;
			}
			case GetUpvalue:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.upValueNames[in.getB()]);
				result.add(';');

				break;
			}
			case SetUpvalue:
			{
				result.add(this.upValueNames[in.getB()]);
				result.add(" = ");
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(';');

				break;
			}
			case GetTable:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.localsDebugData[in.getB()].getName());
				result.add('[');
				result.add(this.RK_toString(in.getC()));
				result.add("];");

				break;
			}
			case SetTable:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add('[');
				result.add(this.RK_toString(in.getB()));
				result.add("] = ");
				result.add(this.RK_toString(in.getC()));

				break;
			}
			case Add:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" + ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case Subtract:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" - ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case Multiply:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" * ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case Divide:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" / ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case Modulo:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" % ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case Power:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(this.RK_toString(in.getB()));
				result.add(" ^ ");
				result.add(this.RK_toString(in.getC()));
				result.add(';');

				break;
			}
			case ArithmeticNegation:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = -");
				result.add(this.localsDebugData[in.getB()].getName());
				result.add(';');
				break;
			}
			case LogicalNegation:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = !");
				result.add(this.localsDebugData[in.getB()].getName());
				result.add(';');
				break;
			}
			case Length:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(GlobalMethods_lengthOf);
				result.add(this.localsDebugData[in.getB()].getName());
				result.add(");");
				break;
			}
			case Concatenation:
			{// TODO: test this
				final int start = in.getB();
				final int end = in.getC();

				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(GlobalMethods_Concatenate);
				result.add(this.localsDebugData[start].getName());

				for (int i = start + 1; i < end; i++)
				{
					result.add(", ");
					result.add(this.localsDebugData[i].getName());
				}

				result.add(");");

				break;
			}
			case Call:
			{
				/*
				 * how this unfolds depends upon whether the previous and next lines are also Calls.
				 */
				break;
			}
			case Return:
			{
				result.add("returning to previous function");
				final int B = in.getB();

				switch (B)
				{
					case 0:
					{
						final int A = in.getA();
						result.add(", whilst carrying ");

						for (int i = A; i < this.localsDebugData.length; i++)
						{
							if (i != A)
							{
								result.add(", ");
							}

							result.add(this.localsDebugData[i].getName());
						}

						result.add(';');

						break;
					}
					case 1:
					{
						result.add(';');
						break;
					}
					default:
					{
						final int A = in.getA();
						final int last = A + B - 1;
						result.add(", whilst carrying ");

						for (int i = A; i < last; i++)
						{
							if (i != A)
							{
								result.add(", ");
							}

							result.add(this.localsDebugData[i].getName());
						}

						result.add(';');

						break;
					}
				}

				break;
			}
			case TailCall:
			{
				// holding off on this and CALL until clarification is obtained

				break;
			}
			case VarArg:
			{
				break;
			}
			case Self:
			{
				// 0307
				final int A = in.getA();
				result.add(this.localsDebugData[A + 1].getName());
				result.add(" = ");
				final String nameB = this.localsDebugData[in.getB()].getName();
				result.add(nameB);
				result.add("; ");
				result.add(this.localsDebugData[A].getName());
				result.add(" = ");
				result.add(nameB);
				result.add('[');
				result.add(this.RK_toString(in.getC()));
				result.add("];");

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
			case TestLogicalComparison:
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
			case TestAndSaveLogicalComparison:
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
			{

				break;
			}
			case Close:
				break;
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private CharList instructionToNaturalLanguage_iABx(Instruction_iABx in)
	{
		// TODO
		final CharList result = new CharList();

		switch (in.getOpCode())
		{
			case LoadK:
			{
				final String regA = this.localsDebugData[in.getA()].getName();
				final String ConstBx = this.constants[in.getBx()].getValAsString();

				result.add(regA);
				result.add(" = ");
				result.add(ConstBx);

				break;
			}
			case GetGlobal:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" = ");
				result.add(GlobalVarsPrefix);
				result.add(this.constants[in.getBx()].getValAsString());
				result.add(';');

				break;
			}
			case SetGlobal:
			{
				result.add(GlobalVarsPrefix);
				result.add(this.constants[in.getBx()].getValAsString());
				result.add(" = ");
				result.add(this.localsDebugData[in.getA()].getName());

				break;
			}
			case Closure:
				break;
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private CharList instructionToNaturalLanguage_iAsBx(Instruction_iAsBx in)
	{
		// TODO
		final CharList result = new CharList();

		switch (in.getOpCode())
		{
			case Jump:
			{
				result.add(programCounter);
				result.add(" += ");
				result.add(Integer.toString(in.get_sBx()));
				break;
			}
			case InitNumericForLoop:
			{
				result.add(this.localsDebugData[in.getA()].getName());
				result.add(" -= ");
				result.add(this.localsDebugData[in.getA() + 2].getName());
				result.add("; ");
				result.add(programCounter);
				result.add(" += ");
				result.add(Integer.toString(in.get_sBx()));
				result.add(';');
				break;
			}
			case IterateNumericForLoop:
			{
				final int A = in.getA();
				final String nameA = this.localsDebugData[A].getName();
				result.add(nameA);
				result.add(" += ");
				result.add(this.localsDebugData[A + 2].getName());
				result.add("; if (");
				result.add(nameA);
				result.add(" ('<' if step is positive, '<=' otherwise) ");
				result.add(this.localsDebugData[A + 1].getName());
				result.add(") then { ");
				result.add(programCounter);
				result.add(" += ");
				result.add(Integer.toString(in.get_sBx()));
				result.add("; ");
				result.add(this.localsDebugData[A + 3].getName());
				result.add(" = ");
				result.add(nameA);
				result.add(';');

				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private final String RK_toString(int C)
	{
		final int compare = C & 0x100;
		// System.out.println("comparing: " + Integer.toBinaryString(C) + " and " + Integer.toBinaryString(0x80) + ", and got " + Integer.toBinaryString(compare));
		if (compare == 0x100)
		{
			final int index = C & ~0x100;
			final Constant curr = this.constants[index];

			if (curr.getType() == ConstantTypes.String)
			{
				return GlobalVarsPrefix + curr.getValAsString();
			}
			else
			{
				return curr.getValAsString();
			}
		}
		else
		{
			return this.localsDebugData[C].getName();
		}
	}

	public static class Local
	{
		private static int nextLocalVarNum = 0;

		private final String name;
		private final int scopeStart;
		private final int scopeEnd;

		public Local(String inName, int inScopeStart, int inScopeEnd)
		{
			this.name = "LocalVar_" + inName;
			this.scopeStart = inScopeStart;
			this.scopeEnd = inScopeEnd;
		}

		public static Local[] parseNextLocalArr(BytesStreamer in, int size_tLength)
		{
			//System.out.println("Starting parse at position: " + Integer.toHexString(in.getPosition()));
			final int length = in.getNextInt(in.getDefaultInputEndianness());
			// System.out.println("localsLength: " + length + " at location before: " + Integer.toHexString(in.getPosition()));
			final Local[] result = new Local[length];

			if (length != 0)
			{
				for (int i = 0; i < length; i++)
				{
					final String inName = MiscAutobot.parseNextLuaString(in, size_tLength, EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
					final int scopeStart = in.getNextInt();
					final int scopeEnd = in.getNextInt();

					result[i] = new Local(inName, scopeStart, scopeEnd);
				}
			}

			return result;
		}

		public static Local[] getDummyLocalsArr(int maxStackSize)
		{
			final Local[] result;
			
			if(maxStackSize < 0)
			{
				result = new Local[0];
			}
			else
			{
				result = new Local[maxStackSize];
			}

			for (int i = 0; i < maxStackSize; i++)
			{
				result[i] = new Local(Integer.toString(nextLocalVarNum++), -1, -1);
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

	private static class FunctionsIterator implements Iterator<Function>
	{
		private final SingleLinkedList<Function> queue;

		public FunctionsIterator(Function start)
		{
			this.queue = new SingleLinkedList<Function>(start);
		}

		@Override
		public boolean hasNext()
		{
			return !this.queue.isEmpty();
		}

		@Override
		public Function next()
		{
			final Function result = this.queue.pop();
			this.queue.add(result.functionPrototypes);
			return result;
		}

	}
}
