package CleanStart.Mk06.CodeBlock3;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Instructions.Label;
import HalfByteRadixMap.HalfByteRadixMap;
import Linkable.LinkAnnulOptions;
import Linkable.LinkableTreeNode;
import Linkable.MauledLinkException;
import Linkable.ParentReference;

public class CodeBlock3
{
	private final Label nameLabel;
	private final HalfByteRadixMap<ParentReference<CodeBlock3>> incoming;
	private final LinkableTreeNode<CodeBlock3> selfNode;
	private final Instruction[] code;

	private CodeBlock3(Label inNameLabel, LinkableTreeNode<CodeBlock3> inSelfNode, Instruction[] inCode)
	{
		this.nameLabel = inNameLabel;
		this.incoming = new HalfByteRadixMap<ParentReference<CodeBlock3>>();
		this.selfNode = inSelfNode;
		this.code = inCode;
	}

	public static CodeBlock3 getInstance(Label inNameLabel, int numChildren, Instruction[] code)
	{
		final LinkableTreeNode<CodeBlock3> wrap = new LinkableTreeNode<CodeBlock3>(numChildren);
		final CodeBlock3 result = getInstance(inNameLabel, wrap, code);
		wrap.setCargo(result);
		return result;
	}

	public static CodeBlock3 getInstance(Label inNameLabel, LinkableTreeNode<CodeBlock3> inSelfNode, Instruction[] code)
	{
		return new CodeBlock3(inNameLabel, inSelfNode, code);
	}

	public final void addIncoming(CodeBlock3 block, int index)
	{
		this.incoming.put(block.getMangledName(), ParentReference.getInstance(block, index));
	}

	public String getMangledName()
	{
		return this.nameLabel.getMangledName(false);
	}

	public Instruction getFlowInstruction()
	{
		return this.code[this.code.length - 1];
	}

	public CharList toCharList(int offset, boolean skipInstructions)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("Label: ");
		result.add(this.nameLabel.getMangledName(false));
		
		//System.out.println("charlisting: " + this.nameLabel.getMangledName(false));

		if (!skipInstructions)
		{
			for(int i = 0; i < this.code.length; i++)
			{
				result.addNewLine();
				result.add('\t', offset+1);
				result.add(this.code[i].toVerboseCommentForm(false), true);
			}
		}
		
		result.addNewLine();
		result.add('\t', offset+1);
		result.add("hasChildren: {");
		
		final LinkableTreeNode<CodeBlock3>[] wrappedKids = this.selfNode.getChildren();
		
		for(int i = 0; i < wrappedKids.length; i++)
		{
			if(i != 0)
			{
				result.add(", ");
			}
			
			result.add(wrappedKids[i].getCargo().getMangledName());
		}
		
		result.add('}');
		result.addNewLine();
		result.add('\t', offset+1);
		result.add("next block: ");
		
		if(this.hasNext())
		{
			result.add(this.getNext().getMangledName());
		}
		else
		{
			result.add("null");
		}
		
		return result;
	}

	/**
	 * hoists passed block to parent's scope, and appends it after that parent
	 * 
	 * @param blockIndex
	 * @return true if block was elevated, false if impossible
	 */
	public boolean elevateBlock(int blockIndex)
	{
		if (blockIndex == -1)
		{
			// elevate this.next
			final CodeBlock3 target = this.getNext();
			CodeBlock3 goodParent = this.findHigherParent(target);

			/*
			 * regarding goodParent.hasNext():
			 * might need to hoist this too, if it exists
			 */
			while (goodParent != null)
			{
				if (goodParent.hasNext())
				{
					if (goodParent.getNext() == target)
					{
						// keep the option to 'Do Not Annul'
						this.selfNode.setNext(null, LinkAnnulOptions.DoNotAnnul);
						return true;
					}

					goodParent = this.findHigherParent(target);
				}
				else
				{
					break;
				}
			}

			if (goodParent != null)
			{
				// order is important here
				this.removeNext();
				goodParent.setNext(target);
				return true;
			}
		}
		else
		{
			// elevate child at index
			final CodeBlock3 target = this.selfNode.getChild(blockIndex).getCargo();
			CodeBlock3 goodParent = this;

			/*
			 * regarding goodParent.hasNext():
			 * might need to hoist this too, if it exists
			 */
			while (goodParent != null)
			{
				if (goodParent.hasNext())
				{
					if (goodParent.getNext() == target)
					{
						// keep the option to 'Do Not Annul'
						this.selfNode.setChild(null, blockIndex, LinkAnnulOptions.DoNotAnnul);
						return true;
					}

					goodParent = this.findHigherParent(target);
				}
				else
				{
					break;
				}
			}

			if (goodParent != null)
			{
				// order is important here
				this.removeChild(blockIndex);
				goodParent.setNext(target);
				return true;
			}
		}

		return false;
	}

	private CodeBlock3 findHigherParent(CodeBlock3 block)
	{
		LinkableTreeNode<CodeBlock3> current = block.selfNode.getParent_indirectly();
		boolean isDone = false;

		while (!isDone)
		{
			if (current == null)
			{
				isDone = true;
				break;
			}
			else
			{
				final CodeBlock3 result = current.getCargo();

				if (result != null)
				{
					return result;
				}
				else
				{
					current = current.getParent_indirectly();
				}
			}
		}

		return null;
	}

	private final boolean hasNext()
	{
		if (this.selfNode.hasNext())
		{
			if (this.selfNode.getNext().hasCargo())
			{
				return true;
			}
			else
			{
				throw new MauledLinkException("Next");
			}
		}
		else
		{
			return false;
		}
	}
	
	public final void setChild(CodeBlock3 in, int index)
	{
		this.selfNode.setChild(in.selfNode, index, LinkAnnulOptions.Annul_Both);
	}
	
	public final void setChild(CodeBlock3 in, int index, LinkAnnulOptions option)
	{
		this.selfNode.setChild(in.selfNode, index, option);
	}
	
	public final CodeBlock3 getChild(int index)
	{
		return this.selfNode.getChild(index).getCargo();
	}

	public final void setNext(CodeBlock3 in)
	{
		this.selfNode.setNext(in.selfNode, LinkAnnulOptions.Annul_Both);
	}
	
	public final void setNext(CodeBlock3 in, LinkAnnulOptions option)
	{
		this.selfNode.setNext(in.selfNode, option);
	}

	private final void removeNext()
	{
		this.selfNode.setNext(null, LinkAnnulOptions.Annul_Both);
	}

	private final void removeChild(int childIndex)
	{
		this.selfNode.setChild(null, childIndex, LinkAnnulOptions.Annul_Both);
	}

	private final CodeBlock3 getNext()
	{
		return this.selfNode.getNext().getCargo();
	}
}
