package LuaBytecodeDecompiler.Mk03.ControlFlow;

import java.util.Iterator;
import java.util.Map.Entry;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_Jump;

public class InstructionPathTracer2
{
	public static CharList toAnnotatedCharList(AnnoInstruction[] instructions, boolean[] Breaks)
	{
		final CharList result = new CharList();

		for (int i = 0; i < instructions.length; i++)
		{
			if (i != 0)
			{
				result.addNewLine();
			}

			if (Breaks[i])
			{
				result.add("true |\t\t");
			}
			else
			{
				result.add("false|\t\t");
			}

			result.add(instructions[i].toCharList(true), true);
		}

		return result;
	}

	public static CodeChunk getAsGraph(HalfByteRadixMap<CodeChunk> in)
	{
		final Iterator<Entry<HalfByteArray, CodeChunk>> itsy = in.iterator();
		final CodeChunk first = itsy.next().getValue();

		first.getNextFromMap(in);

		while (itsy.hasNext())
		{
			final CodeChunk curr = itsy.next().getValue();
			curr.getNextFromMap(in);
		}

		return first;
	}

	public static HalfByteRadixMap<CodeChunk> mapFlowNodes(AnnoInstruction[] in)
	{
		final boolean[] Breaks = getBreaks(in);
		
		//System.out.println(Arrays.toString(Breaks));
		
		final HalfByteRadixMap<CodeChunk> map = new HalfByteRadixMap<CodeChunk>();

		int i = 0;

		while (i < in.length)
		{
			final SingleLinkedList<AnnoInstruction> current = new SingleLinkedList<AnnoInstruction>();
			final int startingOffset = i;

			while (!Breaks[i])
			{
				current.add(in[i]);
				//System.out.println("marked false: " + in[i].toString());
				i++;
			}

			current.add(in[i]);

			switch (in[i].getOpCode())
			{
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					current.add(in[++i]);
					map.put(startingOffset + 1, new twoBranchChunk(startingOffset, current.toArray(new AnnoInstruction[current.getSize()]), null, null));
					break;
				}
				case Jump:
				case Return:
				default:
				{
					map.put(startingOffset + 1, new CodeChunk(startingOffset, current.toArray(new AnnoInstruction[current.getSize()]), null));
					break;
				}
			}

			//System.out.println("chunk starts @line: " + i);
			i++;
		}

		return map;
	}

	public static boolean[] getBreaks(AnnoInstruction[] instructions)
	{
		final boolean[] result = new boolean[instructions.length];

		for (int i = 0; i < result.length; i++)
		{
			final AnnoInstruction instruction = instructions[i];

			switch (instruction.getOpCode())
			{
				case Jump:
				{
					final int offset = ((AnnoInstruction_Jump) instruction).getOffset() + i;
					
					//System.out.println("jumpSetting: " + offset);
					result[offset + 1] = true;
					//result[i+1] = true;
					break;
				}
				case Return:
				{
					result[i] = true;
					break;
				}
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					result[i] = true;
					
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
					break;
				}
			}
		}

		return result;
	}

	public static class CodeChunk
	{
		protected final int startingOffset;
		protected final AnnoInstruction[] contents;
		protected CodeChunk next;

		public CodeChunk(int inStartingOffset, AnnoInstruction[] inContents, CodeChunk inNext)
		{
			this.startingOffset = inStartingOffset;
			this.contents = inContents;
			this.next = inNext;
		}

		public AnnoInstruction[] getContents()
		{
			return this.contents;
		}

		public CodeChunk getNext()
		{
			return this.next;
		}

		public void getNextFromMap(HalfByteRadixMap<CodeChunk> map)
		{
			final AnnoInstruction last = this.contents[this.contents.length - 1];
			//System.out.println("triggering on: " + last.getOpCode());
			switch (last.getOpCode())
			{
				case Jump:
				{
					final int offset = ((AnnoInstruction_Jump) last).getOffset();
					final int trueOffset = this.startingOffset + this.contents.length + offset - 1;
					//System.out.println("trueOffset: " + trueOffset + ", offset: " + offset);
					this.next = map.get(trueOffset);

					if (this.next == null)
					{
						final HalfByteArray key = HalfByteArrayFactory.convertIntoArray(trueOffset);
						throw new IllegalArgumentException("bad lookup from map:\r\nattempted key: " + key.toString() + "\r\nmap:\r\n" + map.toString());
					}
					else
					{
						//System.out.println("linked properly!");
					}
					break;
				}
				case Return:
				{
					this.next = null;
					break;
				}
				default:
				{
					this.next = map.getRelativeTo(HalfByteArrayFactory.convertIntoArray(startingOffset), 1);

					if(this.next == this)
					{
						this.next = map.getRelativeTo(HalfByteArrayFactory.convertIntoArray(startingOffset), 2);
						//throw new IllegalArgumentException();
					}
					//this.next = null;
					break;
				}
			}
		}

		public CharList toCharList()
		{
			@SuppressWarnings("unchecked")
			final SingleLinkedList<CharList>[] result = this.toCharList_Internal(new SingleLinkedList[this.getLineCount(true)], 0, 0);
			final CharList out = new CharList();

			for (int i = 0; i < result.length; i++)
			{
				final SingleLinkedList<CharList> cur = result[i];

				if (cur != null)
				{
					out.addNewLine();

					while (!cur.isEmpty())
					{
						out.add(cur.pop(), true);
						out.add("|");
					}
				}
			}

			return out;
		}

		protected SingleLinkedList<CharList>[] toCharList_Internal(SingleLinkedList<CharList>[] result, int offset, int offset2)
		{
			for (int i = 0; i < this.contents.length; i++)
			{
				if (result[i + offset] == null)
				{
					result[i + offset] = new SingleLinkedList<CharList>();
				}

				result[i + offset].add(this.contents[i].toCharList(false));
			}

			final int locOffset = (this.contents.length > offset2) ? this.contents.length : offset2;

			if (this.next != null)
			{
				this.next.toCharList_Internal(result, offset + locOffset, 0);
			}

			return result;
		}

		public int getLineCount(boolean isInclusive)
		{
			int result = 0;

			if (this.next != null)
			{
				result = this.next.getLineCount(isInclusive);
			}

			if (isInclusive)
			{
				result += this.contents.length;
			}

			return result;
		}
		
		public CharList cascadeCharList()
		{
			final SingleLinkedList<CharList[]> queue = this.cascadeCharList_Internal(new SingleLinkedList<CharList[]>(), new CharList[this.contents.length]);
			final CharList result = new CharList();
			
			while(!queue.isEmpty())
			{
				final CharList[] cur = queue.pop();
				
				for(int i = 0; i < cur.length; i++)
				{
					result.addNewLine();
					final CharList curr = cur[i];
					
					if(curr == null)
					{
						throw new IllegalArgumentException(i+" of " + cur.length);
					}
					
					result.add(cur[i], true);
				}
				result.addNewLine();
			}
			
			return result;
		}
		
		protected SingleLinkedList<CharList[]> cascadeCharList_Internal(SingleLinkedList<CharList[]> result, CharList[] box)
		{
			final CharList intro = new CharList();
			intro.add("Chunk ");
			intro.add(Integer.toString(this.startingOffset));
			box[0] = intro;
			
			for(int i = 1; i < this.contents.length; i++)
			{
				if(box[i] == null)
				{
					box[i] = new CharList();
				}
				
				box[i].add(this.contents[i].toCharList(false), true);
				box[i].add(" | ");
			}
			
			final CharList jumplink = new CharList();
			
			if(this.next != null)
			{
				jumplink.add("GOTO ");
				jumplink.add(Integer.toString(this.next.startingOffset));
				
				final CharList[] inBox = new CharList[this.next.contents.length + 2];
				this.next.cascadeCharList_Internal(result, inBox);
				result.add(inBox);
			}
			
			box[box.length - 1] = jumplink;
			
			return result;
		}
	}

	public static class twoBranchChunk extends CodeChunk
	{
		private CodeChunk FalseNext;

		public twoBranchChunk(int inStartingOffset, AnnoInstruction[] inContents, CodeChunk inNext, CodeChunk inFalseNext)
		{
			super(inStartingOffset, inContents, inNext);
			this.FalseNext = inFalseNext;
		}

		public CodeChunk getFalseNext()
		{
			return this.FalseNext;
		}

		protected SingleLinkedList<CharList>[] toCharList_Internal(SingleLinkedList<CharList>[] result, int offset, int offset2)
		{
			for (int i = 0; i < this.contents.length; i++)
			{
				if (result[i + offset] == null)
				{
					result[i + offset] = new SingleLinkedList<CharList>();
				}

				result[i + offset].add(this.contents[i].toCharList(false));
			}

			final int locOffset = (this.contents.length > offset2) ? this.contents.length : offset2;
			final int nextLength = (this.next != null) ? this.next.contents.length : 0;
			final int falseNextLength = (this.FalseNext != null) ? this.FalseNext.contents.length : 0;

			if (nextLength != 0)
			{
				this.next.toCharList_Internal(result, offset + locOffset, falseNextLength);
			}

			if (falseNextLength != 0)
			{
				this.FalseNext.toCharList_Internal(result, offset + locOffset, nextLength);
			}

			return result;
		}

		@Override
		public void getNextFromMap(HalfByteRadixMap<CodeChunk> map)
		{
			final int lastIndex = this.contents.length - 1;
			final AnnoInstruction last = this.contents[lastIndex];
			switch (last.getOpCode())
			{
				case Jump:
				{
					final int offset = ((AnnoInstruction_Jump) last).getOffset();
					this.next = map.get(this.startingOffset + lastIndex + offset + 3);

					final AnnoInstruction secondLast = this.contents[lastIndex - 1];
					switch (secondLast.getOpCode())
					{
						case TestIfEqual:
						case TestIfLessThan:
						case TestIfLessThanOrEqual:
						case TestLogicalComparison:
						case TestAndSaveLogicalComparison:
						{
							this.FalseNext = map.get(this.startingOffset + lastIndex + 2);
							break;
						}
						default:
						{
							this.FalseNext = null;
							break;
						}
					}
					break;
				}
				case Return:
				{
					this.next = null;
					break;
				}

				default:
				{
					this.next = null;
					break;
				}
			}
		}

		@Override
		public int getLineCount(boolean isInclusive)
		{
			final int next = (this.next != null) ? this.next.getLineCount(isInclusive) : 0;
			final int falseNext = (this.FalseNext != null) ? this.FalseNext.getLineCount(isInclusive) : 0;

			int result = 0;

			if (next < falseNext)
			{
				result = falseNext;
			}
			else
			{
				result = next;
			}

			if (isInclusive)
			{
				result += this.contents.length;
			}

			return result;
		}
		
		@Override
		protected SingleLinkedList<CharList[]> cascadeCharList_Internal(SingleLinkedList<CharList[]> result, CharList[] box)
		{
			final CharList intro = new CharList();
			intro.add("Chunk ");
			intro.add(Integer.toString(this.startingOffset));
			box[0] = intro;
			
			//final int nextLength = (this.next != null)? this.next.contents.length : 0;
			//final int falseNextLength = (this.FalseNext != null)? this.FalseNext.contents.length : 0;
			//final int subLength = (nextLength > falseNextLength)? nextLength : falseNextLength;
			
			//final CharList[] inBox;
			
			
			
			/*
			if(subLength != 0)
			{
				inBox = new CharList[subLength];
				
				if(nextLength > 0)
				{
					this.next.cascadeCharList_Internal(result, inBox);
				}
				
				if(falseNextLength > 0)
				{
					this.FalseNext.cascadeCharList_Internal(result, inBox);
				}
				
				result.add(inBox);
			}
			else
			{
				inBox = new CharList[0];
			}
			*/
			
			for(int i = 1; i < this.contents.length; i++)
			{
				if(box[i] == null)
				{
					box[i] = new CharList();
				}
				
				box[i].add(this.contents[i].toCharList(false), true);
				box[i].add(" | ");
			}
			
			final CharList jumplink = new CharList();
			
			result.add(box);
			
			if(this.next != null)
			{
				//final CharList jumplink = new CharList();
				jumplink.add("GOTO ");
				jumplink.add(Integer.toString(this.next.startingOffset));
				
				final CharList[] inBox = new CharList[this.next.contents.length + 2];
				this.next.cascadeCharList_Internal(result, inBox);
			}
			
			if(this.FalseNext != null)
			{
				//final CharList jumplink = new CharList();
				
				if(jumplink.charSize() > 0)
				{
					jumplink.add(" or ");
				}
				
				jumplink.add("GOTO ");
				jumplink.add(Integer.toString(this.next.startingOffset));
				
				
				final CharList[] inBox = new CharList[this.next.contents.length + 2];
				this.next.cascadeCharList_Internal(result, inBox);
			}
			
			box[box.length - 1] = jumplink;
			
			return result;
		}
	}
}
