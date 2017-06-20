package CleanStart.Mk01;

import java.util.TreeMap;

import CharList.CharList;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Enums.OpCodes;
import CleanStart.Mk01.Instructions.CallFunction;
import CleanStart.Mk01.Instructions.Concat;
import CleanStart.Mk01.Instructions.CopyArray;
import CleanStart.Mk01.Instructions.CreateNewTable;
import CleanStart.Mk01.Instructions.ForLoop;
import CleanStart.Mk01.Instructions.ForPrep;
import CleanStart.Mk01.Instructions.HandleVarArgs;
import CleanStart.Mk01.Instructions.InstantiateFunction;
import CleanStart.Mk01.Instructions.IterateGenericForLoop;
import CleanStart.Mk01.Instructions.JumpBranched;
import CleanStart.Mk01.Instructions.JumpLabel;
import CleanStart.Mk01.Instructions.JumpStraight;
import CleanStart.Mk01.Instructions.LengthOf;
import CleanStart.Mk01.Instructions.MathOp;
import CleanStart.Mk01.Instructions.NonOp;
import CleanStart.Mk01.Instructions.Return_Concretely;
import CleanStart.Mk01.Instructions.Return_Variably;
import CleanStart.Mk01.Instructions.SetVar;
import CleanStart.Mk01.Instructions.SetVar_Bulk;
import CleanStart.Mk01.Instructions.TailCall;
import CleanStart.Mk01.Interfaces.Instruction;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk01.VarFormula.Operators;
import CleanStart.Mk01.Variables.Var_Bool;
import CleanStart.Mk01.Variables.Var_Int;
import CleanStart.Mk01.Variables.Var_Ref;
import CleanStart.Mk01.Variables.Var_String;
import CleanStart.Mk01.Variables.Var_Table;
import CleanStart.Mk01.Variables.Var_TableLookup;
import CleanStart.Mk01.Variables.Var_TableLookupInstanced;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import CleanStart.Mk01.BaseVariable.Var_Null;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class InstructionTranslator
{
	private static final boolean shouldCullVariables = false;
	
	private static final int RK_Mask = 0x100;
	private static final int FieldsPerFlush = 50;
	//private static final StepStage startTime = StepStage.getNullStepStage();
	private static final HalfByteRadixMap<Var_Ref> GlobalVars = new HalfByteRadixMap<Var_Ref>();
	private static final StepStage GlobalVarsInitTime = StepStage.getNullStepStage();

	private static int jumpLabelInstanceCounter = 0;

	private final Variable[] Constants;
	private final FunctionObject[] functionPrototypes;
	private final Var_Ref[] Locals;
	private final Var_Ref[] Upvalues;
	private final TreeMap<Integer, JumpLabelData> jumps;

	public InstructionTranslator(Variable[] inConstants, FunctionObject[] inFunctionPrototypes, Var_Ref[] inLocals, Var_Ref[] inUpvalues)
	{
		this.Constants = inConstants;
		this.functionPrototypes = inFunctionPrototypes;
		this.Locals = inLocals;
		this.Upvalues = inUpvalues;
		this.jumps = new TreeMap<Integer, JumpLabelData>();
	}

	private Variable processRK(int index)
	{
		final int compare = index & RK_Mask;
		final Variable result;

		if (compare == RK_Mask)
		{
			result = this.Constants[index & ~RK_Mask];
		}
		else
		{
			result = this.Locals[index];
		}

		return result;
	}

	/**
	 * if this is functioning as envisioned, will eliminate middleman jumps, which should have the added effect of compensating for the missing jumps that are folded into the conditionals
	 * 
	 * @param currentPlace
	 * @param relativeOffset
	 * @param raws
	 * @return
	 */
	private JumpLabelData getLabelForJumpTo(int currentPlace, int relativeOffset, int[] raws)
	{
		return this.getLabelForJumpTo(currentPlace, relativeOffset, raws, true);
	}

	private JumpLabelData getLabelForJumpTo(int currentPlace, int relativeOffset, int[] raws, boolean updateFurthestPoint)
	{
		final int absoluteOffset = currentPlace + relativeOffset + 1;
		final int destination = raws[absoluteOffset];
		JumpLabelData label = this.jumps.get(absoluteOffset);
		// int state = 0;

		if (label == null)
		{
			if (OpCodes.parse(destination) == OpCodes.Jump)
			{
				final int[] fields = MiscAutobot.parseAs_iAsBx(destination, 2);
				// System.out.println("currentPlace: " + absoluteOffset + ", relativeOffset: " + fields[1]);
				label = this.getLabelForJumpTo(absoluteOffset, fields[1], raws);

				if (updateFurthestPoint)
				{
					label.updateFurthest(absoluteOffset);
				}
				// state = 1;
			}
			else
			{
				label = new JumpLabelData(new JumpLabel("JumpLabel_" + jumpLabelInstanceCounter++), absoluteOffset);
				// state = 2;
			}

			this.jumps.put(absoluteOffset, label);
		}
		else
		{
			if (updateFurthestPoint)
			{
				label.updateFurthest(absoluteOffset);
			}
		}

		// System.out.println("returning " + label.getLabel() + " for absoluteOffset of " + absoluteOffset + " and state of " + state);

		return label;
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

	private void test_printRawsWithJumps(int[] raws)
	{
		final CharList result = new CharList();

		for (int i = 0; i < raws.length; i++)
		{
			final JumpLabel maybeJump = this.jumps.get(i).label;

			if (maybeJump != null)
			{
				result.addNewLine();
				result.add(maybeJump.translateIntoCommentedByteCode(0), true);
			}

			result.addNewLine();
			result.add(OpCodes.parse(raws[i]).toString());
		}

		System.out.println(result.toString());
	}

	private static JumpLabelData consolidateJumps(SingleLinkedList<JumpLabelData> in)
	{
		if (in.getSize() == 1)
		{
			return in.pop();
		}
		else
		{

			// System.out.println("here");
			final JumpLabelData master = in.pop();

			while (!in.isEmpty())
			{
				final JumpLabelData sacrifice = in.pop();
				System.out.println("sacrificed: " + sacrifice.label.translateIntoCommentedByteCode(0).toString() + " to " + master.label.getLabel());
				master.label.absorbSuperfluousJumpLabel(sacrifice.label);
			}

			return master;
		}
	}

	private final Instruction[] insertAllJumpLabels(SingleLinkedList<Instruction> pass1, int[] numTransformed, boolean shouldCull)
	{
		if(shouldCull)
		{
			return this.insertAllJumpLabels(pass1, numTransformed);
		}
		else
		{
			return this.insertAllJumpLabels_noCulling(pass1, numTransformed);
		}
	}
	
	private final Instruction[] insertAllJumpLabels_noCulling(SingleLinkedList<Instruction> pass1, int[] numTransformed)
	{
		final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();

		for (int i = 0; i < numTransformed.length; i++)
		{
			final JumpLabelData maybeJump = this.jumps.get(i);

			if (maybeJump != null)
			{
				result.add(maybeJump.label);
			}

			switch (numTransformed[i])
			{
				case 0:
				{
					result.add(NonOp.getSharedInstance());
					break;
				}
				case 1:
				{
					result.add(pass1.pop());
					break;
				}
				default:
				{
					while (numTransformed[i] > 0)
					{
						final Instruction pez = pass1.pop();

						if (pez == null)
						{
							throw new IllegalArgumentException("alleged remaining: " + numTransformed[i] + " at pos: " + i + " of " + numTransformed.length);
						}

						if (pez.getIrCode() == IrCodes.Jump_Label)
						{
							throw new IllegalArgumentException();
						}

						result.add(pez);
						numTransformed[i]--;
					}
					break;
				}
			}
		}

		return result.toArray(new Instruction[result.getSize()]);
	}

	private final Instruction[] insertAllJumpLabels(SingleLinkedList<Instruction> pass1, int[] numTransformed)
	{
		final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();
		final SingleLinkedList<JumpLabelData> extras = new SingleLinkedList<JumpLabelData>();

		for (int i = 0; i < numTransformed.length; i++)
		{
			final JumpLabelData maybeJump = this.jumps.get(i);

			if (maybeJump != null && i == maybeJump.furthestUse)
			{
				extras.push(maybeJump);
			}

			switch (numTransformed[i])
			{
				case 0:
				{
					// pass1.pop();
					break;
				}
				case 1:
				{
					if (!extras.isEmpty())
					{
						// System.out.println("here");
						result.add(consolidateJumps(extras).label);
					}

					result.add(pass1.pop());
					break;
				}
				default:
				{
					if (!extras.isEmpty())
					{
						result.add(consolidateJumps(extras).label);
					}

					while (numTransformed[i] > 0)
					{
						final Instruction pez = pass1.pop();

						if (pez == null)
						{
							throw new IllegalArgumentException("alleged remaining: " + numTransformed[i] + " at pos: " + i + " of " + numTransformed.length);
						}

						if (pez.getIrCode() == IrCodes.Jump_Label)
						{
							throw new IllegalArgumentException();
						}

						result.add(pez);
						numTransformed[i]--;
					}
					break;
				}
			}
		}

		return result.toArray(new Instruction[result.getSize()]);
	}

	public Instruction[] parseNextInstructionArray(int[] raws)
	{
		final SingleLinkedList<Instruction> pass1 = new SingleLinkedList<Instruction>();
		// final TreeMap<Integer, JumpLabel> jumps = new TreeMap<Integer, JumpLabel>();
		final int[] numTransformed = new int[raws.length];
		int line = 0;
		int subLine = 0;
		int stepNum = 0;

		for (int place = 0; place < raws.length; place++)
		{
			numTransformed[place]++;
			final int first = raws[place];
			final OpCodes type = OpCodes.parse(first & 0x3f);
			// System.out.println("parsing instruction of type: " + type.toString() + ": " + Integer.toBinaryString(first));
			final Instruction locResult;

			switch (type)
			{
				case Add:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Add, operand2);
					break;
				}
				case ArithmeticNegation:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Neg, operand2);
					break;
				}
				case Call:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref startingFunction = this.Locals[fields[0]];
					final boolean ArgsAreVariable;
					final Var_Ref[] Args;

					if (fields[1] == 0)
					{
						ArgsAreVariable = true;
						Args = new Var_Ref[this.Locals.length - (fields[0] + 1)]; // this is meant to simulate the R(A+1)-to-TopOfStack range
					}
					else
					{
						ArgsAreVariable = false;
						Args = new Var_Ref[fields[1] - 1];
					}

					for (int i = 0; i < Args.length; i++)
					{
						Args[i] = this.Locals[i + fields[0] + 1];
					}

					final boolean ReturnsAreVariable;
					final Var_Ref[] Returns;

					if (fields[2] == 0)
					{
						ReturnsAreVariable = true;
						Returns = new Var_Ref[0];
					}
					else
					{
						ReturnsAreVariable = false;
						Returns = new Var_Ref[fields[2] - 1];
					}
					
					for(int i = 0; i < Returns.length; i++)
					{
						Returns[i] = this.Locals[(fields[2] - 1) + i];
					}

					locResult = CallFunction.getInstance(line, subLine, stepNum, startingFunction, ArgsAreVariable, Args, ReturnsAreVariable, Returns);

					break;
				}
				case Close:
				{
					// numTransformed[place]--;
					locResult = NonOp.getSharedInstance();
					break;
				}
				case Closure:
				{
					// remember to consume the pseudo-instructions
					final int[] fields = MiscAutobot.parseAs_iABx(first, 2);
					final FunctionObject targetFunction = this.functionPrototypes[fields[1]];
					final Var_Ref targetForOutput = this.Locals[fields[0]];
					final Var_Ref[] upvalues = targetFunction.getUpvalues();
					// numTransformed[place] += upvalues.length;

					for (int i = 0; i < upvalues.length; i++)
					{
						final int pseudoRaw = raws[++place];
						// numTransformed[place]--;
						final OpCodes code = OpCodes.parse(pseudoRaw);
						final int[] nextFields = MiscAutobot.parseAs_iABC(pseudoRaw, 2);

						switch (code)
						{
							case Move:
							{
								upvalues[i] = this.Locals[nextFields[1]];
								break;
							}
							case GetUpvalue:
							{
								upvalues[i] = this.Upvalues[nextFields[1]];
								break;
							}
							default:
							{
								throw new UnhandledEnumException(code);
							}
						}
					}

					locResult = InstantiateFunction.getInstance(line, subLine, stepNum, targetForOutput, targetFunction, upvalues);

					break;
				}
				case Concatenation:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref target = this.Locals[fields[0]];
					final Var_Ref[] sources = new Var_Ref[(fields[2] - fields[1]) + 1];

					for (int i = fields[1]; i <= fields[2]; i++)
					{
						sources[i - fields[1]] = this.Locals[i];
					}

					locResult = Concat.getInstance(line, subLine, stepNum, target, sources);

					break;
				}
				case Divide:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Div, operand2);
					break;
				}
				case GetGlobal:
				{
					/*
					 * transform into a SetVar
					 */

					final int[] fields = MiscAutobot.parseAs_iABx(first, 2);

					final Var_Ref target = this.Locals[fields[0]];

					final Var_String sourceIndex = (Var_String) this.Constants[fields[1]];
					Var_Ref source = GlobalVars.get(sourceIndex.getCargo());

					if (source == null)
					{
						source = new Var_Ref("GlobalVars." + sourceIndex.getCargo(), GlobalVarsInitTime, new Var_Null(GlobalVarsInitTime));
						GlobalVars.put(sourceIndex.getCargo(), source);
					}

					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);

					break;
				}
				case GetTable:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref target = this.Locals[fields[0]];
					final Var_Ref sourceTable = this.Locals[fields[1]];
					final Variable sourceIndex = this.processRK(fields[2]);

					final Var_TableLookupInstanced combo = new Var_TableLookupInstanced("instanceOfTableLookup_", new StepStage(line, subLine - 1, stepNum), sourceTable, sourceIndex);
					locResult = SetVar.getInstance(line, subLine, stepNum, target, combo);

					break;
				}
				case GetUpvalue:
				{
					/*
					 * transform into a SetVar
					 */

					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final Var_Ref target = this.Locals[fields[0]];
					// System.out.println("numUpvalues: " + this.Upvalues.length);
					final Variable source = this.Upvalues[fields[1]];

					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);
					break;
				}
				case InitNumericForLoop:
				{
					final int[] fields = MiscAutobot.parseAs_iAsBx(first, 2);
					final Var_Ref indexVar = this.Locals[fields[0]];
					final Var_Ref limitVar = this.Locals[fields[0] + 1];
					final Var_Ref stepValue = this.Locals[fields[0] + 2];
					final Var_Ref externalIndexVar = this.Locals[fields[0] + 3];
					final JumpLabel toOtherEnd = this.getLabelForJumpTo(place, fields[1], raws).label;
					final JumpLabel toAfterEnd = this.getLabelForJumpTo(place, fields[1] + 1, raws).label;

					locResult = ForPrep.getInstance(line, subLine, stepNum, indexVar, limitVar, stepValue, externalIndexVar, toOtherEnd, toAfterEnd);

					break;
				}
				case IterateGenericForLoop:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref iteratorFunction = this.Locals[fields[0]];
					final Var_Ref state = this.Locals[fields[0] + 1];
					final Var_Ref enumIndex = this.Locals[fields[0] + 2];
					final Var_Ref[] loopVars = new Var_Ref[fields[2]];

					for (int i = 0; i < fields[2]; i++)
					{
						loopVars[i] = this.Locals[fields[0] + 3 + i];
					}

					// numTransformed[place];
					final int[] nextFields = MiscAutobot.parseAs_iAsBx(raws[++place], 2);
					// numTransformed[place]--;

					final JumpLabel toOtherEnd = this.getLabelForJumpTo(place, nextFields[1], raws).label;
					final JumpLabel toAfterEnd = this.getLabelForJumpTo(place, 0, raws).label;

					locResult = IterateGenericForLoop.getInstance(line, subLine, stepNum, iteratorFunction, state, enumIndex, loopVars, toOtherEnd, toAfterEnd);

					break;
				}
				case IterateNumericForLoop:
				{
					final int[] fields = MiscAutobot.parseAs_iAsBx(first, 2);
					final Var_Ref indexVar = this.Locals[fields[0]];
					final Var_Ref limitVar = this.Locals[fields[0] + 1];
					final Var_Ref stepValue = this.Locals[fields[0] + 2];
					final Var_Ref externalIndexVar = this.Locals[fields[0] + 3];
					final JumpLabel toOtherEnd = this.getLabelForJumpTo(place, fields[1], raws).label;
					final JumpLabel toAfterEnd = this.getLabelForJumpTo(place, 0, raws).label;

					locResult = ForLoop.getInstance(line, subLine, stepNum, indexVar, limitVar, stepValue, externalIndexVar, toOtherEnd, toAfterEnd);

					break;
				}
				case Jump:
				{
					final int[] fields = MiscAutobot.parseAs_iAsBx(first, 2);
					final JumpLabel label = this.getLabelForJumpTo(place, fields[1], raws).label;
					final JumpStraight locLocResult = JumpStraight.getInstance(line, subLine, stepNum, label);
					locResult = locLocResult;
					label.addIncomingJump(locLocResult, 0);

					break;
				}
				case Length:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final Var_Ref target = this.Locals[fields[0]];
					final Variable source = this.Locals[fields[1]];

					locResult = LengthOf.getInstance(line, subLine, stepNum, target, source);
					break;
				}
				case LoadBool:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref target = this.Locals[fields[0]];
					final Variable source = Var_Bool.getInstance(fields[1] != 0);
					final SetVar temp = SetVar.getInstance(line, subLine, stepNum, target, source);

					if (fields[2] != 0)
					{
						// skip next instruction
						final JumpLabel label = this.getLabelForJumpTo(place, 1, raws).label;
						final JumpStraight extra = JumpStraight.getInstance(line, subLine + 1, stepNum, label);
						label.addIncomingJump(extra, 0);
						pass1.add(temp);
						numTransformed[place]++;
						locResult = extra;
					}
					else
					{
						locResult = temp;
					}

					break;
				}
				case LoadK:
				{
					final int[] fields = MiscAutobot.parseAs_iABx(first, 2);
					final Var_Ref target = this.Locals[fields[0]];
					final Variable source = this.Constants[fields[1]];
					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);

					break;
				}
				case LoadNull:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final int length = fields[1] - fields[0];
					final Variable source = new Var_Null(new StepStage(line, subLine - 1, stepNum));

					if (length == 0)
					{
						final Var_Ref target = this.Locals[fields[0]];
						locResult = SetVar.getInstance(line, subLine, stepNum, target, source);
					}
					else
					{
						final Var_Ref[] targets = new Var_Ref[length + 1];

						for (int i = 0; i < targets.length; i++)
						{
							targets[i] = this.Locals[i + fields[0]];
						}

						locResult = SetVar_Bulk.getInstance(line, subLine, stepNum, targets, source);
					}

					break;
				}
				case LogicalNegation:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, Operators.Logic_Not, operand1);
					break;
				}
				case Modulo:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Mod, operand2);
					break;
				}
				case Move:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final Var_Ref target = this.Locals[fields[0]];
					final Variable source = this.Locals[fields[1]];
					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);

					break;
				}
				case Multiply:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Mul, operand2);
					break;
				}
				case NewTable:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref target = this.Locals[fields[0]];
					final Var_Table source = new Var_Table((int) MiscAutobot.parseAsFloatingByte(fields[1]), (int) MiscAutobot.parseAsFloatingByte(fields[1]), new StepStage(line, subLine - 1, stepNum));

					locResult = CreateNewTable.getInstance(line, subLine, stepNum, target, source);
					break;
				}
				case NoOp:
				{
					throw new IllegalArgumentException();
				}
				case Power:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Pow, operand2);
					break;
				}
				case Return:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);

					if (fields[1] == 0)
					{
						// varArgs
						final Var_Ref[] sources = new Var_Ref[this.Locals.length - fields[0]];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Locals[i + fields[0]];
						}

						locResult = Return_Variably.getInstance(line, subLine, stepNum, sources, this.Locals[this.Locals.length - 1]);
					}
					else
					{
						final Var_Ref[] sources = new Var_Ref[fields[1] - 1];

						for (int i = 0; i < sources.length; i++)
						{
							sources[i] = this.Locals[i + fields[0]];
						}

						locResult = Return_Concretely.getInstance(line, subLine, stepNum, sources);
					}

					break;
				}
				case Self:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref sourceTable = this.Locals[fields[1]];
					final Variable indexer = this.processRK(fields[2]);
					final Var_Ref tableTarget = this.Locals[fields[0] + 1];
					final Var_Ref elementTarget = this.Locals[fields[0]];
					final Var_TableLookup sourceElement = new Var_TableLookup("LookupInto:" + sourceTable.getMangledName(), new StepStage(line, subLine + 1, stepNum), sourceTable, indexer);
					
					final SetVar movTable = SetVar.getInstance(line, subLine, stepNum, tableTarget, sourceTable);
					final SetVar movTableElement = SetVar.getInstance(line, subLine + 1, stepNum, elementTarget, sourceElement);

					pass1.add(movTable);
					numTransformed[place]++;
					locResult = movTableElement;

					break;
				}
				case SetGlobal:
				{
					final int[] fields = MiscAutobot.parseAs_iABx(first, 2);
					final Var_Ref source = this.Locals[fields[0]];
					final String key = ((Var_String) this.Constants[fields[1]]).getCargo();
					Var_Ref target = GlobalVars.get(key);

					if (target == null)
					{
						target = new Var_Ref("GlobalVars." + key, GlobalVarsInitTime, new Var_Null(GlobalVarsInitTime));
						GlobalVars.put(key, target);
					}

					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);

					break;
				}
				case SetList:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref targetTable = this.Locals[fields[0]];
					final Var_Ref[] sourceArray = new Var_Ref[fields[1]];

					for (int i = 0; i < sourceArray.length; i++)
					{
						sourceArray[i] = this.Locals[fields[0] + i + 1];
					}

					final int offsetOffset = (fields[2] - 1) * FieldsPerFlush;

					locResult = CopyArray.getInstance(line, subLine, stepNum, offsetOffset, targetTable, sourceArray);
					break;
				}
				case SetTable:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref targetTable = this.Locals[fields[0]];
					final Variable indexer = this.processRK(fields[1]);
					final Var_TableLookup targetElement = new Var_TableLookup("LookupInto:" + targetTable.getMangledName(), new StepStage(line, subLine - 1, stepNum), targetTable, indexer);
					final Variable source = this.processRK(fields[2]);

					locResult = SetVar.getInstance(line, subLine, stepNum, targetElement, source);
					break;
				}
				case SetUpvalue:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final Var_Ref target = this.Upvalues[fields[1]];
					final Variable source = this.Locals[fields[0]];
					locResult = SetVar.getInstance(line, subLine, stepNum, target, source);
					break;
				}
				case Subtract:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);

					final Var_Ref target = this.Locals[fields[0]];
					final Variable operand1 = this.processRK(fields[1]);
					final Variable operand2 = this.processRK(fields[2]);

					locResult = MathOp.generateMathOp(line, subLine, stepNum, target, operand1, Operators.Arith_Sub, operand2);
					break;
				}
				case TailCall:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final Var_Ref functionObject = this.Locals[fields[0]];
					final Var_Ref[] Args;
					final boolean argsAreVariable;

					if (fields[1] == 0)
					{
						argsAreVariable = true;
						Args = new Var_Ref[this.Locals.length - (fields[0] - 1)];
					}
					else
					{
						argsAreVariable = false;
						Args = new Var_Ref[fields[1] - 1];
					}

					for (int i = 0; i < Args.length; i++)
					{
						Args[i] = this.Locals[fields[0] + 1 + i];
					}

					locResult = TailCall.getInstance(line, subLine, stepNum, functionObject, Args, argsAreVariable);

					break;
				}
				case TestAndSaveLogicalComparison:
				{
					/*
					 * if(R(B) == C), then R(A) = R(B); else, PC++;
					 */
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final FormulaBuilder buildy = new FormulaBuilder();
					final Var_Ref varB = this.Locals[fields[1]];
					buildy.add(varB);
					buildy.add(Operators.ArithToBool_Equals);
					buildy.add(new Var_Int("", new StepStage(line, subLine - 1, stepNum), fields[2]));

					final JumpLabel jumpIfFalse = this.getLabelForJumpTo(place, 1, raws).label; // label for skipping branch
					numTransformed[place]++;
					place++;
					// numTransformed[++place]--;

					final int[] nextFields = MiscAutobot.parseAs_iAsBx(raws[place], 2);
					final JumpLabel jumpIfTrue = this.getLabelForJumpTo(place, nextFields[1], raws).label; // label for non-skipping branch
					final Instruction conditional = JumpBranched.getInstance(line, subLine, stepNum, buildy.build(), jumpIfTrue, jumpIfFalse);
					final Instruction setter = SetVar.getInstance(line, subLine + 1, stepNum, this.Locals[fields[0]], varB);

					pass1.add(conditional);
					// pass1.add(NonOp.getSharedInstance());
					locResult = setter;
					break;
				}
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final FormulaBuilder buildy = new FormulaBuilder();
					buildy.add(this.processRK(fields[1]));

					buildy.add(getRelationalOperator(fields[0], type));

					buildy.add(this.processRK(fields[2]));

					final JumpLabel jumpIfTrue = this.getLabelForJumpTo(place, 1, raws).label; // label for branch that skips next instruction
					// numTransformed[++place]--; // drops the next instruction's footprint to 0
					// numTransformed[place]++;
					place++;

					final int[] nextFields = MiscAutobot.parseAs_iAsBx(raws[place], 2);
					final JumpLabel jumpIfFalse = this.getLabelForJumpTo(place, nextFields[1], raws).label; // label for non-skipping branch

					locResult = JumpBranched.getInstance(line, subLine, stepNum, buildy.build(), jumpIfTrue, jumpIfFalse);
					// pass1.add(JumpBranched.getInstance(buildy.build(), jumpIfTrue, jumpIfFalse));
					// locResult = NonOp.getSharedInstance();

					break;
				}
				case TestLogicalComparison:
				{
					/*
					 * if (R(A) != C), then PC++
					 */
					final int[] fields = MiscAutobot.parseAs_iABC(first, 3);
					final FormulaBuilder buildy = new FormulaBuilder();
					final Var_Ref varB = this.Locals[fields[1]];
					buildy.add(varB);
					buildy.add(Operators.ArithToBool_NotEquals);
					buildy.add(new Var_Int("Var_Int_", new StepStage(line, subLine - 1, stepNum), fields[2]));

					final JumpLabel jumpIfFalse = this.getLabelForJumpTo(place, 1, raws).label; // label for skipping branch
					// numTransformed[place++]++;
					place++;
					// numTransformed[++place]--;

					final int[] nextFields = MiscAutobot.parseAs_iAsBx(raws[place], 2);
					final JumpLabel jumpIfTrue = this.getLabelForJumpTo(place, nextFields[1], raws).label; // label for non-skipping branch
					final Instruction conditional = JumpBranched.getInstance(line, subLine, stepNum, buildy.build(), jumpIfTrue, jumpIfFalse);
					// final Instruction setter = new SetVar(this.Locals[fields[0]], varB);

					// pass1.add(conditional);
					// pass1.add(NonOp.getSharedInstance());
					locResult = conditional;

					break;
				}
				case VarArg:
				{
					final int[] fields = MiscAutobot.parseAs_iABC(first, 2);
					final boolean copyAllPossibles;
					final Var_Ref[] targets;

					if (fields[1] == 0)
					{
						targets = new Var_Ref[0];
						copyAllPossibles = true;
					}
					else
					{
						targets = new Var_Ref[fields[1] - 1];
						copyAllPossibles = false;

						for (int i = 0; i < targets.length; i++)
						{
							targets[i] = this.Locals[fields[0] + i];
						}
					}

					locResult = HandleVarArgs.getInstance(line, subLine, stepNum, targets, copyAllPossibles);

					break;
				}
				default:
				{
					throw new UnhandledEnumException(type);
				}
			}

			pass1.add(locResult);
			line++;
			subLine = 0;
			stepNum = 0;
			// System.out.println("new rawInstruction " + type.toString() + " parsed at place " + place + ", with weight: " + numTransformed[place] + ": " + locResult.getIrCode().toString());
			// end of while loop
		}

		/*
		for(int i = 0; i < raws.length; i++)
		{
			System.out.println("Type: " + OpCodes.parse(raws[i]).toString() + ", numTransformed: " + numTransformed[i]);
		}
		
		int sum = 0;
		
		for(int i = 0; i < numTransformed.length; i++)
		{
			sum += numTransformed[i];
		}
		
		if(sum != pass1.getSize())
		{
			System.out.println("sum: " + sum + ", size: " + pass1.getSize());
			
			int i = 0;
			for(Instruction inst: pass1)
			{
				System.out.println("index: " + i + ", type: " + OpCodes.parse(raws[i]).toString() + ", " + numTransformed[i]-- + "\t" + inst.translateIntoCommentedByteCode(0).toString());
				
				if(numTransformed[i] <= 0)
				{
					i++;
					
					while(numTransformed[i] == 0)
					{
						i++;
					}
				}
			}
			throw new IllegalArgumentException("sum: " + sum + ", size: " + pass1.getSize());
		}
		*/

		// this.test_printRawsWithJumps(raws);

		return this.insertAllJumpLabels(pass1, numTransformed, shouldCullVariables);
	}

	private static class JumpLabelData
	{
		private final JumpLabel label;
		private int furthestUse;

		public JumpLabelData(JumpLabel in, int currentFurthest)
		{
			this.label = in;
			this.furthestUse = currentFurthest;
		}

		public void updateFurthest(int in)
		{
			if (in > this.furthestUse)
			{
				this.furthestUse = in;
			}
		}
	}
}
