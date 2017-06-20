package CleanStart.Mk04_Working.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk04_Working.ChunkHeader;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.InstructionList;
import CleanStart.Mk04_Working.MiscAutobot;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.VarsDispenser;
import CleanStart.Mk04_Working.Instructions.CallObject;
import CleanStart.Mk04_Working.Instructions.Concat;
import CleanStart.Mk04_Working.Instructions.CopyArray;
import CleanStart.Mk04_Working.Instructions.CreateFunctionObject;
import CleanStart.Mk04_Working.Instructions.CreateNewTable;
import CleanStart.Mk04_Working.Instructions.ForEachLoop;
import CleanStart.Mk04_Working.Instructions.ForLoop;
import CleanStart.Mk04_Working.Instructions.HandleVarArgs;
import CleanStart.Mk04_Working.Instructions.Jump_Branched;
import CleanStart.Mk04_Working.Instructions.Jump_Straight;
import CleanStart.Mk04_Working.Instructions.LengthOf;
import CleanStart.Mk04_Working.Instructions.MathOp;
import CleanStart.Mk04_Working.Instructions.NonOp;
import CleanStart.Mk04_Working.Instructions.Return_Concretely;
import CleanStart.Mk04_Working.Instructions.Return_Variably;
import CleanStart.Mk04_Working.Instructions.SetVar;
import CleanStart.Mk04_Working.Instructions.SetVar_Bulk;
import CleanStart.Mk04_Working.Instructions.TailcallFunctionObject;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Instructions.Enums.OpCodes;
import CleanStart.Mk04_Working.Instructions.Jump_Branched.BranchTypes;
import CleanStart.Mk04_Working.Labels.JumpLabel;
import CleanStart.Mk04_Working.Labels.Label;
import CleanStart.Mk04_Working.Labels.LoopEndLabel;
import CleanStart.Mk04_Working.MiscAutobot.ParseTypes;
import CleanStart.Mk04_Working.VarFormula.Formula;
import CleanStart.Mk04_Working.VarFormula.Operators;
import CleanStart.Mk04_Working.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk04_Working.Variables.BoolVar;
import CleanStart.Mk04_Working.Variables.FloatVar;
import CleanStart.Mk04_Working.Variables.IntVar;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.NewTableVar;
import CleanStart.Mk04_Working.Variables.NullVar;
import CleanStart.Mk04_Working.Variables.StringVar;
import CleanStart.Mk04_Working.Variables.TableElementVar;
import CleanStart.Mk04_Working.Variables.VarTypes;
import CleanStart.Mk04_Working.VarsDispenser.VarRepos;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class RawFunctionObject extends FunctionObject
{
	/**
	 * 0 == yes, with culling
	 * <br>
	 * 1 == yes, without culling
	 * <br>
	 * 2 == no
	 */
	private static final int shouldReinsertJumpLabels = 0;

	private static final int FieldsPerFlush = 50;

	private static int instanceCounter_constant = 0;

	private final int[] rawInstructions;
	private final RawFunctionObject[] rawFunctionPrototypes;

	private RawFunctionObject(String inName, int inNumUpvalues, int inNumParams, VarArgFlagSet inVarArgFlags, int inNumRegisters_max, int[] inRawInstructions, RawFunctionObject[] inFunctionPrototypes, int[] inSourceLinePositions, VarsDispenser inVars)
	{
		super(inName, inNumUpvalues, inNumParams, inVarArgFlags, inNumRegisters_max, inSourceLinePositions, inVars);
		this.rawInstructions = inRawInstructions;
		this.rawFunctionPrototypes = inFunctionPrototypes;
	}

	/**
	 * assumes that nothing has been read from the stream yet
	 * 
	 * @param in
	 * @return
	 */
	public static RawFunctionObject getNext(BytesStreamer in, String functionName)
	{
		final ChunkHeader header = ChunkHeader.getNext(in);
		final RawFunctionObject result = getNext(in, header, new HalfByteRadixMap<MystVar>());
		result.name = functionName;
		return result;
	}

	public static RawFunctionObject getNext(BytesStreamer in, ChunkHeader header, HalfByteRadixMap<MystVar> inGlobals)
	{
		final String inName;
		final int numUpvalues;
		final int inNumParams;
		final VarArgFlagSet inVarArgFlags;
		final int inNumRegisters_max;
		final int[] inInstructions;
		final Variable[] inConstants;
		final RawFunctionObject[] inFunctionPrototypes;
		final int[] inSourceLinePositions;
		final VarsDispenser inVars;

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

		final String[] LocalVarsNames = getNextLocalsNamesArr(in, inNumRegisters_max, size_t_byteLength);
		final String[] UpvaluesNames = getNextUpvaluesNamesArr(in, numUpvalues, size_t_byteLength);

		inVars = new VarsDispenser(inConstants, LocalVarsNames.length, UpvaluesNames.length, inGlobals);

		return new RawFunctionObject(inName, numUpvalues, inNumParams, inVarArgFlags, inNumRegisters_max, inInstructions, inFunctionPrototypes, inSourceLinePositions, inVars);
	}

	private static String generateName(BytesStreamer in, int size_t_byteLength)
	{
		return MiscAutobot.parseNextLuaString(in, size_t_byteLength) + "FUNCTION_" + instanceCounter_constant++;
	}

	private static Variable[] parseNextConstantsArr(BytesStreamer in, int LuaNumLengthInBytes, boolean numIsFloat, int size_t_byteLength)
	{
		final int numConstants = in.getNextInt();
		final Variable[] result = new Variable[numConstants];

		for (int i = 0; i < numConstants; i++)
		{
			switch (in.getNextByte())
			{
				case 0: // Nil
				{
					result[i] = NullVar.getInstance("Constants", i);
					break;
				}
				case 1: // boolean
				{
					result[i] = BoolVar.getInstance("Constants", i, in.getNextByte() != 0);
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
								result[i] = FloatVar.getInstance("Constants", i, in.getNextFloat());
							}
							else
							{
								result[i] = IntVar.getInstance("Constants", i, in.getNextInt());
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
					result[i] = StringVar.getInstance("Constants", i, MiscAutobot.parseNextLuaString(in, size_t_byteLength)
							//.replace("\r", "\\r").replace("\n", "\\n")
							);
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

	private static RawFunctionObject[] parseNextFunctionObjectArr(BytesStreamer in, ChunkHeader header, HalfByteRadixMap<MystVar> inGlobals)
	{
		final int length = in.getNextInt();
		final RawFunctionObject[] result = new RawFunctionObject[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = RawFunctionObject.getNext(in, header, inGlobals);
		}

		return result;
	}

	private static String[] getNextLocalsNamesArr(BytesStreamer in, int maxStackSize, int size_t_byteLength)
	{
		final int length = in.getNextInt();
		final String[] result;

		if (length == maxStackSize)
		{
			result = new String[length];
		}
		else
		{
			result = new String[maxStackSize];
		}

		int i = 0;

		for (; i < length; i++)
		{
			final String name = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
			in.getNextInt(); // start of local variable scope
			in.getNextInt(); // end of local variable scope

			//System.out.println("new local var created, with name: " + name);

			result[i] = name;
		}

		for (; i < result.length; i++)
		{
			result[i] = "anonLocalVar_" + i;
		}

		return result;
	}

	private static String[] getNextUpvaluesNamesArr(BytesStreamer in, int quantityIfPresent, int size_t_byteLength)
	{
		final int localLength = in.getNextInt();
		final String[] result;
		// final StepStage start = StepStage.getNullStepStage();

		if (localLength == 0 && quantityIfPresent != localLength)
		{
			result = new String[quantityIfPresent];

			for (int i = 0; i < quantityIfPresent; i++)
			{
				result[i] = "anonUpvalueVar_" + i;
			}
		}
		else if (localLength != quantityIfPresent)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			result = new String[localLength];

			for (int i = 0; i < localLength; i++)
			{
				final String upName = MiscAutobot.parseNextLuaString(in, size_t_byteLength);
				result[i] = upName;
			}
		}

		return result;
	}

	public int[] getRawInstructions()
	{
		return this.rawInstructions;
	}

	@Override
	public FunctionObject[] getFunctionPrototypes()
	{
		return this.rawFunctionPrototypes;
	}

	private static Label getSkipNextStepLabel(HalfByteRadixMap<Label> labelQueue, int rawIndex)
	{
		return getLabelForJumpOffsetOf(labelQueue, 1, rawIndex, true);
	}

	private static Label getLabelForJumpOffsetOf(HalfByteRadixMap<Label> labelQueue, int offset, int rawIndex, boolean wantsJumpLabel)
	{
		final int absOffset = offset + rawIndex + 1;
		Label result = labelQueue.get(absOffset);

		if (result == null)
		{
			if (wantsJumpLabel)
			{
				result = JumpLabel.getInstance();
			}
			else
			{
				if (absOffset > 5000)
				{
					throw new IllegalArgumentException("weird magnitude: " + absOffset);
				}
				result = LoopEndLabel.getInstance();
			}

			labelQueue.put(absOffset, result);
			// System.out.println("label '" + result.getLabelValue() + "' created, intended for line " + absOffset);
		}

		return result;
	}

	/*
	private static class LabelAbstractor implements Label
	{
		private Label core;
		
		public LabelAbstractor(Label inCore)
		{
			this.core = inCore;
		}
	
		@Override
		public boolean isExpired()
		{
			return this.core.isExpired();
		}
	
		@Override
		public void setExpirationState(boolean inNewExpirationState)
		{
			this.core.setExpirationState(inNewExpirationState);
		}
	
		@Override
		public IrCodes getIrCode()
		{
			return this.core.getIrCode();
		}
	
		@Override
		public CanBeInlinedTypes canBeInlined()
		{
			return this.core.canBeInlined();
		}
	
		@Override
		public Variable getInlinedForm()
		{
			return this.core.getInlinedForm();
		}
	
		@Override
		public String getMangledName()
		{
			return "WrapperLabel_Around(" + this.core.getMangledName() + ')';
		}
	
		@Override
		public CharList getVerboseCommentForm()
		{
			final CharList result = this.core.getVerboseCommentForm();
			result.push("Wrapper Around: ");
			return result;
		}
	
		@Override
		public void revVarTypes()
		{
			this.core.revVarTypes();
		}
	
		@Override
		public void absorbSuperflousLabel(Label inIn)
		{
			this.core.absorbSuperflousLabel(inIn);
		}
	
		@Override
		public void setInstructionAsIncoming(Instruction inIn, int inOriginIndex)
		{
			this.core.setInstructionAsIncoming(inIn, inOriginIndex);
		}
	
		@Override
		public HalfByteArray getHalfByteHash()
		{
			throw new UnsupportedOperationException();
		}
	
		@Override
		public HalfByteRadixMap<LabelData> getIncoming()
		{
			return this.core.getIncoming();
		}
	
		@Override
		public String getLabelValue()
		{
			return this.getMangledName();
		}
	
		@Override
		public int compareTo(Label inIn)
		{
			return this.core.compareTo(inIn);
		}
	
		@Override
		public int getInstanceNumber()
		{
			return -(this.core.getInstanceNumber() + 1);
		}
	
		@Override
		public void incrementNumCloseBracesToPrepend()
		{
			this.core.incrementNumCloseBracesToPrepend();
		}
	
		@Override
		public void decrementNumCloseBracesToPrepend()
		{
			this.core.decrementNumCloseBracesToPrepend();
		}
	
		@Override
		public int getNumCloseBracesToPrepend()
		{
			return this.core.getNumCloseBracesToPrepend();
		}
	
		@Override
		public void setAsStartOfDoWhile()
		{
			this.core.setAsStartOfDoWhile();
		}
	}
	*/

	private Instruction getMathOp(int currentCode, Operators operator)
	{
		final FormulaBuilder buildy = new FormulaBuilder();

		final MystVar target;
		final Variable operand1;
		final Formula formy;

		final int[] fields;

		switch (operator)
		{
			case Arith_Add:
			case Arith_Div:
			case Arith_Mod:
			case Arith_Mul:
			case Arith_Pow:
			case Arith_Sub:
			{
				fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

				operand1 = this.Vars.requestRkVarForRead(fields[1]);
				final Variable operand2 = this.Vars.requestRkVarForRead(fields[2], VarTypes.Math_Unknown);

				buildy.add(operand1);
				buildy.add(operator);
				buildy.add(operand2);
				formy = buildy.build("FormulaVar", null);

				target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Math_Variable, formy);

				break;
			}
			case Logic_Not:
			case Arith_Neg:
			{
				fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

				operand1 = this.Vars.requestRkVarForRead(fields[1]);

				buildy.add(operator);
				buildy.add(operand1);
				formy = buildy.build("FormulaVar", null);

				target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Formula, formy);

				break;
			}
			default:
			{
				throw new UnhandledEnumException(operator);
			}
		}

		return MathOp.getInstance(target, formy);
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
	
	public InstructionList refineInstructions2(RefinedFunctionObject[] convertedPrototypes)
	{
		final HalfByteRadixMap<Label> labelQueue = new HalfByteRadixMap<Label>();
		final int[] lineWeights = new int[this.rawInstructions.length];
		
		final InstructionList result = new InstructionList();
		
		for (int rawIndex = 0; rawIndex < this.rawInstructions.length; rawIndex++)
		{
			lineWeights[rawIndex]++;
			final int currentCode = this.rawInstructions[rawIndex];
			final int[] fields;
			final OpCodes rawType = OpCodes.parse(currentCode);
			final Instruction locResult;
			boolean addLocResult = true; // set to false when multiple Instructions need to be added, to enhance readability

			switch (rawType)
			{
				case Add:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Add);
					break;
				}
				case ArithmeticNegation:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Neg);
					break;
				}
				case Call:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final MystVar functionObject = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.FunctionObject);
					final boolean ArgsAreVariable;
					final MystVar[] args;

					if (fields[1] == 0)
					{
						ArgsAreVariable = true;

						args = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						ArgsAreVariable = false;

						args = new MystVar[fields[1] - 1];
					}

					for (int i = 0; i < args.length; i++)
					{
						args[i] = this.Vars.requestVarForRead(i + fields[0] + 1, VarRepos.Locals);
						
					}

					final boolean ReturnsAreVariable;
					final MystVar[] Returns;

					if (fields[2] == 0)
					{
						ReturnsAreVariable = true;
						Returns = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0])];
					}
					else
					{
						ReturnsAreVariable = false;
						Returns = new MystVar[fields[2] - 1];
					}

					for (int i = 0; i < Returns.length; i++)
					{
						Returns[i] = this.Vars.requestVarForWrite(i + fields[0], VarRepos.Locals, null); // this needs to be a write instead of a read, to ensure proper variable evolution
						// System.out.println(Returns[i].getMostRelevantIdValue());
					}

					locResult = CallObject.getInstance(functionObject, ArgsAreVariable, args, ReturnsAreVariable, Returns);

					break;
				}
				case Close:
				{
					locResult = NonOp.getInstance();
					break;
				}
				// lineWeights manipulator - indirectly
				case Closure:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);
					final RefinedFunctionObject targetFunctionPrototype = convertedPrototypes[fields[1]];
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.FunctionObject, null);
					final MystVar[] upvalues_args = new MystVar[targetFunctionPrototype.numUpvalues];
					final MystVar[] upvalues_changedState = new MystVar[targetFunctionPrototype.numUpvalues];

					// consume pseudoinstructions
					for (int i = 0; i < upvalues_args.length; i++)
					{
						final int pseudoRaw = this.rawInstructions[++rawIndex];
						final OpCodes locCode = OpCodes.parse(pseudoRaw);
						final int[] nextFields = MiscAutobot.parseRawInstruction(pseudoRaw, ParseTypes.iAB);
						final VarRepos targetRepo;

						switch (locCode)
						{
							case Move:
							{
								targetRepo = VarRepos.Locals;
								break;
							}
							case GetUpvalue:
							{
								targetRepo = VarRepos.Upvalues;
								break;
							}
							default:
							{
								throw new UnhandledEnumException(locCode);
							}
						}

						/*
						 * doing it like this ensures visual continuity is maintained, whilst still keeping memory barrier
						 */
						upvalues_args[i] = this.Vars.requestVarForRead(nextFields[1], targetRepo);
						upvalues_changedState[i] = this.Vars.requestVarForStateChange(nextFields[1], targetRepo);
					}

					locResult = CreateFunctionObject.getInstance(targetFunctionPrototype, upvalues_args, upvalues_changedState, target);

					break;
				}
				case Concatenation:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					
					final Variable[] sources = new Variable[(fields[2] - fields[1]) + 1];

					for (int i = fields[1]; i <= fields[2]; i++)
					{
						// try
						// {
						sources[i - fields[1]] = this.Vars.requestVarForRead(i, VarRepos.Locals, VarTypes.String);
						/*
						}
						catch (IllegalArgumentException e)
						{
						throw new IllegalArgumentException("on line " + rawIndex + " of function " + this.name);
						}
						*/
					}

					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.String, null);
					
					locResult = Concat.getInstance(target, sources);

					break;
				}
				case Divide:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Div);
					break;
				}
				case GetGlobal:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final StringVar globalVarKey = (StringVar) this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar source = this.Vars.requestVarForRead(globalVarKey.getCargo());
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getBestGuess(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case GetTable: // would be better described as GetTableElement
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar sourceTable = this.Vars.requestVarForRead(fields[1], VarRepos.Locals, VarTypes.TableObject);
					final Variable sourceTableIndex = this.Vars.requestRkVarForRead(fields[2]);
					final TableElementVar source = TableElementVar.getInstance(sourceTable.getMostRelevantIdValue(false) + ".[" + sourceTableIndex.getMostRelevantIdValue(false) + ']', null, sourceTable, sourceTableIndex);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.TableElement, source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case GetUpvalue:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final MystVar source = this.Vars.requestVarForRead(fields[1], VarRepos.Upvalues);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getBestGuess(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case InitNumericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAsBx);

					final MystVar indexVar = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar limitVar = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar stepValue = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar externalIndexVar = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);

					final Label toAfterLoop = getLabelForJumpOffsetOf(labelQueue, fields[1] + 1, rawIndex, false);

					locResult = ForLoop.getInstance(indexVar, limitVar, stepValue, externalIndexVar, toAfterLoop);

					break;
				}
				case IterateGenericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar iteratorFunction = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final MystVar state = this.Vars.requestVarForRead(fields[0] + 1, VarRepos.Locals);
					final MystVar enumIndex = this.Vars.requestVarForRead(fields[0] + 2, VarRepos.Locals);
					final MystVar[] loopVars = new MystVar[fields[2]];

					for (int i = 0; i < loopVars.length; i++)
					{
						loopVars[i] = this.Vars.requestVarForRead(i + fields[0] + 3, VarRepos.Locals);
					}

					final int[] nextFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label toOtherEnd = getLabelForJumpOffsetOf(labelQueue, nextFields[0], rawIndex, false);

					locResult = ForEachLoop.getInstance(iteratorFunction, state, enumIndex, loopVars, toOtherEnd);

					break;
				}
				case IterateNumericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.isBx);

					final Label toLoopStart = getLabelForJumpOffsetOf(labelQueue, fields[0] - 1, rawIndex, true);
					locResult = Jump_Straight.getInstance(toLoopStart, true);

					break;
				}
				case Jump:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.isBx);
					final Label jump = getLabelForJumpOffsetOf(labelQueue, fields[0], rawIndex, true);
					locResult = Jump_Straight.getInstance(jump, (fields[0] < 0));

					break;
				}
				case Length:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final MystVar source = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Int, null);

					locResult = LengthOf.getInstance(target, source);

					break;
				}
				// LineWeight Manipulator
				case LoadBool:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final BoolVar source = BoolVar.getInstance("hardCodedBoolVar", null, fields[1] != 0);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Bool, source);
					locResult = SetVar.getInstance(target, source);

					if (fields[2] != 0) // skip next instruction
					{
						addLocResult = false;
						final Label skipLabel = getSkipNextStepLabel(labelQueue, rawIndex);
						final Jump_Straight jump = Jump_Straight.getInstance(skipLabel);
						lineWeights[rawIndex]++;

						result.add(locResult);
						result.add(jump);
					}

					break;
				}
				case LoadK:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final Variable source = this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getVarType(), source);
					locResult = SetVar.getInstance(target, source);

					break;
				}
				case LoadNull:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final int length = fields[1] - fields[0];
					final Variable source = NullVar.getInstance();

					if (length == 0)
					{
						final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Null, source);
						locResult = SetVar.getInstance(target, source);
					}
					else
					{
						final MystVar[] targets = new MystVar[length];

						for (int i = 0; i < length; i++)
						{
							targets[i] = this.Vars.requestVarForWrite(i + fields[0], VarRepos.Locals, VarTypes.Null, source);
						}

						locResult = SetVar_Bulk.getInstance(targets, source);
					}

					break;
				}
				case LogicalNegation:
				{
					locResult = this.getMathOp(currentCode, Operators.Logic_Not);
					break;
				}
				case Modulo:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Mod);
					break;
				}
				case Move:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final Variable source = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getVarType(), source);

					locResult = SetVar.getInstance(target, source);
					break;
				}
				case Multiply:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Mul);
					break;
				}
				case NewTable:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final NewTableVar NewTable = NewTableVar.getInstance("<new table>", fields[1], fields[2]);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.TableObject, NewTable);
					locResult = CreateNewTable.getInstance(target, NewTable);

					break;
				}
				case NoOp:
				{
					locResult = NonOp.getInstance();
					break;
				}
				case Power:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Pow);
					break;
				}
				case Return:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final MystVar[] sources;

					if (fields[1] == 0)
					{
						// varArgs

						final int lengthOfLocalsArr = this.Vars.getLengthOf(VarRepos.Locals);
						sources = new MystVar[lengthOfLocalsArr - fields[0]];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Vars.requestVarForRead(fields[0] + i, VarRepos.Locals);
						}

						final MystVar lastExpression = this.Vars.requestVarForWrite(lengthOfLocalsArr - 1, VarRepos.Locals, null);

						locResult = Return_Variably.getInstance(sources, lastExpression);
					}
					else
					{
						sources = new MystVar[fields[1] - 1];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Vars.requestVarForWrite(fields[0] + i, VarRepos.Locals, null);
						}

						locResult = Return_Concretely.getInstance(sources);
					}

					break;
				}
				// LineWeight Manipulator
				case Self:
				{
					lineWeights[rawIndex]++;
					addLocResult = false;

					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar sourceTable = this.Vars.requestVarForRead(fields[1], VarRepos.Locals, VarTypes.TableObject);
					final Variable indexer = this.Vars.requestRkVarForRead(fields[2]);
					final TableElementVar sourceElement = TableElementVar.getInstance(sourceTable.getMostRelevantIdValue(false) + ".[" + indexer.getMostRelevantIdValue(false) + ']', null, sourceTable, indexer);

					final MystVar targetTable = this.Vars.requestVarForWrite(fields[0] + 1, VarRepos.Locals, VarTypes.TableObject, sourceTable);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, sourceElement.getVarType(), sourceElement);

					final SetVar Self_FirstPart = SetVar.getInstance(targetTable, sourceTable);
					final SetVar Self_SecondPart = SetVar.getInstance(target, sourceElement);

					result.add(Self_FirstPart);
					result.add(Self_SecondPart);

					locResult = null;

					break;
				}
				case SetGlobal:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final Variable source = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final StringVar rawKey = (StringVar) this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar target = this.Vars.requestVarForWrite(rawKey.getCargo(), source.getVarType(), null, source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case SetList:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar targetTable = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.TableObject);
					// final MystVar targetTable = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Table);
					
					final MystVar[] sourceArray;
					final boolean sourcesAreVariable;
					
					if(fields[1] == 0)	//from (A+1) to TopOfStack
					{
						sourceArray = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
						sourcesAreVariable = true;
					}
					else
					{
						sourceArray = new MystVar[fields[1]];
						sourcesAreVariable = false;
					}
					
					for (int i = 0; i < sourceArray.length; i++)
					{
						sourceArray[i] = this.Vars.requestVarForRead(fields[0] + i + 1, VarRepos.Locals);
					}
					
					if(fields[2] == 0)
					{
						fields[2] = this.rawInstructions[++rawIndex];
					}

					final int offsetOffset = (fields[2] - 1) * FieldsPerFlush;

					locResult = CopyArray.getInstance(offsetOffset, targetTable, sourceArray, fields[0] + 1, "Locals", sourcesAreVariable);

					break;
				}
				case SetTable:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar targetTable = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.TableObject);
					// final MystVar targetTable = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Table);

					final Variable indexer = this.Vars.requestRkVarForRead(fields[1]);
					final TableElementVar target = TableElementVar.getInstance("<SetTable: target element>", null, targetTable, indexer);

					final Variable source = this.Vars.requestRkVarForRead(fields[2]);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case SetUpvalue:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final Variable source = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[1], VarRepos.Upvalues, source.getVarType(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case Subtract:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Sub);
					break;
				}
				case TailCall:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final MystVar functionObject = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.FunctionObject);
					final boolean ArgsAreVariable;
					final MystVar[] args;

					if (fields[1] == 0)
					{
						ArgsAreVariable = true;

						args = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						ArgsAreVariable = false;

						args = new MystVar[fields[1] - 1];
					}

					for (int i = 0; i < args.length; i++)
					{
						args[i] = this.Vars.requestVarForRead(i + fields[0] + 1, VarRepos.Locals);
					}

					locResult = TailcallFunctionObject.getInstance(functionObject, ArgsAreVariable, args);

					break;
				}
				// LineWeight Manipulator - especially funky: do not set lineWeights[rawIndex+1] to 0, so as to properly insert the setter
				case TestAndSaveLogicalComparison:
				{
					/*
					 * if(R(B) == C), then R(A) = R(B); else, PC++;
					 */
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar varB = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final IntVar varC = IntVar.getInstance("HardCodedInt", fields[2]);
					final FormulaBuilder buildy = new FormulaBuilder();

					if (fields[2] == 0)
					{
						buildy.add(Operators.Logic_Not);
					}

					buildy.add(varB);

					final Formula formy = buildy.build("<hardcoded conditional formula>", null);

					final Label ifFalse = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifTrue = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, true);
					final boolean TrueIsNegative = jumpFields[0] < 0;

					addLocResult = false;

					final Instruction branchedJump = Jump_Branched.getInstance(formy, ifFalse, TrueIsNegative, ifTrue, false);

					final MystVar varA = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.ComparableVar, varC);
					final Instruction setter = SetVar.getInstance(varA, varC);

					result.add(branchedJump);
					result.add(setter);

					locResult = null;
					break;
				}
				// LineWeight Manipulators
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final FormulaBuilder buildy = new FormulaBuilder();
					buildy.add(this.Vars.requestRkVarForRead(fields[1]));
					buildy.add(getRelationalOperator(fields[0], rawType));
					buildy.add(this.Vars.requestRkVarForRead(fields[2]));

					final Formula formy = buildy.build("hardCoded comparison", null);

					final Label ifTrue = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifFalse = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, jumpFields[0] >= 0); // if jumpFields[0] is negative, implies loop
					final boolean FalseIsNegative = jumpFields[0] < 0;
					lineWeights[rawIndex]--;
					// TODO: determine if ifTrue and ifFalse need to be swapped

					locResult = Jump_Branched.getInstance(formy, ifFalse, false, ifTrue, FalseIsNegative);
					break;
				}
				// LineWeight Manipulator - no weirdness though
				case TestLogicalComparison:
				{
					/*
					 * if (R(A) != C), then PC++
					 */
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAC);

					final MystVar varA = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.ComparableVar);
					// final IntVar varC = IntVar.getInstance("HardCodedInt", fields[1]);
					final FormulaBuilder buildy = new FormulaBuilder();

					if (fields[1] == 0)
					{
						buildy.add(Operators.Logic_Not);
					}

					buildy.add(varA);

					final Formula formy = buildy.build("<hardcoded conditional formula>", null);

					final Label ifFalse = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifTrue = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, true);
					final boolean TrueIsNegative = jumpFields[0] < 0;
					lineWeights[rawIndex]--;

					locResult = Jump_Branched.getInstance(formy, ifFalse, false, ifTrue, TrueIsNegative);

					break;
				}
				case VarArg:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final boolean copyAllPossibles;
					final MystVar[] targets;

					if (fields[1] == 0)
					{
						copyAllPossibles = true;
						targets = new MystVar[0];
					}
					else
					{
						copyAllPossibles = false;
						targets = new MystVar[fields[1] - 1];

						for (int i = 0; i < targets.length; i++)
						{
							targets[i] = this.Vars.requestVarForWrite(fields[0] + i, VarRepos.Locals, null);
						}
					}

					locResult = HandleVarArgs.getInstance(targets, copyAllPossibles);

					break;
				}
				default:
				{
					throw new UnhandledEnumException(rawType);
				}
			}

			if (addLocResult)
			{
				result.add(locResult);

				 //System.out.println(locResult.getVerboseCommentForm().toString());
			}
		}
		
		return this.reinsertLabels2(result.getHead(), labelQueue, lineWeights);
	}

	public SingleLinkedList<Instruction> refineInstructions(RefinedFunctionObject[] convertedPrototypes)
	{
		//System.out.println("refining function: " + this.name);
		final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();
		final HalfByteRadixMap<Label> labelQueue = new HalfByteRadixMap<Label>();
		final int[] lineWeights = new int[this.rawInstructions.length];

		for (int rawIndex = 0; rawIndex < this.rawInstructions.length; rawIndex++)
		{
			lineWeights[rawIndex]++;
			final int currentCode = this.rawInstructions[rawIndex];
			final int[] fields;
			final OpCodes rawType = OpCodes.parse(currentCode);
			final Instruction locResult;
			boolean addLocResult = true; // set to false when multiple Instructions need to be added, to enhance readability

			switch (rawType)
			{
				case Add:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Add);
					break;
				}
				case ArithmeticNegation:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Neg);
					break;
				}
				case Call:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final MystVar functionObject = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.FunctionObject);
					final boolean ArgsAreVariable;
					final MystVar[] args;

					if (fields[1] == 0)
					{
						ArgsAreVariable = true;

						args = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						ArgsAreVariable = false;

						args = new MystVar[fields[1] - 1];
					}

					for (int i = 0; i < args.length; i++)
					{
						args[i] = this.Vars.requestVarForRead(i + fields[0] + 1, VarRepos.Locals);
					}

					final boolean ReturnsAreVariable;
					final MystVar[] Returns;

					if (fields[2] == 0)
					{
						ReturnsAreVariable = true;
						Returns = new MystVar[0];
					}
					else
					{
						ReturnsAreVariable = false;
						Returns = new MystVar[fields[2] - 1];
					}

					for (int i = 0; i < Returns.length; i++)
					{
						Returns[i] = this.Vars.requestVarForWrite(i + fields[0], VarRepos.Locals, null); // this needs to be a write instead of a read, to ensure proper variable evolution
						// System.out.println(Returns[i].getMostRelevantIdValue());
					}

					locResult = CallObject.getInstance(functionObject, ArgsAreVariable, args, ReturnsAreVariable, Returns);

					break;
				}
				case Close:
				{
					locResult = NonOp.getInstance();
					break;
				}
				// lineWeights manipulator - indirectly
				case Closure:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);
					final RefinedFunctionObject targetFunctionPrototype = convertedPrototypes[fields[1]];
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.FunctionObject, null);
					final MystVar[] upvalues_args = new MystVar[targetFunctionPrototype.numUpvalues];
					final MystVar[] upvalues_changedState = new MystVar[targetFunctionPrototype.numUpvalues];

					// consume pseudoinstructions
					for (int i = 0; i < upvalues_args.length; i++)
					{
						final int pseudoRaw = this.rawInstructions[++rawIndex];
						final OpCodes locCode = OpCodes.parse(pseudoRaw);
						final int[] nextFields = MiscAutobot.parseRawInstruction(pseudoRaw, ParseTypes.iAB);
						final VarRepos targetRepo;

						switch (locCode)
						{
							case Move:
							{
								targetRepo = VarRepos.Locals;
								break;
							}
							case GetUpvalue:
							{
								targetRepo = VarRepos.Upvalues;
								break;
							}
							default:
							{
								throw new UnhandledEnumException(locCode);
							}
						}

						/*
						 * doing it like this ensures visual continuity is maintained, whilst still keeping memory barrier
						 */
						upvalues_args[i] = this.Vars.requestVarForRead(nextFields[1], targetRepo);
						upvalues_changedState[i] = this.Vars.requestVarForStateChange(nextFields[1], targetRepo);
					}

					locResult = CreateFunctionObject.getInstance(targetFunctionPrototype, upvalues_args, upvalues_changedState, target);

					break;
				}
				case Concatenation:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					
					final Variable[] sources = new Variable[(fields[2] - fields[1]) + 1];

					for (int i = fields[1]; i <= fields[2]; i++)
					{
						// try
						// {
						sources[i - fields[1]] = this.Vars.requestVarForRead(i, VarRepos.Locals, VarTypes.String);
						/*
						}
						catch (IllegalArgumentException e)
						{
						throw new IllegalArgumentException("on line " + rawIndex + " of function " + this.name);
						}
						*/
					}

					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.String, null);
					
					locResult = Concat.getInstance(target, sources);

					break;
				}
				case Divide:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Div);
					break;
				}
				case GetGlobal:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final Variable globalVarKey = this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar source = this.Vars.requestVarForRead(globalVarKey.valueToString());
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getBestGuess(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case GetTable: // would be better described as GetTableElement
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar sourceTable = this.Vars.requestVarForRead(fields[1], VarRepos.Locals, VarTypes.TableObject);
					final Variable sourceTableIndex = this.Vars.requestRkVarForRead(fields[2]);
					final TableElementVar source = TableElementVar.getInstance("", null, sourceTable, sourceTableIndex);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.TableElement, source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case GetUpvalue:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final MystVar source = this.Vars.requestVarForRead(fields[1], VarRepos.Upvalues);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getBestGuess(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case InitNumericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAsBx);

					final MystVar indexVar = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar limitVar = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar stepValue = this.Vars.requestVarForRead(fields[0]++, VarRepos.Locals);
					final MystVar externalIndexVar = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);

					final Label toAfterLoop = getLabelForJumpOffsetOf(labelQueue, fields[1] + 1, rawIndex, false);

					locResult = ForLoop.getInstance(indexVar, limitVar, stepValue, externalIndexVar, toAfterLoop);

					break;
				}
				case IterateGenericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar iteratorFunction = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final MystVar state = this.Vars.requestVarForRead(fields[0] + 1, VarRepos.Locals);
					final MystVar enumIndex = this.Vars.requestVarForRead(fields[0] + 2, VarRepos.Locals);
					final MystVar[] loopVars = new MystVar[fields[2]];

					for (int i = 0; i < loopVars.length; i++)
					{
						loopVars[i] = this.Vars.requestVarForRead(i + fields[0] + 3, VarRepos.Locals);
					}

					final int[] nextFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label toOtherEnd = getLabelForJumpOffsetOf(labelQueue, nextFields[0], rawIndex, false);

					locResult = ForEachLoop.getInstance(iteratorFunction, state, enumIndex, loopVars, toOtherEnd);

					break;
				}
				case IterateNumericForLoop:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.isBx);

					final Label toLoopStart = getLabelForJumpOffsetOf(labelQueue, fields[0] - 1, rawIndex, true);
					locResult = Jump_Straight.getInstance(toLoopStart, true);

					break;
				}
				case Jump:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.isBx);
					final Label jump = getLabelForJumpOffsetOf(labelQueue, fields[0], rawIndex, true);
					locResult = Jump_Straight.getInstance(jump, (fields[0] < 0));

					break;
				}
				case Length:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final MystVar source = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Int, source);

					locResult = LengthOf.getInstance(target, source);

					break;
				}
				// LineWeight Manipulator
				case LoadBool:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final BoolVar source = BoolVar.getInstance("hardCodedBoolVar", null, fields[1] != 0);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Bool, source);
					locResult = SetVar.getInstance(target, source);

					if (fields[2] != 0) // skip next instruction
					{
						addLocResult = false;
						final Label skipLabel = getSkipNextStepLabel(labelQueue, rawIndex);
						final Jump_Straight jump = Jump_Straight.getInstance(skipLabel);
						lineWeights[rawIndex]++;

						result.add(locResult);
						result.add(jump);
					}

					break;
				}
				case LoadK:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final Variable source = this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getVarType(), source);
					locResult = SetVar.getInstance(target, source);

					break;
				}
				case LoadNull:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final int length = fields[1] - fields[0];
					final Variable source = NullVar.getInstance();

					if (length == 0)
					{
						final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Null, source);
						locResult = SetVar.getInstance(target, source);
					}
					else
					{
						final MystVar[] targets = new MystVar[length];

						for (int i = 0; i < length; i++)
						{
							targets[i] = this.Vars.requestVarForWrite(i + fields[0], VarRepos.Locals, VarTypes.Null, source);
						}

						locResult = SetVar_Bulk.getInstance(targets, source);
					}

					break;
				}
				case LogicalNegation:
				{
					locResult = this.getMathOp(currentCode, Operators.Logic_Not);
					break;
				}
				case Modulo:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Mod);
					break;
				}
				case Move:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final Variable source = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, source.getVarType(), source);

					locResult = SetVar.getInstance(target, source);
					break;
				}
				case Multiply:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Mul);
					break;
				}
				case NewTable:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final NewTableVar NewTable = NewTableVar.getInstance("<new table>", fields[1], fields[2]);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.TableObject, NewTable);
					locResult = CreateNewTable.getInstance(target, NewTable);

					break;
				}
				case NoOp:
				{
					locResult = NonOp.getInstance();
					break;
				}
				case Power:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Pow);
					break;
				}
				case Return:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final MystVar[] sources;

					if (fields[1] == 0)
					{
						// varArgs

						final int lengthOfLocalsArr = this.Vars.getLengthOf(VarRepos.Locals);
						sources = new MystVar[lengthOfLocalsArr - fields[0]];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Vars.requestVarForRead(fields[0] + i, VarRepos.Locals);
						}

						final MystVar lastExpression = this.Vars.requestVarForWrite(lengthOfLocalsArr - 1, VarRepos.Locals, null);

						locResult = Return_Variably.getInstance(sources, lastExpression);
					}
					else
					{
						sources = new MystVar[fields[1] - 1];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Vars.requestVarForWrite(fields[0] + i, VarRepos.Locals, null);
						}

						locResult = Return_Concretely.getInstance(sources);
					}

					break;
				}
				// LineWeight Manipulator
				case Self:
				{
					lineWeights[rawIndex]++;
					addLocResult = false;

					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar sourceTable = this.Vars.requestVarForRead(fields[1], VarRepos.Locals, VarTypes.TableObject);
					final Variable indexer = this.Vars.requestRkVarForRead(fields[2]);
					final TableElementVar sourceElement = TableElementVar.getInstance(sourceTable.getMostRelevantIdValue(false) + ".[" + indexer.getMostRelevantIdValue(false) + ']', null, sourceTable, indexer);

					final MystVar targetTable = this.Vars.requestVarForWrite(fields[0] + 1, VarRepos.Locals, VarTypes.TableObject, sourceTable);
					final MystVar target = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, sourceElement.getVarType(), sourceElement);

					final SetVar Self_FirstPart = SetVar.getInstance(targetTable, sourceTable);
					final SetVar Self_SecondPart = SetVar.getInstance(target, sourceElement);

					result.add(Self_FirstPart);
					result.add(Self_SecondPart);

					locResult = null;

					break;
				}
				case SetGlobal:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABx);

					final Variable source = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final StringVar rawKey = (StringVar) this.Vars.requestConstantVarForRead(fields[1]);
					final MystVar target = this.Vars.requestVarForWrite(rawKey.getCargo(), source.getVarType(), null, source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case SetList:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar targetTable = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.TableObject);
					// final MystVar targetTable = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Table);

					final MystVar[] sourceArray;
					final boolean sourcesAreVariable;
					
					if(fields[1] == 0)	//from (A+1) to TopOfStack
					{
						sourceArray = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
						sourcesAreVariable = true;
					}
					else
					{
						sourceArray = new MystVar[fields[1]];
						sourcesAreVariable = false;
					}
					
					for (int i = 0; i < sourceArray.length; i++)
					{
						sourceArray[i] = this.Vars.requestVarForRead(fields[0] + i + 1, VarRepos.Locals);
					}
					
					if(fields[2] == 0)
					{
						fields[2] = this.rawInstructions[++rawIndex];
					}

					final int offsetOffset = (fields[2] - 1) * FieldsPerFlush;

					locResult = CopyArray.getInstance(offsetOffset, targetTable, sourceArray, fields[0] + 1, "Locals",  sourcesAreVariable);

					break;
				}
				case SetTable:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar targetTable = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.TableObject);
					// final MystVar targetTable = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.Table);

					final Variable indexer = this.Vars.requestRkVarForRead(fields[1]);
					final TableElementVar target = TableElementVar.getInstance("<SetTable: target element>", null, targetTable, indexer);

					final Variable source = this.Vars.requestRkVarForRead(fields[2]);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case SetUpvalue:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);

					final Variable source = this.Vars.requestVarForRead(fields[0], VarRepos.Locals);
					final MystVar target = this.Vars.requestVarForWrite(fields[1], VarRepos.Upvalues, source.getVarType(), source);

					locResult = SetVar.getInstance(target, source);

					break;
				}
				case Subtract:
				{
					locResult = this.getMathOp(currentCode, Operators.Arith_Sub);
					break;
				}
				case TailCall:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final MystVar functionObject = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.FunctionObject);
					final boolean ArgsAreVariable;
					final MystVar[] args;

					if (fields[1] == 0)
					{
						ArgsAreVariable = true;

						args = new MystVar[this.Vars.getLengthOf(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						ArgsAreVariable = false;

						args = new MystVar[fields[1] - 1];
					}

					for (int i = 0; i < args.length; i++)
					{
						args[i] = this.Vars.requestVarForRead(i + fields[0] + 1, VarRepos.Locals);
					}

					locResult = TailcallFunctionObject.getInstance(functionObject, ArgsAreVariable, args);

					break;
				}
				// LineWeight Manipulator - especially funky: do not set lineWeights[rawIndex+1] to 0, so as to properly insert the setter
				case TestAndSaveLogicalComparison:
				{
					/*
					 * if(R(B) == C), then R(A) = R(B); else, PC++;
					 */
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);

					final MystVar varB = this.Vars.requestVarForRead(fields[1], VarRepos.Locals);
					final IntVar varC = IntVar.getInstance("HardCodedInt", fields[2]);
					final FormulaBuilder buildy = new FormulaBuilder();

					if (fields[2] == 0)
					{
						buildy.add(Operators.Logic_Not);
					}

					buildy.add(varB);

					final Formula formy = buildy.build("<hardcoded conditional formula>", null);

					final Label ifFalse = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifTrue = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, true);
					final boolean TrueIsNegative = jumpFields[0] < 0;

					addLocResult = false;

					final Instruction branchedJump = Jump_Branched.getInstance(formy, ifTrue, TrueIsNegative, ifFalse, false);

					final MystVar varA = this.Vars.requestVarForWrite(fields[0], VarRepos.Locals, VarTypes.ComparableVar, varC);
					final Instruction setter = SetVar.getInstance(varA, varC);

					result.add(branchedJump);
					result.add(setter);

					locResult = null;
					break;
				}
				// LineWeight Manipulators
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iABC);
					final FormulaBuilder buildy = new FormulaBuilder();
					buildy.add(this.Vars.requestRkVarForRead(fields[1]));
					buildy.add(getRelationalOperator(fields[0], rawType));
					buildy.add(this.Vars.requestRkVarForRead(fields[2]));

					final Formula formy = buildy.build("hardCoded comparison", null);

					final Label ifTrue = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifFalse = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, jumpFields[0] >= 0); // if jumpFields[0] is negative, implies loop
					final boolean FalseIsNegative = jumpFields[0] < 0;
					lineWeights[rawIndex]--;
					// TODO: determine if ifTrue and ifFalse need to be swapped

					locResult = Jump_Branched.getInstance(formy, ifTrue, false, ifFalse, FalseIsNegative);
					break;
				}
				// LineWeight Manipulator - no weirdness though
				case TestLogicalComparison:
				{
					/*
					 * if (R(A) != C), then PC++
					 */
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAC);

					final MystVar varA = this.Vars.requestVarForRead(fields[0], VarRepos.Locals, VarTypes.ComparableVar);
					// final IntVar varC = IntVar.getInstance("HardCodedInt", fields[1]);
					final FormulaBuilder buildy = new FormulaBuilder();

					if (fields[1] == 0)
					{
						buildy.add(Operators.Logic_Not);
					}

					buildy.add(varA);

					final Formula formy = buildy.build("<hardcoded conditional formula>", null);

					final Label ifFalse = getSkipNextStepLabel(labelQueue, rawIndex);

					// consume and parse next instruction, which will be a jump
					final int[] jumpFields = MiscAutobot.parseRawInstruction(this.rawInstructions[++rawIndex], ParseTypes.isBx);
					final Label ifTrue = getLabelForJumpOffsetOf(labelQueue, jumpFields[0], rawIndex, true);
					final boolean TrueIsNegative = jumpFields[0] < 0;
					lineWeights[rawIndex]--;

					locResult = Jump_Branched.getInstance(formy, ifTrue, TrueIsNegative, ifFalse, false);

					break;
				}
				case VarArg:
				{
					fields = MiscAutobot.parseRawInstruction(currentCode, ParseTypes.iAB);
					final boolean copyAllPossibles;
					final MystVar[] targets;

					if (fields[1] == 0)
					{
						copyAllPossibles = true;
						targets = new MystVar[0];
					}
					else
					{
						copyAllPossibles = false;
						targets = new MystVar[fields[1] - 1];

						for (int i = 0; i < targets.length; i++)
						{
							targets[i] = this.Vars.requestVarForWrite(fields[0] + i, VarRepos.Locals, null);
						}
					}

					locResult = HandleVarArgs.getInstance(targets, copyAllPossibles);

					break;
				}
				default:
				{
					throw new UnhandledEnumException(rawType);
				}
			}

			if (addLocResult)
			{
				result.add(locResult);

				 //System.out.println(locResult.getVerboseCommentForm().toString());
			}
		}

		return this.reinsertLabels(result, labelQueue, lineWeights);
	}

	private InstructionList reinsertLabels2(Instruction old, HalfByteRadixMap<Label> labelQueue, int[] lineWeights)
	{
		final InstructionList result = new InstructionList();
		
		while(old.getIrCode() == IrCodes.NonOp)
		{
			old = old.getNext();
		}
		
		switch (shouldReinsertJumpLabels)
		{
			case 0: // yes, and cull
			{
				final SingleLinkedList<Label> extras = new SingleLinkedList<Label>();
				final HalfByteRadixMap<Instruction> firstLinesAfterLabel = new HalfByteRadixMap<Instruction>();

				for (int i = 0; i < lineWeights.length; i++)
				{
					final Label maybeLabel = labelQueue.get(i);

					if (maybeLabel != null)
					{
						extras.push(maybeLabel);
					}

					if (lineWeights[i] > 0)
					{
						final Instruction first = old;
						old = old.getNext();

						if (!extras.isEmpty())
						{
							final Label label = consolidateLabels(extras);
							result.add(label);
							firstLinesAfterLabel.put(label.getHalfByteHash(), first);
						}

						checkForNegJumps2(first, result, firstLinesAfterLabel);
						
						result.add(first);
						lineWeights[i]--;

						while (lineWeights[i] > 0)
						{
							final Instruction pez = old;
							old = old.getNext();
							result.add(pez);
							lineWeights[i]--;

							checkForNegJumps2(pez, result, firstLinesAfterLabel);
						}
					}
				}
				break;
			}
			case 1: // yes, but skip culling
			{
				for (int i = 0; i < lineWeights.length; i++)
				{
					final Label maybeLabel = labelQueue.get(i);

					if (maybeLabel != null)
					{
						//System.out.println("adding " + maybeLabel.getLabelValue() + " at line " + i);
						result.add(maybeLabel);
					}

					switch (lineWeights[i])
					{
						case 0:
						{
							result.add(NonOp.getInstance());
							break;
						}
						case 1:
						{
							result.add(old);
							old = old.getNext();

							break;
						}
						default:
						{
							while (lineWeights[i] > 0)
							{
								final Instruction pez = old;
								old = old.getNext();
								result.add(pez);
								lineWeights[i]--;
							}

							break;
						}
					}
				}
				break;
			}
			case 2: // no
			{
				result.add(old);
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad option: " + shouldReinsertJumpLabels);
			}
		}
		
		return result;
	}
	
	private SingleLinkedList<Instruction> reinsertLabels(SingleLinkedList<Instruction> old, HalfByteRadixMap<Label> labelQueue, int[] lineWeights)
	{
		//System.out.println("reinserting labels for function: " + this.name);
		final SingleLinkedList<Instruction> result;

		switch (shouldReinsertJumpLabels)
		{
			case 0: // yes, and cull
			{
				//result = this.reinsertLabels_WithCulling2(old, labelQueue, lineWeights);
				result = new SingleLinkedList<Instruction>(this.reinsertLabels_WithCulling(old, labelQueue, lineWeights).toArray());
				break;
			}
			case 1: // yes, but skip culling
			{
				result = this.reinsertLabels_NoCulling(old, labelQueue, lineWeights);
				break;
			}
			case 2: // no
			{
				result = old;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad option: " + shouldReinsertJumpLabels);
			}
		}

		return result;
	}

	/**
	 * implemented so as to be able to do direct node IO
	 * 
	 * @author MechaGent
	 *
	 */
	private static class locList
	{
		private Node head;
		private Node tail;
		private int size;

		public locList()
		{
			this.head = null;
			this.tail = null;
			this.size = 0;
		}

		public void add(Instruction in)
		{
			this.add(new Node(in));
		}

		public void add(Node next)
		{
			switch (this.size)
			{
				case 0:
				{
					this.head = next;
					this.tail = next;
					this.size = 1;

					break;
				}
				case 1:
				{
					this.head.next = next;
					next.prev = this.head;
					this.tail = next;
					this.size = 2;
					break;
				}
				default:
				{
					this.tail.next = next;
					next.prev = this.tail;
					this.tail = next;
					this.size++;

					break;
				}
			}
		}

		public void addAfter(Node before, Instruction in)
		{
			this.addAfter(before, new Node(in));
		}

		public void addBefore(Node before, Node next)
		{
			switch (this.size)
			{
				case 0:
				{
					throw new NullPointerException();
				}
				case 1:
				{
					if (this.head == before)
					{
						this.head.prev = next;
						next.next = this.head;
						this.head = next;
						this.size = 2;
					}
					else
					{
						throw new IllegalArgumentException();
					}

					break;
				}
				default:
				{
					if (before == this.tail)
					{
						this.tail.prev = next;
						next.next = this.tail;
					}
					else
					{
						final Node previous = before.prev;
						previous.next = next;
						next.prev = previous;
						next.next = before;
						before.prev = next;
					}

					this.size++;
					break;
				}
			}
		}

		public void addAfter(Node before, Node next)
		{
			switch (this.size)
			{
				case 0:
				{
					throw new NullPointerException();
				}
				case 1:
				{
					if (this.head == before)
					{
						this.head.next = next;
						next.prev = this.head;
						this.tail = next;
						this.size = 2;
					}
					else
					{
						throw new IllegalArgumentException();
					}

					break;
				}
				default:
				{
					if (before == this.tail)
					{
						this.tail.next = next;
						next.prev = this.tail;
						this.tail = next;
					}
					else
					{
						final Node after = before.next;
						before.next = next;
						next.prev = before;
						next.next = after;
						after.prev = next;
					}

					this.size++;
					break;
				}
			}
		}

		public void remove(Node in)
		{
			switch (this.size)
			{
				case 0:
				{
					throw new NullPointerException();
				}
				case 1:
				{
					if (this.head == in)
					{
						this.head = null;
						this.tail = null;
						this.size = 0;
					}
					else
					{
						throw new IllegalArgumentException();
					}
					break;
				}
				default:
				{
					if (this.head == in)
					{
						this.head = in.next;
						this.head.prev = null;
					}
					else if (this.tail == in)
					{
						this.tail = this.tail.prev;
						this.tail.next = null;
					}
					else
					{
						in.prev.next = in.next;
						in.next.prev = in.prev;
					}

					this.size--;
					break;
				}
			}
		}

		public Instruction[] toArray()
		{
			final Instruction[] result = new Instruction[this.size];
			Node current = this.head;

			for (int i = 0; i < result.length; i++)
			{
				result[i] = current.cargo;
				current = current.next;
			}

			return result;
		}

		private static class Node
		{
			private final Instruction cargo;
			private Node prev;
			private Node next;

			public Node(Instruction inCargo)
			{
				this(null, inCargo, null);
			}

			public Node(Node inPrev, Instruction inCargo, Node inNext)
			{
				if (inCargo == null)
				{
					throw new IllegalArgumentException();
				}

				this.cargo = inCargo;
				this.prev = inPrev;
				this.next = inNext;
			}
		}
	}

	private locList reinsertLabels_WithCulling(SingleLinkedList<Instruction> old, HalfByteRadixMap<Label> labelQueue, int[] lineWeights)
	{
		final locList result = new locList();
		final SingleLinkedList<Label> extras = new SingleLinkedList<Label>();
		final HalfByteRadixMap<locList.Node> firstLinesAfterLabel = new HalfByteRadixMap<locList.Node>();

		for (int i = 0; i < lineWeights.length; i++)
		{
			final Label maybeLabel = labelQueue.get(i);

			if (maybeLabel != null)
			{
				extras.push(maybeLabel);
			}

			if (lineWeights[i] > 0)
			{
				final locList.Node first = new locList.Node(old.pop());

				if (!extras.isEmpty())
				{
					final Label label = consolidateLabels(extras);
					result.add(label);
					firstLinesAfterLabel.put(label.getHalfByteHash(), first);

					
				}

				checkForNegJumps(first, result, firstLinesAfterLabel);
				result.add(first);
				lineWeights[i]--;

				while (lineWeights[i] > 0)
				{
					final locList.Node pez = new locList.Node(old.pop());
					result.add(pez);
					lineWeights[i]--;

					checkForNegJumps(pez, result, firstLinesAfterLabel);
				}
			}
		}

		return result;
	}
	
	private static void checkForNegJumps2(Instruction current, InstructionList all, HalfByteRadixMap<Instruction> firstLinesAfterLabel)
	{
		switch (current.getIrCode())
		{
			case Jump_Straight:
			{
				final Jump_Straight cast = (Jump_Straight) current;

				if (cast.isNegativeJump())
				{
					Instruction test = firstLinesAfterLabel.get(cast.getJumpTo().getHalfByteHash());
					int numPrecedingLines = 0;
					//System.out.println(cast.getJumpTo().getLabelValue() + " maps to " + first.cargo.getVerboseCommentForm());
					boolean isDone = false;

					while (!isDone)
					{
						switch (test.getIrCode())
						{
							case Jump_Conditional:
							{
								final Jump_Branched cast2 = (Jump_Branched) test;
								cast2.setBranchType(BranchTypes.While);
								cast2.setNumPrecedingLines(numPrecedingLines);
								
								if(numPrecedingLines != 0)
								{
									System.err.println("has preceding lines: " + cast2.getVerboseCommentForm().toString());
								}
								isDone = true;
								break;
							}
							case EvalLoop_For:
							case EvalLoop_ForEach:
							{
								isDone = true;
								break;
							}
							default:
							{
								test = test.getNext();
								numPrecedingLines++;
								break;
							}
						}

					}
				}
				break;
			}
			case Jump_Conditional:
			{
				final Jump_Branched cast = (Jump_Branched) current;

				if (cast.isNegative_True())
				{
					//final Jump_Straight dummyJump = Jump_Straight.getInstance(cast.getJump_True());
					final Label jump_True = cast.getJump_True();
					jump_True.incrementNumOpenBracesToAppend();
					final Instruction first = firstLinesAfterLabel.get(jump_True.getHalfByteHash());					
					

					//all.addAfter(current, dummyJump);
					all.remove(current);
					all.addBefore(first, current);
					cast.setBranchType(BranchTypes.DoWhile);
				}

				break;
			}
			case EvalLoop_For:
			default:
			{
				break;
			}
		}
	}

	private static void checkForNegJumps(locList.Node current, locList all, HalfByteRadixMap<locList.Node> firstLinesAfterLabel)
	{
		switch (current.cargo.getIrCode())
		{
			case Jump_Straight:
			{
				final Jump_Straight cast = (Jump_Straight) current.cargo;

				if (cast.isNegativeJump())
				{
					final locList.Node first = firstLinesAfterLabel.get(cast.getJumpTo().getHalfByteHash());

					locList.Node test = first;
					//System.out.println(cast.getJumpTo().getLabelValue() + " maps to " + first.cargo.getVerboseCommentForm());
					boolean isDone = false;

					while (!isDone)
					{
						switch (test.cargo.getIrCode())
						{
							case Jump_Conditional:
							{
								final Jump_Branched cast2 = (Jump_Branched) test.cargo;
								cast2.setBranchType(BranchTypes.While);
								isDone = true;
								break;
							}
							case EvalLoop_For:
							case EvalLoop_ForEach:
							{
								isDone = true;
								break;
							}
							default:
							{
								test = test.next;
								break;
							}
						}

					}
				}
				break;
			}
			case Jump_Conditional:
			{
				final Jump_Branched cast = (Jump_Branched) current.cargo;

				if (cast.isNegative_True())
				{
					final Jump_Straight dummyJump = Jump_Straight.getInstance(cast.getJump_True());
					final locList.Node first = firstLinesAfterLabel.get(cast.getJump_True().getHalfByteHash());

					all.addAfter(current, dummyJump);
					all.remove(current);
					all.addBefore(first, current);
					cast.setBranchType(BranchTypes.DoWhile);
				}

				break;
			}
			case EvalLoop_For:
			default:
			{
				break;
			}
		}
	}

	private static Label consolidateLabels(SingleLinkedList<Label> extras)
	{
		if (extras.getSize() == 1)
		{
			return extras.pop();
		}
		else
		{
			final Label master = extras.pop();

			while (!extras.isEmpty())
			{
				final Label extra = extras.pop();
				System.err.println("Absorbing superfluous label: " + extra.getMangledName());
				master.absorbSuperfluousLabel(extra);
			}

			return master;
		}
	}

	private SingleLinkedList<Instruction> reinsertLabels_NoCulling(SingleLinkedList<Instruction> old, HalfByteRadixMap<Label> labelQueue, int[] lineWeights)
	{
		final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();

		for (int i = 0; i < lineWeights.length; i++)
		{
			final Label maybeLabel = labelQueue.get(i);

			if (maybeLabel != null)
			{
				//System.out.println("adding " + maybeLabel.getLabelValue() + " at line " + i);
				result.add(maybeLabel);
			}

			switch (lineWeights[i])
			{
				case 0:
				{
					result.add(NonOp.getInstance());
					break;
				}
				case 1:
				{
					result.add(old.pop());

					break;
				}
				default:
				{
					while (lineWeights[i] > 0)
					{
						final Instruction pez = old.pop();
						result.add(pez);
						lineWeights[i]--;
					}

					break;
				}
			}
		}

		return result;
	}

	@Override
	protected CharList functionPrototypesToCharList(int offset)
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.rawFunctionPrototypes.length; i++)
		{
			result.addNewLine();
			result.add(this.rawFunctionPrototypes[i].disassemble(offset, false), true);
		}

		return result;
	}

	@Override
	protected CharList disassembleInstructions(int offset)
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.rawInstructions.length;)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.addAsPaddedString(Integer.toString(i), 4, true);
			result.add('\t');
			final int numLinesConsumed = this.translateRawToBytecode(i, offset, result);
			i += numLinesConsumed;
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

			if (!(masked < this.Vars.getLengthOf(VarRepos.Constants)))
			{
				throw new IllegalArgumentException("index: " + index + ", masked: " + masked);
			}

			result.add(this.Vars.requestConstantVarForRead(masked).valueToString());
			result.add(')');
		}
		else
		{
			result.add("Locals[");
			result.addAsString(index);
			result.add(']');
		}
	}

	private int translateRawToBytecode(int lineNum, int offset, CharList result)
	{
		final int line = this.rawInstructions[lineNum];
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
				this.addRkVarIndex(fields[1], result);
				result.add(operator.valueToString());
				this.addRkVarIndex(fields[2], result);
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
				result.add(operator.valueToString());
				this.addRkVarIndex(fields[1], result);
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
				final FunctionObject function = this.rawFunctionPrototypes[fields[1]];
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

						final int pseudo = this.rawInstructions[lineNum + numLinesConsumed];
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
				final String globalVarName = this.Vars.requestConstantVarForRead(fields[1]).valueToString();
				this.Vars.requestVarForRead(globalVarName); // dummy instruction to force Vars to log this as a global var name
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
				this.addRkVarIndex(fields[2], result);
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
				result.add(this.Vars.requestConstantVarForRead(fields[1]).valueToString());
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
				this.addRkVarIndex(fields[2], result);
				result.add(')');
				break;
			}
			case SetGlobal:
			{
				final int[] fields = MiscAutobot.parseRawInstruction(line, ParseTypes.iABx);

				result.add("the global variable '");
				final String globalVarName = this.Vars.requestConstantVarForRead(fields[1]).valueToString();
				this.Vars.requestVarForRead(globalVarName); // dummy instruction to force Vars to log this as a global var name
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
				this.addRkVarIndex(fields[1], result);
				result.add(" is assigned the value ");
				this.addRkVarIndex(fields[2], result);
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
				this.addRkVarIndex(fields[1], result);
				result.add(getRelationalOperator(fields[0], type).valueToString());
				this.addRkVarIndex(fields[2], result);
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

	@Override
	public CharList translateInstructionsToCode(int inOffset, boolean inIncludeGlobals, boolean inWantsCommentedRaws, boolean inShowInferredTypes)
	{
		throw new UnsupportedOperationException();
	}

}
