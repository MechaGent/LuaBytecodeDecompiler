package CleanStart.Mk01;

import CharList.CharList;
import CleanStart.Mk01.BaseVariable.Var_Null;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.OpCodes;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Instruction;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.Operators;
import CleanStart.Mk01.Variables.Var_Bool;
import CleanStart.Mk01.Variables.Var_Float;
import CleanStart.Mk01.Variables.Var_Int;
import CleanStart.Mk01.Variables.Var_Ref;
import CleanStart.Mk01.Variables.Var_String;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class FunctionObject implements Variable
{
	private static final int RK_Mask = 0x100;
	private static final int FieldsPerFlush = 50;

	private static int instanceCounter_constant = 0;
	private static int instanceCounter_upvalue = 0;

	private final String name;
	// private final int numUpvalues;
	private final int numParams;
	private final VarArgFlagSet VarArgFlags;
	private final int numRegisters_max; // maxStackSize
	private final int[] rawInstructions;
	private final Instruction[] instructions;
	private final Variable[] Constants;
	private final FunctionObject[] functionPrototypes;
	private final int[] sourceLinePositions;
	private final Var_Ref[] Locals;
	private final Var_Ref[] Upvalues;

	public FunctionObject(String inName, int inNumParams, VarArgFlagSet inVarArgFlags, int inNumRegisters_max, int[] inRawInstructions, Instruction[] inInstructions, Variable[] inConstants, FunctionObject[] inFunctionPrototypes, int[] inSourceLinePositions, Var_Ref[] inLocals, Var_Ref[] inUpvalues)
	{
		this.name = inName;
		this.numParams = inNumParams;
		this.VarArgFlags = inVarArgFlags;
		this.numRegisters_max = inNumRegisters_max;
		this.rawInstructions = inRawInstructions;
		this.instructions = inInstructions;
		this.Constants = inConstants;
		this.functionPrototypes = inFunctionPrototypes;
		this.sourceLinePositions = inSourceLinePositions;
		this.Locals = inLocals;
		this.Upvalues = inUpvalues;
	}

	/**
	 * assumes that nothing has been read from the stream yet
	 * 
	 * @param in
	 * @return
	 */
	public static FunctionObject getNext(BytesStreamer in)
	{
		final ChunkHeader header = ChunkHeader.getNext(in);
		return getNext(in, header);
	}

	public static FunctionObject getNext(BytesStreamer in, ChunkHeader header)
	{
		final String inName;
		final int inNumParams;
		final VarArgFlagSet inVarArgFlags;
		final int inNumRegisters_max;
		final Instruction[] inInstructions;
		final Variable[] inConstants;
		final FunctionObject[] inFunctionPrototypes;
		final int[] inSourceLinePositions;
		final Var_Ref[] inLocals;
		final Var_Ref[] inUpvalues;

		final int size_t_byteLength = header.getSize_t();
		final int LuaNumLengthInBytes = header.getSize_luaNum();
		final boolean numIsFloat = header.LuaNumsAreFloats();
		final StepStage start = StepStage.getNullStepStage();

		inName = generateName(in, size_t_byteLength);
		in.getNextInt(); // firstLineInSource
		in.getNextInt(); // lastLineInSource
		final int numUpvalues = in.getNextByte();
		inNumParams = in.getNextByte();
		inVarArgFlags = new VarArgFlagSet(in.getNextByte());
		inNumRegisters_max = in.getNextByte();
		final int numInstructions = in.getNextInt();
		final int[] locRawInstructions = in.getNextIntArr(numInstructions);

		inConstants = parseNextConstantsArr(in, start, LuaNumLengthInBytes, numIsFloat, size_t_byteLength);
		inFunctionPrototypes = parseNextFunctionObjectArr(in, header);

		final int numSourceLinePositions = in.getNextInt();
		inSourceLinePositions = in.getNextIntArr(numSourceLinePositions);
		inLocals = getNextLocalsArr(in, inNumRegisters_max, size_t_byteLength, start);
		inUpvalues = getNextUpvaluesArr(in, numUpvalues, size_t_byteLength, start);
		final InstructionTranslator rawsy = new InstructionTranslator(inConstants, inFunctionPrototypes, inLocals, inUpvalues);
		inInstructions = rawsy.parseNextInstructionArray(locRawInstructions);

		final FunctionObject result = new FunctionObject(inName, inNumParams, inVarArgFlags, inNumRegisters_max, locRawInstructions, inInstructions, inConstants, inFunctionPrototypes, inSourceLinePositions, inLocals, inUpvalues);

		return result;
	}

	private static String generateName(BytesStreamer in, int size_t_byteLength)
	{
		return MiscAutobot.parseNextLuaString(in, size_t_byteLength) + "FUNCTION_" + instanceCounter_constant++;
	}

	private static Variable[] parseNextConstantsArr(BytesStreamer in, StepStage start, int LuaNumLengthInBytes, boolean numIsFloat, int size_t_byteLength)
	{
		final int numConstants = in.getNextInt();
		final Variable[] result = new Variable[numConstants];

		for (int i = 0; i < numConstants; i++)
		{
			switch (in.getNextByte())
			{
				case 0: // Nil
				{
					result[i] = new Var_Null(start);
					break;
				}
				case 1: // boolean
				{
					result[i] = Var_Bool.getInstance(in.getNextByte() != 0);
					break;
				}
				case 3: // Num
				{
					switch (LuaNumLengthInBytes)
					{
						case 4:
						{
							if (numIsFloat)
							{
								result[i] = new Var_Float("Constant_" + instanceCounter_constant++ + "_Float", start, in.getNextFloat());
							}
							else
							{
								result[i] = new Var_Int("Constant_" + instanceCounter_constant++ + "_Int", start, in.getNextInt());
							}
							break;
						}
						case 8:
						{
							if (numIsFloat)
							{
								throw new IllegalArgumentException("you didn't finish this, dumbass");
							}
							else
							{
								throw new IllegalArgumentException("you didn't finish this, dumbass");
							}
							// break;
						}
						default:
						{
							throw new IllegalArgumentException("lua num length is " + LuaNumLengthInBytes + ", apparently.");
						}
					}

					break;
				}
				case 4: // String
				{
					result[i] = new Var_String("Constant_" + instanceCounter_constant++ + "_String", start, MiscAutobot.parseNextLuaString(in, size_t_byteLength), true);
					break;
				}
				default:
				{
					throw new IllegalArgumentException();
				}
			}
		}

		return result;
	}

	private static FunctionObject[] parseNextFunctionObjectArr(BytesStreamer in, ChunkHeader header)
	{
		final int length = in.getNextInt();
		final FunctionObject[] result = new FunctionObject[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = FunctionObject.getNext(in, header);
		}

		return result;
	}

	private static Var_Ref[] getNextLocalsArr(BytesStreamer in, int maxStackSize, int size_t_byteLength, StepStage start)
	{
		final int length = in.getNextInt();
		final Var_Ref[] result;

		if (length == maxStackSize)
		{
			result = new Var_Ref[length];
		}
		else
		{
			result = new Var_Ref[maxStackSize];
		}

		int i = 0;

		for (; i < length; i++)
		{
			final String name = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
			in.getNextInt(); // start of local variable scope
			in.getNextInt(); // end of local variable scope

			System.out.println("new local var created, with name: " + name);

			result[i] = new Var_Ref(name, start, new Var_Null(start));
		}

		for (; i < result.length; i++)
		{
			result[i] = new Var_Ref("anonLocalVar_" + i, start, new Var_Null(start));
		}

		return result;
	}

	private static Var_Ref[] getNextUpvaluesArr(BytesStreamer in, int quantityIfPresent, int size_t_byteLength, StepStage start)
	{
		final int localLength = in.getNextInt();
		final Var_Ref[] result;
		// final StepStage start = StepStage.getNullStepStage();

		if (localLength == 0 && quantityIfPresent != localLength)
		{
			result = BaseVariable.getRefArray_NullInit(quantityIfPresent, "Upvalue_", instanceCounter_upvalue++, start);
		}
		else if (localLength != quantityIfPresent)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			result = new Var_Ref[localLength];

			for (int i = 0; i < localLength; i++)
			{
				final String upName = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
				result[i] = new Var_Ref(upName, start, new Var_Null(start));
			}
		}

		return result;
	}

	public String getName()
	{
		return this.name;
	}

	public int getNumParams()
	{
		return this.numParams;
	}

	public VarArgFlagSet getVarArgFlags()
	{
		return this.VarArgFlags;
	}

	public int getNumRegisters_max()
	{
		return this.numRegisters_max;
	}

	public Instruction[] getInstructions()
	{
		return this.instructions;
	}

	public Variable[] getConstants()
	{
		return this.Constants;
	}

	public FunctionObject[] getFunctionPrototypes()
	{
		return this.functionPrototypes;
	}

	public int[] getSourceLinePositions()
	{
		return this.sourceLinePositions;
	}

	public Var_Ref[] getLocals()
	{
		return this.Locals;
	}

	public String rawInstructionsToDisassembly_String(int offset)
	{
		return this.rawInstructionsToDisassembly_CharList(offset).toString();
	}

	public CharList rawInstructionsToDisassembly_CharList(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("Function: ");
		result.add(this.name);

		for (int i = 0; i < this.rawInstructions.length; i++)
		{
			result.addNewLine();
			result.add('\t', offset + 1);
			// result.add(Integer.toBinaryString(this.rawInstructions[i]));
			// result.add(": ");
			result.add(MiscAutobot.rawInstructionToCharList(this.rawInstructions[i]), true);
		}

		for (FunctionObject subFunc : this.functionPrototypes)
		{
			result.addNewLine();
			result.add(subFunc.rawInstructionsToDisassembly_CharList(offset + 1), true);
		}

		return result;
	}

	public String instructionsToString(boolean shouldCascade, int offset)
	{
		return this.instructionsToCharList(shouldCascade, offset).toString();
	}

	public CharList instructionsToCharList(boolean shouldCascade, int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("Function: ");
		result.add(this.name);
		//System.out.println(this.name);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numChildFunctions: ");
		result.addAsString(this.functionPrototypes.length);

		for (int i = 0; i < this.instructions.length; i++)
		{
			result.addNewLine();
			
			if(this.instructions[i].isExpired())
			{
				result.add("// ");
			}
			
			result.addAsString(i);
			result.add('\t');
			//System.out.println("attempting: " + i + ", " + this.instructions[i].toString() + ", with next being: " + (i+1<this.instructions.length? this.instructions[i+1].toString(): ""));
			
			this.instructions[i].translateIntoCommentedByteCode(offset + 1, result);
		}

		if (shouldCascade)
		{
			for (FunctionObject subFunc : this.functionPrototypes)
			{
				//System.out.println("here");
				result.addNewLine();
				result.add(subFunc.instructionsToCharList(shouldCascade, offset + 1), true);
			}
		}
		
		//System.out.println("finishing " + this.name);

		return result;
	}

	public Var_Ref[] getUpvalues()
	{
		return this.Upvalues;
	}

	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Override
	public boolean isOperand()
	{
		return true;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public int getPrecedence()
	{
		return -1;
	}

	@Override
	public String valueToString()
	{
		return this.name;
	}

	@Override
	public String valueToStringAtTime(StepStage inTime)
	{
		return this.name;
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.valueToStringAtTime(inTime);
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return this;
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.FunctionPrototype;
	}

	@Override
	public String getMangledName()
	{
		return this.name;
	}

	@Override
	public Comparisons compareWith(Variable inIn, StepStage inTime)
	{
		if (this == inIn)
		{
			return Comparisons.SameType_SameValue;
		}
		else
		{
			if (inIn.getType() == VariableTypes.FunctionPrototype)
			{
				return Comparisons.SameType_DifferentValue;
			}
			else
			{
				return Comparisons.DifferentType;
			}
		}
	}

	public CharList translateRawsToBytecode(int offset)
	{
		return this.translateRawsToBytecode(offset, true);
	}

	private CharList translateRawsToBytecode(int offset, boolean shouldCascade)
	{
		final CharList result = new CharList();

		result.add('\t', offset++);
		result.add("Function:");
		result.add(this.name);
		result.addNewLine();
		result.add("Constants: {");
		
		for(int i = 0; i < this.Constants.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(this.Constants[i].valueToString());
		}
		
		result.add('}');
		result.addNewLine();
		result.add("Child Functions: {");
		
		for(int i = 0; i < this.functionPrototypes.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(this.functionPrototypes[i].name);
		}
		
		result.add('}');
		result.addNewLine();

		for (int i = 0; i < this.rawInstructions.length;)
		{
			result.addNewLine();

			final String lineString = Integer.toString(i);
			final int lineDif = 4 - lineString.length();

			result.add(lineString);
			result.add(' ', lineDif);

			result.add('\t', offset);

			final OpCodes type = OpCodes.parse(this.rawInstructions[i]);
			final String typeString = type.toString();
			result.add(typeString);

			final int dif = 30 - typeString.length();

			result.add(' ', dif);
			result.add('\t');

			i += this.translateRawToBytecode(this.rawInstructions, i, type, offset, result);
		}

		if (shouldCascade)
		{
			for (FunctionObject function : this.functionPrototypes)
			{
				result.addNewLine();
				result.add(function.translateRawsToBytecode(offset + 1, shouldCascade), true);
			}
		}

		return result;
	}

	private void addRkVarIndex(int index, CharList result)
	{
		final int compare = index & RK_Mask;

		if (compare == RK_Mask)
		{
			result.add("(<constant> ");
			final int masked = index & ~RK_Mask;

			if (!(masked < this.Constants.length))
			{
				throw new IllegalArgumentException("index: " + index + ", masked: " + masked);
			}

			result.add(this.Constants[masked].valueToString());
			result.add(')');
		}
		else
		{
			result.add("Locals[");
			result.addAsString(index);
			result.add(']');
		}
	}

	private int translateRawToBytecode(int[] lines, int lineNum, OpCodes type, int offset, CharList result)
	{
		final int line = lines[lineNum];
		int numLinesConsumed = 1;

		switch (type)
		{
			case Add:
			case Subtract:
			case Multiply:
			case Divide:
			case Modulo:
			case Power:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				final Operators operator = Operators.parse(type);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				this.addRkVarIndex(fields[1], result);
				result.add(operator.valueToString());
				this.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case ArithmeticNegation:
			case LogicalNegation:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);
				final Operators operator = Operators.parse(type);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				result.add(operator.valueToString());
				this.addRkVarIndex(fields[1], result);
				result.add(')');
				break;
			}
			case Call:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("function call (<function> Locals[");

				result.addAsString(fields[0]);
				result.add("])(");

				if (fields[1] == 0)
				{
					result.add("<from Locals[");
					result.addAsString(fields[0] + 1);
					result.add("] to topOfStack (varArgs)>");
				}
				else
				{
					final int numArgs = fields[1] - 1;

					if (numArgs > 0)
					{
						result.add("Locals[");
						result.addAsString(fields[0] + 1);
						result.add(']');
					}

					if (numArgs > 1)
					{
						result.add(" to Locals[");
						result.addAsString(fields[0] + numArgs);
						result.add(']');
					}
				}

				result.add(") returns ");

				final int numReturns = fields[2] - 1;

				if (numReturns == -1)
				{
					result.add("<from Locals[");
					result.addAsString(fields[0]);
					result.add("] to topOfStack (varArgs)>");
				}
				else if (numReturns > 0)
				{
					result.add("Locals[");
					result.addAsString(fields[0]);
					result.add(']');

					if (numReturns > 1)
					{
						result.add(" to Locals[");
						result.addAsString(fields[0] + numReturns - 1);
						result.add(']');
					}
				}

				break;
			}
			case Close:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 1);

				result.add("Close all variables, from Locals[");
				result.addAsString(fields[0]);
				result.add("], to the top of the stack, inclusive");

				break;
			}
			case Closure:
			{
				final int[] fields = MiscAutobot.parseAs_iABx(line, 2);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned a closure, created from the function '");
				final FunctionObject function = this.functionPrototypes[fields[1]];
				result.add(function.getName());
				result.add("', with ");
				final int numUpvalues = function.getUpvalues().length;
				result.addAsString(numUpvalues);
				result.add(" upvalues {");

				if (numUpvalues > 0)
				{
					for (; numLinesConsumed < numUpvalues; numLinesConsumed++)
					{
						if (numLinesConsumed != 1)
						{
							result.add(", ");
						}

						final int pseudo = lines[lineNum + numLinesConsumed];
						final int[] pseudoFields = MiscAutobot.parseAs_iABC(pseudo, 2);
						final OpCodes pseudoType = OpCodes.parse(pseudo);

						switch (pseudoType)
						{
							case Move:
							{
								result.add("Locals[");
								break;
							}
							case GetUpvalue:
							{
								result.add("Upvalues[");
								break;
							}
							default:
							{
								throw new IllegalArgumentException("bad enum: " + pseudoType.toString());
							}
						}

						result.addAsString(pseudoFields[1]);
						result.add(']');
					}
				}

				if (numUpvalues != numLinesConsumed && numLinesConsumed != 1)
				{
					throw new IllegalArgumentException("numUpvalues: " + numUpvalues + ", numLinesConsumed: " + numLinesConsumed);
				}

				result.add('}');

				break;
			}
			case Concatenation:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of the concatenated variables Locals[");
				result.addAsString(fields[1]);
				result.add("] to Locals[");
				result.addAsString(fields[2]);
				result.add("], inclusive");
				break;
			}
			case GetGlobal:
			{
				final int[] fields = MiscAutobot.parseAs_iABx(line, 2);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of the global variable '");
				result.add(this.Constants[fields[1]].valueToString());
				result.add('\'');

				break;
			}
			case GetTable:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("].getFromKey(");
				this.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case GetUpvalue:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Upvalues[");
				result.addAsString(fields[1]);
				result.add(']');
				break;
			}
			case InitNumericForLoop:
			{
				final int[] fields = MiscAutobot.parseAs_iAsBx(line, 2);

				result.add("Initialize forLoop - internalLoopVar/InitialValueOfSame=Locals[");
				result.addAsString(fields[0]);
				result.add("], limitVar=Locals[");
				result.addAsString(fields[0] + 1);
				result.add("], stepValueVar=Locals[");
				result.addAsString(fields[0] + 2);
				result.add("], externalLoopVar=Locals[");
				result.addAsString(fields[0] + 3);
				result.add("] - Loop jumps back after instruction#");
				result.addAsString(fields[1] + lineNum + 1);

				break;
			}
			case IterateGenericForLoop:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Iterate ForEach loop - iteratorFunction=Locals[");
				result.addAsString(fields[0]);
				result.add("], state=Locals[");
				result.addAsString(fields[0] + 1);
				result.add("], enumIndex=Locals[");
				result.addAsString(fields[0] + 2);
				result.add("] - if (enumIndex != NULL), then state is assigned the value of enumIndex. Otherwise, skip the next instruction");

				break;
			}
			case IterateNumericForLoop:
			{
				final int[] fields = MiscAutobot.parseAs_iAsBx(line, 2);
				result.add("Iterate forLoop - internalLoopVar=++Locals[");
				result.addAsString(fields[0]);
				result.add("], limitVar=Locals[");
				result.addAsString(fields[0] + 1);
				result.add("], stepValueVar=Locals[");
				result.addAsString(fields[0] + 2);
				result.add("], externalLoopVar=Locals[");
				result.addAsString(fields[0] + 3);
				result.add("] - if TRUE, externalLoopVar is assigned the value of internalLoopVar, and Loop jumps back to instruction#");
				result.addAsString(fields[1] + lineNum + 1);
				break;
			}
			case Jump:
			{
				final int[] fields = MiscAutobot.parseAs_iAsBx(line, 2);

				result.add("Jump to instruction #");
				result.addAsString(lineNum + fields[1] + 1);

				break;
			}
			case Length:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the length of Locals[");
				result.addAsString(fields[1]);
				result.add(']');

				break;
			}
			case LoadBool:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the hard-coded boolean value ");
				result.addAsString(fields[1] != 0);

				if (fields[2] != 0)
				{
					result.add(", and skips the next instruction");
				}

				break;
			}
			case LoadK:
			{
				final int[] fields = MiscAutobot.parseAs_iABx(line, 2);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the constant ");
				result.add(this.Constants[fields[1]].valueToString());
				break;
			}
			case LoadNull:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] to Locals[");
				result.addAsString(fields[1]);
				result.add("], inclusive, are assigned the value NULL");
				break;
			}
			case Move:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("])");

				break;
			}
			case NewTable:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned a newly-created Table, with arraySize of ");
				result.addAsString(fields[1]);
				result.add(" and hashSize of");
				result.addAsString(fields[2]);

				break;
			}
			case Return:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);
				result.add("return to calling function, with values {");

				if (fields[1] == 0)
				{
					result.add("<from Locals[");
					result.addAsString(fields[0]);
					result.add("] to topOfStack (varArgs)>");
				}
				else
				{
					final int numReturns = fields[1] - 1;

					if (numReturns > 0)
					{
						result.add("Locals[");
						result.addAsString(fields[0]);
						result.add(']');
					}

					if (numReturns > 1)
					{
						result.add(" to Locals[");
						result.addAsString(fields[0] + numReturns - 1);
						result.add(']');
					}
				}

				result.add('}');

				break;
			}
			case Self:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("Locals[");
				result.addAsString(fields[0] + 1);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("], and Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("].getFromKey(");
				this.addRkVarIndex(fields[2], result);
				result.add(')');
				break;
			}
			case SetGlobal:
			{
				final int[] fields = MiscAutobot.parseAs_iABx(line, 2);

				result.add("the global variable '");
				result.add(this.Constants[fields[1]].valueToString());
				result.add("' is assigned the value of Locals[");
				result.addAsString(fields[0]);
				result.add(']');

				break;
			}
			case SetList:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("(<Table> Locals[");
				result.addAsString(fields[0]);
				result.add("])'s elements, from indeces ");
				final int stepSizeThing = (fields[2] - 1) * FieldsPerFlush;
				result.addAsString(stepSizeThing + 1);
				result.add(" to ");
				result.addAsString(stepSizeThing + fields[1]);
				result.add(", are assigned the values of Locals[");
				result.addAsString(fields[0] + 1);
				result.add("] through Locals[");
				result.addAsString(fields[0] + fields[1]);
				result.add("], inclusive");

				result.add("NOTE: Indeces are unNormed");
				break;
			}
			case SetTable:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);
				result.add("(<Table> Locals[");
				result.addAsString(fields[0]);
				result.add("])'s key ");
				this.addRkVarIndex(fields[1], result);
				result.add(" is assigned the value ");
				this.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case SetUpvalue:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);
				result.add("Upvalues[");
				result.addAsString(fields[1]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[0]);
				result.add(']');
				break;
			}
			case TailCall:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);

				result.add("return the return values of function call (<function> ");
				result.addAsString(fields[0]);
				result.add("])(");

				if (fields[1] == 0)
				{
					result.add("<from Locals[");
					result.addAsString(fields[0] + 1);
					result.add("] to topOfStack (varArgs)>");
				}
				else
				{
					final int numArgs = fields[1] - 1;

					if (numArgs > 0)
					{
						result.add("Locals[");
						result.addAsString(fields[0] + 1);
						result.add(']');
					}

					if (numArgs > 1)
					{
						result.add(" to Locals[");
						result.addAsString(fields[0] + numArgs);
						result.add(']');
					}
				}

				result.add(")), namely {");

				result.add("<from Locals[");
				result.addAsString(fields[0]);
				result.add("] to topOfStack (varArgs)>");

				result.add('}');

				break;
			}
			case TestAndSaveLogicalComparison:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);

				result.add("if (Locals[");
				result.addAsString(fields[1]);
				result.add("] != ");
				result.addAsString(fields[2]);
				result.add("), then skip next instruction; otherwise, Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add(']');

				break;
			}
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);

				result.add("if (");
				this.addRkVarIndex(fields[1], result);
				result.add(InstructionTranslator.getRelationalOperator(fields[0], type).valueToString());
				this.addRkVarIndex(fields[2], result);
				result.add("), then skip next instruction");
				break;
			}
			case TestLogicalComparison:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 3);

				result.add("if (Locals[");
				result.addAsString(fields[0]);
				result.add("] != ");
				result.addAsString(fields[2]);
				result.add("), then skip next instruction");

				break;
			}
			case VarArg:
			{
				final int[] fields = MiscAutobot.parseAs_iABC(line, 2);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] to Locals[");
				result.addAsString(fields[1]);
				result.add("], inclusive, are assigned the values passed in by the VarArgs operator (...)");
				break;
			}
			default:
			{
				throw new UnhandledEnumException(type);
			}
		}

		return numLinesConsumed;
	}

	public static class VarArgFlagSet
	{
		private final boolean hasArg;
		private final boolean isVarArg;
		private final boolean needsArg;

		public VarArgFlagSet(int isVarArgsFlagByte)
		{
			this.hasArg = (isVarArgsFlagByte & 1) == 1;
			this.isVarArg = (isVarArgsFlagByte & 2) == 1;
			this.needsArg = (isVarArgsFlagByte & 4) == 1;
		}

		public boolean hasArg()
		{
			return this.hasArg;
		}

		public boolean isVarArg()
		{
			return this.isVarArg;
		}

		public boolean needsArg()
		{
			return this.needsArg;
		}
	}
}
