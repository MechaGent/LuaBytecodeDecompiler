package CleanStart.Mk01.Interfaces;

import CharList.CharList;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.IrCodes;
import HalfByteArray.HalfByteArray;

public interface Instruction
{
	public HalfByteArray getHalfByteHash();
	public IrCodes getIrCode();
	public boolean isExpired();
	public CharList translateIntoCommentedByteCode(int offset);
	public CharList translateIntoCommentedByteCode(int offset, CharList in);
	public CharList translateIntoCommentedByteCode(int offset, CharList in, StepStage time);
}
