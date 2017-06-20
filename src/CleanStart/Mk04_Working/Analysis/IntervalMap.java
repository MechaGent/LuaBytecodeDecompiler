package CleanStart.Mk04_Working.Analysis;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class IntervalMap<U>
{
	private final Node<U> root;
	
	
	public IntervalMap(int globalMin, int globalMax)
	{
		
	}
	
	public static class Interval
	{
		private final int min;
		private final int max;

		public Interval(int inMin, int inMax)
		{
			this.min = inMin;
			this.max = inMax;
		}

		public int getMin()
		{
			return this.min;
		}

		public int getMax()
		{
			return this.max;
		}
	}

	public static class IntervalEntry<U> implements Entry<Interval, U>
	{
		private final Interval key;
		private U cargo;

		public IntervalEntry(int inMin, int inMax, U inCargo)
		{
			this.key = new Interval(inMin, inMax);
			this.cargo = inCargo;
		}

		@Override
		public Interval getKey()
		{
			return this.key;
		}

		@Override
		public U getValue()
		{
			return this.cargo;
		}

		@Override
		public U setValue(U inArg0)
		{
			this.cargo = inArg0;
			return this.cargo;
		}

		public int getMin()
		{
			return this.key.min;
		}

		public int getMax()
		{
			return this.key.max;
		}
	}

	private static class MinComparator implements Comparator<Interval>
	{
		private static final MinComparator sharedInstance = new MinComparator();

		private MinComparator()
		{

		}

		public static MinComparator getInstance()
		{
			return sharedInstance;
		}

		@Override
		public int compare(Interval inArg0, Interval inArg1)
		{
			return inArg0.min - inArg1.min;
		}
	}

	private static class MaxComparator implements Comparator<Interval>
	{
		private static final MaxComparator sharedInstance = new MaxComparator();

		private MaxComparator()
		{
			
		}

		public static MaxComparator getInstance()
		{
			return sharedInstance;
		}

		@Override
		public int compare(Interval inArg0, Interval inArg1)
		{
			return inArg0.max - inArg1.max;
		}
	}

	private static class EntryCollection<U>
	{
		private final TreeMap<Interval, IntervalEntry<U>> compareByMin;
		private final TreeMap<Interval, IntervalEntry<U>> compareByMax;

		public EntryCollection()
		{
			this.compareByMin = new TreeMap<Interval, IntervalEntry<U>>(MinComparator.sharedInstance);
			this.compareByMax = new TreeMap<Interval, IntervalEntry<U>>(MaxComparator.sharedInstance);
		}
	}

	private static class Node<U>
	{
		private final int centerpoint;
		private Node<U> S_Left;
		private final EntryCollection<U> centeredEntries;
		private Node<U> S_Right;
		
		public Node(int inCenter)
		{
			this.centerpoint = inCenter;
			this.S_Left = null;
			this.centeredEntries = new EntryCollection<U>();
			this.S_Right = null;
		}
	}
}
