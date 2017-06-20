package CleanStart.Mk01.Instructions;

import java.util.Map.Entry;

import CharList.CharList;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.Instruction;
import CleanStart.Mk01.Interfaces.JumpingInstruction;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;
import HalfByteRadixMap.HalfByteRadixMap;

public class JumpLabel implements Instruction
{
	private final String label;
	private final HalfByteRadixMap<BackPointer> incoming;
	private boolean isExpired;

	public JumpLabel(String inLabel)
	{
		this.label = inLabel;
		this.incoming = new HalfByteRadixMap<BackPointer>();
		this.isExpired = false;
	}

	@Override
	public HalfByteArray getHalfByteHash()
	{
		return HalfByteArrayFactory.convertIntoArray(this.label);
	}

	public String getLabel()
	{
		return this.label;
	}

	public void addIncomingJump(JumpingInstruction in, int viaIndex)
	{
		final BackPointer result = new BackPointer(in, viaIndex);
		this.incoming.put(in.getHalfByteHash(), result);
	}

	public void absorbSuperfluousJumpLabel(JumpLabel in)
	{
		if (this != in)
		{
			for (Entry<HalfByteArray, BackPointer> pointer : in.incoming)
			{
				final BackPointer point = pointer.getValue();
				this.incoming.put(pointer.getKey(), point);
				point.incoming.setIndexedJumpLabelTo(point.index, this);
			}
		}
	}

	@Override
	public IrCodes getIrCode()
	{
		return IrCodes.Jump_Label;
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset)
	{
		return this.translateIntoCommentedByteCode(inOffset, new CharList());
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList in)
	{
		in.add('\t', inOffset);
		in.add(IrCodes.Jump_Label.toString());
		in.add('-');
		in.add(this.label);
		in.add('\t');
		in.add("Receives jumps from: {");
		boolean isFirst = true;

		for (Entry<HalfByteArray, BackPointer> entry : this.incoming)
		{
			// System.out.println("in set of size " + this.incoming.size() + ", " + entry.getValue().toString());

			if (isFirst)
			{
				isFirst = false;
			}
			else
			{
				in.add(", ");
			}

			in.add(entry.getKey().interpretAsChars(), true);
		}

		in.add('}');

		return in;
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList inIn, StepStage inTime)
	{
		return this.translateIntoCommentedByteCode(inOffset, inIn);
	}
	
	@Override
	public boolean isExpired()
	{
		return this.isExpired;
	}
	
	private static class BackPointer
	{
		private final JumpingInstruction incoming;
		private int index;

		public BackPointer(JumpingInstruction inIncoming, int inIndex)
		{
			this.incoming = inIncoming;
			this.index = inIndex;
		}
	}
}
