package LuaBytecodeDecompiler.Mk03.ControlFlow;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.OneLinkIntList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_Jump;

public class InstructionPathTracer
{
	private final AnnoInstruction[] instructions;
	private final OneLinkIntList[] traces;
	private int pathMarkNum;
	private final SingleLinkedList<pathMark> livePathQueue;
	private final SingleLinkedList<pathMark> donePathQueue;

	public InstructionPathTracer(AnnoInstruction[] in)
	{
		this.instructions = in;
		this.traces = initListArr(in.length);
		this.pathMarkNum = 1;
		this.livePathQueue = new SingleLinkedList<pathMark>();
		this.livePathQueue.add(new pathMark(0, 0));
		this.donePathQueue = new SingleLinkedList<pathMark>();
	}

	private static final OneLinkIntList[] initListArr(int length)
	{
		final OneLinkIntList[] result = new OneLinkIntList[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = new OneLinkIntList();
		}

		return result;
	}

	public void process()
	{
		while (!this.livePathQueue.isEmpty())
		{
			this.takeStep(this.livePathQueue.pop());
		}
	}

	private void takeStep(pathMark current)
	{
		final AnnoInstruction curStep = this.instructions[current.instructionPlace];

		this.traces[current.instructionPlace].add(current.pathNum);

		switch (curStep.getOpCode())
		{
			case Jump:
			{
				final int offset = ((AnnoInstruction_Jump) curStep).getOffset();

				if (offset > 0)
				{
					final pathMark next = new pathMark(this.pathMarkNum++, current.instructionPlace + offset + 1);

					this.livePathQueue.add(next);
					
					current.end();
					this.donePathQueue.add(current);
				}
				else
				{
					/*
					int locPlace = current.instructionPlace + offset;
					final int Id = this.pathMarkNum++;

					for (; locPlace <= current.instructionPlace; locPlace++)
					{
						this.traces[locPlace].add(Id);
					}
					*/

					current.end();
					this.donePathQueue.add(current);
				}

				break;
			}
			case Return:
			{
				this.donePathQueue.add(current);
				break;
			}
			case TestIfEqual:
			case TestIfLessThan:
			case TestIfLessThanOrEqual:
			case TestLogicalComparison:
			case TestAndSaveLogicalComparison:
			{
				final pathMark split = new pathMark(this.pathMarkNum++, current.instructionPlace + 2);
				final pathMark split2 = new pathMark(this.pathMarkNum++, current.instructionPlace + 1);

				this.livePathQueue.add(split);
				this.livePathQueue.add(split2);
				
				current.end();
				
				this.donePathQueue.add(current);
				break;
			}
			case Add:
			case ArithmeticNegation:
			case Call:
			case Close:
			case Closure:
			case Concatenation:
			case Divide:
			case GetGlobal:
			case GetTable:
			case GetUpvalue:
			case InitNumericForLoop:
			case IterateGenericForLoop:
			case IterateNumericForLoop:
			case Length:
			case LoadBool:
			case LoadK:
			case LoadNull:
			case LogicalNegation:
			case Modulo:
			case Move:
			case Multiply:
			case NewTable:
			case NoOp:
			case Power:
			case Self:
			case SetGlobal:
			case SetList:
			case SetTable:
			case SetUpvalue:
			case Subtract:
			case TailCall:
			case VarArg:
			default:
			{
				current.incrementPlace();
				this.livePathQueue.add(current);
				break;
			}
		}
	}

	public CharList toCharList()
	{
		final CharList result = new CharList();

		result.add("total number of paths: ");
		result.add(Integer.toString(this.donePathQueue.getSize()));
		result.addNewLine();
		result.add("Paths: ");

		for (int i = 0; i < this.instructions.length; i++)
		{
			result.addNewLine();
			//MiscAutobot.append(this.instructions[i].getOpCode().toString(), result, ' ', 25);
			result.add(this.instructions[i].toCharList(false), true);
			
			//result.add("||| ");
			final OneLinkIntList cur = this.traces[i];

			if (!cur.isEmpty())
			{
				result.add(Integer.toString(cur.pop()));

				while (!cur.isEmpty())
				{
					result.add(", ");
					result.add(Integer.toString(cur.pop()));
				}
			}
		}

		return result;
	}

	private static class pathMark
	{
		private final int pathNum;
		private int instructionPlace;
		private int start;
		private int end;

		public pathMark(int inPathNum, int inInstructionPlace)
		{
			this.pathNum = inPathNum;
			this.instructionPlace = inInstructionPlace;
			this.start = this.instructionPlace;
			this.end = -1;
		}

		public void incrementPlace()
		{
			this.instructionPlace++;
		}

		public void addToInstructionPlace(int in)
		{
			this.instructionPlace += in;
		}
		
		public void end()
		{
			this.end = this.instructionPlace;
		}
	}
}
