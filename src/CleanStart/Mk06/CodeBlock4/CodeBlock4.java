package CleanStart.Mk06.CodeBlock4;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.Instructions.Label;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import Linkable.LinkAnnulOptions;
import Linkable.LinkableTreeNode;
import Linkable.MauledLinkException;

public abstract class CodeBlock4
{
	protected final Label nameLabel;
	protected final CodeBlockTypes codeBlockType;
	protected final LinkableTreeNode<CodeBlock4> selfNode;
	protected ScopeTypes scopeType;
	protected boolean isLoop;

	public CodeBlock4(Label inNameLabel, CodeBlockTypes inCodeBlockType, LinkableTreeNode<CodeBlock4> inSelfNode)
	{
		this.nameLabel = inNameLabel;
		this.codeBlockType = inCodeBlockType;
		this.selfNode = inSelfNode;
		this.scopeType = ScopeTypes.Linear;
		this.isLoop = false;
	}

	public static CodeBlock4 getInstance(Label inNameLabel, int numKids, Instruction[] code)
	{
		final NormalCodeBlock4 result = new NormalCodeBlock4(inNameLabel, new LinkableTreeNode<CodeBlock4>(numKids), code);

		result.selfNode.setCargo(result);

		return result;
	}

	public static CodeBlock4 getInstance(Label inNameLabel, int numKids, CodeBlock4 inWrapper, CodeBlock4 joiner)
	{
		final WrapCodeBlock4 result = new WrapCodeBlock4(inNameLabel, new LinkableTreeNode<CodeBlock4>(numKids), inWrapper);

		result.selfNode.setCargo(result);

		if (joiner != null)
		{
			result.selfNode.setNext(joiner.selfNode, LinkAnnulOptions.Annul_Both);
		}

		return result;
	}
	
	public static CodeBlock4 getInstance_Joiner(Label inNameLabel, int numKids, CodeBlock4 inFirst, CodeBlock4 inSecond)
	{
		final JoinerCodeBlock4 result = new JoinerCodeBlock4(inNameLabel, new LinkableTreeNode<CodeBlock4>(numKids), inFirst, inSecond);
		
		result.selfNode.setCargo(result);
		
		return result;
	}

	public void setScopeType(ScopeTypes in)
	{
		//System.out.println("setting scopetype of " + this.nameLabel.getMangledName(false) + " to " + in.toString());
		this.scopeType = in;
	}

	public ScopeTypes getScopeType()
	{
		return this.scopeType;
	}

	public Label getNameLabel()
	{
		return this.nameLabel;
	}

	public String getMangledName()
	{
		return this.nameLabel.getMangledName(false);
	}

	public void setLoopState(boolean loopState)
	{
		this.isLoop = loopState;
	}

	protected final boolean hasNext()
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

	protected final CodeBlock4 getNext()
	{
		return this.selfNode.getNext().getCargo();
	}

	public final void setNext(CodeBlock4 in)
	{
		this.selfNode.setNext(in.selfNode, LinkAnnulOptions.Annul_Both);
	}

	public final void setNext(CodeBlock4 in, LinkAnnulOptions option)
	{
		this.selfNode.setNext(in.selfNode, option);
	}

	public final CodeBlock4 getChild(int index)
	{
		return this.selfNode.getChild(index).getCargo();
	}

	public final void setChild(CodeBlock4 in, int index)
	{
		this.selfNode.setChild(in.selfNode, index, LinkAnnulOptions.Annul_Both);
	}

	public final void setChild(CodeBlock4 in, int index, LinkAnnulOptions option)
	{
		this.selfNode.setChild(in.selfNode, index, option);
	}

	public abstract CharList toCharList(int offset, boolean skipInstructions);

	public abstract CharList toCascadingCharList(int offset, boolean skipInstructions);

	public abstract CharList toCodeCharList(int offset);

	private static enum CodeBlockTypes
	{
		Normal,
		Wrapper,
		Joiner;
	}

	public static enum ScopeTypes
	{
		IfElse,
		ElselessIf,
		Loop,
		Linear,
		Wrapper;
	}

	private static ScopeTypes getScopeType(CodeBlock4 in)
	{
		final ScopeTypes result;

		if (in.selfNode.getNumChildren() == 2)
		{
			if (in.isLoop)
			{
				result = ScopeTypes.Loop;
			}
			else if (in.selfNode.getChild(1) == in.selfNode.getNext())
			{
				result = ScopeTypes.ElselessIf;
			}
			else
			{
				result = ScopeTypes.IfElse;
			}
		}
		else
		{
			result = ScopeTypes.Linear;
		}

		return result;
	}

	public static class NormalCodeBlock4 extends CodeBlock4
	{
		private final Instruction[] code;

		protected NormalCodeBlock4(Label inNameLabel, LinkableTreeNode<CodeBlock4> inSelfNode, Instruction[] inCode)
		{
			super(inNameLabel, CodeBlockTypes.Normal, inSelfNode);
			this.code = inCode;
		}

		@Override
		public CharList toCharList(int offset, boolean skipInstructions)
		{
			final CharList result = new CharList();

			result.add('\t', offset);
			result.add("Label: ");
			result.add(this.nameLabel.getMangledName(false));

			// System.out.println("charlisting: " + this.nameLabel.getMangledName(false));

			if (!skipInstructions)
			{
				for (int i = 0; i < this.code.length; i++)
				{
					result.addNewLine();
					result.add('\t', offset + 1);
					result.add(this.code[i].toVerboseCommentForm(false), true);
				}
			}

			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("hasChildren: {");

			final LinkableTreeNode<CodeBlock4>[] wrappedKids = this.selfNode.getChildren();

			for (int i = 0; i < wrappedKids.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				if (wrappedKids[i] != null)
				{
					result.add(wrappedKids[i].getCargo().getMangledName());
				}
				else
				{
					result.add("null");
				}
			}

			result.add('}');
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("next block: ");

			if (this.hasNext())
			{
				result.add(this.getNext().getMangledName());
			}
			else
			{
				result.add("null");
			}

			return result;
		}

		@Override
		public CharList toCascadingCharList(int offset, boolean skipInstructions)
		{
			final CharList result = new CharList();

			result.add('\t', offset);
			result.add("Label: ");
			result.add(this.nameLabel.getMangledName(false));

			if (!skipInstructions)
			{
				for (int i = 0; i < this.code.length; i++)
				{
					result.addNewLine();
					result.add('\t', offset + 1);
					result.add(this.code[i].toVerboseCommentForm(false), true);
				}
			}

			final LinkableTreeNode<CodeBlock4>[] kids = this.selfNode.getChildren();

			if (kids.length > 0)
			{
				result.addNewLine();
				result.add('\t', offset + 1);
				result.add('{');

				for (LinkableTreeNode<CodeBlock4> kid : kids)
				{
					result.addNewLine();
					result.add(kid.getCargo().toCascadingCharList(offset + 2, skipInstructions), true);
				}

				result.addNewLine();
				result.add('\t', offset + 1);
				result.add('}');
			}

			if (this.selfNode.hasNext())
			{
				result.addNewLine();
				result.add(this.selfNode.getNext().getCargo().toCascadingCharList(offset, skipInstructions), true);
			}

			return result;
		}

		@Override
		public CharList toCodeCharList(int offset)
		{
			final CharList result = new CharList();

			switch (this.scopeType)
			{
				case Loop:
				case ElselessIf:
				{
					if (this.code.length != 0)
					{

						Instruction current = this.code[0];
						int index = 0;

						while (current != null && current.getIrCode() != IrCodes.Jump_Branched && index < this.code.length - 1)
						{
							result.addNewLine();
							result.add('\t', offset);
							result.add(current.toVerboseCommentForm(false), true);
							current = this.code[++index];
						}

						// final Jump_Branched branch = (Jump_Branched) current;

						result.addNewLine();
						result.add('\t', offset);
						result.add(current.toVerboseCommentForm(false), true);

						if (this.scopeType == ScopeTypes.Loop)
						{
							result.add('\t');
							result.add("//\tloop");
						}

						result.addNewLine();
						result.add('\t', offset);
						result.add('{');
						result.addNewLine();

						final CodeBlock4 childBlock = this.selfNode.getChildCargo_defensively(0);

						if (childBlock == null)
						{
							throw new NullPointerException("Null child (of " + this.selfNode.getNumChildren() + ") for " + this.nameLabel.getMangledName(false));
						}

						result.add(childBlock.toCodeCharList(offset + 1), true);
						result.addNewLine();
						result.add('\t', offset);
						result.add('}');
					}

					break;
				}
				case IfElse:
				{
					Instruction current = this.code[0];
					int index = 0;

					while (current != null && current.getIrCode() != IrCodes.Jump_Branched)
					{
						result.addNewLine();
						result.add('\t', offset);
						result.add(current.toVerboseCommentForm(false), true);
						current = this.code[++index];
					}

					// final Jump_Branched branch = (Jump_Branched) current;

					result.addNewLine();
					result.add('\t', offset);
					result.add(current.toVerboseCommentForm(false), true);
					result.add('\t');
					result.add("//\tloop");
					result.addNewLine();
					result.add('\t', offset);
					result.add('{');
					result.add(this.selfNode.getChild(0).getCargo().toCodeCharList(offset + 1), true);
					result.addNewLine();
					result.add('\t', offset);
					result.add('}');
					result.addNewLine();
					result.add('\t', offset);
					result.add("else");
					result.addNewLine();
					result.add('\t', offset);
					result.add('{');
					result.addNewLine();
					result.add(this.selfNode.getChild(1).getCargo().toCodeCharList(offset + 1), true);
					result.addNewLine();
					result.add('\t', offset);
					result.add('}');

					break;
				}
				case Linear:
				{
					for (int i = 0; i < this.code.length; i++)
					{
						result.addNewLine();
						result.add('\t', offset);
						result.add(this.code[i].toVerboseCommentForm(false), true);
					}

					final LinkableTreeNode<CodeBlock4> child = this.selfNode.getChild_defensively(0);

					if (child != null)
					{
						if (!(this.hasNext() && this.getNext() == child.getCargo()))
						{
							result.addNewLine();
							result.add(child.getCargo().toCodeCharList(offset), true);
						}
					}

					break;
				}
				default:
				{
					throw new UnhandledEnumException(scopeType);
				}
			}

			if (this.hasNext())
			{
				result.addNewLine();
				result.add(this.getNext().toCodeCharList(offset), true);
			}

			return result;
		}
	}

	public static class WrapCodeBlock4 extends CodeBlock4
	{
		private final CodeBlock4 wrapper;

		protected WrapCodeBlock4(Label inNameLabel, LinkableTreeNode<CodeBlock4> inSelfNode, CodeBlock4 inWrapper)
		{
			super(inNameLabel, CodeBlockTypes.Wrapper, inSelfNode);
			this.wrapper = inWrapper;
		}

		@Override
		public CharList toCharList(int offset, boolean skipInstructions)
		{
			final CharList result = new CharList();

			result.add('\t', offset);
			result.add("Label: ");
			result.add(this.nameLabel.getMangledName(false));

			// System.out.println("charlisting: " + this.nameLabel.getMangledName(false));

			if (!skipInstructions)
			{
				result.addNewLine();
				result.add(this.wrapper.toCharList(offset + 1, skipInstructions), true);
			}

			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("wraps: {");

			final LinkableTreeNode<CodeBlock4>[] wrappedWrappedKids = this.wrapper.selfNode.getChildren();

			for (int i = 0; i < wrappedWrappedKids.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				if (wrappedWrappedKids[i] != null)
				{
					result.add(wrappedWrappedKids[i].getCargo().getMangledName());
				}
				else
				{
					result.add("null");
				}
			}
			result.add('}');

			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("hasChildren: {");

			final LinkableTreeNode<CodeBlock4>[] wrappedKids = this.selfNode.getChildren();

			for (int i = 0; i < wrappedKids.length; i++)
			{
				if (i != 0)
				{
					result.add(", ");
				}

				if (wrappedKids[i] != null)
				{
					result.add(wrappedKids[i].getCargo().getMangledName());
				}
				else
				{
					result.add("null");
				}
			}

			result.add('}');
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("next block: ");

			if (this.hasNext())
			{
				result.add(this.getNext().getMangledName());
			}
			else
			{
				result.add("null");
			}

			return result;
		}

		@Override
		public CharList toCascadingCharList(int offset, boolean skipInstructions)
		{
			final CharList result = new CharList();

			result.add('\t', offset);
			result.add("Label: ");
			result.add(this.nameLabel.getMangledName(false));

			final LinkableTreeNode<CodeBlock4>[] kids = this.selfNode.getChildren();

			if (kids.length > 0)
			{
				result.addNewLine();
				result.add('\t', offset + 1);
				result.add('{');

				for (LinkableTreeNode<CodeBlock4> kid : kids)
				{
					if (kid != null)
					{
						result.addNewLine();
						result.add(kid.getCargo().toCascadingCharList(offset + 2, skipInstructions), true);
					}
				}

				result.addNewLine();
				result.add('\t', offset + 1);
				result.add('}');
			}

			if (this.selfNode.hasNext())
			{
				result.addNewLine();
				result.add(this.selfNode.getNext().getCargo().toCascadingCharList(offset, skipInstructions), true);
			}

			return result;
		}

		@Override
		public CharList toCodeCharList(int offset)
		{
			return this.selfNode.getChildCargo(0).toCodeCharList(offset);
		}
	}
	
	public static class JoinerCodeBlock4 extends CodeBlock4
	{
		private final CodeBlock4 first;
		private final CodeBlock4 second;
		
		public JoinerCodeBlock4(Label inNameLabel, LinkableTreeNode<CodeBlock4> inSelfNode, CodeBlock4 inFirst, CodeBlock4 inSecond)
		{
			super(inNameLabel, CodeBlockTypes.Joiner, inSelfNode);
			this.first = inFirst;
			this.second = inSecond;
		}

		@Override
		public CharList toCharList(int offset, boolean skipInstructions)
		{
			final CharList result = new CharList();
			
			result.add('\t', offset);
			result.add("Label: ");
			result.add(this.nameLabel.getMangledName(false));
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("Joined: ");
			result.add(this.first.getMangledName());
			result.add(", ");
			result.add(this.second.getMangledName());
			
			return result;
		}

		@Override
		public CharList toCascadingCharList(int inOffset, boolean inSkipInstructions)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CharList toCodeCharList(int inOffset)
		{
			final CharList result = this.first.toCodeCharList(inOffset);
			
			result.addNewLine();
			result.add(this.second.toCodeCharList(inOffset), true);
			
			return result;
		}
	}
}
