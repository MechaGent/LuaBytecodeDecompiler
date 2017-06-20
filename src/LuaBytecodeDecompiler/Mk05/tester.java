package LuaBytecodeDecompiler.Mk05;

import java.nio.file.Paths;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import LuaBytecodeDecompiler.Mk02.Chunk;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk03.AnnoFunction;
import LuaBytecodeDecompiler.Mk04.CodeBlock;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class tester
{
	public static void main(String[] args)
	{
		test_Interpreter();
	}
	
	private static void test_Interpreter()
	{
		final SingleLinkedList<AnnoFunction> queue = new SingleLinkedList<AnnoFunction>();
		final AnnoFunction daddyFunction = tester.helper_getTestAnnoFunction();
		queue.add(daddyFunction);
		//System.out.println(daddyFunction.toString());
		CharList raw = new CharList();
		final CharList out = new CharList();

		while (!queue.isEmpty())
		{
			final AnnoFunction testbed = queue.pop();
			queue.add(testbed.getFunctions());
			final CodeBlock[] blocks = CodeBlock.splitIntoCodeBlocks(testbed.getInstructions());
			
			raw.addNewLine();
			raw.add("*****NEW RAW LINES");
			
			for(int i = 0; i < blocks.length; i++)
			{
				raw.addNewLine();
				raw.add(blocks[i].toCharList(true), true);
			}
			
			raw.addNewLine();
			raw.add("*****");
			
			System.out.println(raw.toString());
			raw = new CharList();
			
			out.addNewLine();
			final Interpreter intsy = new Interpreter();
			out.add("*****NEW Recomposed Function ");
			out.add(testbed.getFunctionName());
			out.add(":\r\n");
			out.add(intsy.recompose(blocks[0]));
			out.addNewLine();
			out.add("*****");
		}
		
		System.out.println(
				//raw.toString() + "\r\n\r\n" + 
		out.toString());
	}
	
	private static AnnoFunction helper_getTestAnnoFunction()
	{
		return AnnoFunction.convert(tester.helper_getTestFunction());
	}

	private static Function helper_getTestFunction()
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/targets/";
		// final String generalBinPath = "C:/Users/wrighc4/Documents/Eclipse tempWorkspace/Lua Decompiler/";
		final String binPath = generalBinPath + "KubrowCharge.lua";
		// final String binPath = generalBinPath + "luac_ManyLocals.out";

		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(binPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();

		final Chunk chunky = Chunk.parseNextChunk(stream, mapper);

		return chunky.getRootFunction();
	}
}
