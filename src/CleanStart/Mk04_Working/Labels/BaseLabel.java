package CleanStart.Mk04_Working.Labels;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.BaseInstruct;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;
import HalfByteRadixMap.HalfByteRadixMap;

public abstract class BaseLabel extends BaseInstruct implements Label
{
	protected static int instanceCounter = 0;
	private final String prefix;
	private final HalfByteRadixMap<LabelData> incoming;

	private int numCloseBracesToPrepend;
	private int numOpenBracesToAppend;
	private boolean shouldPrependElseOpener;
	// private boolean isStartOfDoWhile;

	public BaseLabel(String inPrefix, int inInstanceNumber)
	{
		super(IrCodes.Label, inInstanceNumber);
		this.prefix = inPrefix;
		this.incoming = new HalfByteRadixMap<LabelData>();
		this.numCloseBracesToPrepend = 0;
		this.numOpenBracesToAppend = 0;
		this.shouldPrependElseOpener = false;
		// this.isStartOfDoWhile = false;
	}

	@Override
	public String getLabelValue()
	{
		return this.prefix + '_' + this.instanceNumber;
	}

	@Override
	public String getMangledName()
	{
		return this.getLabelValue();
	}

	@Override
	public HalfByteRadixMap<LabelData> getIncoming()
	{
		return this.incoming;
	}

	@Override
	public HalfByteArray getHalfByteHash()
	{
		return HalfByteArrayFactory.convertIntoArray(this.getLabelValue());
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.No;
	}

	@Override
	public Variable getInlinedForm()
	{
		return null;
	}

	@Override
	public CharList getCodeForm(int offset, boolean shouldCommentOut)
	{
		final CharList result = new CharList();

		result.add('}', this.numCloseBracesToPrepend);

		if (this.shouldPrependElseOpener)
		{
			result.add("else");
			result.addNewLine();
			result.add('\t', offset);
			result.add('{');
			result.addNewLine();
			result.add('\t', offset);
		}

		if (shouldCommentOut)
		{
			result.add('\t');
			result.add("//");
		}

		result.add(this.getLabelValue());

		if (this.numOpenBracesToAppend != 0)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add('{', this.numOpenBracesToAppend);
		}

		return result;
	}

	@Override
	public void setInstructionAsIncoming(Instruction in, int originIndex)
	{
		final LabelData result = new LabelData(originIndex, in);
		this.incoming.put(result.getHalfByteHash(), result);
	}

	@Override
	public void absorbSuperfluousLabel(Label in)
	{
		if (this != in)
		{
			for (Entry<HalfByteArray, LabelData> entry : in.getIncoming())
			{
				this.incoming.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();

		result.add("Label: ");
		result.add(this.getLabelValue());

		return result;
	}

	@Override
	public void revVarTypes()
	{

	}

	@Override
	public int compareTo(Label in)
	{
		return this.instanceNumber - in.getInstanceNumber();
	}

	@Override
	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}

	@Override
	public void incrementNumCloseBracesToPrepend()
	{
		this.numCloseBracesToPrepend++;
	}

	@Override
	public void decrementNumCloseBracesToPrepend()
	{
		this.numCloseBracesToPrepend--;
	}

	@Override
	public void incrementNumOpenBracesToAppend()
	{
		this.numOpenBracesToAppend++;
	}

	@Override
	public void decrementNumOpenBracesToAppend()
	{
		this.numOpenBracesToAppend--;
	}

	@Override
	public void prependElseOpener()
	{
		this.shouldPrependElseOpener = true;
	}

	@Override
	public int getNumCloseBracesToPrepend()
	{
		return this.numCloseBracesToPrepend;
	}

	@Override
	public int getNumOpenBracesToAppend()
	{
		return this.numOpenBracesToAppend;
	}

	/*
	@Override
	public CharList getDecompiledForm()
	{
		final CharList result = new CharList('}', this.numCloseBracesToPrepend);
		result.addNewLine();
		result.add("//\t");
		result.add(this.getLabelValue());
		return result;
	}
	 */

	public static class LabelData
	{
		private final int originIndex;
		private final Instruction incoming;
		private final String mangledName;

		public LabelData(int inOriginIndex, Instruction inIncoming)
		{
			this.originIndex = inOriginIndex;
			this.incoming = inIncoming;

			final CharList mangle = new CharList();
			mangle.add(inIncoming.getMangledName());
			mangle.add('_');
			mangle.addAsString(inOriginIndex);

			this.mangledName = mangle.toString();
		}

		public String getMangledName()
		{
			return this.mangledName;
		}

		public HalfByteArray getHalfByteHash()
		{
			return HalfByteArrayFactory.convertIntoArray(this.mangledName);
		}

		public int getOriginIndex()
		{
			return this.originIndex;
		}

		public Instruction getIncoming()
		{
			return this.incoming;
		}
	}
}
