package CleanStart.Mk06.Instructions;

import java.util.Map.Entry;

import CharList.CharList;

import java.util.TreeMap;

import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public class Label extends Instruction
{
	private static int instanceNumber = 0;

	private final String prefix;
	private final TreeMap<StepNum, InstructionIndex> incoming;

	//private int numOpenBracesToAppend;
	private int incomingCounter;
	private int scope;

	private Label(StepNum inStartTime, String inPrefix)
	{
		super(IrCodes.Label, instanceNumber++, inStartTime);
		this.prefix = inPrefix;
		this.incoming = new TreeMap<StepNum, InstructionIndex>();
		//this.numOpenBracesToAppend = 0;
		this.incomingCounter = 0;
		this.scope = Integer.MAX_VALUE;
	}

	private Label(StepNum inStartTime, String inPrefix, int forceInstanceNumber)
	{
		super(IrCodes.Label, forceInstanceNumber, inStartTime);
		this.prefix = inPrefix;
		this.incoming = new TreeMap<StepNum, InstructionIndex>();
		//this.numOpenBracesToAppend = 0;
		this.incomingCounter = 0;
		this.scope = Integer.MAX_VALUE;
	}

	public static Label getInstance(StepNum inStartTime, String inPrefix)
	{
		return new Label(inStartTime, inPrefix);
	}

	public static Label getStartLabel(StepNum inStartTime)
	{
		return new Label(inStartTime, "StartLabel", -1);
	}

	public void setAsIncoming(InstructionIndex in)
	{
		this.incoming.put(in.getTarget().getStartTime(), in);
		this.incomingCounter++;
	}

	public InstructionIndex getIncomingJump(SearchJumpsFor searchType)
	{
		final Entry<StepNum, InstructionIndex> raw;
		final InstructionIndex result;

		switch (searchType)
		{
			case FurthestDistance_AfterThis:
			{
				raw = this.incoming.lastEntry();

				if (raw.getKey().isGreaterThan(this.startTime))
				{
					result = raw.getValue();
				}
				else
				{
					result = null;
				}

				break;
			}
			case FurthestDistance_BeforeThis:
			{
				raw = this.incoming.firstEntry();

				if (raw.getKey().isLessThan(this.startTime))
				{
					result = raw.getValue();
				}
				else
				{
					result = null;
				}

				break;
			}
			default:
			{
				throw new UnhandledEnumException(searchType);
			}
		}

		return result;
	}

	/*
	public void incrementNumOpenBracesToAppend()
	{
		//this.numOpenBracesToAppend++;
	}
	*/

	public void decrementIncomingCounter()
	{
		this.incomingCounter--;
	}

	public int getIncomingCounter()
	{
		return this.incomingCounter;
	}

	public void setScope(int newScope)
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

	public boolean scopeIsUnset()
	{
		return this.scope == Integer.MAX_VALUE;
	}

	@Override
	public void setPartialExpiredState(boolean inNewState, int inIndex)
	{

	}

	@Override
	public void logAllReadsAndWrites()
	{

	}
	
	@Override
	public String getMangledName(boolean includeMetaData)
	{
		return this.toVerboseCommentForm_internal(false).toString();
	}

	@Override
	protected CharList toVerboseCommentForm_internal(boolean appendMetadata)
	{
		final CharList result = new CharList();

		result.add(this.prefix);

		if (this.instanceNumber_specific != -1)
		{
			result.add(" #");
			result.addAsString(this.instanceNumber_specific);
		}

		return result;
	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.NonApplicable;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = this.toVerboseCommentForm_internal(false);

		result.push("//\t");
		result.push('\t', offset);

		return result;
	}

	public static enum SearchJumpsFor
	{
		/**
		 * returns the highest entry from the subset of origins whose StepNums are higher than this Label's StepNum.
		 */
		FurthestDistance_AfterThis,

		/**
		 * returns the lowest entry from teh subset of origins whose StepNums are lower than this Label's StepNum.
		 */
		FurthestDistance_BeforeThis;
	}
}
