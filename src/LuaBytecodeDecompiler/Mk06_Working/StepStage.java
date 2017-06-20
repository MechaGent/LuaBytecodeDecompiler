package LuaBytecodeDecompiler.Mk06_Working;

import CharList.CharList;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;

public class StepStage implements Comparable<StepStage>
{
	private static final boolean allowWriteSwaps = false;
	private static final StepStage NullStepStage = new StepStage(-1, -1, -1, "NullStepStage");
	private static final StepStage ZeroStepStage = new StepStage(0, 0, 0, "ZeroStepStage");
	private static final int DefaultMinChars = 10;
	
	private final int lineNum;
	private final int subLineNum;
	private final int stepNum;
	private final String stageName;
	
	private HalfByteArray halfByteHash;

	public StepStage(int inLineNum, int inSubLineNum, int inStepNum, String inStageName)
	{
		this.lineNum = inLineNum;
		this.subLineNum = inSubLineNum;
		this.stepNum = inStepNum;
		this.stageName = inStageName;
		this.halfByteHash = null;
	}

	public int getLineNum()
	{
		return this.lineNum;
	}

	public int getSubLineNum()
	{
		return this.subLineNum;
	}

	public int getStepNum()
	{
		return this.stepNum;
	}

	public String getStageName()
	{
		return this.stageName;
	}
	
	public CharList toCharList()
	{
		return this.toCharList(DefaultMinChars);
	}
	
	public CharList toCharList(int minChars)
	{
		final CharList result = new CharList();
		
		result.addAsString(this.lineNum);
		result.add('_');
		result.addAsString(this.subLineNum);
		result.add('_');
		result.addAsString(this.stepNum);
		
		final int dif = minChars - result.size();
		
		if(dif > 0)
		{
			result.push(' ', dif);
		}
		
		return result;
	}
	
	@Override
	public int compareTo(StepStage in)
	{
		int result = this.lineNum - in.lineNum;
		
		if(result == 0)
		{
			result = this.subLineNum - in.subLineNum;
			
			if(result == 0)
			{
				result = this.stepNum - in.stepNum;
			}
		}
		
		return result;
	}
	
	public int compareLineNums(StepStage in)
	{
		return this.lineNum - in.lineNum;
	}
	
	public boolean isFirstStepStage()
	{
		return this.lineNum == 0
				&& this.subLineNum == 0
				&& this.stepNum == 0;
	}
	
	public HalfByteArray getHalfByteHash()
	{
		if(this.halfByteHash == null)
		{
			this.halfByteHash = HalfByteArrayFactory.convertIntoArray(this.lineNum, this.subLineNum, this.stepNum);
		}
		
		return this.halfByteHash;
	}

	public static StepStage getNullStepStage()
	{
		return NullStepStage;
	}
	
	public static StepStage getZeroStepStage()
	{
		return ZeroStepStage;
	}
	
	public static final StepStage min(StepStage var1, StepStage var2)
	{
		if(var1.compareTo(var2) > 0)
		{
			return var2;
		}
		else
		{
			return var1;
		}
	}
	
	public static final StepStage max(StepStage var1, StepStage var2)
	{
		if(var2.compareTo(var1) > 0)
		{
			return var2;
		}
		else
		{
			return var1;
		}
	}
	
	public String toString()
	{
		return this.lineNum + " " + this.subLineNum + " " + this.stepNum + " " + this.stageName;
	}
	
	public static boolean allowWriteSwaps()
	{
		return allowWriteSwaps;
	}
}
