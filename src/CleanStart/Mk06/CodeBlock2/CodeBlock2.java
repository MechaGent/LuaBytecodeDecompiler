package CleanStart.Mk06.CodeBlock2;

import java.util.Iterator;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Jump_Branched;
import CleanStart.Mk06.Instructions.Jump_Straight;
import CleanStart.Mk06.Instructions.Label;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteRadixMap.HalfByteRadixMap;
import Linkable.LinkAnnulOptions;
import Linkable.Linkable;
import Linkable.LinkableNode;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock2
{
	private int scope;
	private final Label name;
	private final Instruction[] instructions;
	private final Instruction director; // the instruction from which the next blocks, if any, are determined. If null, there are no more blocks.
	private final CodeBlock2[] children;
	private LinkableNode<CodeBlock2> wrapperNode;
	private ParentRef ref;

	private CodeBlock2(Label inName, Instruction[] inInstructions, Instruction inDirector, int numChildren)
	{
		this.scope = Integer.MAX_VALUE;
		this.name = inName;
		this.instructions = inInstructions;
		this.director = inDirector;

		switch (numChildren)
		{
			case 0:
			case 1:
			case 2:
			{
				this.children = new CodeBlock2[numChildren];
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Illegal Number of Children: " + numChildren);
			}
		}

		this.wrapperNode = null;
		this.ref = null;
	}

	public static CodeBlock2 getInstance(Label inName, Instruction[] inInstructions, Instruction inDirector, int numChildren)
	{
		final CodeBlock2 result = new CodeBlock2(inName, inInstructions, inDirector, numChildren);
		result.wrapperNode = new LinkableNode<CodeBlock2>(result);
		return result;
	}

	private static CodeBlock2[] getLinkedInstances(Instruction first)
	{
		// System.out.println("in CodeBlock2.getLinkedInstances()");
		final SingleLinkedList<CodeBlockData> rawResult = new SingleLinkedList<CodeBlockData>();
		final CodeBlockDataIterator itsy = CodeBlockDataIterator.getInstance(first);
		final HalfByteRadixMap<CodeBlockData> map = new HalfByteRadixMap<CodeBlockData>();

		/*
		 * parsing the blocks
		 */
		while (itsy.hasNext())
		{
			final CodeBlockData current = itsy.next();
			rawResult.add(current);
			map.put(current.name, current);
		}

		// System.out.println("past here");

		/*
		 * converting the list into an array, whilst concurrently linking the blocks
		 */
		final CodeBlock2[] result = new CodeBlock2[rawResult.getSize()];

		for (int i = 0; i < result.length; i++)
		{
			final CodeBlockData current = rawResult.pop();
			result[i] = current.block;

			/*
			if (i == 0)
			{
				result[0].setScope(0);
			}
			*/

			for (int j = 0; j < current.children.length; j++)
			{
				final CodeBlockData child = map.get(current.children[j].getMangledName(false));
				result[i].setAsChild(child.block, j);
			}
		}

		return result;
	}

	private static CodeBlock2[] getLinkedAndScopedInstances(Instruction first)
	{
		final CodeBlock2[] result = CodeBlock2.getLinkedInstances(first);
		// System.out.println("in CodeBlock2.getLinkedAndScopedInstances()");

		result[0].setScope(0);

		for (int i = 0; i < result.length; i++)
		{
			final CodeBlock2 block = result[i];

			switch (block.children.length)
			{
				case 0:
				{
					break;
				}
				case 1:
				{
					/*
					if (block.director != null && block.director.getIrCode() == IrCodes.Jump_Straight)
					{
						// scope is indeterminate, but is at most this one's scope
						System.err.println("(CodeBlock2.getLinkedAndScopedInstances()) does this even happen?");
					}
					*/

					if (block.children[0].getScope() == block.scope)
					{
						block.children[0].setScope(block.scope - 1);
					}
					else
					{
						block.children[0].setScope(block.scope);
						link(block, block.children[0], LinkAnnulOptions.Annul_Both);
						block.children[0] = null;
					}

					break;
				}
				case 2:
				{
					final CodeBlock2 firstChild = block.children[0];
					firstChild.setScope(block.scope + 1);

					if (firstChild.director != null)
					{
						switch (firstChild.director.getIrCode())
						{
							case Jump_Straight:
							{
								block.children[1].setScope(block.scope + 1); // remember to catch the edge cases for this later
								break;
							}
							/*
							case Jump_Branched:
							{
								final Instruction criticalInstruction = block.children[1].name.getLiteralPrev();
								System.out.println("critical instruction for block " + block.children[1].name.getMangledName(false) + ": " + criticalInstruction.getMangledName(false));
								switch (criticalInstruction.getIrCode()) // the double getLiteralPrev() is because the first one's a label
								{
									case Jump_Straight:
									case Return_Concretely:
									case Return_Variably:
									{
										block.children[1].setScope(block.scope + 1);
										break;
									}
									default:
									{
										link(block.wrapperNode, block.children[1].wrapperNode, LinkAnnulOptions.Annul_Both);
										block.children[1].setScope(block.scope);
										block.children[1] = null;
										break;
									}
								}
								
							break;
							}
							*/
							case Return_Concretely:
							case Return_Variably:
							default:
							{
								link(block, block.children[1], LinkAnnulOptions.Annul_Both);
								block.children[1].setScope(block.scope);
								block.children[1] = null;

								break;
							}
						}
					}

					break;
				}
				default:
				{
					throw new IllegalArgumentException("bad length: " + block.children.length);
				}
			}
		}

		return result;
	}

	public static CodeBlock2[] getLinkedAndScopedInstances_twoPass(Instruction first)
	{
		final CodeBlock2[] result = getLinkedAndScopedInstances(first);
		// System.out.println("in CodeBlock2.getLinkedInstances_twoPass()");
		final HalfByteRadixMap<CodeBlock2> lastInScopes = new HalfByteRadixMap<CodeBlock2>();

		for (int i = 0; i < result.length; i++)
		{
			final CodeBlock2 block = result[i];

			switch (block.children.length)
			{
				case 0:
				{
					break;
				}
				case 1:
				{
					if (block.children[0] != null)
					{
						if (block.children[0].getScope() < block.scope)
						{
							final CodeBlock2 downScope = lastInScopes.get(block.children[0].getScope());

							if (downScope != null)
							{
								/*
								System.out.println("suggesting cutAndPaste of " 
								+ block.children[0].name.getMangledName(false) 
								+ " from " + block.name.getMangledName(false) 
								+ " to " + downScope.name.getMangledName(false));
								*/

								link(downScope, block.children[0], LinkAnnulOptions.Annul_Both);
							}

							block.children[0] = null;
						}
					}

					break;
				}
				case 2:
				{
					if (block.children[1] != null)
					{
						final int secondChildScope = block.children[1].getScope();

						if (secondChildScope == block.scope)
						{
							link(block, block.children[1], LinkAnnulOptions.Annul_Both);
							block.children[1] = null;
						}
						else if (secondChildScope < block.scope)
						{
							block.children[1] = null;
						}
					}
					else if (block.wrapperNode.getNext() != null)
					{
						final CodeBlock2 next = block.wrapperNode.getNext().getCargo();

						if (block.children[0].director.getIrCode() == IrCodes.Jump_Branched)
						{
							final Jump_Branched cast = (Jump_Branched) block.children[0].director;

							if (!cast.isNegativeJump())
							{
								//System.out.println("not a negative jump: " + cast.toVerboseCommentForm(false));
								final Instruction criticalInstruction = next.name.getLiteralPrev();

								switch (criticalInstruction.getIrCode()) // the double getLiteralPrev() is because the first one's a label
								{
									case Jump_Straight:
									{
										final Jump_Straight cast2 = (Jump_Straight) criticalInstruction;
										
										if(cast2.isNegativeJump())
										{
											break;
										}
									}
									case Return_Concretely:
									case Return_Variably:
									{
										next.setScope(block.scope + 1);
										block.setAsChild(next, 1);
										Linkable.link_serial(block.wrapperNode, null, LinkAnnulOptions.Annul_Both);
										break;
									}
									default:
									{
										break;
									}
								}
							}
						}
						/*
						case Jump_Branched:
						{
							final Instruction criticalInstruction = block.children[1].name.getLiteralPrev();
							System.out.println("critical instruction for block " + block.children[1].name.getMangledName(false) + ": " + criticalInstruction.getMangledName(false));
							switch (criticalInstruction.getIrCode()) // the double getLiteralPrev() is because the first one's a label
							{
								case Jump_Straight:
								case Return_Concretely:
								case Return_Variably:
								{
									block.children[1].setScope(block.scope + 1);
									break;
								}
								default:
								{
									link(block.wrapperNode, block.children[1].wrapperNode, LinkAnnulOptions.Annul_Both);
									block.children[1].setScope(block.scope);
									block.children[1] = null;
									break;
								}
							}
							
						break;
						}
						*/
					}

					break;
				}
				default:
				{
					throw new IllegalArgumentException("bad length: " + block.children.length);
				}
			}

			lastInScopes.put(block.scope, block);
			//System.out.println("setting scope " + block.scope + " to " + block.name.getMangledName(false));
		}

		return result;
	}

	private void setAsChild(CodeBlock2 child, int index)
	{
		this.children[index] = child;
		child.ref = new ParentRef(this, index);
		Linkable.link_serial(null, child.wrapperNode, LinkAnnulOptions.Annul_Both);
	}

	private void setScope(int newScope)
	{
		if (newScope < this.scope)
		{
			this.scope = newScope;
		}
	}

	public int getScope()
	{
		return this.scope;
	}

	public CharList toCharList(int offset)
	{
		// System.out.println("in CodeBlock2.toCharList() with " + this.name.getMangledName(false));
		final CharList result = new CharList();

		result.addNewLine();
		result.add('\t', offset);
		result.add('*');
		result.add(this.name.getMangledName(false));

		for (int i = 0; i < this.instructions.length; i++)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add(this.instructions[i].toVerboseCommentForm(false), true);
		}

		if (this.director != null)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add("**");
			result.add(this.director.toVerboseCommentForm(false), true);
		}

		switch (this.children.length)
		{
			case 0:
			{
				break;
			}
			case 1:
			{
				if (this.director == null)
				{

				}
				else if (!((Jump_Straight) this.director).isNegativeJump())
				{
					if (this.children[0] != null)
					{
						if (this.children[0].getScope() == this.scope)
						{
							result.addNewLine();
							result.add("//\tDIAG: codeblock " + this.name.getMangledName(false) + " is jumping to codeblock " + this.children[0].name.getMangledName(false));
							// result.add(this.children[0].toCharList(offset), true);
						}
					}
				}
				else
				{
					result.addNewLine();
					result.add('\t', offset);
					result.add("//\tnegJump avoided!");
				}

				break;
			}
			case 2:
			{
				result.addNewLine();
				result.add('\t', offset);
				result.add('{');
				// result.addNewLine();
				result.add(this.children[0].toCharList(offset + 1), true);
				result.addNewLine();
				result.add('\t', offset);
				result.add('}');

				if (this.children[1] != null)
				{
					// if (this.children[1].getScope() + 1 == this.scope)
					// {
					result.addNewLine();
					result.add('\t', offset);
					result.add("else");
					result.addNewLine();
					result.add('\t', offset);
					result.add('{');
					// result.addNewLine();
					result.add(this.children[1].toCharList(offset + 1), true);
					result.addNewLine();
					result.add('\t', offset);
					result.add('}');
					/*
					}
					else
					{
					throw new IllegalArgumentException("childScope: " + (this.children[1].getScope()+1) + ", parentScope: " + this.scope);
					}
					*/
				}
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad length: " + this.children.length);
			}
		}

		final LinkableNode<CodeBlock2> nextNode = this.wrapperNode.getNext();

		if (nextNode != null)
		{
			final CodeBlock2 nextBlock = nextNode.getCargo();

			if (nextBlock.getScope() == this.getScope())
			{
				result.add(this.wrapperNode.getNext().getCargo().toCharList(offset), true);
			}
			else
			{
				result.addNewLine();
				result.add('\t', offset);
				result.add("//\t block " + nextBlock.name.getMangledName(false) + " was next, but fell through!");
			}
		}

		return result;
	}

	private static void link(CodeBlock2 first, CodeBlock2 second, LinkAnnulOptions option)
	{
		Linkable.link_serial(first.wrapperNode, second.wrapperNode, option);
		second.setScope(first.getScope());
		second.ref = null;
	}

	private static ParentRef getEnclosingBlock(CodeBlock2 in)
	{
		while (in != null)
		{
			if (in.ref == null)
			{
				in = in.wrapperNode.getPrev().getCargo();
			}
			else
			{
				return in.ref;
			}
		}

		return null;
	}

	private static class CodeBlockData
	{
		private final String name;
		private final CodeBlock2 block;
		private final Label[] children;

		private CodeBlockData(CodeBlock2 inBlock, Label[] inChildren)
		{
			this.name = inBlock.name.getMangledName(false);
			this.block = inBlock;
			this.children = inChildren;
		}

		public static CodeBlockData getInstance(CodeBlock2 inBlock, Label[] inChildren)
		{
			return new CodeBlockData(inBlock, inChildren);
		}
	}

	private static class CodeBlockDataIterator implements Iterator<CodeBlockData>
	{
		private Instruction current;

		private CodeBlockDataIterator(Instruction inStart)
		{
			this.current = inStart;
		}

		public static CodeBlockDataIterator getInstance(Instruction inStart)
		{
			while (inStart.getIrCode() == IrCodes.NonOp)
			{
				inStart = inStart.getLiteralNext();
			}

			return new CodeBlockDataIterator(inStart);
		}

		@Override
		public boolean hasNext()
		{
			return this.current != null;
		}

		@Override
		public CodeBlockData next()
		{
			final Label label = (Label) this.current;
			this.current = this.current.getLiteralNext();
			final SingleLinkedList<Instruction> queue = new SingleLinkedList<Instruction>();

			while (this.current != null && this.current.getIrCode() != IrCodes.Label)
			{
				queue.add(this.current);
				this.current = this.current.getLiteralNext();
			}

			final Instruction[] instructions;
			Instruction director;
			final Label[] childrenLabels;

			if (this.current != null)
			{
				director = queue.getLast();
				
				if(director == null)
				{
					throw new IllegalArgumentException("label: " + label.getMangledName(false) + "\r\ncurrent: " + this.current.getMangledName(false));
				}

				switch (director.getIrCode())
				{
					case Label:
					{
						throw new UnhandledEnumException(this.current.getIrCode());
					}
					case Jump_Straight:
					{
						final Jump_Straight cast = (Jump_Straight) director;
						childrenLabels = new Label[1];
						childrenLabels[0] = cast.getJumpTo();
						instructions = helper_toArray(queue);
						break;
					}
					case Jump_Branched:
					{
						final Jump_Branched cast = (Jump_Branched) director;
						childrenLabels = new Label[2];
						childrenLabels[0] = cast.getJumpTo();
						childrenLabels[1] = cast.getJumpTo_ifFalse();
						instructions = helper_toArray(queue);
						break;
					}
					case Return_Concretely:
					case Return_Variably:
					{
						childrenLabels = new Label[0];
						instructions = helper_toArray(queue);
						break;
					}
					default:
					{
						director = null;
						instructions = queue.toArray(new Instruction[queue.getSize()]);
						childrenLabels = new Label[1];
						childrenLabels[0] = (Label) this.current;
						break;
					}
				}
			}
			else
			{
				director = null;
				instructions = queue.toArray(new Instruction[queue.getSize()]);
				childrenLabels = new Label[0];
			}

			final CodeBlock2 block = CodeBlock2.getInstance(label, instructions, director, childrenLabels.length);
			final CodeBlockData result = CodeBlockData.getInstance(block, childrenLabels);
			return result;
		}

		/**
		 * converts {@code in} to an array, whilst dropping the last element
		 * 
		 * @param in
		 * @return
		 */
		private static Instruction[] helper_toArray(SingleLinkedList<Instruction> in)
		{
			final Instruction[] result = new Instruction[in.getSize() - 1];

			for (int i = 0; i < result.length; i++)
			{
				result[i] = in.pop();
			}

			return result;
		}
	}

	private static class ParentRef
	{
		private final CodeBlock2 parent;
		private final int ref;

		public ParentRef(CodeBlock2 inParent, int inRef)
		{
			this.parent = inParent;
			this.ref = inRef;
		}
	}
}
