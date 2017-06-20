package CleanStart.Mk06.CodeBlock5;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;
import HalfByteRadixMap.HalfByteRadixMap;
import SingleLinkedList.Mk01.SingleLinkedList;

public class PassData
{
	private String name;
	private HalfByteArray halfByteName;
	private final Payload payload;
	private BlockTypes blockType;
	protected int numVisibleChildren;
	private boolean isWrapped;
	private boolean wasWrittenAlready;

	protected PassData(String inName, Payload inPayload, BlockTypes inBlockType, int inNumVisibleChildren)
	{
		this.name = inName;
		this.halfByteName = HalfByteArrayFactory.wrapIntoArray(inName);
		this.payload = inPayload;
		this.blockType = inBlockType;
		this.numVisibleChildren = inNumVisibleChildren;
		this.isWrapped = false;
		this.wasWrittenAlready = false;
	}

	public static PassData getInstance(String inName, SingleLinkedList<Instruction> inPayload, String... inChildren)
	{
		return getInstance(inName, inPayload.toArray(new Instruction[inPayload.getSize()]), inChildren);
	}

	public static PassData getInstance(String inName, Instruction[] inPayload, String... inChildren)
	{
		final Payload locPayload = new InstructionPayload(inPayload);
		final PassData result = getInstance(inName, locPayload, inChildren);

		return result;
	}

	public static PassData getInstance(String inName, PassData inPayload, String... inChildren)
	{
		final Payload locPayload = new PassDataPayload(inPayload);
		final PassData result = getInstance(inName, locPayload, inChildren);

		return result;
	}

	private static PassData getInstance(String inName, Payload inPayload, String... inChildren)
	{
		final PassData result;

		switch (inChildren.length)
		{
			case 0:
			{
				result = new PassData(inName, inPayload, BlockTypes.Linear_Flows, 0);
				break;
			}
			case 1:
			{
				result = new OneChildPassData(inName, inPayload, BlockTypes.Linear_Flows, inChildren[0]);
				break;
			}
			case 2:
			{
				result = new TwoChildPassData(inName, inPayload, BlockTypes.Linear_Flows, inChildren[0], inChildren[1]);
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}

		return result;
	}

	public void setWrapState(boolean wrapState)
	{
		this.isWrapped = wrapState;
	}

	public boolean isWrapped()
	{
		return this.isWrapped;
	}

	public BlockTypes getBlockType()
	{
		return this.blockType;
	}

	public void setBlockType(BlockTypes inBlockType)
	{
		this.blockType = inBlockType;
	}

	public boolean isTerminal()
	{
		final boolean result;

		switch (this.blockType)
		{
			case Linear_Returns:
			{
				result = true;
				break;
			}
			default:
			{
				result = this.numVisibleChildren == 0;
				break;
			}
		}

		return result;
	}

	public void setName(String newName)
	{
		this.name = newName;
		this.halfByteName = HalfByteArrayFactory.wrapIntoArray(newName);
	}

	public String getName()
	{
		return this.name;
	}

	public HalfByteArray getHalfByteName()
	{
		return this.halfByteName;
	}

	public void ignoreChild0()
	{

	}

	public void ignoreChild1()
	{

	}

	public int getNumVisibleChildren()
	{
		return this.numVisibleChildren;
	}

	public boolean hasChild0()
	{
		return false;
	}

	public boolean hasChild1()
	{
		return false;
	}

	public String getChild0()
	{
		return "<ErRoR>";
	}

	public String getChild1()
	{
		return "<ErRoR>";
	}

	public CharList toSimpleCharList()
	{
		final CharList result = new CharList();

		if (this.wasWrittenAlready)
		{
			System.err.println("Block " + this.name + " was already written!");
		}
		else
		{
			this.wasWrittenAlready = true;
		}

		result.add("name: ");
		result.add(this.name);

		if (this.payload instanceof PassDataPayload)
		{
			final PassDataPayload cast = (PassDataPayload) this.payload;

			result.addNewLine();
			result.add('\t', 2);
			result.add("wraps: ");
			result.add(cast.cargo.name);
		}

		result.addNewLine();
		result.add('\t');
		result.add("blockType: ");
		result.add(this.blockType.toString());
		result.addNewLine();
		result.add('\t');
		result.add("numVisibleChildren: ");
		result.addAsString(this.numVisibleChildren);

		return result;
	}

	public CharList toDiagCharList()
	{
		final CharList result = new CharList();

		if (this.wasWrittenAlready)
		{
			System.err.println("Block " + this.name + " was already written!");
		}
		else
		{
			this.wasWrittenAlready = true;
		}

		result.add("name: ");
		result.add(this.name);

		if (this.payload instanceof PassDataPayload)
		{
			final PassDataPayload cast = (PassDataPayload) this.payload;

			result.addNewLine();
			result.add('\t', 2);
			result.add("wraps: ");
			result.add(cast.cargo.name);
		}
		else if (this.payload instanceof InstructionPayload)
		{
			final InstructionPayload cast = (InstructionPayload) this.payload;

			for (int i = 0; i < cast.cargo.length; i++)
			{
				result.addNewLine();
				result.add('\t', 3);
				// System.out.println("printing: " + cast.cargo[i].getMangledName(false));
				result.add(cast.cargo[i].toVerboseCommentForm(false), true);
			}
		}

		result.addNewLine();
		result.add('\t');
		result.add("blockType: ");
		result.add(this.blockType.toString());
		result.addNewLine();
		result.add('\t');
		result.add("numVisibleChildren: ");
		result.addAsString(this.numVisibleChildren);

		return result;
	}

	public CharList toCascadingCodeCharList(int offset, HalfByteRadixMap<PassData> map)
	{
		final CharList result = new CharList();

		result.addNewLine();
		//result.add('\t', offset);
		//result.add("//BlockType: ");
		//result.add(this.blockType.toString());
		//result.addNewLine();

		switch (this.blockType)
		{
			case Loop:
			{
				result.addNewLine();
				result.add('\t', offset);
				result.add("//loop");
			}
			case ElselessIf_ComplexFlows:
			case ElselessIf_Flows:
			case ElselessIf_TerminalIf:
			case IfElse_Simple:
			{
				result.add(this.payload.toCodeCharList(offset, map), true);
				result.addNewLine();
				result.add('\t', offset);
				result.add('{');
				//result.addNewLine();

				if (this.hasChild0())
				{
					final PassData Child0 = map.get(this.getChild0());

					result.add(Child0.toCascadingCodeCharList(offset + 1, map), true);
					result.addNewLine();
				}
				else
				{
					throw new IllegalArgumentException();
				}

				result.add('\t', offset);
				result.add('}');

				if (this.hasChild1())
				{
					final PassData Child1 = map.get(this.getChild1());

					result.addNewLine();

					if (this.blockType == BlockTypes.Loop)
					{
						result.add(Child1.toCascadingCodeCharList(offset, map), true);
					}
					else
					{
						result.add('\t', offset);
						result.add("else");
						result.addNewLine();
						result.add('\t', offset);
						result.add('{');
						result.add(Child1.toCascadingCodeCharList(offset + 1, map), true);
						result.addNewLine();
						result.add('\t', offset);
						result.add('}');
					}

					// result.addNewLine();
				}

				break;
			}
			case Linear_Flows:
			case Linear_Returns:
			case Linear_Stops:
			{
				result.add(this.payload.toCodeCharList(offset, map), true);

				if (this.hasChild0())
				{
					final PassData Child0 = map.get(this.getChild0());

					result.addNewLine();
					result.add(Child0.toCascadingCodeCharList(offset, map), true);
				}

				// result.addNewLine();

				break;
			}
			default:
			{
				throw new UnhandledEnumException(this.blockType);
			}
		}

		return result;
	}

	private static abstract class Payload
	{
		public abstract CharList toCodeCharList(int offset, HalfByteRadixMap<PassData> map);
	}

	private static class PassDataPayload extends Payload
	{
		private final PassData cargo;

		public PassDataPayload(PassData inCargo)
		{
			super();
			this.cargo = inCargo;
		}

		@Override
		public CharList toCodeCharList(int offset, HalfByteRadixMap<PassData> map)
		{
			// System.out.println("here");
			return this.cargo.toCascadingCodeCharList(offset, map);
		}
	}

	private static class InstructionPayload extends Payload
	{
		private final Instruction[] cargo;

		public InstructionPayload(Instruction[] inCargo)
		{
			super();
			this.cargo = inCargo;
		}

		@Override
		public CharList toCodeCharList(int offset, HalfByteRadixMap<PassData> inMap)
		{
			final CharList result = new CharList();

			for (int i = 0; i < this.cargo.length; i++)
			{
				if (i != 0)
				{
					result.addNewLine();
				}
				
				result.add(this.cargo[i].toDecompiledForm(offset), true);
			}

			return result;
		}
	}

	private static class OneChildPassData extends PassData
	{
		private final String Child0;
		private boolean ignore_Child0;

		public OneChildPassData(String inName, Payload inPayload, BlockTypes inBlockType, String inChild0)
		{
			this(inName, inPayload, inBlockType, 1, inChild0);
		}

		protected OneChildPassData(String inName, Payload inPayload, BlockTypes inBlockType, int inNumVisibleChildren, String inChild0)
		{
			super(inName, inPayload, inBlockType, inNumVisibleChildren);
			this.Child0 = inChild0;
			this.ignore_Child0 = false;
		}

		@Override
		public void ignoreChild0()
		{
			if (!this.ignore_Child0)
			{
				this.ignore_Child0 = true;
				this.numVisibleChildren--;
			}
		}

		@Override
		public String getChild0()
		{
			if (!this.ignore_Child0)
			{
				return this.Child0;
			}
			else
			{
				return "";
			}
		}

		@Override
		public boolean hasChild0()
		{
			return !this.ignore_Child0;
		}

		@Override
		public CharList toSimpleCharList()
		{
			final CharList result = super.toSimpleCharList();

			result.addNewLine();
			result.add('\t');
			result.add("Child0: ");
			result.add(this.Child0);

			if (this.ignore_Child0)
			{
				result.add("(ignored)");
			}

			return result;
		}
	}

	private static class TwoChildPassData extends OneChildPassData
	{
		private final String Child1;
		private boolean ignore_Child1;

		public TwoChildPassData(String inName, Payload inPayload, BlockTypes inBlockType, String inChild0, String inChild1)
		{
			super(inName, inPayload, inBlockType, 2, inChild0);
			this.Child1 = inChild1;
			this.ignore_Child1 = false;
		}

		@Override
		public void ignoreChild1()
		{
			if (!this.ignore_Child1)
			{
				this.ignore_Child1 = true;
				this.numVisibleChildren--;
			}
		}

		@Override
		public String getChild1()
		{
			if (!this.ignore_Child1)
			{
				return this.Child1;
			}
			else
			{
				return "";
			}
		}

		@Override
		public boolean hasChild1()
		{
			return !this.ignore_Child1;
		}

		@Override
		public CharList toSimpleCharList()
		{
			final CharList result = super.toSimpleCharList();

			result.add('\t');
			result.add("Child1: ");
			result.add(this.Child1);

			if (this.ignore_Child1)
			{
				result.add("(ignored)");
			}

			return result;
		}
	}
}
