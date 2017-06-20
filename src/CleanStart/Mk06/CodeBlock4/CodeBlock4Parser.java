package CleanStart.Mk06.CodeBlock4;

import java.util.Iterator;
import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.CodeBlock4.CodeBlock4.ScopeTypes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_ForLoop;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import CleanStart.Mk06.Instructions.NonOp;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;
import HalfByteRadixMap.HalfByteRadixMap;
import Linkable.LinkAnnulOptions;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock4Parser
{
	private CodeBlock4Parser()
	{

	}

	public static CodeBlock4[] process(Instruction first)
	{
		return process_firstPass(first);
	}

	private static CodeBlock4[] process_firstPass(Instruction first)
	{
		final SingleLinkedList<firstPassData> result = new SingleLinkedList<firstPassData>();
		final HalfByteRadixMap<firstPassData> map = new HalfByteRadixMap<firstPassData>();
		final HalfByteRadixMap<Integer> counts = new HalfByteRadixMap<Integer>();
		final SingleLinkedList<firstPassData> negJumps = new SingleLinkedList<firstPassData>();
		Instruction current = firstPassHelper_getFirstLabel(first);

		while (current != null)
		{
			final Label nameLabel = (Label) current;

			final SingleLinkedList<Instruction> instList = new SingleLinkedList<Instruction>();
			instList.add(nameLabel);
			current = current.getLiteralNext();
			Label[] children = null;
			Label next = null;
			boolean isDone = false;
			boolean isNegJump = false;
			boolean isTerminal = false;

			while (!isDone)
			{
				switch (current.getIrCode())
				{
					case Label:
					{
						if (instList.getSize() != 0)
						{
							next = (Label) current;
							children = new Label[] {
														next };
							isDone = true;
							break;
						}
					}
					case Jump_Straight:
					{
						instList.add(current);
						final Jump_Straight cast = (Jump_Straight) current;
						/*
						children = new Label[0];
						next = cast.getJumpTo();
						*/
						children = new Label[]{cast.getJumpTo()};
						next = null;

						if (cast.isNegativeJump())
						{
							isNegJump = true;
						}

						isDone = true;
						break;
					}
					case Jump_Branched:
					{
						instList.add(current);
						final Jump_Branched cast = (Jump_Branched) current;
						children = new Label[] {
													cast.getJumpTo(),
													cast.getJumpTo_ifFalse() };

						isDone = true;
						break;
					}
					case EvalLoop_For:
					{
						final Jump_ForLoop cast = (Jump_ForLoop) current;
						children = new Label[] {
													cast.getJumpTo(),
													cast.getJumpTo_ifFalse() };

						isDone = true;
						break;
					}
					case Return_Concretely:
					case Return_Variably:
					{
						final IrCodes prev = current.getLiteralPrev().getIrCode();

						if (prev != IrCodes.Return_Concretely && prev != IrCodes.Return_Variably)
						{
							instList.add(current);
							children = new Label[0];
							isTerminal = true;
						}
						else
						{
							return process_loopPass(result, map, negJumps, counts);
						}

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

			final firstPassData loopResult = new firstPassData(nameLabel, instList, children, isTerminal, next);
			result.add(loopResult);
			final String mangle = nameLabel.getMangledName(false);
			map.put(mangle, loopResult);

			if (next != null)
			{
				final String nextName = next.getMangledName(false);
				final Integer temp = counts.get(nextName);

				if (temp == null)
				{
					counts.put(nextName, 1);
				}
				else
				{
					counts.put(nextName, temp + 1);
				}
			}

			for (int i = 0; i < children.length; i++)
			{
				final String childName = children[i].getMangledName(false);
				final Integer temp = counts.get(childName);

				if (temp == null)
				{
					counts.put(childName, 1);
				}
				else
				{
					counts.put(childName, temp + 1);
				}
			}

			if (isNegJump)
			{
				negJumps.add(loopResult);
				// System.out.println("isLoop: " + loopResult.toString());
			}

			// System.out.println(loopResult.toString());

			if (current.getIrCode() != IrCodes.Label)
			{
				current = firstPassHelper_getFirstLabel(current);
			}
		}

		 return process_linkPass(map);
		//return process_loopPass(result, map, negJumps, counts);

		// return firstPassData.toBlockArray(result);
	}

	private static Instruction firstPassHelper_getFirstLabel(Instruction first)
	{
		while (first != null && first.getIrCode() != IrCodes.Label)
		{
			first = first.getLiteralNext();
		}

		return first;
	}

	private static CodeBlock4[] process_loopPass(SingleLinkedList<firstPassData> listy, HalfByteRadixMap<firstPassData> map, SingleLinkedList<firstPassData> negJumps, HalfByteRadixMap<Integer> counts)
	{
		final HalfByteRadixMap<Integer> initialCounts = getCounts(map);

		for (firstPassData negJump : negJumps)
		{
			final String loopStartName = negJump.next.getMangledName(false);
			// System.out.println(loopStartName);
			final firstPassData loopStart = map.get(loopStartName);
			final firstPassData clone = new firstPassData(loopStart.data, new Label[] {
																						loopStart.children[0] },
					false, loopStart.children[1]);
			// clone.next = loopStart.children[1];
			clone.data.setScopeType(ScopeTypes.Loop);
			map.put(loopStartName, clone);
			decrementCount(negJump.next, initialCounts);
			negJump.next = null;
			/*
			final CodeBlock4 wrap = CodeBlock4.getInstance(loopStart.data.nameLabel, 1, loopStart.data, negJump.data);
			wrap.setScopeType(ScopeTypes.Loop);
			
			// wrap.setNext(loopAfter, LinkAnnulOptions.DoNotAnnul);
			final firstPassData wrapWrap = new firstPassData(wrap, new Label[] {
																					loopStart.children[0] },
					false, loopStart.children[1]);
			// System.out.println("labels for " + loopStartName + ": 0: " + loopStart.children[0].getMangledName(false) + ", 1: " + loopStart.children[1].getMangledName(false));
			map.put(loopStartName, wrapWrap);
			final firstPassData dummy = new firstPassData(negJump.data.nameLabel, NonOp.getInstance(StepNum.getInitializerStep()), new Label[0], false, null);
			map.put(negJump.data.getMangledName(), dummy);
			*/
		}

		// return firstPassData.toBlockArray(listy);
		 //return process_linkPass(map);
		return process_collapseSimpleScopes(listy, map, initialCounts);
	}

	private static CodeBlock4[] process_collapseSimpleScopes(SingleLinkedList<firstPassData> listy, HalfByteRadixMap<firstPassData> map, HalfByteRadixMap<Integer> initialCounts)
	{

		boolean isDone = false;

		while (!isDone)
		{
			final Iterator<Entry<HalfByteArray, firstPassData>> bitsy = map.iterator();
			boolean foundOne = false;

			while (!foundOne && bitsy.hasNext())
			{
				final firstPassData curr = bitsy.next().getValue();
				System.out.println("processing: " + curr.toString());
				ifElseParseOptions option = ifElseParseOptions.Other;

				// if (!curr.isProcessed)
				{
					if (curr.next == null)
					{
						if (curr.children.length == 2 && curr.children[1] != null)
						{
							// is a branch of some kind

							final firstPassData child0 = map.get(curr.children[0].getMangledName(false));
							final firstPassData child1 = map.get(curr.children[1].getMangledName(false));

							if (child0.next != null)
							{
								if (child1.next != null)
								{
									if (child0.next == child1.next)
									{
										option = ifElseParseOptions.ifElse;
										// System.out.println("condition 1 on " + curr.data.getMangledName());
									}
									else
									{
										if (child0.next == curr.children[1])
										{
											option = ifElseParseOptions.elselessIf;
											// curr.children[1] = null;
											// System.out.println("condition 2 on " + curr.data.getMangledName());
										}
										else
										{
											final firstPassData speculative = map.get(child0.next.getMangledName(false));
											if (speculative.next == curr.children[1])
											{
												// option = ifElseParseOptions.elselessIf;
												// System.out.println("triggered on node " + curr.toString());

												if (curr.data.getScopeType() == ScopeTypes.Linear)
												{
													curr.data.setScopeType(ScopeTypes.ElselessIf);
												}

												curr.next = curr.children[1];
												curr.next_initial = curr.children[1];

												decrementCount(curr.children[1], initialCounts);
												curr.children[1] = null;
												// curr.isProcessed = true;
												foundOne = true;
											}
										}
									}
								}
								/*
								else if (initialCounts.get(child1.data.getMangledName()) > 1)
								{
									option = ifElseParseOptions.elselessIf;
								}
								*/
								else
								{
									if (child0.next == curr.children[1])
									{
										option = ifElseParseOptions.elselessIf;
										// curr.children[1] = null;
										// System.out.println("condition 3 on " + curr.data.getMangledName());
									}

									// System.out.println("bouncing: " + curr.data.getMangledName() + " with count " + initialCounts.get(curr.data.getMangledName()));
								}
							}
							else if (child0.isTerminal)
							{
								if (child1.next != null)
								{
									option = ifElseParseOptions.elselessIf;
									// System.out.println("condition 4 on " + curr.data.getMangledName());
								}
							}
							else if (curr.next != null)
							{
								if (child1 != null)
								{
									option = ifElseParseOptions.elselessIf;
									// System.out.println("condition 5 on " + curr.data.getMangledName());

								}
							}

							if (!foundOne)
							{
								switch (option)
								{
									case ifElse:
									{
										curr.data.setScopeType(ScopeTypes.IfElse);

										curr.next = child0.next;
										curr.next_initial = child0.next;

										decrementCount(child0.next, initialCounts);
										child0.next = null;
										// decrementCount(child1.next, initialCounts);
										child1.next = null;

										foundOne = true;
										// curr.isProcessed = true;
										// System.out.println("Iprocessed: " + curr.toString());
										break;
									}
									case elselessIf:
									{
										final ScopeTypes scope = curr.data.getScopeType();

										if (scope != ScopeTypes.ElselessIf)
										{
											if (scope == ScopeTypes.Linear)
											{
												curr.data.setScopeType(ScopeTypes.ElselessIf);
											}

											curr.next = curr.children[1];
											curr.next_initial = curr.children[1];

											decrementCount(curr.children[1], initialCounts);
											curr.children[1] = null;

											// decrementCount(curr.next, initialCounts);
											child0.next = null;
											foundOne = true;
											// System.out.println("Eprocessed: " + curr.toString());
											// curr.isProcessed = true;
										}
										break;
									}
									default:
									{
										break;
									}
								}
							}
						}
						else if (curr.children.length == 1)
						{

							if (curr.children[0] != null)
							{
								// curr.next = curr.children[0];
								// curr.children[0] = null;
							}

						}
					}
					else
					{
						final firstPassData nextData = map.get(curr.next.getMangledName(false));

						if (nextData.next != null)
						{

							// final Label name = curr.data.nameLabel;
							// curr.data.setNext(nextData.data, LinkAnnulOptions.DoNotAnnul);
							// final Label next = nextData.next;
							// curr.next = null;
							// nextData.next = null;
							// foundOne = true;
							// System.out.println("Lprocessed: " + curr.toString());
						}
					}
				}
			}

			if (!foundOne)
			{
				isDone = true;
			}
		}

		// return firstPassData.toBlockArray(listy);
		return process_linkPass(map);
	}

	private static HalfByteRadixMap<Integer> getCounts(HalfByteRadixMap<firstPassData> map)
	{
		final HalfByteRadixMap<Integer> result = new HalfByteRadixMap<Integer>();

		for (Entry<HalfByteArray, firstPassData> entry : map)
		{
			final SingleLinkedList<Label> queue = new SingleLinkedList<Label>();
			final firstPassData value = entry.getValue();

			for (Label label : value.children)
			{
				queue.add(label);
			}

			if (value.next != null)
			{
				if (value.children.length == 2)
				{
					if (value.children[1] != value.next)
					{
						queue.add(value.next);
					}
				}
				else
				{
					queue.add(value.next);
				}
			}

			for (Label label : queue)
			{
				final String labelName = label.getMangledName(false);
				final HalfByteArray convertedName = HalfByteArrayFactory.wrapIntoArray(labelName);
				final Integer proof = result.get(convertedName);

				if (proof == null)
				{
					result.put(convertedName, 1);
				}
				else
				{
					result.put(convertedName, proof + 1);
				}
			}
		}

		return result;
	}

	private static void decrementCount(Label label, HalfByteRadixMap<Integer> counts)
	{
		if (label != null)
		{
			final String labelName = label.getMangledName(false);
			final HalfByteArray convertedName = HalfByteArrayFactory.wrapIntoArray(labelName);
			final Integer target = counts.get(convertedName);

			if (target != null)
			{
				counts.put(convertedName, target - 1);
				// System.out.println("decrementing " + labelName + " to " + (target - 1));
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
	}

	private static final boolean diag_collapseSimpleScopes = false;

	private static CodeBlock4[] process_collapseSimpleScopes2(SingleLinkedList<firstPassData> listy, HalfByteRadixMap<firstPassData> map)
	{
		boolean isDone = false;

		while (!isDone)
		{
			final Iterator<Entry<HalfByteArray, firstPassData>> bitsy = map.iterator();
			boolean foundOne = false;

			while (!foundOne && bitsy.hasNext())
			{
				final firstPassData curr = bitsy.next().getValue();

				if (diag_collapseSimpleScopes)
				{
					final CharList diag = new CharList();
					diag.add("Label: ");
					diag.add(curr.data.getMangledName());
					diag.addNewLine();
					diag.add('\t', 1);
					diag.add("numChildren: ");
					diag.addAsString(curr.children.length);

					if (curr.children.length == 2)
					{
						diag.addNewLine();
						diag.add('\t', 1);
						diag.add("Child0: ");

						if (curr.children[0] == null)
						{
							diag.add("null");
						}
						else
						{
							diag.add(curr.children[0].getMangledName(false));
						}

						diag.addNewLine();
						diag.add('\t', 1);
						diag.add("Child1: ");

						if (curr.children[1] == null)
						{
							diag.add("null");
						}
						else
						{
							diag.add(curr.children[1].getMangledName(false));
						}
					}

					System.out.println(diag.toString());
				}

				if (curr.children.length == 2)
				{
					if (curr.children[0] != null)
					{
						final firstPassData Child0 = map.get(curr.children[0].getMangledName(false));

						if (curr.children[1] != null)
						{
							final firstPassData Child1 = map.get(curr.children[1].getMangledName(false));

							if (Child0.next != null && Child0.next == Child1.next)
							{
								// both kids are going to the same label, and should be rescoped
								// System.out.println("rescoping: " + curr.data.getMangledName() + " as ifElse");
								final CodeBlock4 wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, map.get(Child0.next.getMangledName(false)).data);

								curr.data.setChild(Child0.data, 0, LinkAnnulOptions.DoNotAnnul);
								curr.data.setChild(Child1.data, 1, LinkAnnulOptions.DoNotAnnul);

								// Child0.next = null;
								// Child1.next = null;

								// final Label wrapLabel = Label.getInstance(StepNum.getInitializerStep(), "wrap");
								final firstPassData wrapWrap = new firstPassData(wrap, new Label[0], false, curr.next);
								System.out.println("created: " + wrapWrap.toString());
								// map.put(wrapLabel.getMangledName(false), curr);
								map.put(curr.data.getMangledName(), wrapWrap);
								foundOne = true;
							}
							else if ((curr.children[1] == Child0.next) ^ (Child0.next == null))
							{
								if (Child0.next == null)
								{
									System.out.println("rescoping: " + curr.data.getMangledName() + " as elselessIf, with Child0 of: " + Child0.data.getMangledName() + ", curr.children[1]: " + curr.children[1].getMangledName(false));
								}
								else
								{
									System.out.println("rescoping: " + curr.data.getMangledName() + " as elselessIf, with Child0.next of: " + Child0.next.getMangledName(false) + ", curr.children[1]: " + curr.children[1].getMangledName(false));
								}

								// System.out.println("rescoping: " + curr.data.getMangledName() + " as elselessIf, with Child0 of: " + Child0.data.getMangledName() + ", curr.children[1]: " + curr.children[1].getMangledName(false));
								final CodeBlock4 wrap;

								wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, Child1.data);
								/*
								if (Child0.next == null)
								{
									wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, Child1.data);
								}
								else
								{
									final String Child0Next = Child0.next.getMangledName(false);
									final firstPassData join = map.get(Child0Next);
									System.out.println("for block " + curr.data.getMangledName() + ", attempting lookup of " + Child0Next + ", with result: " + join);
									//wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, join.data);
									wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, Child1.data);
								}
								*/

								if (Child0.next != null)
								{
									// Child0.next = null;
								}

								curr.data.setChild(Child0.data, 0, LinkAnnulOptions.DoNotAnnul);
								final firstPassData wrapWrap = new firstPassData(wrap, new Label[] {
																										curr.children[0] },
										(Child0.next == null), curr.children[1]);
								wrapWrap.data.setChild(Child0.data, 0, LinkAnnulOptions.DoNotAnnul);
								System.out.println("created: " + wrapWrap.toString());
								map.put(curr.data.getMangledName(), wrapWrap);
								foundOne = true;
							}
							/*
							else if (Child1.next != null && curr.children[0] == Child1.next)
							{
								final CodeBlock4 wrap;
							
								wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, Child0.data);
							
								/*
								if (Child1.next == null)
								{
									wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, null);
								}
								else
								{
									wrap = CodeBlock4.getInstance(curr.data.nameLabel, 1, curr.data, map.get(Child1.next.getMangledName(false)).data);
								}
								*
							
								if (Child1.next != null)
								{
									//Child1.next = null;
								}
							
								curr.data.setChild(Child1.data, 0, LinkAnnulOptions.DoNotAnnul);
								final firstPassData wrapWrap = new firstPassData(wrap, new Label[0], Child1.next);
								wrapWrap.data.setChild(Child1.data, 0, LinkAnnulOptions.DoNotAnnul);
								System.out.println("created: " + wrapWrap.toString());
								map.put(curr.data.getMangledName(), wrapWrap);
								foundOne = true;
							}
							*/
						}
					}
				}
				else
				{
					// System.out.println("bypassed: " + curr.data.getMangledName());
				}
			}

			if (!foundOne)
			{
				isDone = true;
			}
		}

		// return firstPassData.toBlockArray(listy);
		return process_linkPass(map);
	}

	/**
	 * linker
	 * 
	 * @param listy
	 * @param map
	 * @return
	 */
	private static CodeBlock4[] process_linkPass(HalfByteRadixMap<firstPassData> map)
	{
		final SingleLinkedList<firstPassData> result = new SingleLinkedList<firstPassData>();

		// for (firstPassData curr : listy)
		for (Entry<HalfByteArray, firstPassData> entry : map)
		{
			final firstPassData curr = entry.getValue();
			final int length = curr.children.length;
			final boolean hasNext = curr.next != null;

			if (!(length == 1 && hasNext && curr.children[0] == curr.next))
			{
				for (int i = 0; i < curr.children.length; i++)
				{
					if (curr.children[i] != null)
					{
						final firstPassData child = map.get(curr.children[i].getMangledName(false));

						if (child == null)
						{
							throw new NullPointerException("bad lookup: " + curr.children[i].getMangledName(false));
						}

						curr.data.setChild(child.data, i, LinkAnnulOptions.Annul_Both);
						// child.data.addIncoming(curr.data, i);

						// System.out.println("linking: " + curr.data.getMangledName() + "[" + i + "] as " + child.data.getMangledName());
					}
				}
			}

			if (hasNext)
			{
				final firstPassData next = map.get(curr.next.getMangledName(false));
				curr.data.setNext(next.data, LinkAnnulOptions.Annul_Both);
				// next.data.addIncoming(curr.data, -1);
				// System.out.println("linking: " + curr.data.getMangledName() + ".next as " + next.data.getMangledName());
			}

			result.add(curr);
		}

		// return result.toArray(new CodeBlock4[result.getSize()]);
		// return process_loopPass(listy, map, negJumps);
		return firstPassData.toBlockArray(result);
	}

	private static class firstPassData
	{
		private final CodeBlock4 data;
		private final Label[] children;
		private final boolean isTerminal;
		private Label next;
		private Label next_initial;
		private boolean isProcessed;

		public firstPassData(Label nameLabel, Instruction rawData, Label[] inChildren, boolean inIsTerminal, Label inNext)
		{
			this(CodeBlock4.getInstance(nameLabel, inChildren.length, new Instruction[] {
																							rawData }),
					inChildren, inIsTerminal, inNext);
		}

		public firstPassData(Label nameLabel, SingleLinkedList<Instruction> rawData, Label[] inChildren, boolean inIsTerminal, Label inNext)
		{
			this(CodeBlock4.getInstance(nameLabel, inChildren.length, rawData.toArray(new Instruction[rawData.getSize()])), inChildren, inIsTerminal, inNext);
		}

		public firstPassData(CodeBlock4 inData, Label[] inChildren, boolean inIsTerminal, Label inNext)
		{
			this.data = inData;
			this.children = inChildren;
			this.isTerminal = inIsTerminal;
			this.next = inNext;
			this.isProcessed = false;
			this.next_initial = inNext;
		}

		public static CodeBlock4[] toBlockArray(SingleLinkedList<firstPassData> listy)
		{
			final CodeBlock4[] result = new CodeBlock4[listy.getSize()];
			final Iterator<firstPassData> itsy = listy.iterator();

			for (int i = 0; i < result.length; i++)
			{
				result[i] = itsy.next().data;
			}

			return result;
		}

		public String toString()
		{
			final CharList result = new CharList();

			result.add("name: ");
			result.add(this.data.getMangledName());

			for (int i = 0; i < this.children.length; i++)
			{
				if (i == 0)
				{
					result.add("\tchildren: {");
				}
				else
				{
					result.add(", ");
				}

				if (this.children[i] != null)
				{
					result.add(this.children[i].getMangledName(false));
				}
			}

			if (this.children.length > 0)
			{
				result.add('}');
			}

			if (this.next != null)
			{
				result.add('\t');
				result.add("next: ");

				result.add(this.next.getMangledName(false));
			}

			if (this.next_initial != null)
			{
				result.add('\t');
				result.add("next_init: ");

				result.add(this.next_initial.getMangledName(false));
			}

			return result.toString();
		}
	}

	private static enum ifElseParseOptions
	{
		ifElse,
		elselessIf,
		iflessElse,
		linearRedundancy,
		Other;
	}
}
