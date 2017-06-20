package LuaBytecodeDecompiler;

import java.nio.file.Paths;

import CharList.CharList;
import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.OneLinkIntList;
import HalfByteRadixMap.HalfByteRadixMap;
import Mk02.BufferedByteWriter;
import Mk02.DecompressorStream;
import Mk02.FileHeader;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class LuaFileDedicatedExctractor
{
	private static final String generalOutputDir = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/";
	private static final String cachesCache = "C:/Steam/steamapps/common/Warframe/Cache.Windows/";
	private static final String luaHeadersCache = "h.font";
	private static final String luaBodiesCache = "b.font";
	
	public static void main(String[] args)
	{
		test_extractLuaBodies();
	}
	
	private static void test_extractLuaBodies()
	{
		final String genHeadersPath = cachesCache + luaBodiesCache;
		final String tocPath = genHeadersPath + ".toc";
		final BytesStreamer tocStream = BytesStreamer.getInstance(Paths.get(tocPath), EndianSettings.LeftBitLeftByte, EndianSettings.LeftBitLeftByte);

		final String[] suffixes = new String[] {
													"lua" };

		final FileHeader[] headers = FileHeader.parseAllWithSelectedSuffix(suffixes, tocStream, false);
		final String outputPath = generalOutputDir + "LuaFiles/";
		final HalfByteRadixMap<FileHeader> doneHeaders = new HalfByteRadixMap<FileHeader>();

		for (FileHeader cur : headers)
		{
			final String name;
			
			if(doneHeaders.contains(cur.getFileName()))
			{
				name = cur.getFileName().replaceAll("\\.lua", "2.lua");
			}
			else
			{
				name = cur.getFileName();
			}
			
			doneHeaders.put(name, cur);
			final String curPath = outputPath + name;// + ".body";
			final DecompressorStream pump = DecompressorStream.getInstance(genHeadersPath + ".cache", cur);
			final BufferedByteWriter writey = new BufferedByteWriter(1024, curPath);
			final String fileName = cur.getFileName();
			System.out.println("starting write of: " + fileName);
			int count = 0;
			
			while (!pump.isDone())
			{
				final byte next = pump.getNextByte();
				
				/*
				if(fileName.equals("ApexFuntions.lua"))
				{
					System.out.println(Integer.toHexString(next & 0xff));
				}
				*/
				writey.write(next);
				count++;
			}
			
			//System.out.println("numBytes: " + count);

			writey.flush();
			writey.close();
		}
	}
}
