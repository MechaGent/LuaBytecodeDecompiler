package LuaBytecodeDecompiler.Mk01.Enums;

import java.util.Iterator;

public enum LuaVersions
{
	v5_1,
	v5_2,
	v5_3;
	
	private static final LuaVersions[] All = LuaVersions.values();
	
	public Iterator<LuaVersions> getAscendingIterator()
	{
		return new AscendingVersionIterator(this);
	}
	
	public static LuaVersions getFirst()
	{
		return All[0];
	}
	
	public static Iterator<LuaVersions> getTotalAscendingIterator()
	{
		return All[0].getAscendingIterator();
	}
	
	public static LuaVersions[] getAll()
	{
		return All;
	}
	
	public static LuaVersions getVersion(byte in)
	{
		return getVersion((int) in);
	}
	
	/**
	 * highHex digit is major version, lowHex is minor
	 * @param in
	 * @return
	 */
	public static LuaVersions getVersion(int in)
	{
		final LuaVersions result;
		
		switch(in)
		{
			case 0x51:
			{
				result = v5_1;
				break;
			}
			case 0x52:
			{
				result = v5_2;
				break;
			}
			case 0x53:
			{
				result = v5_3;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Unrecognized version: " + Integer.toHexString(in));
			}
		}
		
		return result;
	}
	
	private static class AscendingVersionIterator implements Iterator<LuaVersions>
	{
		private int place;
		
		public AscendingVersionIterator(LuaVersions start)
		{
			this.place = start.ordinal();
		}
		
		@Override
		public boolean hasNext()
		{
			return this.place < All.length;
		}

		@Override
		public LuaVersions next()
		{
			return All[this.place++];
		}
	}
}
