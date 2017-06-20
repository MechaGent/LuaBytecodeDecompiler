package CleanStart.Mk06;

import java.util.PriorityQueue;

import CharList.CharList;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import HalfByteRadixMap.HalfByteRadixMap;

public abstract class Instruction implements Comparable<Instruction>
{
	private static int instanceCounter = 0;

	protected final IrCodes irCode;
	protected final int instanceNumber_overall;
	protected final int instanceNumber_specific;

	protected final StepNum startTime;
	protected boolean isExpired;

	private Instruction literalPrev;
	private Instruction literalNext;

	protected Instruction(IrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime)
	{
		this.irCode = inIrCode;
		this.instanceNumber_overall = instanceCounter++;
		this.instanceNumber_specific = inInstanceNumber_Specific;
		this.startTime = inStartTime;
		this.isExpired = false;
		this.literalPrev = null;
		this.literalNext = null;
	}

	public String getMangledName(boolean includeMetaData)
	{
		return this.irCode.toString() + '_' + this.instanceNumber_specific;
	}

	/**
	 * sets overall expiration state
	 * 
	 * @param newState
	 */
	public void setExpiredState(boolean newState)
	{
		this.isExpired = newState;
	}

	public boolean isExpired()
	{
		return this.isExpired;
	}

	public void enqueueAllNexts(PriorityQueue<Instruction> queue)
	{
		if (this.literalNext != null)
		{
			queue.add(this.literalNext);
		}
	}

	/**
	 * will show limited reaching definitions, but not all
	 * 
	 * @param appendMetadata
	 * @return
	 */
	public CharList toVerboseCommentForm(boolean appendMetadata)
	{
		final CharList result = this.toVerboseCommentForm_internal(appendMetadata);

		result.push('\t');
		result.push(this.irCode.toString());
		result.push('\t');
		result.push(this.startTime.getInDiagForm(4), true);

		/*
		if (this.isExpired)
		{
			result.push('\t');
			result.push("//");
		}
		*/

		return result;
	}

	/**
	 * will show all reaching definitions
	 * 
	 * @param appendMetadata
	 * @return
	 */
	public CharList toTimestampedVerboseCommentForm(boolean appendMetadata)
	{
		final CharList result = this.toTimestampedVerboseCommentForm_internal(appendMetadata);

		return result;
	}

	public CharList toCodeForm(boolean appendMetadata, int offset)
	{
		final CharList result = this.toCodeForm_internal(appendMetadata, offset);

		return result;
	}

	public abstract void setPartialExpiredState(boolean newState, int index);

	public abstract InlineSchemes getRelevantSchemeForIndex(int index);

	/**
	 * make sure to call this only after all reorderings have been done
	 */
	public abstract void logAllReadsAndWrites();

	protected abstract CharList toVerboseCommentForm_internal(boolean appendMetadata);

	protected CharList toTimestampedVerboseCommentForm_internal(boolean appendMetadata)
	{
		return this.toVerboseCommentForm_internal(appendMetadata);
	}

	protected CharList toCodeForm_internal(boolean appendMetadata, int offset)
	{
		final CharList result = this.toVerboseCommentForm(false);

		result.push('\t', offset);

		return result;
	}

	public CharList toDecompiledForm(int offset)
	{
		final CharList result = this.toDecompiledForm_internal(offset);

		result.push('\t');
		result.push(this.startTime.getInDiagForm(4), true);
		
		if(this.irCode.decompiledSourceIsMultiline())
		{
			result.pushNewLine();
			result.addNewLine();
		}
		
		return result;
	}

	protected abstract CharList toDecompiledForm_internal(int offset);

	public StepNum getStartTime()
	{
		return this.startTime;
	}

	public Instruction getLiteralPrev()
	{
		return this.literalPrev;
	}

	public Instruction getLiteralNext()
	{
		return this.literalNext;
	}

	public IrCodes getIrCode()
	{
		return this.irCode;
	}

	@Override
	public int compareTo(Instruction inArg0)
	{
		return this.startTime.compareTo(inArg0.startTime);
	}

	/**
	 * 
	 * @param before
	 * @param beforeCanEqual
	 * @param after
	 * @param afterCanEqual
	 * @return 0 if between {@code before} and {@code after}, -1 if before {@code before}, 1 if after {@code after}.
	 */
	public int isBetween(Instruction before, boolean beforeCanEqual, Instruction after, boolean afterCanEqual)
	{
		return this.startTime.isBetween(before.startTime, beforeCanEqual, after.startTime, afterCanEqual);
	}

	private static final boolean includeDiagnostics_withLink = false;

	public static void link(Instruction first, Instruction second)
	{
		link(first, second, true);
	}

	/**
	 *
	 * 0b0000 0 both are non-null, both have priors
	 * <br>
	 * 0b0001 1 first is null, second has prior
	 * <br>
	 * 0b0010 2 second is null, first has prior
	 * <br>
	 * 0b0011 3 both are null
	 * <br>
	 * <br>
	 * 0b0100 4 both are non-null, first has no prior
	 * <br>
	 * 0b0101 5 impossible
	 * <br>
	 * 0b0110 6 second is null, first has no prior
	 * <br>
	 * 0b0111 7 impossible
	 * <br>
	 * <br>
	 * 0b1000 8 both are non-null, first has prior
	 * <br>
	 * 0b1001 9 first is null, second has no prior
	 * <br>
	 * 0b1010 10 impossible
	 * <br>
	 * 0b1011 11 impossible
	 * <br>
	 * <br>
	 * 0b1100 12 both are non-null, neither have priors
	 * <br>
	 * 0b1101 13 impossible
	 * <br>
	 * 0b1110 14 impossible
	 * <br>
	 * 0b1111 15 impossible
	 * 
	 * @param first
	 * @param second
	 * @param annulPriorLinks
	 * @return
	 */
	private static int link_getCase(Instruction first, Instruction second, boolean annulPriorLinks)
	{
		int result = 0;

		if (annulPriorLinks)
		{
			if (first == null)
			{
				result |= 0b0001;
			}
			else if (first.literalNext == null)
			{
				result |= 0b0100;
			}

			if (second == null)
			{
				result |= 0b0010;
			}
			else if (second.literalPrev == null)
			{
				result |= 0b1000;
			}
		}
		else
		{
			if (first == null)
			{
				result |= 0b01;
			}

			if (second == null)
			{
				result |= 0b10;
			}

			switch (result)
			{
				case 0:
				{
					result = 12;
					break;
				}
				case 1:
				{
					result = 9;
					break;
				}
				case 2:
				{
					result = 6;
					break;
				}
				case 3:
				{
					// result = 3;
					break;
				}
				default:
				{
					throw new IllegalArgumentException("impossible case: " + result);
				}
			}
		}

		return result;
	}

	private static void link(Instruction first, Instruction second, boolean annulPriorLinks)
	{
		final int caseNum = link_getCase(first, second, annulPriorLinks);

		if (includeDiagnostics_withLink)
		{
			final CharList diag = new CharList();

			switch (caseNum)
			{
				case 0: // both are non-null, both have priors
				{
					diag.add("first.literalNext.literalPrev: ");
					diag.add(first.literalNext.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("second.literalPrev.literalNext: ");
					diag.add(second.literalPrev.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("first.literalNext: ");
					diag.add(first.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add(second.getMangledName(false));
					diag.add("; ");

					diag.add("second.literalPrev: ");
					diag.add(second.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add(first.getMangledName(false));
					diag.add("; ");

					break;
				}
				case 1: // first is null, second has prior
				{
					diag.add("second.literalPrev.literalNext: ");
					diag.add(first.literalPrev.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("second.literalPrev: ");
					diag.add(second.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					break;
				}
				case 2: // second is null, first has prior
				{
					diag.add("first.literalNext.literalPrev: ");
					diag.add(first.literalNext.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("first.literalNext: ");
					diag.add(first.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");
					break;
				}
				case 3: // both are null
				{
					// System.err.println("Unnecessary call to link! (both null)");
					break;
				}
				case 4: // both are non-null, first has no prior
				{
					diag.add("second.literalPrev.literalNext: ");
					diag.add(second.literalPrev.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("first.literalNext: ");
					diag.add("null");
					diag.add(" -> ");
					diag.add(second.getMangledName(false));
					diag.add("; ");

					diag.add("second.literalPrev: ");
					diag.add(second.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add(first.getMangledName(false));
					diag.add("; ");
					break;
				}
				case 6: // second is null, first has no prior
				{
					diag.add("first.literalNext: ");
					diag.add(first.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");
					break;
				}
				case 8: // both are non-null, first has prior
				{
					diag.add("first.literalNext.literalPrev: ");
					diag.add(first.literalNext.literalPrev.getMangledName(false));
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					diag.add("first.literalNext: ");
					diag.add(first.literalNext.getMangledName(false));
					diag.add(" -> ");
					diag.add(second.getMangledName(false));
					diag.add("; ");

					diag.add("second.literalPrev: ");
					diag.add("null");
					diag.add(" -> ");
					diag.add(first.getMangledName(false));
					diag.add("; ");

					break;
				}
				case 9: // first is null, second has no prior
				{
					diag.add("second.literalPrev: ");
					diag.add("null");
					diag.add(" -> ");
					diag.add("null");
					diag.add("; ");

					break;
				}
				case 12: // both are non-null, neither have priors
				{
					diag.add("first.literalNext: ");
					diag.add("null");
					diag.add(" -> ");
					diag.add(second.getMangledName(false));
					diag.add("; ");

					diag.add("second.literalPrev: ");
					diag.add("null");
					diag.add(" -> ");
					diag.add(first.getMangledName(false));
					diag.add("; ");

					break;
				}
				case 5:
				case 7:
				case 10:
				case 11:
				case 13:
				case 14:
				case 15:
				{
					throw new IllegalArgumentException("Impossible case: " + caseNum + "!");
				}
				default:
				{
					throw new IllegalArgumentException("bad annulled case: " + caseNum);
				}
			}

			System.out.println(diag.toString());
		}

		switch (caseNum)
		{
			case 0: // both are non-null, both have priors
			{
				if (!(first.literalNext == second && second.literalPrev == first))
				{
					first.literalNext.literalPrev = null;
					second.literalPrev.literalNext = null;

					first.literalNext = second;
					second.literalPrev = first;
				}

				break;
			}
			case 1: // first is null, second has prior
			{
				second.literalPrev.literalNext = null;

				second.literalPrev = null;
				break;
			}
			case 2: // second is null, first has prior
			{
				first.literalNext.literalPrev = null;

				first.literalNext = null;
				break;
			}
			case 3: // both are null
			{
				System.err.println("Unnecessary call to link! (both null)");
				break;
			}
			case 4: // both are non-null, first has no prior
			{
				second.literalPrev.literalNext = null;

				first.literalNext = second;
				second.literalPrev = first;

				break;
			}
			case 6: // second is null, first has no prior
			{
				first.literalNext = null;

				break;
			}
			case 8: // both are non-null, first has prior
			{
				first.literalNext.literalPrev = null;

				first.literalNext = second;
				second.literalPrev = first;

				break;
			}
			case 9: // first is null, second has no prior
			{
				second.literalPrev = null;

				break;
			}
			case 12: // both are non-null, neither have priors
			{
				first.literalNext = second;
				second.literalPrev = first;

				break;
			}
			case 5:
			case 7:
			case 10:
			case 11:
			case 13:
			case 14:
			case 15:
			{
				throw new IllegalArgumentException("Impossible case: " + caseNum + "!");
			}
			default:
			{
				throw new IllegalArgumentException("bad annulled case: " + caseNum);
			}
		}
	}

	/**
	 * inserts second ahead of first in the literal chain
	 * 
	 * @param first
	 * @param second
	 */
	public static void insertAhead(Instruction first, Instruction second)
	{
		final int caseNum = link_getCase(first, second, true);

		switch (caseNum)
		{
			case 0: // both are non-null, both have priors
			{
				final Instruction third = first.literalNext;

				first.literalNext = second;
				second.literalPrev = first;

				second.literalNext = third;
				third.literalPrev = second;

				break;
			}
			case 3: // both are null
			{
				System.err.println("Unnecessary call to link! (both null)");
				break;
			}
			case 8: // both are non-null, first has prior
			{
				final Instruction third = first.literalNext;

				first.literalNext = second;
				second.literalPrev = first;

				second.literalNext = third;
				third.literalPrev = second;

				break;
			}
			case 12: // both are non-null, neither have priors
			{
				first.literalNext = second;
				second.literalPrev = first;

				break;
			}
			case 1: // first is null, second has prior
			case 2: // second is null, first has prior
			case 4: // both are non-null, first has no prior
			case 5:
			case 6: // second is null, first has no prior
			case 7:
			case 9: // first is null, second has no prior
			case 10:
			case 11:
			case 13:
			case 14:
			case 15:
			{
				throw new IllegalArgumentException("Impossible case: " + caseNum + "!");
			}
			default:
			{
				throw new IllegalArgumentException("bad annulled case: " + caseNum);
			}
		}
	}

	public static PathFinder findCommonInstruction(Instruction first, Instruction second)
	{
		PathFinder result = null;

		if (first == second)
		{
			result = new PathFinder_Success(PathFinderResults.Success_InstructionsMatched, first);
		}
		else
		{
			final boolean swapInstructions = first.compareTo(second) <= 0;

			if (!swapInstructions)
			{
				final Instruction temp = first;
				first = second;
				second = temp;
			}

			final PriorityQueue<Instruction> queue = new PriorityQueue<Instruction>();
			final HalfByteRadixMap<Instruction> done = new HalfByteRadixMap<Instruction>();
			boolean isDone = false;
			boolean onPhaseTwo = false;
			done.put(second.getMangledName(false), second);

			first.enqueueAllNexts(queue);

			while (!isDone && !queue.isEmpty())
			{
				final Instruction test = queue.poll();

				if (second.compareTo(test) >= 0)
				{
					if (done.contains(test.getMangledName(false)))
					{
						isDone = true;
						result = new PathFinder_Success(PathFinderResults.Success_FoundJoiner, test);
					}
					else
					{
						done.put(test.getMangledName(false), test);
						test.enqueueAllNexts(queue);
					}
				}
				else if (!onPhaseTwo)
				{
					onPhaseTwo = true;
					second.enqueueAllNexts(queue);
				}
			}

			if (!isDone)
			{
				result = new PathFinder_Failure(PathFinderResults.Failure);
			}
		}

		return result;
	}

	/*
	public static void normalizeStartTimes(Instruction first)
	{
		StepNum currentTime = StepNum.getFirstStep();
		first.startTime = currentTime;
		Instruction currentInstruct = first;
		
		while(currentInstruct != null)
		{
			currentTime = StepNum.getInstance(currentTime, ForkBehaviors.IncrementLast);
		}
	}
	*/

	public static enum PathFinderResults
	{
		Success_FoundJoiner(
				true),
		Success_InstructionsMatched(
				true),
		Failure(
				false),;

		private final boolean wasSuccess;

		private PathFinderResults(boolean inWasSuccess)
		{
			this.wasSuccess = inWasSuccess;
		}

		public boolean wasSuccess()
		{
			return this.wasSuccess;
		}
	}

	public static abstract class PathFinder
	{
		private final PathFinderResults result;

		public PathFinder(PathFinderResults inResult)
		{
			this.result = inResult;
		}

		public PathFinderResults getResult()
		{
			return this.result;
		}
	}

	public static class PathFinder_Success extends PathFinder
	{
		private final Instruction joiner;

		public PathFinder_Success(PathFinderResults inResult, Instruction inJoiner)
		{
			super(inResult);
			this.joiner = inJoiner;
		}

		public Instruction getJoiner()
		{
			return this.joiner;
		}
	}

	public static class PathFinder_Failure extends PathFinder
	{
		// private final Instruction lastFromFirst;
		// private final Instruction lastFromSecond;

		private PathFinder_Failure(PathFinderResults inResult)
		{
			super(inResult);
		}
	}

	public static class InstructionList_Locked
	{
		private final Instruction first;
		private final Instruction last;

		private InstructionList_Locked(Instruction inFirst, Instruction inLast)
		{
			this.first = inFirst;
			this.last = inLast;
		}

		public static InstructionList_Locked getInstance(Instruction inFirst, Instruction inLast)
		{
			return new InstructionList_Locked(inFirst, inLast);
		}

		public Instruction getFirst()
		{
			return this.first;
		}

		public Instruction getLast()
		{
			return this.last;
		}
	}
}
