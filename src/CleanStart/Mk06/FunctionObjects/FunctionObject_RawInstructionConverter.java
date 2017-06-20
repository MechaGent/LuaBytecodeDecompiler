package CleanStart.Mk06.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Instruction.InstructionList_Locked;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.MiscAutobot;
import CleanStart.Mk06.MiscAutobot.ParseTypes;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.StepNum.ForkBehaviors;
import CleanStart.Mk06.SubVars.BoolVar;
import CleanStart.Mk06.SubVars.IntVar;
import CleanStart.Mk06.SubVars.MethodVar;
import CleanStart.Mk06.SubVars.NullVar;
import CleanStart.Mk06.SubVars.TableVar;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Enums.OpCodes;
import CleanStart.Mk06.Enums.RawEnums;
import CleanStart.Mk06.FunctionObjects.CodeBlock.CodeBlockChainFactory;
import CleanStart.Mk06.FunctionObjects.ProcessingData.VarRepos;
import CleanStart.Mk06.Instructions.Call;
import CleanStart.Mk06.Instructions.Concat;
import CleanStart.Mk06.Instructions.CreateNewFunctionInstance;
import CleanStart.Mk06.Instructions.HandleVarArgs;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_Branched.BranchTypes;
import CleanStart.Mk06.Instructions.Jump_ForEachLoop;
import CleanStart.Mk06.Instructions.Jump_ForLoop;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import CleanStart.Mk06.Instructions.NonOp;
import CleanStart.Mk06.Instructions.Return;
import CleanStart.Mk06.Instructions.SetTableElements;
import CleanStart.Mk06.Instructions.SetVar;
import CleanStart.Mk06.Instructions.SetVar_Bulk;
import CleanStart.Mk06.Instructions.MetaInstructions.MetaInstruction;
import CleanStart.Mk06.Instructions.MetaInstructions.MetaIrCodes;
import CleanStart.Mk06.Instructions.MetaInstructions.SimpleIfElse;
import CleanStart.Mk06.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk06.VarFormula.Operators;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class FunctionObject_RawInstructionConverter extends RawFunctionObject
{
	// to prevent instantiation
	private FunctionObject_RawInstructionConverter(RawFunctionObject inIn)
	{
		super(inIn);
	}

	/*
	private static Label getSkipNextStepLabel(HalfByteRadixMap<Label> labelQueue, int rawIndex, StepNum inStartTime)
	{
		return getLabelForJumpOffsetOf(labelQueue, 1, rawIndex, inStartTime);
	}
	
	private static Label getLabelForJumpOffsetOf(HalfByteRadixMap<Label> labelQueue, int offset, int rawIndex, StepNum inStartTime)
	{
		final int absOffset = offset + rawIndex + 1;
		Label result = labelQueue.get(absOffset);
	
		if (result == null)
		{
			result = Label.getInstance(inStartTime, "jumpLabel");
			labelQueue.put(absOffset, result);
		}
	
		return result;
	}
	*/

	private static Operators getRelationalOperator(int varA, OpCodes code)
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

	public static Instruction parseRawInstructions(RefinementStagesChecklist checklist, RawFunctionObject rawObject)
	{
		StepNum currentStep = StepNum.getInitializerStep();
		// ProcessingData data = ProcessingData.getInstance(rawObject, checklist, InstructionList_Locked.getInstance(NonOp.getInstance(currentStep), null));
		// ProcessingData data = ProcessingData.getInstance(rawObject, checklist, InstructionList_Locked.getInstance(Label.getInstance(currentStep, "StartLabel"), null));
		ProcessingData data = ProcessingData.getInstance(rawObject, checklist, InstructionList_Locked.getInstance(Label.getStartLabel(currentStep), null));

		data.setInstructions(parseRawInstructions(StepNum.getInitializerStep(), data));
		// System.out.println("here");

		if (checklist.shouldReinsertLabels())
		{
			data.setInstructions(reinsertLabels(data));
		}

		/*
		if (checklist.shouldDeduceLoops())
		{
			deduceLoops_HandleStraightJumps(data);
			// deduceLoops_HandleBranchedJumps(data);
		}
		
		if (checklist.shouldProcessSimpleIfElseStatements())
		{
			deduceIfs(checklist, data.getFirstInstruction());
			//HandleSimpleIfElseStatements(data);
		}
		*/

		return data.getFirstInstruction();
	}

	private static InstructionList_Locked parseRawInstructions(StepNum currentStep, ProcessingData data)
	{
		Instruction current = data.getFirstInstruction();
		boolean addLocResult = true; // set to false when multiple Instructions need to be added, to enhance readability
		final int limit = data.getNumRawInstructions();

		for (int rawIndex = 0; rawIndex < limit; rawIndex++)
		{
			currentStep = currentStep.fork(ForkBehaviors.IncrementLast);

			data.incrementLineWeight(rawIndex);
			final int currentCode = data.getRawLine(rawIndex);
			final int[] fields;
			final OpCodes rawType = data.getOpcodeForRawLine(rawIndex);
			final Instruction locResult;

			//final CharList diag = new CharList();
			//RawFunctionObjectHelper.translateRawToBytecode(data.getRawObject(), rawIndex, 0, diag);
			//diag.push('\t');
			//diag.pushAsString(rawIndex);
			//System.out.println(diag.toString());

			// System.out.println("on instruction: " + rawIndex + " which is type " + rawType.toString());

			switch (rawType)
			{
				case Add:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Add, currentStep);
					break;
				}
				case ArithmeticNegation:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Neg, currentStep);
					break;
				}
				case Call:
				case TailCall: // hopefully this won't cause any problems
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef subjectObject = data.getVar(VarRepos.Locals, fields[0]);
					final boolean argsAreVariable;
					final VarRef[] args;

					if (fields[1] == 0)
					{
						argsAreVariable = true;

						args = new VarRef[data.getRepoLength(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						argsAreVariable = false;
						args = new VarRef[fields[1] - 1];
					}

					for (int i = 0; i < args.length; i++)
					{
						args[i] = data.getVar(VarRepos.Locals, i + fields[0] + 1);
					}

					final boolean returnsAreVariable;
					final VarRef[] Returns;

					if (fields[2] == 0)
					{
						returnsAreVariable = true;
						Returns = new VarRef[data.getRepoLength(VarRepos.Locals) - fields[0]];
					}
					else
					{
						returnsAreVariable = false;
						Returns = new VarRef[fields[2] - 1];
					}

					for (int i = 0; i < Returns.length; i++)
					{
						Returns[i] = data.getVar(VarRepos.Locals, fields[0] + i);
					}
					
					//locResult = Call.getInstance(currentStep, subjectObject, argsAreVariable, args, returnsAreVariable, Returns);
					
					if (Returns.length == 1)
					{
						final MethodVar callMethod = MethodVar.getInstance("", currentStep, subjectObject, "call", argsAreVariable, args, returnsAreVariable, Returns);
						
						//final VarRef wrap = VarRef.getInstance_forWrapper(callMethod, InferenceTypes.Mystery, InlineSchemes.Inline_IfOnlyReadOnce);
						//locResult = SetVar.getInstance(currentStep, Returns[0], wrap, InlineSchemes.Inline_IfOnlyReadOnce);
						locResult = SetVar.getInstance(currentStep, Returns[0], callMethod, InlineSchemes.Inline_IfOnlyReadOnce);
					}
					else
					{
						locResult = Call.getInstance(currentStep, subjectObject, argsAreVariable, args, returnsAreVariable, Returns);
					}

					break;
				}
				case Close:
				{
					locResult = NonOp.getInstance(currentStep);
					break;
				}
				case Closure:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABx);
					final FunctionObject prototype = data.getFunctionPrototype(fields[1]);
					final VarRef[] upvals = new VarRef[prototype.getUpvalues().length];
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					for (int i = 0; i < upvals.length; i++)
					{
						// final int pseudoraw = rawObject.rawInstructions[++rawIndex];
						final OpCodes opcode = data.getOpcodeForRawLine(++rawIndex);
						// OpCodes.parse(pseudoraw);
						final int[] nextFields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);
						// MiscAutobot.parseRawInstruction(pseudoraw, ParseTypes.iAB);

						switch (opcode)
						{
							case Move:
							{
								upvals[i] = data.getVar(VarRepos.Locals, nextFields[1]);
								break;
							}
							case GetUpvalue:
							{
								upvals[i] = data.getVar(VarRepos.Upvalues, nextFields[1]);
								// rawObject.Upvalues[nextFields[1]];
								break;
							}
							default:
							{
								throw new UnhandledEnumException(opcode);
							}
						}
					}

					locResult = CreateNewFunctionInstance.getInstance(currentStep, prototype, upvals, target);

					break;
				}
				case Concatenation:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);
					final VarRef[] sources = new VarRef[(fields[2] - fields[1]) + 1];

					for (int i = 0; i < sources.length; i++)
					{
						sources[i] = data.getVar(VarRepos.Locals, fields[1] + i);
					}

					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = Concat.getInstance(currentStep, target, sources);
					break;
				}
				case Divide:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Div, currentStep);
					break;
				}
				case GetGlobal:
				{
					locResult = data.composeGlobalGetterOrSetter(rawIndex, currentStep, true);
					break;
				}
				case GetTable:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef sourceTable = data.getVar(VarRepos.Locals, fields[1]);
					final VarRef sourceIndex = data.getRkVar(fields[2]);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance_fromTableElement(currentStep, target, sourceTable, sourceIndex);
					break;
				}
				case GetUpvalue:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					// System.out.println("numLocals: " + data.getRepoLength(VarRepos.Locals));
					final VarRef source = data.getVar(VarRepos.Upvalues, fields[1]);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source);
					break;
				}
				case InitNumericForLoop:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAsBx);

					final VarRef indexVar = data.getVar(VarRepos.Locals, fields[0]++);
					final VarRef limitVar = data.getVar(VarRepos.Locals, fields[0]++);
					final VarRef stepValue = data.getVar(VarRepos.Locals, fields[0]++);
					final VarRef externalIndexVar = data.getVar(VarRepos.Locals, fields[0]++);

					StepNum nextStep = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label jumpTo = data.getSkipNextStepLabel(rawIndex - 1, nextStep);

					nextStep = nextStep.fork(ForkBehaviors.IncrementLast);

					final Label jumpTo_ifFalse = data.getLabelForJumpOffsetOf(fields[1] + 1, rawIndex, nextStep);

					final boolean falseJumpIsNegative = fields[1] < 0;

					locResult = Jump_ForLoop.getInstance(currentStep, jumpTo, false, jumpTo_ifFalse, falseJumpIsNegative, indexVar, limitVar, stepValue, externalIndexVar);

					currentStep = nextStep;
					break;
				}
				case IterateGenericForLoop:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef iteratorFunction = data.getVar(VarRepos.Locals, fields[0]);
					final VarRef state = data.getVar(VarRepos.Locals, fields[0] + 1);
					final VarRef enumIndex = data.getVar(VarRepos.Locals, fields[0] + 2);
					final VarRef[] loopVars = new VarRef[fields[2]];

					for (int i = 0; i < loopVars.length; i++)
					{
						loopVars[i] = data.getVar(VarRepos.Locals, i + fields[0] + 3);
					}

					final int[] nextFields = data.parseFieldsOfRawLineAs(++rawIndex, ParseTypes.isBx);

					StepNum nextStep = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label jumpTo = data.getLabelForJumpOffsetOf(nextFields[0], rawIndex, nextStep);

					nextStep = nextStep.fork(ForkBehaviors.IncrementLast);

					final Label jumpTo_ifFalse = data.getSkipNextStepLabel(rawIndex-1, nextStep);

					locResult = Jump_ForEachLoop.getInstance(currentStep, jumpTo, jumpTo_ifFalse, iteratorFunction, state, enumIndex, loopVars);

					currentStep = nextStep;
					break;
				}
				case IterateNumericForLoop:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.isBx);
					fields[0]--;

					final StepNum nextStep = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label jumpTo = data.getLabelForJumpOffsetOf(fields[0], rawIndex, nextStep);
					final boolean isNegativeJump = fields[0] < 0;
					final Jump_Straight temp = Jump_Straight.getInstance(currentStep, jumpTo, isNegativeJump);
					locResult = temp;
					data.addJump_Straight(temp);

					currentStep = nextStep;
					break;
				}
				case Jump:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.isBx);

					final StepNum nextStep = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label jumpTo = data.getLabelForJumpOffsetOf(fields[0], rawIndex, nextStep);
					final boolean isNegativeJump = fields[0] < 0;
					final Jump_Straight temp = Jump_Straight.getInstance(currentStep, jumpTo, isNegativeJump);
					locResult = temp;
					data.addJump_Straight(temp);
					
					final Label dummy = data.getSkipNextStepLabel(rawIndex-1, nextStep.fork(ForkBehaviors.IncrementLast));

					currentStep = nextStep;
					break;
				}
				case Length:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final VarRef rawSource = data.getVar(VarRepos.Locals, fields[1]);

					final MethodVar LengthOfMethod = MethodVar.getInstance("", currentStep, null, "lengthOf", false, new VarRef[] {
																																	rawSource },
							false, new VarRef[0]);

					final VarRef source = VarRef.getInstance_forWrapper(LengthOfMethod, InferenceTypes.FunctionObject);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source);
					break;
				}
				// LineWeight Manipulator
				case LoadBool:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);
					final BoolVar rawSource = BoolVar.getInstance("<loadBoolRaw>", RawEnums.StaticVar.getValue(), fields[1] != 0);
					final VarRef source = VarRef.getInstance_forWrapper(rawSource, InferenceTypes.Bool);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source);

					if (fields[2] != 0) // skip next instruction
					{
						addLocResult = false;
						currentStep = currentStep.fork(ForkBehaviors.IncrementLast);
						final StepNum nextStep = currentStep.fork(ForkBehaviors.IncrementLast);
						final Label skipLabel = data.getSkipNextStepLabel(rawIndex, nextStep);
						final Jump_Straight jump = Jump_Straight.getInstance(currentStep, skipLabel, false);
						data.addJump_Straight(jump);
						final Label dummy = data.getSkipNextStepLabel(rawIndex-1, nextStep.fork(ForkBehaviors.IncrementLast));

						Instruction.link(current, locResult);
						Instruction.link(locResult, jump);
						current = jump;
						data.incrementLineWeight(rawIndex);
					}
					break;
				}
				case LoadK:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABx);

					final VarRef source = data.getVar(VarRepos.Constants, fields[1]);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source);
					break;
				}
				case LoadNull:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final int length = (fields[1] - fields[0]) + 1;
					final NullVar rawSource = NullVar.getInstance("LoadNull_Raw");
					final VarRef source = VarRef.getInstance_forWrapper(rawSource, InferenceTypes.Null);

					if (length == 1)
					{
						final VarRef target = data.getVar(VarRepos.Locals, fields[0]);
						locResult = SetVar.getInstance(currentStep, target, source);
					}
					else
					{
						final VarRef[] targets = new VarRef[length];

						for (int i = 0; i < length; i++)
						{
							targets[i] = data.getVar(VarRepos.Locals, fields[0] + i);
						}

						locResult = SetVar_Bulk.getInstance(currentStep, source, targets, true);
					}

					break;
				}
				case LogicalNegation:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Logic_Not, currentStep);
					break;
				}
				case Modulo:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Mod, currentStep);
					break;
				}
				case Move:
				{

					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final VarRef source = data.getVar(VarRepos.Locals, fields[1]);
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source);
					// System.out.println("result of parse of MOV: ");
					break;
				}
				case Multiply:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Mul, currentStep);
					break;
				}
				case NewTable:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final TableVar rawSource = TableVar.getInstance("<newTableCreated>", fields[1], fields[2]);
					final VarRef source = VarRef.getInstance_forWrapper(rawSource, InferenceTypes.TableObject); // might need to change the inference type to Mystery

					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					locResult = SetVar.getInstance(currentStep, target, source, InlineSchemes.Inline_IfOnlyReadOnce);
					break;
				}
				case Power:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Pow, currentStep);
					break;
				}
				case Return:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final VarRef[] sources;
					final boolean isVariable = fields[1] == 0;

					if (isVariable)
					{
						// it's a varArg'd return
						sources = new VarRef[data.getRepoLength(VarRepos.Locals) - fields[0]];
					}
					else
					{
						sources = new VarRef[fields[1] - 1];
					}

					for (int i = 0; i < sources.length; i++)
					{
						sources[i] = data.getVar(VarRepos.Locals, fields[0] + i);
					}

					locResult = Return.getInstance(currentStep, sources, isVariable);
					data.getSkipNextStepLabel(rawIndex-1, currentStep.fork(ForkBehaviors.ForkToSubstage));

					break;
				}
				// LineWeight Manipulator
				case Self:
				{
					locResult = null;
					data.incrementLineWeight(rawIndex);
					addLocResult = false;

					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef source_tableFetch = data.getVar(VarRepos.Locals, fields[1]);
					final VarRef target_tableFetch = data.getVar(VarRepos.Locals, fields[0] + 1);

					final VarRef indexer_tableElementFetch = data.getRkVar(fields[2]);
					final VarRef target_tableElementFetch = data.getVar(VarRepos.Locals, fields[0]);

					final SetVar firstPart = SetVar.getInstance(currentStep, target_tableFetch, source_tableFetch);
					currentStep = currentStep.fork(ForkBehaviors.IncrementLast);
					final SetVar secondPart = SetVar.getInstance_fromTableElement(currentStep, target_tableElementFetch, source_tableFetch, indexer_tableElementFetch);

					Instruction.link(current, firstPart);
					Instruction.link(firstPart, secondPart);

					current = secondPart;

					break;
				}
				case SetGlobal:
				{
					locResult = data.composeGlobalGetterOrSetter(rawIndex, currentStep, false);
					break;
				}
				case SetList:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					if (fields[2] == 0)
					{
						fields[2] = data.getRawLine(++rawIndex);
					}

					final int target_startIndex = (fields[2] - 1) * FieldsPerFlush;
					final VarRef target = data.getVar(VarRepos.Locals, fields[0]);

					final int sources_startIndex = fields[0] + 1;
					final VarRef[] sources;

					if (fields[1] == 0)
					{
						sources = new VarRef[data.getRepoLength(VarRepos.Locals) - sources_startIndex];
					}
					else
					{
						sources = new VarRef[fields[1]];
					}

					for (int i = 0; i < sources.length; i++)
					{
						sources[i] = data.getVar(VarRepos.Locals, sources_startIndex + i);
					}

					locResult = SetTableElements.getInstance(currentStep, target, target_startIndex, sources, sources_startIndex, VarRepos.Locals.toString());
					break;
				}
				case SetTable:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef targetTable = data.getVar(VarRepos.Locals, fields[0]);
					final VarRef targetIndex = data.getRkVar(fields[1]);
					final VarRef source = data.getRkVar(fields[2]);

					locResult = SetVar.getInstance_toTableElement(currentStep, targetTable, targetIndex, source);
					break;
				}
				case SetUpvalue:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final VarRef source = data.getVar(VarRepos.Locals, fields[0]);
					final VarRef target = data.getVar(VarRepos.Upvalues, fields[1]);

					locResult = SetVar.getInstance(currentStep, target, source);

					break;
				}
				case Subtract:
				{
					locResult = data.parseRawLineAsMathOp(rawIndex, Operators.Arith_Sub, currentStep);
					break;
				}
				// LineWeight Manipulator
				case TestAndSaveLogicalComparison:
				{
					locResult = null;
					addLocResult = false;

					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final VarRef varA = data.getVar(VarRepos.Locals, fields[0]);
					final VarRef varB = data.getVar(VarRepos.Locals, fields[1]);
					final VarRef varC = VarRef.getInstance_forWrapper(IntVar.getInstance("<testAndSave_raw>", RawEnums.WrapperVar.getValue(), fields[2]), InferenceTypes.Int);

					final FormulaBuilder formy = new FormulaBuilder();

					if (fields[2] == 0)
					{
						formy.add(Operators.Logic_Not);
					}

					formy.add(varB);

					StepNum next = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label ifFalse = data.getSkipNextStepLabel(rawIndex-1, next);
					next = next.fork(ForkBehaviors.IncrementLast);

					// consume and parse next instruction, which will be a jump
					final int[] nextFields = data.parseFieldsOfRawLineAs(++rawIndex, ParseTypes.isBx);
					final Label ifTrue = data.getLabelForJumpOffsetOf(nextFields[0], rawIndex, next);
					next = next.fork(ForkBehaviors.IncrementLast);
					final boolean trueIsNegative = nextFields[0] < 0;

					final Jump_Branched jump = Jump_Branched.getInstance(currentStep, ifTrue, trueIsNegative, formy.build("<tAs>"), ifFalse, false);
					data.addJump_Branched(jump);
					final SetVar setter = SetVar.getInstance(next, varA, varC);
					data.incrementLineWeight(rawIndex);

					Instruction.link(current, jump);
					Instruction.link(jump, setter);

					current = setter;
					currentStep = next;

					break;
				}
				// LineWeight Manipulators
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iABC);

					final FormulaBuilder buildy = new FormulaBuilder();
					buildy.add(data.getRkVar(fields[1]));
					buildy.add(getRelationalOperator(fields[0], rawType));
					buildy.add(data.getRkVar(fields[2]));

					StepNum next = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label ifTrue = data.getSkipNextStepLabel(rawIndex, next);
					next = next.fork(ForkBehaviors.IncrementLast);

					// consume and parse next instruction, which will be a jump
					final int[] nextFields = data.parseFieldsOfRawLineAs(++rawIndex, ParseTypes.isBx);
					final Label ifFalse = data.getLabelForJumpOffsetOf(nextFields[0], rawIndex, next);
					final boolean falseIsNegative = nextFields[0] < 0;

					final Jump_Branched temp = Jump_Branched.getInstance(currentStep, ifTrue, false, buildy.build("aTb"), ifFalse, falseIsNegative);
					data.addJump_Branched(temp);
					locResult = temp;
					currentStep = next;

					break;
				}
				case TestLogicalComparison:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAC);

					final VarRef varA = data.getVar(VarRepos.Locals, fields[0]);
					// final VarRef varC = VarRef.getInstance_forWrapper(IntVar.getInstance("<test_raw>", -1, fields[1]), InferenceTypes.Int);

					final FormulaBuilder buildy = new FormulaBuilder();

					if (fields[1] == 0)
					{
						buildy.add(Operators.Logic_Not);
					}

					buildy.add(varA);

					StepNum next = currentStep.fork(ForkBehaviors.IncrementLast);
					final Label ifTrue = data.getSkipNextStepLabel(rawIndex, next);
					next = next.fork(ForkBehaviors.IncrementLast);

					// consume and parse next instruction, which will be a jump
					final int[] nextFields = data.parseFieldsOfRawLineAs(++rawIndex, ParseTypes.isBx);
					final Label ifFalse = data.getLabelForJumpOffsetOf(nextFields[0], rawIndex, next);
					final boolean FalseIsNegative = nextFields[0] < 0;

					final Jump_Branched temp = Jump_Branched.getInstance(currentStep, ifTrue, false, buildy.build("t"), ifFalse, FalseIsNegative);
					data.addJump_Branched(temp);
					locResult = temp;
					currentStep = next;
					break;
				}
				case VarArg:
				{
					fields = data.parseFieldsOfRawLineAs(rawIndex, ParseTypes.iAB);

					final boolean copyAllPossible;
					final VarRef[] targets;

					if (fields[1] == 0)
					{
						copyAllPossible = true;

						// TODO: okay, I *think* this logic is correct
						targets = new VarRef[data.getRepoLength(VarRepos.Locals) - (fields[0] + 1)];
					}
					else
					{
						copyAllPossible = false;
						targets = new VarRef[fields[1] - 1];
					}

					for (int i = 0; i < targets.length; i++)
					{
						targets[i] = data.getVar(VarRepos.Locals, fields[0] + i);
					}

					locResult = HandleVarArgs.getInstance(currentStep, copyAllPossible, targets, fields[0]);

					break;
				}
				default:
				{
					throw new UnhandledEnumException(rawType);
				}
			}

			if (addLocResult)
			{
				Instruction.link(current, locResult);
				current = locResult;
			}
			else
			{
				addLocResult = true;
			}

			//System.out.println("done with cycle");
		}

		// System.out.println("done translating");
		return InstructionList_Locked.getInstance(data.getFirstInstruction(), current);
	}

	private static InstructionList_Locked reinsertLabels(ProcessingData data)
	{
		Instruction curTail = data.getFirstInstruction(); // first should be a NonOp
		int absOffset = 0;

		for (; absOffset < data.getNumRawInstructions(); absOffset++)
		{
			final Label maybe = data.getLabelFromQueue(absOffset);

			if (maybe != null)
			{
				Instruction.insertAhead(curTail, maybe);
				curTail = maybe;
			}

			int lineWeight = data.getLineWeight(absOffset);

			while (lineWeight > 0)
			{
				curTail = curTail.getLiteralNext();
				lineWeight--;
			}
		}

		// return deduceLoops(checklist, first);
		return InstructionList_Locked.getInstance(data.getFirstInstruction(), curTail);
	}

	/**
	 * unhooks loop innards from literal chain, and hooks them into appropriate spots for their over-arcing control structure
	 * 
	 * @param checklist
	 * @param first
	 * @return
	 */
	public static Instruction deduceIfs(RefinementStagesChecklist checklist, Instruction first)
	{

		if (checklist.shouldProcessSimpleIfElseStatements())
		{
			// System.out.println("triggering 'deduceIfs()'");
			final CodeBlockChainFactory factory = CodeBlockChainFactory.getInstance(first);
			factory.buildChain();
		}

		return first;
	}

	private static void deduceLoops_HandleStraightJumps(ProcessingData data)
	{
		for (Jump_Straight current : data.getJumps_Straight())
		{
			if (current.isNegativeJump())
			{
				final Instruction preambleStart = current.getJumpTo().getLiteralNext();
				Instruction preamble = preambleStart;

				int preambleSize = 0;
				boolean isDone = false;

				while (!isDone)
				{
					switch (preamble.getIrCode())
					{
						case Jump_Branched:
						{
							final Jump_Branched cast = (Jump_Branched) preamble;
							cast.setBranchType(BranchTypes.While);

							if (preambleSize != 0)
							{
								cast.setPrecedingLines(InstructionList_Locked.getInstance(preambleStart, preamble.getLiteralPrev()));

								Instruction.link(preambleStart.getLiteralPrev(), cast);
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
							preamble = preamble.getLiteralNext();
							preambleSize++;
							break;
						}
					}
				}
			}
		}
	}

	private static void deduceLoops_HandleBranchedJumps(ProcessingData data)
	{
		/*
		for (Jump_Branched current : data.getJumps_Branched())
		{
			helper_handleSimpleIfElseStatement(current);
		}
		*/
	}

	private static void HandleSimpleIfElseStatements(ProcessingData data)
	{
		Instruction current = data.getFirstInstruction();
		System.out.println("starting here");

		while (current != null)
		{
			switch (current.getIrCode())
			{
				case Jump_Branched:
				{
					current = helper_handleSimpleIfElseStatement((Jump_Branched) current, false);
					current = current.getLiteralNext();
					break;
				}
				default:
				{
					current = current.getLiteralNext();
					break;
				}
			}

			// System.out.println("loopcheck");
		}
		System.out.println("ending here");
	}

	private static SimpleIfElse helper_handleSimpleIfElseStatement(Jump_Branched current, boolean isNested)
	{
		final SimpleIfElse result;
		final Label IfTrue = current.getJumpTo();
		final Label IfFalse = current.getJumpTo_ifFalse();

		final Instruction checked = checkRangeForSimpleIfs(IfTrue, IfFalse.getLiteralPrev());
		final Instruction FalsePrev;
		FalsePrev = IfFalse.getLiteralPrev();

		/*
		if(checked != null)
		{
			FalsePrev = checked;
		}
		else
		{
			FalsePrev = IfFalse.getLiteralPrev();
		}
		*/

		if (FalsePrev.getIrCode() == IrCodes.Jump_Straight)
		{
			final Jump_Straight cast = (Jump_Straight) FalsePrev;

			if (!cast.isNegativeJump())
			{
				// cast is an If/Else statement

				// System.out.println("literalNext: " + cast.getJumpTo().getMangledName(false));
				// checkRangeForSimpleIfs(IfTrue, cast);

				if (cast.getJumpTo().getIncomingCounter() <= 1)
				{
					final Instruction last = checkRangeForSimpleIfs(IfFalse, cast.getJumpTo());
					result = SimpleIfElse.getInstance(current.getStartTime(), current.getTest(), IfTrue, IfFalse, current.getLiteralPrev(), cast.getJumpTo());
				}
				else
				{
					result = SimpleIfElse.getInstance(current.getStartTime(), current.getTest(), IfTrue, null, current.getLiteralPrev(), null);
					cast.getJumpTo().decrementIncomingCounter();
				}
			}
			else
			{
				result = SimpleIfElse.getInstance(current.getStartTime(), current.getTest(), IfTrue, null, current.getLiteralPrev(), IfFalse);
				cast.getJumpTo().decrementIncomingCounter();
				System.out.println("negJump: " + cast.getMangledName(false));
			}
		}
		else
		{
			// cast is an If statement

			result = SimpleIfElse.getInstance(current.getStartTime(), current.getTest(), IfTrue, null, current.getLiteralPrev(), IfFalse);
		}

		return result;
	}

	/**
	 * will never need to worry about start and stop changing, as start will always be Label, and stop isn't touched
	 * 
	 * @param start
	 * @param stop
	 */
	private static Instruction checkRangeForSimpleIfs(Instruction start, Instruction stop)
	{
		Instruction temp = start;
		boolean lastOne = true;
		boolean isDone = false;

		while (temp != null && !isDone)
		{
			if (temp == stop)
			{
				break;
			}

			switch (temp.getIrCode())
			{
				case Jump_Branched:
				{
					final Instruction next = helper_handleSimpleIfElseStatement((Jump_Branched) temp, true);

					if (next != null)
					{
						// Instruction.link(temp.getLiteralPrev(), next);

						temp = next;// .getLiteralNext();
					}
					else
					{
						temp = temp.getLiteralNext();
					}
					break;
				}
				case Jump_Straight:
				{
					isDone = true;
					break;
				}
				case MetaOp:
				{
					final MetaInstruction cast1 = (MetaInstruction) temp;

					if (cast1.getMetaIrCode() == MetaIrCodes.SimpleIfElse)
					{

					}
					break;
				}
				default:
				{
					temp = temp.getLiteralNext();
					break;
				}
			}
		}

		if (temp != null)
		{
			System.out.println(temp.getStartTime().getInDiagForm().toString());
		}

		Instruction.link(temp, null);
		return temp;
	}

	private static void detectNestedIfs(Jump_Branched current, int scope)
	{
		final Label IfTrue = current.getJumpTo();
		final Label IfFalse = current.getJumpTo_ifFalse();
		final Instruction IfFalse_LiteralPrev = IfFalse.getLiteralPrev();

		switch (IfFalse_LiteralPrev.getIrCode())
		{
			case Return_Concretely:
			case Return_Variably:
			{
				// TODO
				break;
			}
			case Jump_Straight:
			{
				// possible elselessIf
				final Jump_Straight cast1 = (Jump_Straight) IfFalse_LiteralPrev;
				final Label dest = cast1.getJumpTo();

				if (dest.scopeIsUnset())
				{
					// this is an ifElse
					dest.setScope(scope);
				}
				else
				{
					// this may be a nested elselessIf
				}
				break;
			}
			default:
			{
				break;
			}
		}
	}

	private static void ProofOfConcept_NonNestedIfs(Jump_Branched current)
	{
		final Label IfTrue = current.getJumpTo();
		final Label IfFalse = current.getJumpTo_ifFalse();

		final Instruction arg_IfTrue;
		final Instruction arg_IfFalse;
		final Instruction LiteralNext;

		if (IfTrue.compareTo(IfFalse) < 0)
		{
			final Instruction FalsePrev = IfFalse.getLiteralPrev();

			if (FalsePrev.getIrCode() == IrCodes.Jump_Straight)
			{
				// cast is an If/Else statement
				arg_IfTrue = IfTrue;
				arg_IfFalse = IfFalse;
				LiteralNext = ((Jump_Straight) FalsePrev).getJumpTo();
			}
			else
			{
				// cast is an If statement
				arg_IfTrue = IfTrue;
				arg_IfFalse = null;
				LiteralNext = IfFalse;
			}
		}
		else
		{
			throw new IllegalArgumentException();
			/*
			final Instruction TruePrev = IfTrue.getLiteralPrev();
			
			if(TruePrev.getIrCode() == IrCodes.Jump_Straight)
			{
				// cast is an If/Else statement
				throw new IllegalArgumentException();
			}
			else
			{
				// cast is an Else statement
				arg_IfTrue = null;
				arg_IfFalse = IfFalse;
				LiteralNext = IfTrue;
			}
			*/
		}

		final SimpleIfElse result = SimpleIfElse.getInstance(current.getStartTime(), current.getTest(), arg_IfTrue, arg_IfFalse, current.getLiteralPrev(), LiteralNext);
	}
}
