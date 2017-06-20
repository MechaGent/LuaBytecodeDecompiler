package LuaBytecodeDecompiler.Mk01.LuaJTesting;

import java.nio.file.Paths;

import LuaBytecodeDecompiler.Mk01.Abstract.Chunk;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class tester2
{
	public static void main(String[] args)
	{
		test_parseBinary();
	}

	private static void test_parseBinary()
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/";
		//final String binPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/luac_DoEnd.out";
		//final String binPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/luac_HelloWorld.out";
		final String binPath = generalBinPath + "targets/Background.lua";
		//final String binPath = generalBinPath + "luac_simpleFunctionExample.out";
		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(binPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();

		final Chunk testChunk = new Chunk(stream, mapper);

		System.out.println(testChunk.disAssemble().toString());
	}
}
