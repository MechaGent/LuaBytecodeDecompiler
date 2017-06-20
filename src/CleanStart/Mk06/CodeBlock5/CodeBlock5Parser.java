package CleanStart.Mk06.CodeBlock5;

import java.util.Iterator;
import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_ForLoop;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock5Parser
{
	private CodeBlock5Parser()
	{
		// to prevent instantiation
	}

	public static CharList process_toCharList(Instruction first)
	{
		final HalfByteRadixMap<PassData> map = initMappings(first);
		reduceMappings(map);

		final CharList result = new CharList();

		for (Entry<HalfByteArray, PassData> entry : map)
		{
			result.addNewLine();
			result.add(entry.getValue().toSimpleCharList(), true);
			//result.add(entry.getValue().toDiagCharList(), true);
			result.addNewLine();
		}

		return result;
	}

	public static CharList process_toCodeCharList(Instruction first)
	{
		final HalfByteRadixMap<PassData> map = initMappings(first);
		reduceMappings(map);

		final CharList result = map.get("StartLabel").toCascadingCodeCharList(0, map);

		return result;
	}

	private static HalfByteRadixMap<PassData> initMappings(Instruction first)
	{
		final HalfByteRadixMap<PassData> result = new HalfByteRadixMap<PassData>();
		final SingleLinkedList<String> negJumps = new SingleLinkedList<String>();
		
		Instruction current = first;

		final String[] dummyArray = new String[0]; // so I don't have to keep creating empty arrays

		while (current != null)
		{
			final SingleLinkedList<Instruction> runningList = new SingleLinkedList<Instruction>();
			//System.out.println("outer processing: " + current.getMangledName(false));
			
			if(current.getIrCode() != IrCodes.Label)
			{
				throw new IllegalArgumentException("choking on: " + current.getMangledName(false) + " with prev: " + current.getLiteralPrev().getMangledName(false) + " with timestamp: " + current.getLiteralPrev().getStartTime().getInDiagForm().toString());
			}
			final Label nameLabel = (Label) current;
			runningList.add(nameLabel);
			current = current.getLiteralNext();
			String[] children = dummyArray;
			BlockTypes blockType = BlockTypes.Linear_Flows;

			boolean isDone = false;

			while (!isDone)
			{
				//System.out.println("inner processing: " + current.getMangledName(false));

				switch (current.getIrCode())
				{
					case EvalLoop_For:
					{
						final Jump_ForLoop cast = (Jump_ForLoop) current;
						children = new String[2];
						children[0] = cast.getJumpTo().getMangledName(false);
						children[1] = cast.getJumpTo_ifFalse().getMangledName(false);

						isDone = true;
						runningList.add(current);
						
						if(current != null)
						{
							current = current.getLiteralNext();
						}
						break;
					}
					case Label:
					{
							children = new String[1];
							children[0] = current.getMangledName(false);
							//runningList.pop();
							//System.out.println("popping: " + runningList.pop().getMangledName(false));
							current = current.getLiteralPrev();

							isDone = true;
							
							if(current != null)
							{
								current = current.getLiteralNext();
							}

						break;
					}
					case Jump_Straight:
					{
						final Jump_Straight cast = (Jump_Straight) current;

						if (!cast.isNegativeJump())
						{
							children = new String[1];
							children[0] = cast.getJumpTo().getMangledName(false);
						}
						else
						{
							negJumps.add(cast.getJumpTo().getMangledName(false));
						}

						isDone = true;
						runningList.add(current);
						
						if(current != null)
						{
							current = current.getLiteralNext();
						}
						break;
					}
					case Jump_Branched:
					{
						final Jump_Branched cast = (Jump_Branched) current;
						children = new String[2];
						children[0] = cast.getJumpTo().getMangledName(false);
						children[1] = cast.getJumpTo_ifFalse().getMangledName(false);

						isDone = true;
						runningList.add(current);
						
						if(current != null)
						{
							current = current.getLiteralNext();
						}

						break;
					}
					case Return_Concretely:
					case Return_Variably:
					{
						runningList.add(current);
						final Instruction nextInstruct = current.getLiteralNext();

						if (nextInstruct != null)
						{
							final IrCodes next = nextInstruct.getIrCode();

							//System.out.println("triggering2");
							if (next == IrCodes.Return_Concretely || next == IrCodes.Return_Variably)
							{
								//System.out.println("triggering");
								current = nextInstruct;
							}
						}

						isDone = true;
						
						if(current != null)
						{
							current = current.getLiteralNext();
						}
						
						break;
					}
					default:
					{
						runningList.add(current);
						current = current.getLiteralNext();
						break;
					}
				}
				
				
			}

			// final CodeBlock5 core = CodeBlock5.getInstance(nameLabel, children.length);
			final PassData data = PassData.getInstance(nameLabel.getMangledName(false), runningList, children);

			if (blockType != BlockTypes.Linear_Flows)
			{
				data.setBlockType(blockType);
			}

			result.put(data.getHalfByteName(), data);
			// current = current.getLiteralNext();
		}
		
		for(String name: negJumps)
		{
			final PassData loop = result.get(name);
			loop.setBlockType(BlockTypes.Loop);
		}

		return result;
	}

	private static void reduceMappings(HalfByteRadixMap<PassData> map)
	{
		boolean isDone_all = false;

		while (!isDone_all)
		{
			final Iterator<Entry<HalfByteArray, PassData>> itsy = map.iterator();
			boolean isDone_pass = false;

			while (!isDone_pass && itsy.hasNext())
			{
				final PassData curr = itsy.next().getValue();

				if (!curr.isWrapped() && curr.getNumVisibleChildren() == 2)
				{
					/*
					 * currentBlock has both Child0 and Child1
					 */

					final String Child0Name = curr.getChild0();
					final PassData Child0 = map.get(Child0Name);

					if (Child0.isTerminal())
					{
						/*
						 * currentBlock has both Child0 and Child1
						 * Child0 is terminal
						 */

						// ElselessIf_TerminalIf
						reduceMappings_TerminalIf(map, curr);
						isDone_pass = true;
					}
					else if (Child0.getNumVisibleChildren() == 1)
					{
						/*
						 * currentBlock has both Child0 and Child1
						 * currentBlock.Child0 has 1 visible Child
						 */

						final String Child0Child0Name = Child0.getChild0();
						final String Child1Name = curr.getChild1();

						if (Child0Child0Name.equals(Child1Name))
						{
							/*
							 * currentBlock has both Child0 and Child1
							 * currentBlock.Child0 has 1 visible Child
							 * currentBlock.Child0.Child0 == currentBlock.Child1
							 */

							// ElselessIf_Flowthrough
							reduceMappings_ElselessIf_Flows(map, curr, Child0Child0Name, Child0);
							isDone_pass = true;
						}

						if (!isDone_pass)
						{
							final PassData Child1 = map.get(Child1Name);

							if (Child1.getNumVisibleChildren() == 1)
							{
								/*
								 * currentBlock has both Child0 and Child1
								 * currentBlock.Child0 has 1 visible Child
								 * currentBlock.Child1 has 1 visible Child
								 */

								final String Child1Child0Name = Child1.getChild0();

								if (Child0Child0Name.equals(Child1Child0Name))
								{
									/*
									 * currentBlock has both Child0 and Child1
									 * currentBlock.Child0 has 1 visible Child
									 * currentBlock.Child1 has 1 visible Child
									 * currentBlock.Child0.Child0 == currentBlock.Child1.Child0
									 */

									// set the wrapper's type as IfElse_Simple
									reduceMappings_IfElse_Simple(map, curr, Child0Child0Name, Child0, Child1);
									isDone_pass = true;
								}
							}
						}

						if (!isDone_pass)
						{
							final PassData Child0Child0 = map.get(Child0Child0Name);

							if (Child0Child0.getNumVisibleChildren() == 1)
							{
								/*
								 * currentBlock has both Child0 and Child1
								 * currentBlock.Child0 has 1 visible Child
								 * currentBlock.Child0.Child0 has 1 visible Child
								 */

								final String Child0Child0Child0Name = Child0Child0.getChild0();

								if (Child0Child0Child0Name.equals(Child1Name))
								{
									/*
									 * currentBlock has both Child0 and Child1
									 * currentBlock.Child0 has 1 visible Child
									 * currentBlock.Child0.Child0 has 1 visible Child
									 * currentBlock.Child0.Child0.Child0 == currentBlock.Child1
									 */

									// set the wrapper's type as ElselessIf_ComplexFlowthrough
									reduceMappings_ElselessIf_ComplexFlows(map, curr, Child1Name, Child0Child0);
									isDone_pass = true;
								}
							}
						}
					}
				}
			}

			if (!isDone_pass)
			{
				isDone_all = true;
			}
		}
	}

	/**
	 * if
	 * {
	 * currentBlock has both Child0 and Child1
	 * currentBlock.Child0 is terminal
	 * }, then set the wrapper's type as ElselessIf_TerminalIf
	 * 
	 * @param map
	 */
	private static void reduceMappings_TerminalIf(HalfByteRadixMap<PassData> map, PassData curr)
	{
		final String curName = curr.getName();
		//System.out.println("parsed as TerminalIf: " + curName);
		final PassData wrap = PassData.getInstance(curName, curr, curr.getChild1());
		curr.setName(curName + "_wrap");
		curr.setWrapState(true);
		// wrap.setBlockType(BlockTypes.ElselessIf_TerminalIf);
		curr.setBlockType(BlockTypes.ElselessIf_TerminalIf);
		curr.ignoreChild1();
		map.put(wrap.getHalfByteName(), wrap);
		map.put(curr.getHalfByteName(), curr);
	}

	/**
	 * if
	 * {
	 * currentBlock has both Child0 and Child1
	 * currentBlock.Child0 has 1 visible Child
	 * currentBlock.Child0.Child0 == currentBlock.Child1 (hereafter referred to as ChildJoiner)
	 * }, then set the wrapper's type as ElselessIf_Flowthrough
	 * 
	 * @param map
	 */
	private static void reduceMappings_ElselessIf_Flows(HalfByteRadixMap<PassData> map, PassData curr, String Child0Child0Name, PassData Child0)
	{
		final String curName = curr.getName();
		//System.out.println("parsed as ElselessIf_flows: " + curName);
		final PassData wrap = PassData.getInstance(curName, curr, Child0Child0Name);
		curr.setName(curName + "_wrap");
		curr.setWrapState(true);
		// wrap.setBlockType(BlockTypes.ElselessIf_Flows);
		curr.setBlockType(BlockTypes.ElselessIf_Flows);
		Child0.ignoreChild0();
		curr.ignoreChild1();
		map.put(wrap.getHalfByteName(), wrap);
		map.put(curr.getHalfByteName(), curr);
	}

	/**
	 * if
	 * {
	 * currentBlock has both Child0 and Child1
	 * currentBlock's Child0 has 1 visible Child
	 * currentBlock.Child0.Child0 has 1 visible Child
	 * currentBlock.Child0.Child0.Child0 == currentBlock.Child1 (hereafter referred to as ChildJoiner)
	 * }, then set the wrapper's type as ElselessIf_ComplexFlowthrough
	 * 
	 * @param map
	 */
	private static void reduceMappings_ElselessIf_ComplexFlows(HalfByteRadixMap<PassData> map, PassData curr, String Child1Name, PassData Child0Child0)
	{
		final String curName = curr.getName();
		//System.out.println("parsed as ElselessIf_complexFlows: " + curName);
		final PassData wrap = PassData.getInstance(curName, curr, Child1Name);
		curr.setName(curName + "_wrap");
		// wrap.setBlockType(BlockTypes.ElselessIf_ComplexFlows);
		curr.setBlockType(BlockTypes.ElselessIf_ComplexFlows);
		curr.setWrapState(true);
		Child0Child0.ignoreChild0();
		curr.ignoreChild1();
		map.put(wrap.getHalfByteName(), wrap);
		map.put(curr.getHalfByteName(), curr);
	}

	/**
	 * if
	 * {
	 * currentBlock has both Child0 and Child1
	 * currentBlock's Child0 and Child1 both have 1 visible Child
	 * currentBlock.Child0.Child0 == currentBlock.Child1.Child0 (hereafter referred to as ChildJoiner)
	 * }, then set the wrapper's type as IfElse_Simple
	 * 
	 * @param map
	 */
	private static void reduceMappings_IfElse_Simple(HalfByteRadixMap<PassData> map, PassData curr, String Child0Child0Name, PassData Child0, PassData Child1)
	{
		final String curName = curr.getName();
		//System.out.println("parsed as IfElse_Simple: " + curName);
		final PassData wrap = PassData.getInstance(curName, curr, Child0Child0Name);
		curr.setName(curName + "_wrap");
		curr.setWrapState(true);
		// wrap.setBlockType(BlockTypes.IfElse_Simple);
		curr.setBlockType(BlockTypes.IfElse_Simple);
		Child0.ignoreChild0();
		Child1.ignoreChild0();
		map.put(wrap.getHalfByteName(), wrap);
		map.put(curr.getHalfByteName(), curr);
	}
}
