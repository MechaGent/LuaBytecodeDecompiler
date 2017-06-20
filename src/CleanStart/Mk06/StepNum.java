package CleanStart.Mk06;

import CharList.CharList;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public class StepNum implements Comparable<StepNum>
{
	private static final StepNum initializerStep = new StepNum(new int[] {
																			-1 });
	private static final StepNum firstStep = new StepNum(new int[] {
																		0 });

	private static final int Diag_MinSpacing = 4;

	private final int[] orders;

	private StepNum(int[] in)
	{
		this.orders = in;
	}

	/*
	public static StepNum getInstance(int order)
	{
		return new StepNum(new int[] {
										order });
	}
	*/

	public static StepNum getInstance(StepNum previous, ForkBehaviors action)
	{
		final int[] result;

		switch (action)
		{
			case DecrementLast:
			{
				result = new int[previous.orders.length];

				for (int i = 0; i < result.length; i++)
				{
					result[i] = previous.orders[i];
				}

				result[previous.orders.length - 1]--;

				break;
			}
			case ForkToSubstage:
			{
				result = new int[previous.orders.length + 1];

				for (int i = 0; i < previous.orders.length; i++)
				{
					result[i] = previous.orders[i];
				}

				result[previous.orders.length] = 1;

				break;
			}
			case IncrementLast:
			{
				result = new int[previous.orders.length];

				for (int i = 0; i < result.length; i++)
				{
					result[i] = previous.orders[i];
				}

				result[previous.orders.length - 1]++;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(action);
			}
		}

		return new StepNum(result);
	}

	public StepNum fork(ForkBehaviors action)
	{
		return getInstance(this, action);
	}

	public StepNum createOffsetStep(int offset)
	{
		final int[] core = new int[this.orders.length];
		final int lastIndex = this.orders.length - 1;

		for (int i = 0; i < lastIndex; i++)
		{
			core[i] = this.orders[i];
		}

		core[lastIndex] = this.orders[lastIndex] + offset;

		return new StepNum(core);
	}

	public int getTailElement()
	{
		return this.orders[this.orders.length - 1];
	}

	public CharList getInDiagForm()
	{
		return this.getInDiagForm(Diag_MinSpacing);
	}

	public CharList getInDiagForm(int minSpacing)
	{
		final CharList result = new CharList();

		result.add('[');

		for (int i = 0; i < this.orders.length; i++)
		{
			if (i != 0)
			{
				result.add('_');
			}

			if (this.orders[i] < 0)
			{
				result.add('-');
				result.addAsPaddedString(Integer.toString(-this.orders[i]), '0', minSpacing - 1, false);
			}
			else
			{
				result.addAsPaddedString(Integer.toString(this.orders[i]), '0', minSpacing, false);
			}
		}

		result.add(']');

		return result;
	}

	@Override
	public int compareTo(StepNum other)
	{
		int result = 0;

		if (this != other)
		{
			boolean isDone = false;
			int i = 0;

			while (!isDone && result == 0)
			{
				if (i < this.orders.length)
				{
					if (i < other.orders.length)
					{
						result = this.orders[i] - other.orders[i];
						i++;
					}
					else
					{
						if(this.orders[i] < 0)
						{
							result = -1;
						}
						else
						{
							result = 1; // this is longer than other
						}
						
						isDone = true;
					}
				}
				else
				{
					if (i < other.orders.length)
					{
						if (other.orders[i] < 0)
						{
							result = 1;
						}
						else
						{
							result = -1;
						}
					}

					isDone = true;
				}
			}
		}

		return result;
	}

	/**
	 * yes, I realize how unnecessary this method is, but it helps my comprehension speed
	 * 
	 * @param other
	 * @return
	 */
	public boolean isGreaterThan(StepNum other)
	{
		return this.compareTo(other) > 0;
	}

	/**
	 * yes, I realize how unnecessary this method is, but it helps my comprehension speed
	 * 
	 * @param other
	 * @return
	 */
	public boolean isGreaterThanOrEqualTo(StepNum other)
	{
		return this.compareTo(other) >= 0;
	}

	/**
	 * yes, I realize how unnecessary this method is, but it helps my comprehension speed
	 * 
	 * @param other
	 * @return
	 */
	public boolean isLessThan(StepNum other)
	{
		return this.compareTo(other) < 0;
	}

	/**
	 * yes, I realize how unnecessary this method is, but it helps my comprehension speed
	 * 
	 * @param other
	 * @return
	 */
	public boolean isLessThanOrEqualTo(StepNum other)
	{
		return this.compareTo(other) <= 0;
	}

	public int isBetween(StepNum before, boolean beforeCanEqual, StepNum after, boolean afterCanEqual)
	{
		final int result;

		if (beforeCanEqual)
		{
			if (afterCanEqual)
			{
				result = this.isBetween_InclusiveInclusive(before, after);
			}
			else
			{
				result = this.isBetween_InclusiveExclusive(before, after);
			}
		}
		else
		{
			if (afterCanEqual)
			{
				result = this.isBetween_ExclusiveInclusive(before, after);
			}
			else
			{
				result = this.isBetween_ExclusiveExclusive(before, after);
			}
		}

		return result;
	}

	public int isBetween_ExclusiveInclusive(StepNum before, StepNum after)
	{
		final int result;

		if (before.isLessThan(this))
		{
			if (this.isLessThanOrEqualTo(after))
			{
				result = 0;
			}
			else
			{
				result = 1;
			}
		}
		else
		{
			result = -1;
		}

		return result;
	}

	public int isBetween_InclusiveInclusive(StepNum before, StepNum after)
	{
		final int result;

		if (before.isLessThanOrEqualTo(this))
		{
			if (this.isLessThanOrEqualTo(after))
			{
				result = 0;
			}
			else
			{
				result = 1;
			}
		}
		else
		{
			result = -1;
		}

		return result;
	}

	public int isBetween_InclusiveExclusive(StepNum before, StepNum after)
	{
		final int result;

		if (before.isLessThanOrEqualTo(this))
		{
			if (this.isLessThan(after))
			{
				result = 0;
			}
			else
			{
				result = 1;
			}
		}
		else
		{
			result = -1;
		}

		return result;
	}

	public int isBetween_ExclusiveExclusive(StepNum before, StepNum after)
	{
		final int result;

		if (before.isLessThan(this))
		{
			if (this.isLessThan(after))
			{
				result = 0;
			}
			else
			{
				result = 1;
			}
		}
		else
		{
			result = -1;
		}

		return result;
	}

	public static StepNum getInitializerStep()
	{
		return initializerStep;
	}

	public static StepNum getFirstStep()
	{
		return firstStep;
	}

	public static enum ForkBehaviors
	{
		IncrementLast,
		DecrementLast,
		ForkToSubstage;
	}
}
