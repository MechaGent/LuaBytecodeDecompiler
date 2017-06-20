package LuaBytecodeDecompiler.Mk02;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class Chunk
{
	private final ChunkHeader header;
	private final Function rootFunction;
	private final Accountant tracker;

	private Chunk(ChunkHeader inHeader, Function inRootFunction, Accountant inTracker)
	{
		this.header = inHeader;
		this.rootFunction = inRootFunction;
		this.tracker = inTracker;
	}

	public static Chunk parseNextChunk(BytesStreamer in, OpCodeMapper mapper)
	{
		final ChunkHeader inHeader = ChunkHeader.parseNextChunkHeader(in);
		final Accountant inTracker = Accountant.getInstance();
		final Function inRootFunction = Function.parseNextFunction(in, inHeader, mapper, "Null", inTracker);
		
		return new Chunk(inHeader, inRootFunction, inTracker);
	}

	public CharList disAssemble()
	{
		final CharList result = this.header.disAssemble();

		result.addNewLine();
		result.add("***rootFunction Start***");
		result.addNewLine();
		result.add(this.rootFunction.disAssemble(), true);

		return result;
	}

	public CharList disAssemble(int offset)
	{
		final CharList result = this.header.disAssemble();

		result.addNewLine();
		result.add("***rootFunction Start***");
		result.addNewLine();
		result.add(this.rootFunction.disAssemble(offset + 1), true);

		return result;
	}
	
	public Function getRootFunction()
	{
		return this.rootFunction;
	}
}
