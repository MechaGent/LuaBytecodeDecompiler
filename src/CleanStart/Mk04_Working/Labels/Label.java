package CleanStart.Mk04_Working.Labels;

import CharList.CharList;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Labels.BaseLabel.LabelData;
import HalfByteArray.HalfByteArray;
import HalfByteRadixMap.HalfByteRadixMap;

public interface Label extends Instruction
{
	public void absorbSuperfluousLabel(Label in);
	
	/**
	 * 
	 * @param in
	 * @param originIndex for example, can only be 0 for Jump_Straight, but could be either 0 or 1 for Jump_Branched
	 */
	public void setInstructionAsIncoming(Instruction in, int originIndex);
	public HalfByteArray getHalfByteHash();
	public CharList getCodeForm(int offset, boolean shouldCommentOut);
	public HalfByteRadixMap<LabelData> getIncoming();

	public String getLabelValue();
	public int compareTo(Label in);
	public int getInstanceNumber();
	public void incrementNumCloseBracesToPrepend();
	public void decrementNumCloseBracesToPrepend();
	public void incrementNumOpenBracesToAppend();
	public void decrementNumOpenBracesToAppend();
	public void prependElseOpener();
	public int getNumCloseBracesToPrepend();
	public int getNumOpenBracesToAppend();
}
