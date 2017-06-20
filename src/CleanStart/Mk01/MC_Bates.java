package CleanStart.Mk01;

import java.nio.file.Paths;

import CharList.CharList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

/**
 * primary control and entrypoint class
 *
 */
public class MC_Bates
{
	public static void main(String[] args)
	{
		// test_bitShiftingSignedBytes();
		// test_MethodOverloadingTriggers();
		// test_functionObjectToDisassembly();
		//test_functionObjectParsePass1();
		//test_rawAssemblyToCommentedString();
		test_getBothAssemblyAndTranslation();
	}
	
	private static void test_getBothAssemblyAndTranslation()
	{
		final FunctionObject root = getTestFunction();
		final CharList result = root.translateRawsToBytecode(0);
		result.addNewLine();
		result.add("*****Translation begins");
		result.addNewLine();
		result.add(root.instructionsToCharList(true, 0), true);
		
		System.out.println(result.toString());
	}
	
	private static void test_rawAssemblyToCommentedString()
	{
		final FunctionObject root = getTestFunction();
		
		System.out.println(root.translateRawsToBytecode(0));
	}

	private static void test_functionObjectParsePass1()
	{
		final FunctionObject root = getTestFunction();

		System.out.println(root.instructionsToString(true, 0));
	}

	private static void test_functionObjectToDisassembly()
	{
		final FunctionObject root = getTestFunction();

		System.out.println(root.rawInstructionsToDisassembly_String(0));
	}

	private static FunctionObject getTestFunction()
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
		final String binPath;
		// final String generalBinPath = "C:/Users/wrighc4/Documents/Eclipse tempWorkspace/Lua Decompiler/";
		// binPath = generalBinPath + "KubrowCharge.lua";
		// binPath = generalBinPath + "luac_ManyLocals.out";
		// binPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/AbilitySelector.lua";
		// binPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/AddAscarisNegator.lua";
		// binPath = generalBinPath + "ApexFuntions.lua";
		binPath = generalBinPath + "Background.lua";

		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(binPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		return FunctionObject.getNext(stream);
	}

	private static void test_MethodOverloadingTriggers()
	{
		class testA
		{
			public String getTestValue(testA in)
			{
				return "testA";
			}
		}

		final class testB extends testA
		{
			public String getTestValue(testB in)
			{
				return "testB";
			}
		}

		final testA cast1 = new testA();
		final testA cast2 = new testB();

		System.out.println(cast1.getTestValue(cast1)); // testA
		System.out.println(cast1.getTestValue(cast2)); // testA
		System.out.println(cast2.getTestValue(cast1)); // testA
		System.out.println(cast2.getTestValue(cast2)); // testA
	}

	private static void test_bitShiftingSignedBytes()
	{
		final byte controlByte = -10;
		final int controlInt = -10;

		int test1 = (controlByte & 0xff) << 14;

		System.out.println(Integer.toBinaryString(controlByte) + "\r\n" + Integer.toBinaryString(test1) + "\r\n" + Integer.toBinaryString(controlInt));

		test1 = test1 >>> 14;

		System.out.println(Integer.toBinaryString(test1));
	}
}
