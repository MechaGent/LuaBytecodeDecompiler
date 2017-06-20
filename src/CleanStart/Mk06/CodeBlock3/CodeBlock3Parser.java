package CleanStart.Mk06.CodeBlock3;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import HalfByteRadixMap.HalfByteRadixMap;
import Linkable.LinkAnnulOptions;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock3Parser
{
	private CodeBlock3Parser()
	{
		//to prevent instantiation
	}
	
	public static CodeBlock3[] process(Instruction first)
	{
		CodeBlock3[] result = process_firstPass(first);
		
		return result;
	}
	
	private static CodeBlock3[] process_firstPass(Instruction first)
	{
		final SingleLinkedList<firstPassData> result = new SingleLinkedList<firstPassData>();
		final HalfByteRadixMap<firstPassData> map = new HalfByteRadixMap<firstPassData>();
		Instruction current = firstPassHelper_getFirstLabel(first);
		
		while(current != null)
		{
			final Label nameLabel = (Label) current;
			
			final SingleLinkedList<Instruction> instList = new SingleLinkedList<Instruction>();
			current = current.getLiteralNext();
			Label[] children = null;
			Label next = null;
			boolean isDone = false;
			
			while(!isDone)
			{
				switch(current.getIrCode())
				{
					case Label:
					{
						if(instList.getSize() != 0)
						{
						children = new Label[0];
						next = (Label) current;
						isDone = true;
						break;
						}
					}
					case Jump_Straight:
					{
						instList.add(current);
						final Jump_Straight cast = (Jump_Straight) current;
						children = new Label[0];
						next = cast.getJumpTo();
						
						isDone = true;
						break;
					}
					case Jump_Branched:
					{
						instList.add(current);
						final Jump_Branched cast = (Jump_Branched) current;
						children = new Label[]{cast.getJumpTo(), cast.getJumpTo_ifFalse()};
						
						isDone = true;
						break;
					}
					case Return_Concretely:
					case Return_Variably:
					{
						instList.add(current);
						children = new Label[0];
						
						isDone = true;
						break;
					}
					default:
					{
						instList.add(current);
						current = current.getLiteralNext();
						break;
					}
				}
			}
			
			final firstPassData loopResult = new firstPassData(nameLabel, instList, children, next);
			result.add(loopResult);
			map.put(nameLabel.getMangledName(false), loopResult);
			
			//System.out.println(loopResult.toString());
			
			if(current.getIrCode() != IrCodes.Label)
			{
				current = firstPassHelper_getFirstLabel(current);
			}
		}
		
		return process_secondPass(result, map);
		
		//return result.toArray(new CodeBlock3[result.getSize()]);
	}
	
	private static Instruction firstPassHelper_getFirstLabel(Instruction first)
	{
		while(first != null && first.getIrCode() != IrCodes.Label)
		{
			first = first.getLiteralNext();
		}
		
		return first;
	}
	
	/**
	 * linker
	 * @param listy
	 * @param map
	 * @return
	 */
	private static CodeBlock3[] process_secondPass(SingleLinkedList<firstPassData> listy, HalfByteRadixMap<firstPassData> map)
	{
		final SingleLinkedList<CodeBlock3> result = new SingleLinkedList<CodeBlock3>();
		
		if(listy.getSize() != map.size())
		{
			throw new IllegalArgumentException();
		}
		
		for(firstPassData curr: listy)
		{
			//System.out.println("linking: " + curr.data.getMangledName());
			for(int i = 0; i < curr.children.length; i++)
			{
				final firstPassData child = map.get(curr.children[i].getMangledName(false));
				
				curr.data.setChild(child.data, i, LinkAnnulOptions.DoNotAnnul);
				child.data.addIncoming(curr.data, i);
			}
			
			if(curr.next != null)
			{
				final firstPassData next = map.get(curr.next.getMangledName(false));
				curr.data.setNext(next.data, LinkAnnulOptions.DoNotAnnul);
				next.data.addIncoming(curr.data, -1);
			}
			
			result.add(curr.data);
		}
		
		return result.toArray(new CodeBlock3[result.getSize()]);
	}
	
	private static class firstPassData
	{
		private final CodeBlock3 data;
		private final Label[] children;
		private final Label next;
		
		public firstPassData(Label nameLabel, SingleLinkedList<Instruction> rawData, Label[] inChildren, Label inNext)
		{
			this.data = CodeBlock3.getInstance(nameLabel, inChildren.length, rawData.toArray(new Instruction[rawData.getSize()]));
			this.children = inChildren;
			this.next = inNext;
		}
		
		public String toString()
		{
			final CharList result = new CharList();
			
			result.add("name: ");
			result.add(this.data.getMangledName());
			
			for(int i = 0; i < this.children.length; i++)
			{
				if(i == 0)
				{
					result.add("\tchildren: ");
				}
				else
				{
					result.add(", ");
				}
				
				result.add(this.children[i].getMangledName(false));
			}
			
			if(this.next != null)
			{
				result.add('\t');
				result.add(this.next.getMangledName(false));
			}
			
			return result.toString();
		}
	}
}
