package CleanStart.Mk06.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk06.ChunkHeader;
import CleanStart.Mk06.MiscAutobot;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Var;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.OpCodes;
import CleanStart.Mk06.Instructions.Label;
import CleanStart.Mk06.MiscAutobot.ParseTypes;
import CleanStart.Mk06.SubVars.BoolVar;
import CleanStart.Mk06.SubVars.FloatVar;
import CleanStart.Mk06.SubVars.IntVar;
import CleanStart.Mk06.SubVars.NullVar;
import CleanStart.Mk06.SubVars.StringVar;
import CleanStart.Mk06.VarFormula.Operators;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

/**
 * Extracted these parsing and toString methods here, to try to cut down on the clutter
 * 
 * @author MechaGent
 *
 */
public class RawFunctionObjectHelper extends RawFunctionObject
{
	//to prevent instantiation
	private RawFunctionObjectHelper(RawFunctionObject inOld)
	{
		super(inOld);
	}

	private static int instanceCounter = 0;

	public static RawFunctionObject getNext(BytesStreamer in, ChunkHeader header, HalfByteRadixMap<VarRef> inGlobals)
	{
		final String inName;
		final int numUpvalues;
		final int inNumParams;
		final VarArgFlagSet inVarArgFlags;
		final int inNumRegisters_max;
		final int[] inInstructions;
		final VarRef[] inConstants;
		final RawFunctionObject[] inFunctionPrototypes;
		final int[] inSourceLinePositions;
		final VarRef[] Locals;
		final VarRef[] Upvalues;

		final int size_t_byteLength = header.getSize_t();
		final int LuaNumLengthInBytes = header.getSize_luaNum();
		final boolean numIsFloat = header.LuaNumsAreFloats();

		inName = generateName(in, size_t_byteLength);
		in.getNextInt(); // firstLineInSource
		in.getNextInt(); // lastLineInSource
		numUpvalues = in.getNextByte();
		inNumParams = in.getNextByte();
		inVarArgFlags = new VarArgFlagSet(in.getNextByte());
		inNumRegisters_max = in.getNextByte();
		final int numInstructions = in.getNextInt();
		inInstructions = in.getNextIntArr(numInstructions);

		inConstants = parseNextConstantsArr(in, LuaNumLengthInBytes, numIsFloat, size_t_byteLength);
		inFunctionPrototypes = parseNextFunctionObjectArr(in, header, inGlobals);

		final int numSourceLinePositions = in.getNextInt();
		inSourceLinePositions = in.getNextIntArr(numSourceLinePositions);

		Locals = getNextLocalsNamesArr(in, inNumRegisters_max, size_t_byteLength);
		Upvalues = getNextUpvaluesNamesArr(in, numUpvalues, size_t_byteLength);

		return new RawFunctionObject(inName, numUpvalues, inNumParams, inVarArgFlags, inNumRegisters_max, inSourceLinePositions, inConstants, Locals, Upvalues, inGlobals, inInstructions, inFunctionPrototypes);
	}

	private static String generateName(BytesStreamer in, int size_t_byteLength)
	{
		return MiscAutobot.parseNextLuaString(in, size_t_byteLength) + "FUNCTION_" + instanceCounter++;
	}

	private static VarRef[] parseNextConstantsArr(BytesStreamer in, int LuaNumLengthInBytes, boolean numIsFloat, int size_t_byteLength)
	{
		final int numConstants = in.getNextInt();
		final VarRef[] result = new VarRef[numConstants];

		for (int i = 0; i < numConstants; i++)
		{
			final Var core;
			final InferenceTypes expectedType;

			switch (in.getNextByte())
			{
				case 0: // Nil
				{
					core = NullVar.getInstance("Constants", i);
					expectedType = InferenceTypes.Null;
					break;
				}
				case 1: // boolean
				{
					core = BoolVar.getInstance("Constants", i, in.getNextByte() != 0);
					expectedType = InferenceTypes.Bool;
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
								core = FloatVar.getInstance("Constants", i, in.getNextFloat());
								expectedType = InferenceTypes.Float;
							}
							else
							{
								core = IntVar.getInstance("Constants", i, in.getNextInt());
								expectedType = InferenceTypes.Int;
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
					core = StringVar.getInstance("Constants", i, MiscAutobot.parseNextLuaString(in, size_t_byteLength)
					// .replace("\r", "\\r").replace("\n", "\\n")
					);
					expectedType = InferenceTypes.String;
					break;
				}
				default:
				{
					throw new IllegalArgumentException();
				}
			}

			//result[i] = VarRef.getInstance("Constants", i);
			result[i] = VarRef.getInstance_forWrapper(core, expectedType);
			result[i].setInitialValue(core, expectedType);
		}

		return result;
	}

	private static RawFunctionObject[] parseNextFunctionObjectArr(BytesStreamer in, ChunkHeader header, HalfByteRadixMap<VarRef> inGlobals)
	{
		final int length = in.getNextInt();
		final RawFunctionObject[] result = new RawFunctionObject[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = RawFunctionObjectHelper.getNext(in, header, inGlobals);
		}

		return result;
	}

	private static VarRef[] getNextLocalsNamesArr(BytesStreamer in, int maxStackSize, int size_t_byteLength)
	{
		final int length = in.getNextInt();
		final VarRef[] result;

		if (length == maxStackSize)
		{
			result = new VarRef[length];
		}
		else
		{
			result = new VarRef[maxStackSize];
		}

		int i = 0;

		for (; i < length; i++)
		{
			final String name = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
			in.getNextInt(); // start of local variable scope
			in.getNextInt(); // end of local variable scope

			// System.out.println("new local var created, with name: " + name);

			result[i] = VarRef.getInstance(name);
		}

		for (; i < result.length; i++)
		{
			result[i] = VarRef.getInstance("anonLocalVar", i);
		}

		return result;
	}

	private static VarRef[] getNextUpvaluesNamesArr(BytesStreamer in, int quantityIfPresent, int size_t_byteLength)
	{
		final int localLength = in.getNextInt();
		final VarRef[] result;
		// final StepStage start = StepStage.getNullStepStage();

		if (localLength == 0 && quantityIfPresent != localLength)
		{
			result = new VarRef[quantityIfPresent];

			for (int i = 0; i < quantityIfPresent; i++)
			{
				result[i] = VarRef.getInstance("anonUpvalueVar_", i);
			}
		}
		else if (localLength != quantityIfPresent)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			result = new VarRef[localLength];

			for (int i = 0; i < localLength; i++)
			{
				final String upName = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
				result[i] = VarRef.getInstance(upName, -1);
			}
		}

		return result;
	}

	public static final Operators getRelationalOperator(int varA, OpCodes code)
	{
		final Operators result;

		switch (code)
		{
			case TestIfEqual:
			{
				/*
				 * if((RK(B) == RK(C)) != A, then PC++;
				 * which is equivalent to:
				 * if(RK(B) == RK(C)), then PC++; //where A == false
				 * if(RK(B) != RK(C)), then PC++; //where A == true
				 */

				if (varA == 0) // A == false
				{
					result = Operators.ArithToBool_Equals;
				}
				else
				{
					result = Operators.ArithToBool_NotEquals;
				}

				break;
			}
			case TestIfLessThan:
			{
				/*
				 * if((RK(B) < RK(C)) != A, then PC++;
				 * which is equivalent to:
				 * if(RK(B) < RK(C)), then PC++; //where A == false
				 * if(RK(B) >= RK(C)), then PC++; //where A == true
				 * 
				 */

				if (varA == 0) // A == false
				{
					result = Operators.ArithToBool_LessThan;
				}
				else
				{
					result = Operators.ArithToBool_GreaterThanOrEquals;
				}

				break;
			}
			case TestIfLessThanOrEqual:
			{
				if (varA == 0) // A == false
				{
					result = Operators.ArithToBool_GreaterThan;
				}
				else
				{
					result = Operators.ArithToBool_LessThanOrEquals;
				}

				break;
			}
			default:
			{
				throw new UnhandledEnumException(code);
			}
		}

		return result;
	}

	public static Label getSkipNextStepLabel(HalfByteRadixMap<Label> labelQueue, int rawIndex, StepNum time)
	{
		return getLabelForJumpOffsetOf(labelQueue, 1, rawIndex, time);
	}

	public static Label getLabelForJumpOffsetOf(HalfByteRadixMap<Label> labelQueue, int offset, int rawIndex, StepNum time)
	{
		final int absOffset = offset + rawIndex + 1;
		Label result = labelQueue.get(absOffset);

		if (result == null)
		{
			result = Label.getInstance(time, "Label ");

			if (absOffset > 5000)
			{
				throw new IllegalArgumentException("weird magnitude: " + absOffset);
			}

			labelQueue.put(absOffset, result);
			// System.out.println("label '" + result.getLabelValue() + "' created, intended for line " + absOffset);
		}

		return result;

	}

	public static int translateRawToBytecode(RawFunctionObject subject, int lineNum, int offset, CharList result)
	{
		final int line = subject.rawInstructions[lineNum];
		final OpCodes type = OpCodes.parse(line);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
				final Operators operator = Operators.parse(type);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				subject.addRkVarIndex(fields[1], result);
				result.add(operator.getValueAsString());
				subject.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case ArithmeticNegation:
			case LogicalNegation:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);
				final Operators operator = Operators.parse(type);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				result.add(operator.getValueAsString());
				subject.addRkVarIndex(fields[1], result);
				result.add(')');
				break;
			}
			case Call:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iA);

				result.add("Close all variables, from Locals[");
				result.addAsString(fields[0]);
				result.add("], to the top of the stack, inclusive");

				break;
			}
			case Closure:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABx);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned a closure, created from the function '");
				final FunctionObject function = subject.rawFunctionPrototypes[fields[1]];
				result.add(function.getName());
				result.add("', with ");
				final int numUpvalues = function.numUpvalues;
				result.addAsString(numUpvalues);
				result.add(" upvalues {");

				if (numUpvalues > 0)
				{
					for (; numLinesConsumed < numUpvalues + 1; numLinesConsumed++)
					{
						if (numLinesConsumed != 1)
						{
							result.add(", ");
						}

						final int pseudo = subject.rawInstructions[lineNum + numLinesConsumed];
						final int[] pseudoFields = MiscAutobot.parseRawInstruction(pseudo, ParseTypes.iAB);
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

				if (numUpvalues + 1 != numLinesConsumed && numLinesConsumed != 1)
				{
					throw new IllegalArgumentException("numUpvalues: " + numUpvalues + ", numLinesConsumed: " + numLinesConsumed);
				}

				result.add('}');

				break;
			}
			case Concatenation:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABx);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of the global variable '");
				final String globalVarName = subject.Constants[fields[1]].getNonInlinedValueAtTime(StepNum.getFirstStep()).getValueAsString();
				subject.globals.put(globalVarName, null); // dummy instruction to force Vars to log this as a global var name
				result.add(globalVarName);
				result.add('\'');

				break;
			}
			case GetTable:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("].getFromKey(");
				subject.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case GetUpvalue:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Upvalues[");
				result.addAsString(fields[1]);
				result.add(']');
				break;
			}
			case InitNumericForLoop:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAsBx);

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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAsBx);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAsBx);

				result.add("Jump to instruction #");
				result.addAsString(lineNum + fields[1] + 1);

				break;
			}
			case Length:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the length of Locals[");
				result.addAsString(fields[1]);
				result.add(']');

				break;
			}
			case LoadBool:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABx);
				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the constant ");
				result.add(subject.Constants[fields[1]].getNonInlinedValueAtTime(StepNum.getFirstStep()).getValueAsString());
				break;
			}
			case LoadNull:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] to Locals[");
				result.addAsString(fields[1]);
				result.add("], inclusive, are assigned the value NULL");
				break;
			}
			case Move:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);

				result.add("Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of (");
				result.add("Locals[");
				result.addAsString(fields[1]);
				result.add("])");

				break;
			}
			case NewTable:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
				result.add("Locals[");
				result.addAsString(fields[0] + 1);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("], and Locals[");
				result.addAsString(fields[0]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[1]);
				result.add("].getFromKey(");
				subject.addRkVarIndex(fields[2], result);
				result.add(')');
				break;
			}
			case SetGlobal:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABx);

				result.add("the global variable '");
				final String globalVarName = subject.Constants[fields[1]].getNonInlinedValueAtTime(StepNum.getFirstStep()).getValueAsString();
				subject.globals.put(globalVarName, null); // dummy instruction to force Vars to log this as a global var name
				result.add(globalVarName);
				result.add("' is assigned the value of Locals[");
				result.addAsString(fields[0]);
				result.add(']');

				break;
			}
			case SetList:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);
				result.add("(<Table> Locals[");
				result.addAsString(fields[0]);
				result.add("])'s key ");
				subject.addRkVarIndex(fields[1], result);
				result.add(" is assigned the value ");
				subject.addRkVarIndex(fields[2], result);
				result.add(')');

				break;
			}
			case SetUpvalue:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);
				result.add("Upvalues[");
				result.addAsString(fields[1]);
				result.add("] is assigned the value of Locals[");
				result.addAsString(fields[0]);
				result.add(']');
				break;
			}
			case TailCall:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);

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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);

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
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);

				result.add("if (");
				subject.addRkVarIndex(fields[1], result);
				result.add(getRelationalOperator(fields[0], type).getValueAsString());
				subject.addRkVarIndex(fields[2], result);
				result.add("), then skip next instruction");
				break;
			}
			case TestLogicalComparison:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABC);

				result.add("if (Locals[");
				result.addAsString(fields[0]);
				result.add("] != ");
				result.addAsString(fields[2]);
				result.add("), then skip next instruction");

				break;
			}
			case VarArg:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iAB);

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
}
