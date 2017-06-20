package CleanStart.Mk01;

import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class ChunkHeader
{
	private final int signature;
	private final int version;
	private final byte Format;
	private final EndianSettings SigEnd;
	private final int size_Int;
	private final int size_t;
	private final int size_Instruction;
	private final int size_luaNum;
	private final boolean luaNumsAreFloats;
	
	private ChunkHeader(int inSignature, int inVersion, byte inFormat, EndianSettings inSigEnd, int inSize_Int, int inSize_t, int inSize_Instruction, int inSize_luaNum, boolean inLuaNumsAreFloats)
	{
		this.signature = inSignature;
		this.version = inVersion;
		this.Format = inFormat;
		this.SigEnd = inSigEnd;
		this.size_Int = inSize_Int;
		this.size_t = inSize_t;
		this.size_Instruction = inSize_Instruction;
		this.size_luaNum = inSize_luaNum;
		this.luaNumsAreFloats = inLuaNumsAreFloats;
	}
	
	public static ChunkHeader getNext(BytesStreamer in)
	{
		final int inSignature = in.getNextInt();
		final int inVersion = in.getNextByte();
		final byte inFormat = (byte) in.getNextByte();
		final EndianSettings inSigEnd = (in.getNextByte() == 1)? EndianSettings.LeftBitRightByte : EndianSettings.LeftBitLeftByte;
		final int inSize_Int = in.getNextByte();
		final int inSize_t = in.getNextByte();
		final int inSize_Instruction = in.getNextByte();
		final int inSize_luaNum = in.getNextByte();
		final int testNumFloats = in.getNextByte();
		final boolean inLuaNumsAreFloats = testNumFloats == 0;
		//System.out.println("raw byte: " + testNumFloats + ", with result: " + inLuaNumsAreFloats);
		final ChunkHeader result = new ChunkHeader(inSignature, inVersion, inFormat, inSigEnd, inSize_Int, inSize_t, inSize_Instruction, inSize_luaNum, inLuaNumsAreFloats);
		
		return result;
	}

	public int getSignature()
	{
		return this.signature;
	}

	public int getVersion()
	{
		return this.version;
	}

	public byte getFormat()
	{
		return this.Format;
	}

	public EndianSettings getSigEnd()
	{
		return this.SigEnd;
	}

	public int getSize_Int()
	{
		return this.size_Int;
	}

	public int getSize_t()
	{
		return this.size_t;
	}

	public int getSize_Instruction()
	{
		return this.size_Instruction;
	}

	public int getSize_luaNum()
	{
		return this.size_luaNum;
	}

	public boolean LuaNumsAreFloats()
	{
		return this.luaNumsAreFloats;
	}
}
