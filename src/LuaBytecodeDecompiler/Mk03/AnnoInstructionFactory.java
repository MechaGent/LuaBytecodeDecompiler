package LuaBytecodeDecompiler.Mk03;

import java.util.Iterator;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABC;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABx;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iAsBx;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Call;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Close;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Concat;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_GenericForLoop;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_GetTable;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_GetUpvalue;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Length;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_LoadBool;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_LoadNull;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Math;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Move;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Negation;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_NewTable;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Relational;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Return;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Self;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetList;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetTable;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_SetUpvalue;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_TailCall;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_Test;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_TestSet;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_VarArg;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_Closure;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_GetGlobal;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_LoadK;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx.AnnoInstruction_SetGlobal;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_ForLoop;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_ForPrep;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_Jump;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_String;

public class AnnoInstructionFactory
{
	private static int RK_Mask = 0x100;

	private final HalfByteRadixMap<AnnoVar> Globals;
	private final AnnoVar[] Locals;
	private final AnnoVar[] Constants;
	private final AnnoVar[] Upvalues;
	private final AnnoFunction[] Functions;
	private final String[] inverseLabels;
	private int numLabels;

	public AnnoInstructionFactory(HalfByteRadixMap<AnnoVar> inGlobals, AnnoVar[] inLocals, AnnoVar[] inConstants, AnnoVar[] inUpvalues, AnnoFunction[] inFunctions, int numInstructions)
	{
		this.Globals = inGlobals;
		this.Locals = inLocals;
		this.Constants = inConstants;
		this.Upvalues = inUpvalues;
		this.Functions = inFunctions;
		this.inverseLabels = new String[numInstructions];
		this.numLabels = 0;
	}
	
	public HalfByteRadixMap<Integer> getLabelsMap()
	{
		final HalfByteRadixMap<Integer> result = new HalfByteRadixMap<Integer>();
		
		for(int i = 0; i < this.inverseLabels.length; i++)
		{
			final String loc = this.inverseLabels[i];
			
			if(loc != null)
			{
				result.add(loc, i);
			}
		}
		
		return result;
	}
	
	public String[] getLabelsSparseArr()
	{
		return this.inverseLabels;
	}

	public AnnoInstruction parseInstruction(InstructionIterator in)
	{
		final AnnoInstruction result;
		final Instruction current = in.next();
		
		//System.out.println("current instruction: " + current.disAssemble().toString());

		switch (current.getType())
		{
			case iABC:
			{
				result = this.parseInstruction((Instruction_iABC) current);
				break;
			}
			case iABx:
			{
				result = this.parseInstruction((Instruction_iABx) current, in);
				break;
			}
			case iAsBx:
			{
				result = this.parseInstruction((Instruction_iAsBx) current, in.getPlace());
				break;
			}
			default:
			{
				throw new IllegalArgumentException(current.getType().toString());
			}

		}

		return result;
	}

	private AnnoInstruction parseInstruction(Instruction_iABC in)
	{
		final AnnoInstruction result;

		switch (in.getOpCode())
		{
			case Move:
			{
				final AnnoVar target = this.Locals[in.getA()];
				final AnnoVar copy = this.Locals[in.getB()];
				result = new AnnoInstruction_Move(in.getRaw(), in.getInstructionAddress(), target, copy);
				break;
			}
			case LoadNull:
			{
				final int A = in.getA();
				final int Limit = in.getB() + 1;
				final AnnoVar[] vars = new AnnoVar[Limit - A];
				final AnnoVar nully = AnnoVar.getNullAnnoVar();

				for (int i = A; i < Limit; i++)
				{
					vars[i - A] = nully;
					this.Locals[i] = nully;
				}

				result = new AnnoInstruction_LoadNull(in.getRaw(), in.getInstructionAddress(), vars);
				break;
			}
			case LoadBool:
			{
				result = new AnnoInstruction_LoadBool(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], in.getB() != 0, in.getC() != 0);
				break;
			}
			case GetUpvalue:
			{
				result = new AnnoInstruction_GetUpvalue(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Upvalues[in.getB()]);
				break;
			}
			case SetUpvalue:
			{
				result = new AnnoInstruction_SetUpvalue(in.getRaw(), in.getInstructionAddress(), this.Upvalues[in.getB()], this.Locals[in.getA()]);
				break;
			}
			case GetTable:
			{
				result = new AnnoInstruction_GetTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Locals[in.getB()], this.processRK(in.getC()));
				break;
			}
			case SetTable:
			{
				result = new AnnoInstruction_SetTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.processRK(in.getB()), this.processRK(in.getC()));
				break;
			}
			case Add:
			case Subtract:
			case Multiply:
			case Divide:
			case Modulo:
			case Power:
			{
				result = new AnnoInstruction_Math(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), this.Locals[in.getA()], this.processRK(in.getB()), this.processRK(in.getC()));
				break;
			}
			case ArithmeticNegation:
			case LogicalNegation:
			{
				result = new AnnoInstruction_Negation(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), this.Locals[in.getA()], this.Locals[in.getB()]);
				break;
			}
			case Length:
			{
				result = new AnnoInstruction_Length(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Locals[in.getB()]);
				break;
			}
			case Concatenation:
			{
				final int B = in.getB();
				final int C = in.getC();

				final AnnoVar[] sources = new AnnoVar[C - B + 1];

				for (int i = B; i <= C; i++)
				{
					sources[i - B] = this.Locals[i];
				}

				result = new AnnoInstruction_Concat(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], sources);
				break;
			}
			case Call:
			{
				final int A = in.getA();
				final int B = in.getB();
				final int C = in.getC();

				final AnnoVar[] targets;
				final AnnoVar function = this.getFunctionForm(this.Locals[A]);
				final AnnoVar[] params;

				switch (B)
				{
					case 0:
					{
						params = new AnnoVar[this.Locals.length - (A + 1)];

						for (int i = 0; i < params.length; i++)
						{
							params[i] = this.Locals[i + A + 1];
						}

						break;
					}
					case 1:
					{
						params = new AnnoVar[0];
						break;
					}
					default:
					{
						params = new AnnoVar[B - 1];

						for (int i = A + 1; i < A + B; i++)
						{
							params[i - (A + 1)] = this.Locals[i];
						}

						break;
					}
				}

				//System.out.println("C: " + C);
				switch (C)
				{
					case 0:
					{
						targets = new AnnoVar[this.Locals.length - A];

						for (int i = 0; i < targets.length; i++)
						{
							final AnnoVar loc = this.Locals[i + A];
							
							if(loc == null)
							{
								throw new IllegalArgumentException("null at index: " + (i + A + 1));
							}
							
							targets[i] = loc;
						}
						break;
					}
					case 1:
					{
						targets = new AnnoVar[0];
						break;
					}
					default:
					{
						targets = new AnnoVar[C - 1];

						for (int i = 0; i < targets.length; i++)
						{
							final AnnoVar loc = this.Locals[i + A + 1];
							
							if(loc == null)
							{
								throw new IllegalArgumentException("null at index: " + (i + A + 1));
							}
							
							targets[i] = loc;
						}
						break;
					}
				}

				result = new AnnoInstruction_Call(in.getRaw(), in.getInstructionAddress(), targets, function, params);

				// result = new AnnoInstruction_Call(in.getRaw(), in.getInstructionAddress(), new AnnoVar[0], AnnoVar.getNullAnnoVar(), new AnnoVar[0]);
				break;
			}
			case Return:
			{
				final AnnoVar[] basket;
				final int A = in.getA();
				final int B = in.getB();

				switch (B)
				{
					case 0:
					{
						basket = new AnnoVar[this.Locals.length - A + 1];

						for (int i = 0; i < basket.length; i++)
						{
							basket[i] = this.Locals[i + A];
						}
						break;
					}
					case 1:
					{
						basket = new AnnoVar[0];
						break;
					}
					default:
					{
						basket = new AnnoVar[B - 1];

						for (int i = A; i < A + B - 1; i++)
						{
							basket[i - A] = this.Locals[i];
						}

						break;
					}
				}

				result = new AnnoInstruction_Return(in.getRaw(), in.getInstructionAddress(), basket);
				break;
			}
			case TailCall:
			{
				final int A = in.getA();
				final int B = in.getB();
				final AnnoVar_Function function = (AnnoVar_Function) this.getFunctionForm(this.Locals[A]);
				final AnnoVar[] basket;

				switch (B)
				{
					case 0:
					{
						basket = new AnnoVar[this.Locals.length - A + 1];

						for (int i = 0; i < basket.length; i++)
						{
							basket[i] = this.Locals[i + A];
						}

						break;
					}
					case 1:
					{
						basket = new AnnoVar[0];
						break;
					}
					default:
					{
						basket = new AnnoVar[B - 1];

						for (int i = A; i < A + B - 1; i++)
						{
							basket[i - A] = this.Locals[i];
						}

						break;
					}
				}

				result = new AnnoInstruction_TailCall(in.getRaw(), in.getInstructionAddress(), function, basket);
				break;
			}
			case VarArg:
			{
				// final int A = in.getA();
				// final int B = in.getB();

				final AnnoVar[] additionalArgs;
				final AnnoVar[] targets;

				// TODO - figure out how it knows how many vars to copy
				
				if(in.getB() == 0)
				{
					/*
					//I think this is right, really unsure though
					additionalArgs = new AnnoVar[this.Locals.length - in.getA()];
					*/
					
					//okay, if I'm reading the source correctly, it basically derives the length from stack differences ie @runtime.
					additionalArgs = new AnnoVar[0];
					targets = new AnnoVar[additionalArgs.length];
				}
				else
				{
					final int A = in.getA();
					final int B = in.getB();
					
					additionalArgs = new AnnoVar[B - 1];
					targets = new AnnoVar[additionalArgs.length];
					
					for(int i = 0; i < additionalArgs.length; i++)
					{
						additionalArgs[i] = new AnnoVar("Param_" + i, ScopeLevels.Parameter);
						targets[i] = this.Locals[i+A];
					}
				}

				result = new AnnoInstruction_VarArg(in.getRaw(), in.getInstructionAddress(), additionalArgs, targets);
				break;
			}
			case Self:
			{
				final int A = in.getA();
				final int B = in.getB();

				result = new AnnoInstruction_Self(in.getRaw(), in.getInstructionAddress(), this.Locals[A + 1], this.Locals[B], this.Locals[A], this.processRK(in.getC()));
				break;
			}
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			{
				final boolean A = (in.getA() != 0);
				final AnnoVar operand1 = this.processRK(in.getB());
				final AnnoVar operand2 = this.processRK(in.getC());

				result = new AnnoInstruction_Relational(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), A, operand1, operand2);
				break;
			}
			case TestLogicalComparison:
			{
				final boolean C = in.getC() != 0;
				final AnnoVar source = this.Locals[in.getA()];

				result = new AnnoInstruction_Test(in.getRaw(), in.getInstructionAddress(), source, C);
				break;
			}
			case TestAndSaveLogicalComparison:
			{
				final AnnoVar source = this.Locals[in.getB()];
				final AnnoVar target = this.Locals[in.getA()];
				final boolean C = in.getC() != 0;

				result = new AnnoInstruction_TestSet(in.getRaw(), in.getInstructionAddress(), target, source, C);
				break;
			}
			case IterateGenericForLoop:
			{
				final int A = in.getA();
				final int C = in.getC();
				final AnnoVar_Function rA = (AnnoVar_Function) this.getFunctionForm(this.Locals[A]);
				final AnnoVar rA1 = this.Locals[A + 1];
				final AnnoVar rA2 = this.Locals[A + 2];
				final AnnoVar[] rA3 = new AnnoVar[C];

				final int offset = A + 3;

				for (int i = 0; i < rA3.length; i++)
				{
					rA3[i] = this.Locals[i + offset];
				}

				result = new AnnoInstruction_GenericForLoop(in.getRaw(), in.getInstructionAddress(), rA, rA1, rA2, rA3);

				break;
			}
			case NewTable:
			{
				final int B = (int) MiscAutobot.parseAsFloatingPointByte(in.getB());
				final int C = (int) MiscAutobot.parseAsFloatingPointByte(in.getC());

				result = new AnnoInstruction_NewTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], B, C);
				break;
			}
			case SetList:
			{
				final int A = in.getA();
				final int B = in.getB();
				final AnnoVar[] sources;

				if (B == 0)
				{
					sources = new AnnoVar[this.Locals.length - A];
				}
				else
				{
					sources = new AnnoVar[B];
				}

				for (int i = 0; i < sources.length; i++)
				{
					sources[i] = this.Locals[A + i + 1];
				}

				result = new AnnoInstruction_SetList(in.getRaw(), in.getInstructionAddress(), this.Locals[A], sources, B, in.getC());

				break;
			}
			case Close:
			{
				final int A = in.getA();
				final int limit = this.Locals.length - A;
				final AnnoVar[] closeTargets = new AnnoVar[limit];

				for (int i = 0; i < limit; i++)
				{
					closeTargets[i] = this.Locals[A + i];
				}

				result = new AnnoInstruction_Close(in.getRaw(), in.getInstructionAddress(), closeTargets);

				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private AnnoInstruction parseInstruction(Instruction_iABx in, InstructionIterator itsy)
	{
		final AnnoInstruction result;

		switch (in.getOpCode())
		{
			case LoadK:
			{
				result = new AnnoInstruction_LoadK(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Constants[in.getBx()]);
				break;
			}
			case GetGlobal:
			{
				final String globalVarName = ((AnnoVar_String) this.Constants[in.getBx()]).getValue();
				AnnoVar global = this.Globals.get(globalVarName);

				if (global == null)
				{
					global = new AnnoVar(globalVarName, ScopeLevels.Global);
					this.Globals.add(globalVarName, global);
				}

				final AnnoVar target = this.Locals[in.getA()];
				result = new AnnoInstruction_GetGlobal(in.getRaw(), in.getInstructionAddress(), target, global);
				//result = new AnnoInstruction_GetGlobal(in.getRaw(), in.getInstructionAddress(), this.Locals[0], global);
				break;
			}
			case SetGlobal:
			{
				final String globalVarName = ((AnnoVar_String) this.Constants[in.getBx()]).getValue();
				AnnoVar global = this.Globals.get(globalVarName);

				if (global == null)
				{
					global = new AnnoVar(globalVarName, ScopeLevels.Global);
					this.Globals.add(globalVarName, global);
				}

				result = new AnnoInstruction_SetGlobal(in.getRaw(), in.getInstructionAddress(), global, this.Locals[in.getA()]);
				break;
			}
			case Closure:
			{
				final int A = in.getA();
				final AnnoVar target = this.Locals[A];
				final AnnoVar_Function source = this.Functions[in.getBx()].getAsAnnoVar();
				final SingleLinkedList<AnnoVar> argsList = new SingleLinkedList<AnnoVar>();
				boolean isDone = false;

				while (itsy.hasNext() && !isDone)
				{
					final Instruction next = itsy.peekAtNext();
					// System.out.println(next.disAssemble().toString());

					switch (next.getOpCode())
					{
						case Move:
						{
							final Instruction_iABC caster = (Instruction_iABC) next;
							argsList.add(this.Locals[caster.getB()]);
							itsy.next();
							break;
						}
						case GetUpvalue:
						{
							final Instruction_iABC caster = (Instruction_iABC) next;
							argsList.add(this.Upvalues[caster.getB()]);
							itsy.next();
							break;
						}
						default:
						{
							// throw new UnhandledEnumException(next.getOpCode());
							isDone = true;
							break;
						}
					}
				}

				result = new AnnoInstruction_Closure(in.getRaw(), in.getInstructionAddress(), target, source, argsList.toArray(new AnnoVar[argsList.getSize()]));

				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private AnnoInstruction parseInstruction(Instruction_iAsBx in, int stepNumber)
	{
		final AnnoInstruction result;

		switch (in.getOpCode())
		{
			case Jump:
			{
				final int offset = stepNumber + in.get_sBx();
				
				String label = this.inverseLabels[offset];
				
				if(label == null)
				{
					label = "label_" + (this.numLabels++);
					this.inverseLabels[offset] = label;
				}
				
				result = new AnnoInstruction_Jump(in.getRaw(), in.getInstructionAddress(), in.get_sBx(), label);
				break;
			}
			case InitNumericForLoop:
			{
				final int A = in.getA();
				result = new AnnoInstruction_ForPrep(in.getRaw(), in.getInstructionAddress(), this.Locals[A], this.Locals[A + 2], this.Locals[A + 3], in.get_sBx());
				break;
			}
			case IterateNumericForLoop:
			{
				final int A = in.getA();
				final AnnoVar rA = this.Locals[A];
				final AnnoVar rA1 = this.Locals[A + 1];
				final AnnoVar rA2 = this.Locals[A + 2];
				final AnnoVar rA3 = this.Locals[A + 3];

				result = new AnnoInstruction_ForLoop(in.getRaw(), in.getInstructionAddress(), rA, rA1, rA2, rA3, in.get_sBx());
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}

		return result;
	}

	private AnnoVar processRK(int index)
	{
		final int compare = index & RK_Mask;
		final AnnoVar result;

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

	private AnnoVar getFunctionForm(AnnoVar in)
	{
		// System.out.println("attempting to resolve function: " + in.getAdjustedName());

		if (in instanceof AnnoVar_Function)
		{
			return (AnnoVar_Function) in;
		}
		else
		{
			return in;
		}
	}

	public boolean checkIfGlobalVar(String in)
	{
		return this.Globals.containsKey(in);
	}

	public static class InstructionIterator implements Iterator<Instruction>
	{
		private final Instruction[] core;
		private int place;

		public InstructionIterator(Instruction[] inCore)
		{
			this.core = inCore;
			this.place = 0;
		}

		@Override
		public boolean hasNext()
		{
			return this.place < this.core.length;
		}

		@Override
		public Instruction next()
		{
			final Instruction result = this.core[this.place++];

			// System.out.println("dispensing: " + result.disAssemble().toString());

			return result;
		}

		public Instruction peekAtNext()
		{
			final Instruction result = this.core[this.place];

			// System.out.println("peeking: " + result.disAssemble().toString());

			return result;
		}
		
		public int getPlace()
		{
			return this.place;
		}
	}

	/*
	 * private AnnoInstruction parseInstruction(Instruction_iABC in)
	{
		final AnnoInstruction result;
	
		switch (in.getOpCode())
		{
			case Move:
			{
				result = new AnnoInstruction_Move(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Locals[in.getB()]);
				break;
			}
			case LoadNull:
			{
				final int A = in.getA();
				final int Limit = in.getB() + 1;
				final AnnoVar[] vars = new AnnoVar[Limit - A];
	
				for (int i = A; i < Limit; i++)
				{
					vars[i - A] = this.Locals[i];
				}
	
				result = new AnnoInstruction_LoadNull(in.getRaw(), in.getInstructionAddress(), vars);
				break;
			}
			case LoadBool:
			{
				result = new AnnoInstruction_LoadBool(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], in.getB() != 0, in.getC() != 0);
				break;
			}
			case GetUpvalue:
			{
				result = new AnnoInstruction_GetUpvalue(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Upvalues[in.getB()]);
				break;
			}
			case SetUpvalue:
			{
				result = new AnnoInstruction_SetUpvalue(in.getRaw(), in.getInstructionAddress(), this.Upvalues[in.getB()], this.Locals[in.getA()]);
				break;
			}
			case GetTable:
			{
				result = new AnnoInstruction_GetTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Locals[in.getB()], this.processRK(in.getC()));
				break;
			}
			case SetTable:
			{
				result = new AnnoInstruction_SetTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.processRK(in.getB()), this.processRK(in.getC()));
				break;
			}
			case Add:
			case Subtract:
			case Multiply:
			case Divide:
			case Modulo:
			case Power:
			{
				result = new AnnoInstruction_Math(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), this.Locals[in.getA()], this.processRK(in.getB()), this.processRK(in.getC()));
				break;
			}
			case ArithmeticNegation:
			case LogicalNegation:
			{
				result = new AnnoInstruction_Negation(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), this.Locals[in.getA()], this.Locals[in.getB()]);
				break;
			}
			case Length:
			{
				result = new AnnoInstruction_Length(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Locals[in.getB()]);
				break;
			}
			case Concatenation:
			{
				final int B = in.getB();
				final int C = in.getC();
	
				final AnnoVar[] sources = new AnnoVar[C - B + 1];
	
				for (int i = B; i <= C; i++)
				{
					sources[i - B] = this.Locals[i];
				}
	
				result = new AnnoInstruction_Concat(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], sources);
				break;
			}
			case Call:
			{
				final int A = in.getA();
				final int B = in.getB();
				final int C = in.getC();
	
				final AnnoVar[] targets;
				final AnnoVar function = this.getFunctionForm(this.Locals[A]);
				final AnnoVar[] params;
	
				switch (B)
				{
					case 0:
					{
						params = new AnnoVar[this.Locals.length - (A + 1)];
	
						for (int i = 0; i < params.length; i++)
						{
							params[i] = this.Locals[i + A + 1];
						}
	
						break;
					}
					case 1:
					{
						params = new AnnoVar[0];
						break;
					}
					default:
					{
						params = new AnnoVar[B - 1];
	
						for (int i = A + 1; i < A + B; i++)
						{
							params[i - (A + 1)] = this.Locals[i];
						}
	
						break;
					}
				}
	
				switch (C)
				{
					case 0:
					{
						targets = new AnnoVar[this.Locals.length - A];
	
						for (int i = 0; i < params.length; i++)
						{
							targets[i] = this.Locals[i + A];
						}
						break;
					}
					case 1:
					{
						targets = new AnnoVar[0];
						break;
					}
					default:
					{
						targets = new AnnoVar[C - 1];
	
						for (int i = A + 1; i < (A + C - 1); i++)
						{
							targets[i - (A + 1)] = this.Locals[i];
						}
						break;
					}
				}
	
				result = new AnnoInstruction_Call(in.getRaw(), in.getInstructionAddress(), targets, function, params);
	
				// result = new AnnoInstruction_Call(in.getRaw(), in.getInstructionAddress(), new AnnoVar[0], AnnoVar.getNullAnnoVar(), new AnnoVar[0]);
				break;
			}
			case Return:
			{
				final AnnoVar[] basket;
				final int A = in.getA();
				final int B = in.getB();
	
				switch (B)
				{
					case 0:
					{
						basket = new AnnoVar[this.Locals.length - A + 1];
	
						for (int i = 0; i < basket.length; i++)
						{
							basket[i] = this.Locals[i + A];
						}
						break;
					}
					case 1:
					{
						basket = new AnnoVar[0];
						break;
					}
					default:
					{
						basket = new AnnoVar[B - 1];
	
						for (int i = A; i < A + B - 1; i++)
						{
							basket[i - A] = this.Locals[i];
						}
	
						break;
					}
				}
	
				result = new AnnoInstruction_Return(in.getRaw(), in.getInstructionAddress(), basket);
				break;
			}
			case TailCall:
			{
				final int A = in.getA();
				final int B = in.getB();
				final AnnoVar_Function function = (AnnoVar_Function) this.getFunctionForm(this.Locals[A]);
				final AnnoVar[] basket;
	
				switch (B)
				{
					case 0:
					{
						basket = new AnnoVar[this.Locals.length - A + 1];
	
						for (int i = 0; i < basket.length; i++)
						{
							basket[i] = this.Locals[i + A];
						}
	
						break;
					}
					case 1:
					{
						basket = new AnnoVar[0];
						break;
					}
					default:
					{
						basket = new AnnoVar[B - 1];
	
						for (int i = A; i < A + B - 1; i++)
						{
							basket[i - A] = this.Locals[i];
						}
	
						break;
					}
				}
	
				result = new AnnoInstruction_TailCall(in.getRaw(), in.getInstructionAddress(), function, basket);
				break;
			}
			case VarArg:
			{
				// final int A = in.getA();
				// final int B = in.getB();
	
				final AnnoVar[] additionalArgs;
	
				// TODO - figure out how it knows how many vars to copy
				additionalArgs = new AnnoVar[0];
	
				result = new AnnoInstruction_VarArg(in.getRaw(), in.getInstructionAddress(), additionalArgs);
				break;
			}
			case Self:
			{
				final int A = in.getA();
				final int B = in.getB();
	
				result = new AnnoInstruction_Self(in.getRaw(), in.getInstructionAddress(), this.Locals[A + 1], this.Locals[B], this.Locals[A], this.processRK(in.getC()));
				break;
			}
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			{
				final boolean A = (in.getA() != 0);
				final AnnoVar operand1 = this.processRK(in.getB());
				final AnnoVar operand2 = this.processRK(in.getC());
	
				result = new AnnoInstruction_Relational(in.getRaw(), in.getInstructionAddress(), in.getOpCode(), A, operand1, operand2);
				break;
			}
			case TestLogicalComparison:
			{
				final boolean C = in.getC() != 0;
				final AnnoVar source = this.Locals[in.getA()];
	
				result = new AnnoInstruction_Test(in.getRaw(), in.getInstructionAddress(), source, C);
				break;
			}
			case TestAndSaveLogicalComparison:
			{
				final AnnoVar source = this.Locals[in.getB()];
				final AnnoVar target = this.Locals[in.getA()];
				final boolean C = in.getC() != 0;
	
				result = new AnnoInstruction_TestSet(in.getRaw(), in.getInstructionAddress(), target, source, C);
				break;
			}
			case IterateGenericForLoop:
			{
				final int A = in.getA();
				final int C = in.getC();
				final AnnoVar_Function rA = (AnnoVar_Function) this.getFunctionForm(this.Locals[A]);
				final AnnoVar rA1 = this.Locals[A + 1];
				final AnnoVar rA2 = this.Locals[A + 2];
				final AnnoVar[] rA3 = new AnnoVar[C];
	
				final int offset = A + 3;
	
				for (int i = 0; i < rA3.length; i++)
				{
					rA3[i] = this.Locals[i + offset];
				}
	
				result = new AnnoInstruction_GenericForLoop(in.getRaw(), in.getInstructionAddress(), rA, rA1, rA2, rA3);
	
				break;
			}
			case NewTable:
			{
				final int B = (int) MiscAutobot.parseAsFloatingPointByte(in.getB());
				final int C = (int) MiscAutobot.parseAsFloatingPointByte(in.getC());
	
				result = new AnnoInstruction_NewTable(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], B, C);
				break;
			}
			case SetList:
			{
				final int A = in.getA();
				final int B = in.getB();
				final AnnoVar[] sources;
	
				if (B == 0)
				{
					sources = new AnnoVar[this.Locals.length - A];
				}
				else
				{
					sources = new AnnoVar[B];
				}
	
				for (int i = 0; i < sources.length; i++)
				{
					sources[i] = this.Locals[A + i + 1];
				}
	
				result = new AnnoInstruction_SetList(in.getRaw(), in.getInstructionAddress(), this.Locals[A], sources, B, in.getC());
	
				break;
			}
			case Close:
			{
				final int A = in.getA();
				final int limit = this.Locals.length - A;
				final AnnoVar[] closeTargets = new AnnoVar[limit];
	
				for (int i = 0; i < limit; i++)
				{
					closeTargets[i] = this.Locals[A + i];
				}
	
				result = new AnnoInstruction_Close(in.getRaw(), in.getInstructionAddress(), closeTargets);
	
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}
	
		return result;
	}
	
	private AnnoInstruction parseInstruction(Instruction_iABx in, InstructionIterator itsy)
	{
		final AnnoInstruction result;
	
		switch (in.getOpCode())
		{
			case LoadK:
			{
				result = new AnnoInstruction_LoadK(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], this.Constants[in.getBx()]);
				break;
			}
			case GetGlobal:
			{
				final String globalVarName = ((AnnoVar_String) this.Constants[in.getBx()]).getValue();
				AnnoVar global = this.Globals.get(globalVarName);
	
				if (global == null)
				{
					global = new AnnoVar(globalVarName, ScopeLevels.Global);
					this.Globals.add(globalVarName, global);
				}
	
				result = new AnnoInstruction_GetGlobal(in.getRaw(), in.getInstructionAddress(), this.Locals[in.getA()], global);
				break;
			}
			case SetGlobal:
			{
				final String globalVarName = ((AnnoVar_String) this.Constants[in.getBx()]).getValue();
				AnnoVar global = this.Globals.get(globalVarName);
	
				if (global == null)
				{
					global = new AnnoVar(globalVarName, ScopeLevels.Global);
					this.Globals.add(globalVarName, global);
				}
	
				result = new AnnoInstruction_SetGlobal(in.getRaw(), in.getInstructionAddress(), global, this.Locals[in.getA()]);
				break;
			}
			case Closure:
			{
				final int A = in.getA();
				final AnnoVar target = this.Locals[A];
				final AnnoVar_Function source = this.Functions[in.getBx()].getAsAnnoVar();
				final SingleLinkedList<AnnoVar> argsList = new SingleLinkedList<AnnoVar>();
				boolean isDone = false;
				
				while(itsy.hasNext() && !isDone)
				{
					final Instruction next = itsy.peekAtNext();
					//System.out.println(next.disAssemble().toString());
	
					switch (next.getOpCode())
					{
						case Move:
						{
							final Instruction_iABC caster = (Instruction_iABC) next;
							argsList.add(this.Locals[caster.getB()]);
							itsy.next();
							break;
						}
						case GetUpvalue:
						{
							final Instruction_iABC caster = (Instruction_iABC) next;
							argsList.add(this.Upvalues[caster.getB()]);
							itsy.next();
							break;
						}
						default:
						{
							//throw new UnhandledEnumException(next.getOpCode());
							isDone = true;
							break;
						}
					}
				}
	
				result = new AnnoInstruction_Closure(in.getRaw(), in.getInstructionAddress(), target, source, argsList.toArray(new AnnoVar[argsList.getSize()]));
	
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}
	
		return result;
	}
	
	private AnnoInstruction parseInstruction(Instruction_iAsBx in)
	{
		final AnnoInstruction result;
	
		switch (in.getOpCode())
		{
			case Jump:
			{
				result = new AnnoInstruction_Jump(in.getRaw(), in.getInstructionAddress(), in.get_sBx());
				break;
			}
			case InitNumericForLoop:
			{
				final int A = in.getA();
				result = new AnnoInstruction_ForPrep(in.getRaw(), in.getInstructionAddress(), this.Locals[A], this.Locals[A + 2], in.get_sBx());
				break;
			}
			case IterateNumericForLoop:
			{
				final int A = in.getA();
				final AnnoVar rA = this.Locals[A];
				final AnnoVar rA1 = this.Locals[A + 1];
				final AnnoVar rA2 = this.Locals[A + 2];
				final AnnoVar rA3 = this.Locals[A + 3];
	
				result = new AnnoInstruction_ForLoop(in.getRaw(), in.getInstructionAddress(), rA, rA1, rA2, rA3, in.get_sBx());
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in.getOpCode());
			}
		}
	
		return result;
	}
	 */
}
