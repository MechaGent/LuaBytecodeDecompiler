package LuaBytecodeDecompiler.Mk03;

import java.nio.file.Paths;

import DataStructures.Linkages.CharList.Mk03.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.OneLinkIntList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import LuaBytecodeDecompiler.Mk02.Chunk;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk03.ControlFlow.InstructionPathTracer;
import LuaBytecodeDecompiler.Mk03.ControlFlow.InstructionPathTracer2;
import LuaBytecodeDecompiler.Mk03.ControlFlow.InstructionPathTracer2.CodeChunk;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class AnnoProcessTester
{
	public static void main(String[] args)
	{
		test_convertFunctionToAnnoFunction();
		// test_OneLinkIntList();
		//test_InstructionPathTracer();
		//test_InstructionPathTracer2();
		//test_InstructionPathTracer2_getGraph();
		//test_InstructionPathTracer2_getGraph2();
	}
	
	private static void test_InstructionPathTracer2_getGraph2()
	{
		final Function testIn = helper_getTestFunction();
		final AnnoFunction testAnno = AnnoFunction.convert(testIn);
		final AnnoInstruction[] stuff = testAnno.getInstructions();
		final HalfByteRadixMap<CodeChunk> map = InstructionPathTracer2.mapFlowNodes(stuff);
		final CodeChunk first = InstructionPathTracer2.getAsGraph(map);
		System.out.println(first.toCharList().toString());
	}
	
	private static void test_InstructionPathTracer2_getGraph()
	{
		final Function testIn = helper_getTestFunction();
		final AnnoFunction testAnno = AnnoFunction.convert(testIn);
		
		final SingleLinkedList<AnnoFunction> queue = new SingleLinkedList<AnnoFunction>();
		queue.add(testAnno);
		
		while(!queue.isEmpty())
		{
			final AnnoFunction testOut = queue.pop();
			queue.add(testOut.getFunctions());

			final AnnoInstruction[] instructions = testOut.getInstructions();
			final CodeChunk first = InstructionPathTracer2.getAsGraph(InstructionPathTracer2.mapFlowNodes(instructions));
			System.out.println("CHUNK:\r\n" + first.cascadeCharList() + "\r\n******");
		}
	}

	private static void test_InstructionPathTracer2()
	{
		final Function testIn = helper_getTestFunction();
		final AnnoFunction testAnno = AnnoFunction.convert(testIn);
		
		final SingleLinkedList<AnnoFunction> queue = new SingleLinkedList<AnnoFunction>();
		queue.add(testAnno);
		
		while(!queue.isEmpty())
		{
			final AnnoFunction testOut = queue.pop();
			queue.add(testOut.getFunctions());

			final AnnoInstruction[] instructions = testOut.getInstructions();
			final boolean[] Breaks = InstructionPathTracer2.getBreaks(instructions);
			
			final CharList result = InstructionPathTracer2.toAnnotatedCharList(instructions, Breaks);
			
			System.out.println(result.toString() + "\r\n");
		}
	}
	
	private static void test_InstructionPathTracer()
	{
		final Function testIn = helper_getTestFunction();

		final SingleLinkedList<AnnoFunction> queue = new SingleLinkedList();
		queue.add(AnnoFunction.convert(testIn));

		while (!queue.isEmpty())
		{
			final AnnoFunction cur = queue.pop();
			final InstructionPathTracer test = new InstructionPathTracer(cur.getInstructions());
			test.process();
			
			for(AnnoFunction curFunction: cur.getFunctions())
			{
				queue.add(curFunction);
			}
			
			System.out.println(test.toCharList().toString() + "\r\n");
		}
	}

	private static void test_OneLinkIntList()
	{
		final OneLinkIntList test = new OneLinkIntList();
		test.add(1);
		test.add(5);
		test.add(-4);

		while (!test.isEmpty())
		{
			System.out.println(Integer.toString(test.pop()));
		}
	}

	private static void test_convertFunctionToAnnoFunction()
	{
		final Function testIn = helper_getTestFunction();

		final AnnoFunction testOut = AnnoFunction.convert(testIn);

		System.out.println(testOut.toString());
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
