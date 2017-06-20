package LuaBytecodeDecompiler.Mk06_Working;

import java.util.Iterator;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import ExpressionParser.Formula;
import ExpressionParser.FormulaToken;
import ExpressionParser.Operators;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Constant;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk02.Function.Local;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABC;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABx;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iAsBx;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.JumpTypes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CallMethod;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Concat;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CreateFunctionInstance;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.CreateNewMap;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_For;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_ForEach;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.EvalLoop_While;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.FormulaOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.JumpLabel;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Conditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Jump_Unconditional;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.LengthOf;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.NoOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PlaceholderOp;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PrepLoop_For;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.PrepLoop_While;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Stuff;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Return_Void;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.SetVar_Bulk;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.Stasisify;
import LuaBytecodeDecompiler.Mk06_Working.IrInstructions.TailcallMethod;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner.IoAction;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner.VarIoTimeline;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Boolean;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Function;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Indexed;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Int;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Mapped;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_NullValued;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_String;
import SingleLinkedList.Mk01.SingleLinkedList;

public class Translator2
{
	private static final int RK_Mask = 0x100;
	private static final int FieldsPerFlush = 50;

	private final Instruction[] raws;
	private final IrVar_Ref[] Locals;
	private final HalfByteRadixMap<IrVar_Ref> additionalLocals;
	private final IrVar[] Constants;
	private final IrVar[] Upvalues;
	private final Function[] subFunctions;
	private final HalfByteRadixMap<IrVar> Globals;
	private final HalfByteRadixMap<JumpLabel> labels;
	private final HalfByteRadixMap<IrVar_Function> functionWrappers;
	private final VarCleaner cleaner;

	public Translator2(Instruction[] inRaws, IrVar_Ref[] inLocals, IrVar[] inConstants, IrVar[] inUpvalues, Function[] inSubFunctions, HalfByteRadixMap<IrVar> inGlobals)
	{
		this.raws = inRaws;
		this.Locals = inLocals;
		this.additionalLocals = new HalfByteRadixMap<IrVar_Ref>();
		this.Constants = inConstants;
		this.Upvalues = inUpvalues;
		this.subFunctions = inSubFunctions;
		this.Globals = inGlobals;
		this.labels = new HalfByteRadixMap<JumpLabel>();
		this.functionWrappers = new HalfByteRadixMap<IrVar_Function>();
		this.cleaner = new VarCleaner();
	}

	public Translator2(Function in, HalfByteRadixMap<IrVar> inGlobals)
	{
		this.raws = in.getInstructions();
		this.Locals = initLocals(in.getLocalsDebugData());
		this.additionalLocals = new HalfByteRadixMap<IrVar_Ref>();
		this.Constants = initConstants(in.getConstants());
		this.Upvalues = initUpvalues(in.getUpValueNames());
		this.subFunctions = in.getFunctionPrototypes();
		this.Globals = inGlobals;
		this.labels = new HalfByteRadixMap<JumpLabel>();
		this.functionWrappers = new HalfByteRadixMap<IrVar_Function>();
		this.cleaner = new VarCleaner();

		/*
		for(Instruction current: this.raws)
		{
			System.out.println(current.disAssemble().toString());
		}
		*/
	}

	private static IrVar_Ref[] initLocals(Local[] in)
	{
		final IrVar_Ref[] result = new IrVar_Ref[in.length];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = new IrVar_Ref(in[i].getName(), ScopeLevels.Local_Variable, false);
		}

		return result;
	}

	private static IrVar[] initConstants(Constant[] in)
	{
		final IrVar[] result = new IrVar[in.length];
		final ScopeLevels level = ScopeLevels.Local_Constant;

		for (int i = 0; i < result.length; i++)
		{
			final IrVar locResult;
			final Constant current = in[i];

			switch (current.getType())
			{
				case Bool:
				{
					locResult = new IrVar_Boolean(level, false, true, current.getValAsBool());
					break;
				}
				case Null:
				{
					locResult = IrVar_NullValued.getInstance();
					break;
				}
				case Num:
				{
					locResult = new IrVar_Int(level, false, current.getValAsInt());
					break;
				}
				case String:
				{
					locResult = new IrVar_String(level, false, current.getValAsString());
					break;
				}
				case Unknown:
				default:
				{
					locResult = new IrVar_Ref(level, false);
					break;
				}
			}

			result[i] = locResult;
		}

		return result;
	}

	private static IrVar[] initUpvalues(String[] upvalueNames)
	{
		final IrVar[] result = new IrVar[upvalueNames.length];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = new IrVar_Ref(upvalueNames[i], ScopeLevels.UpValue, false);
		}

		return result;
	}

	public SingleLinkedList<IrInstruction> parseAndTidyUp()
	{
		// return this.correctLoops(this.reinsertJumpLabelsAndLinkEverything(this.simpleParseRaws()));
		final SingleLinkedList<IrInstruction> result = this.correctLoops(this.tidyUpConditionals(this.reinsertJumpLabelsAndLinkEverything(this.simpleParseRaws())));

		// this.foldVars2(result);
		evalVars(result).foldAll();

		return result;
	}

	private static final VarAssignMap evalVars(SingleLinkedList<IrInstruction> in)
	{
		final VarAssignMap map = new VarAssignMap();

		for (IrInstruction line : in)
		{
			line.evalSwapStatus(map);
		}

		return map;
	}

	private void foldVars2(SingleLinkedList<IrInstruction> in)
	{
		final VarAssignMap map = new VarAssignMap();

		for (IrInstruction line : in)
		{
			line.evalSwapStatus(map);
		}
	}

	private void foldVars()
	{
		while (this.cleaner.hasMoreReads())
		{
			final IoAction nextRead = this.cleaner.getNextRead();
			final VarIoTimeline history = this.cleaner.getWriteHistory(nextRead);

			if (history != null)
			{
				IoAction lastWrite = history.getLastWriteBefore(nextRead);

				if (lastWrite != null)
				{
					System.out.println("Swapping " + nextRead.getVar().label + " with " + lastWrite.getVar().label);
					nextRead.getLine().swapSteps(nextRead.getCoord(), lastWrite.getVar());
					// lastWrite.getLine().setExpiredState(true);
				}
			}
			else
			{
				// System.out.println(nextRead.getVar().label + ", " + nextRead.getLine().toCharList(true, false));
			}
		}
	}

	/**
	 * iterates through the listing, and replaces if statements to which are jumped back with while loops
	 * <br>
	 * specifically, the pattern is Jump_Conditional ... Jump_Unconditional(Jump_Neg)
	 * 
	 * @param in
	 * @return
	 */
	private SingleLinkedList<IrInstruction> correctLoops(SingleLinkedList<IrInstruction> in)
	{
		final SingleLinkedList<IrInstruction> result = new SingleLinkedList<IrInstruction>();
		final ReverseIterator itsy = new ReverseIterator(in);

		while (itsy.hasNext())
		{
			final IrInstruction current = itsy.next();

			if (current.getOpCode() == IrCodes.Jump_Unconditional)
			{

				final IrInstruction possibleNegJump = current.getNext();

				if (possibleNegJump.getLinePosData().compareLineNums(current.getLinePosData()) < 0)
				{
					// System.out.println("here, with type: " + possibleNegJump.getOpCode());
					IrInstruction initialTest = possibleNegJump.getNext();

					/*
					while (initialTest.getOpCode() == IrCodes.Jump_Label)
					{
						initialTest = initialTest.getNext();
					}
					*/

					result.push(this.parseNextWhileLoop(itsy, initialTest, possibleNegJump), true);
				}
				else
				{
					// System.out.println("here");
					result.push(current);
				}
			}
			else
			{
				result.push(current);
			}
		}

		return result;
	}

	private SingleLinkedList<IrInstruction> parseNextWhileLoop(Iterator<IrInstruction> itsy, IrInstruction initialTest, IrInstruction NegJump)
	{
		final SingleLinkedList<IrInstruction> result = new SingleLinkedList<IrInstruction>();
		boolean isDone = false;

		while (itsy.hasNext() && !isDone)
		{
			final IrInstruction current = itsy.next();
			// System.out.println("current: " + current.mangledName);

			if (current == initialTest)
			{
				final StepStage firstLine = initialTest.getLinePosData();
				final StepStage lastLine = current.getLinePosData();
				// final IrInstruction jumpLabel_InnerBlockStart = NegJump.getNext(); // should be current's jumpIfTrue
				final JumpLabel jumpLabel_InnerBlockStart = this.saveJumpLabel(firstLine.getLineNum());

				if (initialTest.getNumNexts() == 2)
				{
					final IrInstruction jumpLabel_LoopEnd = initialTest.getNext(1);
					final Formula boolStatement;

					switch (initialTest.getOpCode())
					{
						case Jump_Conditional:
						{
							boolStatement = ((Jump_Conditional) initialTest).getBoolStatement();
							break;
						}
						default:
						{
							throw new UnhandledEnumException(initialTest.getOpCode());
						}
					}

					final PrepLoop_While first = PrepLoop_While.getInstance(firstLine, boolStatement);
					first.setNext_FirstInstructionInLoop(jumpLabel_InnerBlockStart);
					first.setNext_EndOfLoop(jumpLabel_LoopEnd);
					final EvalLoop_While last = EvalLoop_While.getInstance(lastLine, boolStatement);
					last.setNext_StartOfLoop(jumpLabel_InnerBlockStart);
					last.setNext_FirstInstructionAfterLoop(jumpLabel_LoopEnd);
					result.push(first);
					result.add(last);
					isDone = true;
				}
			}
			else if (current.getOpCode() == IrCodes.Jump_Conditional)
			{
				final IrInstruction possibleNegJump = current.getFurthestNegJump();

				if (possibleNegJump != null)
				{
					IrInstruction locInitialTest = possibleNegJump.getNext(0);

					while (locInitialTest.getOpCode() == IrCodes.Jump_Label)
					{
						locInitialTest = locInitialTest.getNext();
					}

					result.push(this.parseNextWhileLoop(itsy, locInitialTest, possibleNegJump), true);
				}
				else
				{
					result.push(current);
				}
			}
			else
			{
				result.push(current);
			}
		}

		return result;
	}

	private SingleLinkedList<IrInstruction> tidyUpConditionals(SingleLinkedList<IrInstruction> old)
	{
		final SingleLinkedList<IrInstruction> result = new SingleLinkedList<IrInstruction>();

		while (!old.isEmpty())
		{
			final IrInstruction current = old.pop();

			switch (current.getOpCode())
			{
				case Jump_Conditional:
				{
					final IrInstruction firstLabel = current.getNext(0);
					// IrInstruction secondLabel = expiredMappings.get(firstLabel.linePosData.getHalfByteHash());
					final IrInstruction firstUncondJump = firstLabel.getNext();
					final IrInstruction secondLabel;

					if (firstUncondJump.hasExpired())
					{
						secondLabel = this.labels.get(firstLabel.linePosData.getLineNum());
					}
					else
					{
						secondLabel = firstUncondJump.getNext();
						firstLabel.setExpiredState(true, "collapsed into conditional jump");
						firstUncondJump.setExpiredState(true, "collapsed into conditional jump");
						this.labels.put(firstLabel.linePosData.getLineNum(), (JumpLabel) secondLabel);
					}

					current.setNext(secondLabel, 0);
					secondLabel.addIncomingJump(current, JumpTypes.Conditional_Pos);

					// System.out.println("current: " + current.toCharList(false, false) + "\r\nfirst label: " + firstLabel.mangledName + ", second label: " + secondLabel.mangledName);

					// break;
				}
				default:
				{
					result.add(current);
					/*
					if (!current.hasExpired())
					{
						result.add(current);
					}
					*/
					break;
				}
			}
		}

		return result;
	}

	/**
	 * re-inserts jump labels, and makes sure everything is properly linked.
	 * 
	 * @param old
	 * @return
	 */
	private SingleLinkedList<IrInstruction> reinsertJumpLabelsAndLinkEverything(SingleLinkedList<IrInstruction> old)
	{
		final SingleLinkedList<IrInstruction> result = new SingleLinkedList<IrInstruction>();
		final IrInstruction[] buffer = new IrInstruction[] {
																IrInstruction.getNullIrInstruction(),
																IrInstruction.getNullIrInstruction() };
		int bufferIndex = 0;
		int oppoIndex = 1;
		StepStage lastLine = StepStage.getNullStepStage();

		while (!old.isEmpty())
		{
			buffer[bufferIndex] = old.pop();

			if (lastLine.compareLineNums(buffer[bufferIndex].getLinePosData()) < 0)
			{
				lastLine = buffer[bufferIndex].getLinePosData();
				JumpLabel maybe = this.labels.get(lastLine.getLineNum());

				if (maybe != null)
				{
					// System.out.println("adding jumplabel to line " + lastLine.toCharList().toString() + ": " + maybe.getLinePosData().toCharList().toString());
					old.push(buffer[bufferIndex]);
					buffer[bufferIndex] = maybe;

					// buffer[oppoIndex] = maybe;

					/*
					// if previous IrInstruction was linear, add current instruction as previous' next
					if (buffer[bufferIndex].getNumNexts() == 1)
					{
						buffer[bufferIndex].setNext(maybe, 0);
						// buffer[oppoIndex].setDirectPrev(buffer[bufferIndex]);
					}
					*/

					// System.out.println(lineCount + ": " + maybe.getLabel() + " on line " + maybe.getLineNum() + " versus current line, which is: " + current.getLineNum());
				}
			}

			result.add(buffer[bufferIndex]);

			// if previous IrInstruction was linear, add current instruction as previous' next
			linkUpLines(buffer[oppoIndex], buffer[bufferIndex]);

			oppoIndex = bufferIndex;
			bufferIndex ^= 1; // fancy trick to toggle bufferIndex between 0 and 1

			// lineCount++;
		}

		return result;
	}

	private static final void linkUpLines(IrInstruction prev, IrInstruction curr)
	{
		switch (prev.getOpCode())
		{
			case Jump_Unconditional:
			{
				break;
			}
			case EvalLoop_For:
			case EvalLoop_ForEach:
			case PrepLoop_For:
			case PrepLoop_While:
			{
				prev.setNext(curr, 1);
				curr.setDirectPrev(prev);
				break;
			}
			case Jump_Conditional:
			{
				prev.setNext(curr, 0);
				curr.setDirectPrev(prev);
				break;
			}
			default:
			{
				if (prev.getNumNexts() == 1)
				{
					prev.setNext(curr, 0);
					curr.setDirectPrev(prev);
				}
				break;
			}
		}
	}

	/**
	 * this method is meant ONLY for direct translation - do not attempt to look for loops and such.
	 * Ensure to save jumplabels in the map for them
	 * 
	 * @return
	 */
	public SingleLinkedList<IrInstruction> simpleParseRaws()
	{
		final SingleLinkedList<IrInstruction> result = new SingleLinkedList<IrInstruction>();

		for (int rawLineNum = this.raws.length - 1; rawLineNum > -1; rawLineNum--)
		{
			final Instruction current = this.raws[rawLineNum];
			final IrInstruction locResult;
			final StepStage lineNum = new StepStage(rawLineNum, 0, 0, "");

			// System.out.println(current.disAssemble().toString());

			switch (current.getOpCode())
			{
				case Add:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Add);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					// locResult = FormulaOp.getInstance_Simple(lineNum, target, Operators.Arith_Add, operand1, operand2);

					break;
				}
				case Subtract:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Sub);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					// locResult = FormulaOp.getInstance_Simple(lineNum, target, Operators.Arith_Sub, operand1, operand2);
					break;
				}
				case Multiply:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Mul);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case Divide:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Div);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case Modulo:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Mod);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case Power:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(Operators.Arith_Pow);
					rawFormula.add(operand2);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case ArithmeticNegation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.getLocalVarFromIndex(cast.getB());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(Operators.Arith_Neg);
					rawFormula.add(operand1);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case LogicalNegation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar operand1 = this.getLocalVarFromIndex(cast.getB());

					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(Operators.Logic_Not);
					rawFormula.add(operand1);
					locResult = new FormulaOp(lineNum, target, Formula.parse(rawFormula.iterator()));
					break;
				}
				case Call:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int initialFunctionIndex = cast.getA();
					final int numParams = cast.getB() - 1;
					final int numReturns = cast.getC() - 1;

					final IrVar[] Args;

					if (numParams > 0)
					{
						Args = new IrVar[numParams];
					}
					else
					{
						Args = new IrVar[0];
					}

					final IrVar[] Returns;

					if (numReturns > 0)
					{
						Returns = new IrVar[numReturns];
					}
					else
					{
						Returns = new IrVar[0];
					}

					for (int i = 0; i < Args.length; i++)
					{
						Args[i] = this.getLocalVarFromIndex(i + initialFunctionIndex + 1);
					}

					for (int i = 0; i < Returns.length; i++)
					{
						Returns[i] = this.getLocalVarFromIndex(i + initialFunctionIndex);
					}

					locResult = CallMethod.getInstance(lineNum, this.getLocalVarFromIndex(initialFunctionIndex), Args, numParams == -1, Returns, numReturns == -1);

					break;
				}
				case Concatenation:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar_Ref target = this.getLocalVarFromIndex(cast.getA());
					final int startIndex = cast.getB();
					final int endIndex = cast.getC();
					final IrVar[] sourceTable = new IrVar[endIndex - startIndex + 1];

					for (int i = 0; i < sourceTable.length; i++)
					{
						sourceTable[i] = this.getLocalVarFromIndex(i + startIndex);
					}

					locResult = Concat.getInstance(lineNum, target, sourceTable);
					break;
				}
				case GetGlobal:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					// System.out.println(this.Constants[cast.getBx()]);
					// final String globalVarName = ((IrVar_String) this.Constants[cast.getBx()]).getValue();
					final String globalVarName = this.Constants[cast.getBx()].valueToString();
					IrVar globalVar = this.Globals.get(globalVarName);

					if (globalVar == null)
					{
						globalVar = new IrVar_Ref(globalVarName, ScopeLevels.Global, false);
						this.Globals.put(globalVarName, globalVar);
					}

					locResult = SetVar.getInstance(lineNum, target, globalVar);
					break;
				}
				case GetTable:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar sourceTable = this.getLocalVarFromIndex(cast.getB());
					final IrVar sourceIndex = this.processRK(cast.getC());
					final IrVar source = new IrVar_Mapped(ScopeLevels.Temporary, sourceTable, sourceIndex);

					locResult = SetVar.getInstance(lineNum, target, source);
					break;
				}
				case GetUpvalue:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar source = this.Upvalues[cast.getB()];

					locResult = SetVar.getInstance(lineNum, target, source);
					break;
				}
				case Length:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar source = this.getLocalVarFromIndex(cast.getB());

					locResult = LengthOf.getInstance(lineNum, target, source);
					break;
				}
				case LoadBool:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final boolean rawValue = cast.getB() != 0;
					final IrVar value = new IrVar_Boolean(ScopeLevels.Hardcoded_Bool, false, true, rawValue);
					final boolean skipNextInstruction = cast.getC() != 0; // this refers to skipping whilst executing, not now

					if (skipNextInstruction)
					{
						final JumpLabel label = this.saveJumpLabel(rawLineNum + 1);
						// System.out.println("LoadBool jump: " + label.mangledName + " of magnitude: 1");

						final IrInstruction jumper = Jump_Unconditional.getInstance(new StepStage(rawLineNum, 1, 0, "Prev LoadBool needs to skip"), label);
						label.addIncomingJump(jumper, JumpTypes.Jump_Pos);
						result.push(jumper);
					}

					locResult = SetVar.getInstance(lineNum, target, value);

					break;
				}
				case LoadK:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar source = this.Constants[cast.getBx()];

					locResult = SetVar.getInstance(lineNum, target, source);
					break;
				}
				case LoadNull:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;

					final IrVar_Ref[] targetArray = this.Locals;
					final int startIndex = cast.getA();
					final int endIndex = cast.getB();
					final IrVar source = IrVar_NullValued.getInstance();

					if (startIndex == endIndex)
					{
						locResult = SetVar.getInstance(lineNum, this.getLocalVarFromIndex(startIndex), source);
					}
					else
					{
						locResult = SetVar_Bulk.getInstance(lineNum, targetArray, startIndex, endIndex, source);
					}
					break;
				}
				case Move:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int A = cast.getA();

					final IrVar target;

					// if(A < this.Locals.length)
					// {
					target = this.getLocalVarFromIndex(A);
					/*
					}
					else
					{
					locResult = PlaceholderOp.getInstance(lineNum, "Move: LocalVars[" + A + "] = LocalVars[" + cast.getB() + "]; //LocalVars.length == " + this.Locals.length);
					break;
					}
					*/

					final IrVar source = this.getLocalVarFromIndex(cast.getB());

					locResult = SetVar.getInstance(lineNum, target, source);
					break;
				}
				case NewTable:
				{
					// B and C are initial size and hashSize, respectively
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());

					locResult = CreateNewMap.getInstance(lineNum, target);
					break;
				}
				case NoOp:
				{
					locResult = NoOp.getInstance(lineNum);
					break;
				}
				case Return:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int startIndex = cast.getA();
					final int numReturns = cast.getB() - 1;
					final IrVar[] sourceTable;

					switch (numReturns)
					{
						case -1: // varArgs
						{
							sourceTable = new IrVar[1];
							sourceTable[0] = this.getLocalVarFromIndex(startIndex);
							locResult = Return_Stuff.getInstance(lineNum, sourceTable, true);
							break;
						}
						case 0: // returns nothing
						{
							sourceTable = new IrVar[0];
							locResult = Return_Void.getInstance(lineNum);
							break;
						}
						default:
						{
							sourceTable = new IrVar[numReturns];

							for (int i = 0; i < numReturns; i++)
							{
								sourceTable[i] = this.getLocalVarFromIndex(startIndex + i);
							}

							locResult = Return_Stuff.getInstance(lineNum, sourceTable, false);
							break;
						}
					}
					
					final IrInstruction extraCheck = result.getFirst();
					
					if(extraCheck != null && extraCheck.getOpCode() == IrCodes.Return_Void)
					{
						extraCheck.setExpiredState(true, "extraneous return");
					}

					break;
				}
				case Self:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int A = cast.getA();
					final IrVar functionReference = this.getLocalVarFromIndex(A);
					final IrVar tableReference = this.getLocalVarFromIndex(cast.getB());
					final IrVar tableTarget = this.Locals[A + 1];
					final IrVar tableIndex = this.processRK(cast.getC());
					final IrVar indexedTableSource = new IrVar_Mapped(ScopeLevels.Temporary, tableReference, tableIndex);

					locResult = SetVar.getInstance(lineNum, tableTarget, tableReference);
					final IrInstruction next = SetVar.getInstance(new StepStage(rawLineNum, 1, 0, ""), functionReference, indexedTableSource);
					result.push(next);
					break;
				}
				case SetGlobal:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					// System.out.println(Integer.toHexString(cast.getRaw()) + ", at place: " + cast.getInstructionAddress());
					final IrVar GlobalsTableIndex = this.Constants[cast.getBx()];
					final IrVar indexed = new IrVar_Mapped(ScopeLevels.Global, IrVar.getGlobalsTable(), GlobalsTableIndex);
					final int A = cast.getA();
					final IrVar source;

					if (A < this.Locals.length)
					{
						source = this.getLocalVarFromIndex(A);
					}
					else
					{
						locResult = PlaceholderOp.getInstance(lineNum, "SetGlobal: " + indexed.valueToString() + " = LocalVars[" + A + "]; //LocalVars.length == " + this.Locals.length);
						// throw new IndexOutOfBoundsException("went for: " + A + " out of " + this.Locals.length);
						break;
					}

					locResult = SetVar.getInstance(lineNum, indexed, source);
					break;
				}
				case SetList:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int TargetIndexOffset = (cast.getC() - 1) * FieldsPerFlush;
					final int SourceIndexOffset = cast.getA();
					final IrVar table = this.Locals[SourceIndexOffset];

					for (int j = cast.getB(); j > 1; j--)
					{
						final IrVar targetIndex = new IrVar_Int(ScopeLevels.Hardcoded_Int, false, j + TargetIndexOffset);
						final IrVar indexed = new IrVar_Indexed(ScopeLevels.Temporary, table, targetIndex);
						final IrVar source = this.Locals[j + SourceIndexOffset];
						final IrInstruction loc = SetVar.getInstance(new StepStage(rawLineNum, 1, 0, ""), indexed, source);
						result.push(loc);
					}

					final IrVar targetIndex = new IrVar_Int(ScopeLevels.Hardcoded_Int, false, TargetIndexOffset + 1);
					final IrVar indexed = new IrVar_Indexed(ScopeLevels.Temporary, table, targetIndex);
					final IrVar source = this.Locals[SourceIndexOffset + 1];
					locResult = SetVar.getInstance(lineNum, indexed, source);

					break;
				}
				case SetTable:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar table = this.getLocalVarFromIndex(cast.getA());
					final IrVar index = this.processRK(cast.getB());
					final IrVar indexed = new IrVar_Mapped(ScopeLevels.Temporary, table, index);
					final IrVar source = this.processRK(cast.getC());

					locResult = SetVar.getInstance(lineNum, indexed, source);
					break;
				}
				case SetUpvalue:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar target = this.getLocalVarFromIndex(cast.getA());
					final IrVar source = this.Upvalues[cast.getB()];

					locResult = SetVar.getInstance(lineNum, target, source);
					break;
				}
				case TailCall:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int initialFunctionIndex = cast.getA();
					final int numParams = cast.getB() - 1;
					//final Function funct = this.subFunctions[initialFunctionIndex];
					final IrVar[] args;
					final IrVar rawInitialFunction = this.getLocalVarFromIndex(initialFunctionIndex);
					
					IrVar cargo = this.functionWrappers.get(rawInitialFunction.getLabel());
					
					if(cargo == null)
					{
						cargo = new IrVar_Ref(rawInitialFunction.valueToString(), ScopeLevels.Temporary, false);
					}
					
					if(numParams != -1)
					{
						args = new IrVar[numParams];
						
						for(int i = 0; i < numParams; i++)
						{
							args[i] = this.getLocalVarFromIndex(i + initialFunctionIndex + 1);
						}
					}
					else
					{
						args = new IrVar[0];
					}
					
					locResult = TailcallMethod.getInstance(lineNum, cargo, numParams, args);
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
					final int C = cast.getC();
					final IrVar boolToTest = this.getLocalVarFromIndex(cast.getB());
					final SingleLinkedList<FormulaToken> raw = new SingleLinkedList<FormulaToken>();

					switch (C)
					{
						case 0:
						{
							// boolStatement = IrVar_Formula.getInstance_Simple(ScopeLevels.Temporary, false, Operators.Logic_Not, boolToTest);
							// boolStatement = Formula.getInstance(Operators.Logic_Not, boolToTest);

							// raw.push(Operators.Logic_Not);
							raw.add(IrVar_Boolean.getInstance_True());
							break;
						}
						case 1:
						{
							//raw.push(Operators.Logic_Not);
							raw.add(IrVar_Boolean.getInstance_False());
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}
					
					raw.add(Operators.ArithToBool_Equals);
					raw.add(boolToTest);

					final Formula boolStatement = Formula.parse(raw.iterator());
					
					//System.out.println("new formula: " + boolStatement.toCharList().toString());

					final JumpLabel label = this.saveJumpLabel(rawLineNum + 1);
					// System.out.println("TestAndSaveLogicalComparison jump: " + label.mangledName + " of magnitude: 1");

					final IrInstruction setter = SetVar.getInstance(new StepStage(rawLineNum, 1, 0, "expanded setter"), this.getLocalVarFromIndex(cast.getA()), boolToTest);
					setter.registerAllReadsAndWrites(this.cleaner);
					result.push(setter);
					locResult = Jump_Conditional.getInstance(lineNum, boolStatement, setter, label);
					label.addIncomingJump(locResult, JumpTypes.Conditional_Pos);

					break;
				}
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final IrVar operand1 = this.processRK(cast.getB());
					final IrVar operand2 = this.processRK(cast.getC());
					final Operators operator;

					switch (current.getOpCode())
					{
						case TestIfEqual:
						{
							if (cast.getA() == 0)
							{
								operator = Operators.ArithToBool_NotEquals;
							}
							else
							{
								operator = Operators.ArithToBool_Equals;
							}
							break;
						}
						case TestIfLessThan:
						{
							if (cast.getA() == 0)
							{
								operator = Operators.ArithToBool_GreaterThanOrEquals;
							}
							else
							{
								operator = Operators.ArithToBool_LessThan;
							}
							break;
						}
						case TestIfLessThanOrEqual:
						{
							if (cast.getA() == 0)
							{
								operator = Operators.ArithToBool_GreaterThan;
							}
							else
							{
								operator = Operators.ArithToBool_LessThanOrEquals;
							}
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}

					// final IrVar_Formula boolStatement = IrVar_Formula.getInstance_Simple(ScopeLevels.Temporary, false, operator, operand1, operand2);
					// final Formula boolStatement = Formula.getInstance(operator, operand1, operand2);
					final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();
					rawFormula.add(operand1);
					rawFormula.add(operator);
					rawFormula.add(operand2);
					final Formula boolStatement = Formula.parse(rawFormula.iterator());
					//System.out.println("new formula: " + boolStatement.toCharList().toString());

					final JumpLabel jumpIfFalse = this.saveJumpLabel(rawLineNum);

					final JumpLabel jumpIfTrue = this.saveJumpLabel(rawLineNum + 1);

					// System.out.println("Comparison jumps: " + jumpIfTrue.mangledName + " of magnitude: 0" + ", " + jumpIfFalse.mangledName + " of magnitude: 1");

					locResult = Jump_Conditional.getInstance(lineNum, boolStatement, jumpIfTrue, jumpIfFalse);
					jumpIfTrue.addIncomingJump(locResult, JumpTypes.Conditional_Pos);
					jumpIfFalse.addIncomingJump(locResult, JumpTypes.Conditional_Neg);

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
					final int C = cast.getC();
					final IrVar boolToTest = this.getLocalVarFromIndex(cast.getA());
					final SingleLinkedList<FormulaToken> raw = new SingleLinkedList<FormulaToken>();

					switch (C)
					{
						case 0:
						{
							// boolStatement = IrVar_Formula.getInstance_Simple(ScopeLevels.Temporary, false, Operators.Logic_Not, boolToTest);
							// boolStatement = Formula.getInstance(Operators.Logic_Not, boolToTest);

							// raw.push(Operators.Logic_Not);
							raw.add(IrVar_Boolean.getInstance_False());
							break;
						}
						case 1:
						{
							//raw.push(Operators.Logic_Not);
							raw.add(IrVar_Boolean.getInstance_True());
							break;
						}
						default:
						{
							throw new IllegalArgumentException();
						}
					}
					
					raw.add(Operators.ArithToBool_Equals);
					raw.add(boolToTest);

					final Formula boolStatement = Formula.parse(raw.iterator());
					//System.out.println("new formula: " + boolStatement.toCharList().toString());

					final JumpLabel jumpIfFalse = this.saveJumpLabel(rawLineNum);

					final JumpLabel jumpIfTrue = this.saveJumpLabel(rawLineNum + 1);

					// System.out.println("TestLogicalComparison jumps: " + jumpIfTrue.mangledName + " of magnitude: 0" + ", " + jumpIfFalse.mangledName + " of magnitude: 1");

					locResult = Jump_Conditional.getInstance(lineNum, boolStatement, jumpIfTrue, jumpIfFalse);
					jumpIfTrue.addIncomingJump(locResult, JumpTypes.Conditional_Pos);
					jumpIfFalse.addIncomingJump(locResult, JumpTypes.Conditional_Neg);

					break;
				}
				case Jump:
				{
					final Instruction_iAsBx cast = (Instruction_iAsBx) current;
					final int relOffset = cast.get_sBx();
					final JumpLabel label = this.saveJumpLabel(relOffset + rawLineNum);

					if (this.raws[rawLineNum - 1].getOpCode() == AbstractOpCodes.IterateGenericForLoop)
					{
						final Instruction_iABC cast2 = (Instruction_iABC) this.raws[--rawLineNum];
						final int A = cast2.getA();
						final int C = cast2.getC();
						final IrVar iteratorFunction = this.getLocalVarFromIndex(A);
						final IrVar state = this.getLocalVarFromIndex(A + 1);
						final IrVar enumIndex = this.getLocalVarFromIndex(A + 2);
						final IrVar[] internalLoopVars = new IrVar[C];
						final int offset = A + 3;

						for (int j = 0; j < internalLoopVars.length; j++)
						{
							internalLoopVars[j] = this.getLocalVarFromIndex(j + offset);
						}

						locResult = EvalLoop_ForEach.getInstance(new StepStage(rawLineNum, 0, 0, "substituted forEach loop"), iteratorFunction, state, enumIndex, internalLoopVars);
						label.addIncomingJump(locResult, JumpTypes.Conditional_Pos);
						locResult.setNext(label, 0);
						final JumpLabel endOfLoop = this.saveJumpLabel(rawLineNum + 1);
						endOfLoop.addIncomingJump(locResult, JumpTypes.Conditional_Neg);
					}
					else
					{
						locResult = Jump_Unconditional.getInstance(lineNum, label);
						label.addIncomingJump(locResult, (relOffset < 0) ? JumpTypes.Jump_Neg : JumpTypes.Jump_Pos);
					}
					break;
				}
				case InitNumericForLoop:
				{
					final Instruction_iAsBx cast = (Instruction_iAsBx) current;
					final int A = cast.getA();
					final int relOffset = cast.get_sBx();
					final JumpLabel label = this.saveJumpLabel(relOffset + rawLineNum + 1);
					final IrVar indexVar = this.getLocalVarFromIndex(A);
					final IrVar intialIndexValue = indexVar;
					final IrVar limitVar = this.getLocalVarFromIndex(A + 1);

					final PrepLoop_For tempLocResult = PrepLoop_For.getInstance(lineNum, intialIndexValue, indexVar, limitVar);
					label.addIncomingJump(tempLocResult, JumpTypes.Conditional_Neg);
					tempLocResult.setNext_EndOfLoop(label);
					locResult = tempLocResult;
					break;
				}
				case IterateNumericForLoop:
				{
					final Instruction_iAsBx cast = (Instruction_iAsBx) current;
					final int A = cast.getA();
					final IrVar indexVar = this.getLocalVarFromIndex(A);
					final IrVar limitVar = this.getLocalVarFromIndex(A + 1);
					final IrVar stepVar = this.getLocalVarFromIndex(A + 2);

					final EvalLoop_For tempLocResult = EvalLoop_For.getInstance(lineNum, indexVar, limitVar, stepVar);

					final int relOffset = cast.get_sBx();
					final JumpLabel label = this.saveJumpLabel(relOffset + rawLineNum);
					label.addIncomingJump(tempLocResult, JumpTypes.Conditional_Pos);
					tempLocResult.setNext_StartOfLoop(label);
					locResult = tempLocResult;
					break;
				}
				case IterateGenericForLoop:
				{
					throw new IllegalArgumentException();
					/*
					 * see case Jump
					final Instruction_iABC cast2 = (Instruction_iABC) current;
					final int A = cast2.getA();
					final int C = cast2.getC();
					final IrVar iteratorFunction = this.getLocalVarFromIndex(A);
					final IrVar state = this.Locals[A + 1];
					final IrVar enumIndex = this.Locals[A + 2];
					final IrVar[] internalLoopVars = new IrVar[C];
					final int offset = A + 3;
					
					for (int j = 0; j < internalLoopVars.length; j++)
					{
						internalLoopVars[j] = this.Locals[j + offset];
					}
					
					locResult = EvalLoop_ForEach.getInstance(lineNum, iteratorFunction, state, enumIndex, internalLoopVars);
					*/
				}
				case Closure:
				{
					final Instruction_iABx cast = (Instruction_iABx) current;
					final int A = cast.getA();
					final IrVar_Ref target = this.getLocalVarFromIndex(A);
					final int Bx = cast.getBx();
					final Function sourceFunction;

					if (this.subFunctions.length > Bx)
					{
						sourceFunction = this.subFunctions[cast.getBx()];
					}
					else
					{
						sourceFunction = Function.generateDummyFunction();
					}

					int numUpvalues = sourceFunction.getNumUpvalues();
					final IrVar[] upvals;

					if (numUpvalues < 0)
					{
						upvals = new IrVar[0];
					}
					else
					{
						upvals = new IrVar[numUpvalues];
					}

					for (int j = 0; j < upvals.length; j++)
					{
						// result.pop(); // discarding pseudo-instructions
						// System.out.println("discarding pseudo-instruction: " + result.pop().toCharList(true, false));

						final Instruction_iABC locCur = (Instruction_iABC) this.raws[rawLineNum + j + 1];

						switch (locCur.getOpCode())
						{
							case Move:
							{
								upvals[j] = this.getLocalVarFromIndex(locCur.getB());
								break;
							}
							case GetUpvalue:
							{
								upvals[j] = this.Upvalues[locCur.getB()];
								break;
							}
							default:
							{
								throw new UnhandledEnumException(locCur.getOpCode());
							}
						}
					}

					final IrVar[] params = new IrVar[0];

					locResult = CreateFunctionInstance.getInstance(lineNum, target, this.getWrapper(sourceFunction), params, upvals);
					break;
				}
				case Close:
				{
					final Instruction_iABC cast = (Instruction_iABC) current;
					final int firstVarIndex = cast.getA();

					locResult = Stasisify.getInstance(lineNum, this.Locals, firstVarIndex);
					break;
				}
				case VarArg:
				{
					locResult = PlaceholderOp.getInstance(lineNum, current.disAssemble().toString());
					break;
				}
				case UnknownOpCode:
				default:
				{
					// locResult = JumpLabel.getInstance(lineNum, "<bypassed>: " + current.getOpCode() + " - 0x" + Integer.toHexString(current.getRaw()));
					throw new UnhandledEnumException(current.getOpCode());
				}
			}

			// locResult.registerAllReadsAndWrites(this.cleaner);

			result.push(locResult);
		}

		return result;
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
			result = this.getLocalVarFromIndex(index);
		}

		return result;
	}

	private IrVar_Ref getLocalVarFromIndex(int index)
	{
		if (index < this.Locals.length)
		{
			return this.Locals[index];
		}
		else
		{
			IrVar_Ref result = this.additionalLocals.get(index);

			if (result == null)
			{
				result = new IrVar_Ref("additionalVar_" + index, ScopeLevels.Local_Variable, false);
				this.additionalLocals.put(index, result);
			}

			return result;
		}
	}

	/**
	 * NOTE TO SELF: do not try to take PC stepping into account with lineNums, this method will account for that drift
	 * 
	 * @param lineNum
	 */
	private JumpLabel saveJumpLabel(int lineNum)
	{
		JumpLabel result = this.labels.get(++lineNum);

		if (result == null)
		{
			result = JumpLabel.getInstance(new StepStage(lineNum, -1, 0, "jumplabel"));
			this.labels.put(lineNum, result);
		}

		return result;
	}

	private IrVar_Function getWrapper(Function in)
	{
		IrVar_Function result = this.functionWrappers.get(in.getFunctionName());

		if (result == null)
		{
			result = new IrVar_Function(in.getFunctionName(), ScopeLevels.Local_Variable, false, in);
			this.functionWrappers.get(in.getFunctionName());
		}

		return result;
	}

	@SuppressWarnings("unused")
	private JumpLabel getJumpLabel(int lineNum)
	{
		return this.labels.get(lineNum + 1);
	}

	private static class ReverseIterator implements Iterator<IrInstruction>
	{
		private final IrInstruction[] lines;
		private int place;

		public ReverseIterator(IrInstruction[] inLines)
		{
			this.lines = inLines;
			this.place = inLines.length - 1;
		}

		public ReverseIterator(SingleLinkedList<IrInstruction> inLines)
		{
			this(inLines.toArray(new IrInstruction[inLines.getSize()]));
		}

		@Override
		public boolean hasNext()
		{
			return this.place >= 0;
		}

		@Override
		public IrInstruction next()
		{
			return this.lines[this.place--];
		}
	}
}
