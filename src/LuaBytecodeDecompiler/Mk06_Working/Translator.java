package LuaBytecodeDecompiler.Mk06_Working;

import java.util.Iterator;

import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABC;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABx;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iAsBx;
import LuaBytecodeDecompiler.Mk06_Working.Enums.FormulaOperators;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CallMethod;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Concat;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CreateNewMap;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.FormulaOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.JumpLabel;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Conditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Unconditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.LengthOf;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.NoOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PrepLoop_While;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Stuff;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Void;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar_Bulk;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.TailcallMethod;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Boolean;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Formula;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Indexed;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Int;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_NullValued;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_String;

public class Translator
{
	private static final int RK_Mask = 0x100;
	private static final int FieldsPerFlush = 50;

	private final Instruction[] raws;
	private final IrVar[] Locals;
	private final IrVar[] Constants;
	private final IrVar[] Upvalues;
	private final HalfByteRadixMap<IrVar> Globals;
	private final lineTypes[] sigLines;

	public Translator(Instruction[] inRaws, IrVar[] inLocals, IrVar[] inConstants, IrVar[] inUpvalues, HalfByteRadixMap<IrVar> inGlobals)
	{
		this.raws = inRaws;
		this.Locals = inLocals;
		this.Constants = inConstants;
		this.Upvalues = inUpvalues;
		this.Globals = inGlobals;
		this.sigLines = new lineTypes[inRaws.length];

		for (int i = 0; i < inRaws.length; i++)
		{
			this.sigLines[i] = lineTypes.Ordinary;
		}
	}

	private SingleLinkedList<IrInstruction> parseRawInstructions_Internal(int stopIndex, SingleLinkedList<IrInstruction> result)
	{
		for (int i = this.raws.length - 1; i > stopIndex; i--)
		{
			final Instruction current = this.raws[i];
			final IrInstruction locResult;

			switch (current.getOpCode())
			{
				case Add:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Add, operand1, operand2);
					break;
				}
				case Subtract:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Sub, operand1, operand2);
					break;
				}
				case Multiply:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Mul, operand1, operand2);
					break;
				}
				case Divide:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Div, operand1, operand2);
					break;
				}
				case Modulo:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Mod, operand1, operand2);
					break;
				}
				case Power:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Pow, operand1, operand2);
					break;
				}
				case ArithmeticNegation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.Locals[cast.getB()];

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Arith_Neg, operand1);
					break;
				}
				case LogicalNegation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.Locals[cast.getA()];
					final IrVar operand1 = this.Locals[cast.getB()];

					locResult = FormulaOp.getInstance_Simple(cast.getInstructionAddress(), target, FormulaOperators.Logic_Not, operand1);
					break;
				}
				case Call:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int initialFunctionIndex = cast.getA();
					final int numParams = cast.getB() - 1;
					final int numReturns = cast.getC() - 1;
					locResult = CallMethod.getInstance(cast.getInstructionAddress(), initialFunctionIndex, numParams, numReturns);
					break;
				}
				case Concatenation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final int startIndex = cast.getB();
					final int endIndex = cast.getC();

					locResult = Concat.getInstance(cast.getInstructionAddress(), target, this.Locals, startIndex, endIndex);
					break;
				}
				case GetGlobal:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final IrVar target = this.Locals[cast.getA()];
					final String globalVarName = ((IrVar_String) this.Constants[cast.getBx()]).getValue();
					final IrVar globalVar = this.Globals.get(globalVarName);

					if (globalVar == null)
					{
						final IrVar newGlobal = new IrVar_Ref(globalVarName, ScopeLevels.Global, false);
						this.Globals.put(globalVarName, newGlobal);
					}

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, globalVar);
					break;
				}
				case GetTable:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar sourceTable = this.Locals[cast.getB()];
					final IrVar sourceIndex = this.processRK(cast.getC());
					final IrVar source = new IrVar_Indexed(ScopeLevels.Temporary, sourceTable, sourceIndex);

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case GetUpvalue:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar source = this.Upvalues[cast.getB()];

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case InitNumericForLoop:
				{
					// TODO

					this.sigLines[i] = lineTypes.PrepLine_For;
					break;
				}
				case IterateGenericForLoop:
					break;
				case IterateNumericForLoop:
					break;
				case Jump:
				{
					/*
					 * skeleton of a while loop:
					 * 
					 * <ConditionalTest>
					 * <Jump to first line after loop>
					 * ...
					 * <jump to ConditionalTest>
					 */

					final Instruction_iAsBx cast = (Instruction_iAsBx) current;
					final int relOffset = cast.get_sBx();

					if (relOffset < 0) // it's the last line of a raw while loop
					{
						boolean isDone = false;
						int place = i - 1;

						while (!isDone)
						{
							if (this.raws[place].getOpCode() == AbstractOpCodes.Jump)
							{
								final int startToEndOffset = ((Instruction_iAsBx) this.raws[place]).get_sBx();

								if (startToEndOffset == i + 1) // aka if jump goes to first line after loop
								{
									isDone = true;
								}
								else
								{
									place--;
								}
							}
							else
							{
								place--;
							}
						}

						final JumpLabel afterLoop = JumpLabel.getInstance(i + 1);
						result.push(afterLoop);
						this.parseRawInstructions_Internal(place + 1, result);
						i = place; // this.raws[i] is now pointing at the starting jump instruction

						final Instruction_iAsBx startingJumpInstruction = (Instruction_iAsBx) this.raws[i];
						i--;
						final Instruction_iABC conditional = (Instruction_iABC) this.raws[i];

						final FormulaOperators comparitor;

						switch (conditional.getOpCode())
						{
							case TestIfEqual:
							{
								comparitor = FormulaOperators.ArithToBool_Equals;
								break;
							}
							case TestIfLessThan:
							{
								comparitor = FormulaOperators.ArithToBool_LessThan;
								break;
							}
							case TestIfLessThanOrEqual:
							{
								comparitor = FormulaOperators.ArithToBool_LessThanOrEquals;
								break;
							}
							default:
							{
								throw new IllegalArgumentException();
							}
						}
						
						final IrVar operand1;
						final IrVar operand2;
						
						locResult = PrepLoop_While.getInstance(conditional.getInstructionAddress(), new FormulaOperators[]{comparitor}, new IrVar[]{operand1, operand2});
					}
					else
					{
						final SingleLinkedList<IrInstruction> catcherStack = new SingleLinkedList<IrInstruction>();
						int count = 0;

						while (count < relOffset)
						{
							final IrInstruction cur = result.pop();

							if (cur.getOpCode() != IrCodes.Jump_Label)
							{
								count++;
							}

							catcherStack.add(cur);
						}

						final IrInstruction cur = result.getFirst(); // this will be the jumptarget
						final IrInstruction jumpTarget;

						if (cur.getOpCode() == IrCodes.Jump_Label) // someone else is already jumping there, so recycle the label
						{
							jumpTarget = cur;
						}
						else
						{
							jumpTarget = JumpLabel.getInstance(cur.getLineNum());
							result.push(jumpTarget);
						}

						result.push(catcherStack, true);

					}

					break;
				}
				case Length:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar source = this.Locals[cast.getB()];

					locResult = LengthOf.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case LoadBool:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int lineAddr = cast.getInstructionAddress();
					final IrVar target = this.Locals[cast.getA()];
					final boolean rawValue = cast.getB() != 0;
					final IrVar value = new IrVar_Boolean(ScopeLevels.Hardcoded_Bool, false, true, rawValue);
					final boolean skipNextInstruction = cast.getC() != 0; // this refers to skipping whilst executing, not now

					if (skipNextInstruction)
					{
						final IrInstruction skip = result.pop();
						final IrInstruction label = JumpLabel.getInstance(lineAddr);
						result.push(label);
						result.push(skip);
						final IrInstruction jumper = Jump_Unconditional.getInstance(lineAddr, label);
						result.push(jumper);
					}

					locResult = SetVar.getInstance(lineAddr, target, value);

					break;
				}
				case LoadK:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar source = this.Constants[cast.getBx()];

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case LoadNull:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar[] targetArray = this.Locals;
					final int startIndex = cast.getA();
					final int endIndex = cast.getB();
					final IrVar source = IrVar_NullValued.getInstance();

					locResult = SetVar_Bulk.getInstance(cast.getInstructionAddress(), targetArray, startIndex, endIndex, source);
					break;
				}
				case Move:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar source = this.Locals[cast.getB()];

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case NewTable:
				{
					// B and C are initial size and hashSize, respectively
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];

					locResult = CreateNewMap.getInstance(cast.getInstructionAddress(), target);
					break;
				}
				case NoOp:
				{
					locResult = NoOp.getInstance(current.getInstructionAddress());
					break;
				}
				case Return:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int startIndex = cast.getA();
					final int numReturns = cast.getB() - 1;

					switch (numReturns)
					{
						case -1: // varArgs
						{
							locResult = Return_Stuff.getInstance(cast.getInstructionAddress(), startIndex, 0);
							break;
						}
						case 0: // returns nothing
						{
							locResult = Return_Void.getInstance(cast.getInstructionAddress());
							break;
						}
						default:
						{
							locResult = Return_Stuff.getInstance(cast.getInstructionAddress(), startIndex, numReturns);
							break;
						}
					}

					break;
				}
				case Self:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int instAddr = cast.getInstructionAddress();
					final int A = cast.getA();
					final IrVar functionReference = this.Locals[A];
					final IrVar tableReference = this.Locals[cast.getB()];
					final IrVar tableTarget = this.Locals[A + 1];
					final IrVar tableIndex = this.processRK(cast.getC());
					final IrVar indexedTableSource = new IrVar_Indexed(ScopeLevels.Temporary, tableReference, tableIndex);

					locResult = SetVar.getInstance(instAddr, tableTarget, tableReference);
					final IrInstruction next = SetVar.getInstance(instAddr, functionReference, indexedTableSource);
					result.push(next);
					break;
				}
				case SetGlobal:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final IrVar GlobalsTableIndex = this.Constants[cast.getBx()];
					final IrVar indexed = new IrVar_Indexed(ScopeLevels.Global, IrVar.getGlobalsTable(), GlobalsTableIndex);
					final IrVar source = this.Locals[cast.getA()];

					locResult = SetVar.getInstance(cast.getInstructionAddress(), indexed, source);
					break;
				}
				case SetList:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int instAddr = cast.getInstructionAddress();
					final int TargetIndexOffset = (cast.getC() - 1) * FieldsPerFlush;
					final int SourceIndexOffset = cast.getA();
					final IrVar table = this.Locals[SourceIndexOffset];

					for (int j = cast.getB(); j > 1; j--)
					{
						final IrVar targetIndex = new IrVar_Int(ScopeLevels.Hardcoded_Int, false, j + TargetIndexOffset);
						final IrVar indexed = new IrVar_Indexed(ScopeLevels.Temporary, table, targetIndex);
						final IrVar source = this.Locals[j + SourceIndexOffset];
						final IrInstruction loc = SetVar.getInstance(instAddr, indexed, source);
						result.push(loc);
					}

					final IrVar targetIndex = new IrVar_Int(ScopeLevels.Hardcoded_Int, false, TargetIndexOffset + 1);
					final IrVar indexed = new IrVar_Indexed(ScopeLevels.Temporary, table, targetIndex);
					final IrVar source = this.Locals[SourceIndexOffset + 1];
					locResult = SetVar.getInstance(instAddr, indexed, source);

					break;
				}
				case SetTable:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar table = this.Locals[cast.getA()];
					final IrVar index = this.processRK(cast.getB());
					final IrVar indexed = new IrVar_Indexed(ScopeLevels.Temporary, table, index);
					final IrVar source = this.processRK(cast.getC());

					locResult = SetVar.getInstance(cast.getInstructionAddress(), indexed, source);
					break;
				}
				case SetUpvalue:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.Locals[cast.getA()];
					final IrVar source = this.Upvalues[cast.getB()];

					locResult = SetVar.getInstance(cast.getInstructionAddress(), target, source);
					break;
				}
				case TailCall:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int initialFunctionIndex = cast.getA();
					final int numParams = cast.getB() - 1;
					locResult = TailcallMethod.getInstance(cast.getInstructionAddress(), initialFunctionIndex, numParams);
					break;
				}
				case TestAndSaveLogicalComparison:
				{
					/*
					 * if(R(B) == C)
					 * {
					 * 	PC++;
					 * }
					 * else
					 * {
					 * 	R(A) = R(B);
					 * }
					 */

					final Instruction_iABC cast = (Instruction_iABC) current;
					final int lineAddr = cast.getInstructionAddress();
					final int C = cast.getC();
					final IrVar boolToTest = this.Locals[cast.getB()];
					final IrVar_Formula boolStatement;

					switch (C)
					{
						case 0:
						{
							boolStatement = IrVar_Formula.getInstance_Simple(ScopeLevels.Temporary, false, FormulaOperators.Logic_Not, boolToTest);
							break;
						}
						case 1:
						{
							boolStatement = IrVar_Formula.getInstance_Wrapped(ScopeLevels.Temporary, false, boolToTest);
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}

					final IrInstruction skip = result.pop();
					final IrInstruction label = JumpLabel.getInstance(lineAddr);
					final IrInstruction setter = SetVar.getInstance(lineAddr, this.Locals[cast.getA()], boolToTest);
					result.push(label);
					result.push(skip);
					result.push(setter);
					locResult = Jump_Conditional.getInstance(lineAddr, boolStatement, label, setter);

					break;
				}
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{

					break;
				}
				case TestLogicalComparison:
				{
					/*
					 * if(R(A) == C)
					 * {
					 * 	PC++
					 * }
					 */

					final Instruction_iABC cast = (Instruction_iABC) current;
					final int lineAddr = cast.getInstructionAddress();
					final int C = cast.getC();
					final IrVar boolToTest = this.Locals[cast.getA()];
					final IrVar_Formula boolStatement;

					switch (C)
					{
						case 0:
						{
							boolStatement = IrVar_Formula.getInstance_Simple(ScopeLevels.Temporary, false, FormulaOperators.Logic_Not, boolToTest);
							break;
						}
						case 1:
						{
							boolStatement = IrVar_Formula.getInstance_Wrapped(ScopeLevels.Temporary, false, boolToTest);
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}

					final IrInstruction skip = result.pop();
					final IrInstruction label = JumpLabel.getInstance(lineAddr);
					result.push(label);
					result.push(skip);
					locResult = Jump_Conditional.getInstance(lineAddr, boolStatement, label, skip);

					break;
				}
				case UnknownOpCode:
					break;
				case VarArg:
					break;

				case Close:
				case Closure:
				default:
					break;
			}

			result.push(locResult);
		}

		return result;
	}

	public SingleLinkedList<IrInstruction> parseRawInstructions()
	{
		return this.parseRawInstructions_Internal(-1);
	}

	private IrVar processRK(int index)
	{
		final int compare = index & RK_Mask;
		final IrVar result;

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

	private static enum lineTypes
	{
		Ordinary,
		PrepLine_While,
		EvalLine_While,
		StartLine_WhileBlock,
		FirstLineAfter_While,
		PrepLine_For,
		EvalLine_For,
		StartLine_ForBlock,
		FirstLineAfter_For,;
	}

	private static class ReverseIterator implements Iterator<Instruction>
	{
		private final Instruction[] core;
		private int place;

		public ReverseIterator(Instruction[] inCore)
		{
			this.core = inCore;
			this.place = inCore.length - 1;
		}

		@Override
		public boolean hasNext()
		{
			return this.place >= 0;
		}

		@Override
		public Instruction next()
		{
			return this.core[this.place--];
		}
	}
}
