package LuaBytecodeDecompiler.Mk01.LuaJTesting;

public enum TestCases
{
	test1(
			"do end",
			"1  [-]  RETURN  0 1",
			new byte[] {
							0x1B,
							0x4C,
							0x75,
							0x61,
							0x52,
							0x00,
							0x00,
							0x04,
							0x04,
							0x04,
							0x08,
							0x00,
							0x19,
							(byte) 0x93,
							0x0D,
							0x0A,
							0x1A,
							0x0A,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x01,
							0x02,
							0x00,
							0x00,
							0x00,
							0x04,
							0x00,
							0x40,
							0x00,
							0x06,
							0x00,
							0x00,
							0x40,
							0x41,
							0x01,
							0x00,
							0x40,
							0x1D,
							0x00,
							(byte) 0x80,
							0x00,
							0x1F,
							0x00,
							0x00,
							0x00,
							0x02,
							0x04,
							0x00,
							0x00,
							0x00,
							0x06,
							0x70,
							0x72,
							0x69,
							0x6E,
							0x74,
							0x00,
							0x04,
							0x00,
							0x00,
							0x00,
							0x0D,
							0x68,
							0x65,
							0x6C,
							0x6C,
							0x6F,
							0x2C,
							0x20,
							0x77,
							0x6F,
							0x72,
							0x6C,
							0x64,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x01,
							0x01,
							0x00,
							0x00,
							0x00,
							0x00,
							0x09,
							0x6D,
							0x61,
							0x69,
							0x6E,
							0x2E,
							0x6C,
							0x75,
							0x61,
							0x00,
							0x00,
							0x00,
							0x00,
							0x04,
							0x00,
							0x00,
							0x00,
							0x01,
							0x00,
							0x00,
							0x00,
							0x01,
							0x00,
							0x00,
							0x00,
							0x01,
							0x00,
							0x00,
							0x00,
							0x01,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x00,
							0x01,
							0x00,
							0x00,
							0x00,
							0x05,
							0x5F,
							0x45,
							0x4E,
							0x56,
							0x00 }),
	test2(
			"",
			"",
			new byte[] {});

	private final String code;
	private final String Formatted;
	private final byte[] raw;

	private TestCases(String inCode, String inFormatted, byte[] inRaw)
	{
		this.code = inCode;
		this.Formatted = inFormatted;
		this.raw = inRaw;
	}

	public String getCode()
	{
		return this.code;
	}

	public String getFormatted()
	{
		return this.Formatted;
	}

	public byte[] getRaw()
	{
		return this.raw;
	}
}
