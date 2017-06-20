package LuacTranslation;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuacTranslation.lobject.Proto;

public class LuaC
{
	private static String PROGNAME = "luac"; /* default program name */
	private static String OUTPUT = PROGNAME + ".out"; /* default output file */
	private static String LUA_RELEASE = "5.1.3";
	private static String LUA_COPYRIGHT = "Copyright (C) 1994-2008 Lua.org, PUC-Rio";

	private boolean listing; /* list bytecodes? */
	private boolean dumping; /* dump bytecodes? */
	private boolean stripping; /* strip debug information? */
	private String output = OUTPUT; /* actual output file name */
	private String progname = PROGNAME; /* actual program name */

	void fatal(String message)
	{
		throw new IllegalArgumentException(this.progname + ": " + message);
	}

	void cannot(String what)
	{
		throw new IllegalArgumentException(this.progname + ": cannot " + what + " " + output);
	}

	void usage(String message)
	{
		final CharList result = new CharList();
		result.add(this.progname);
		result.add(": ");

		if (message.equals("-"))
		{
			result.add("unrecognized option '");
			result.add(message);
			result.add('\'');
		}
		else
		{
			result.add(message);
		}

		result.addNewLine();
		result.add("usage: ");
		result.add(this.progname);
		result.add(" [options] [filenames].");
		result.addNewLine();
		result.add("Available options are: ");
		result.addNewLine();
		result.add("  -        process stdin\n");
		result.add("  -l       list\n");
		result.add("  -o name  output to file 'name'");
		result.add(" (default is \"");
		result.add(this.output);
		result.add("\")\n");
		result.add("  -p       parse only\n");
		result.add("  -s       strip debug information\n");
		result.add("  -v       show version information\n");
		result.add("  --       stop handling options\n");

		throw new IllegalArgumentException(result.toString());
	}

	int doargs(int argc, String[] argv)
	{
		int i;
		int version = 0;

		if (argv[0] != null && argv[0].length() != 0)
		{
			this.progname = argv[0];
		}

		boolean isDone = false;

		for (i = 1; !isDone && i < argc; i++)
		{
			final String arg = argv[i];

			if (arg.charAt(0) != '-') // end of options; keep it
			{
				isDone = true;
			}
			else
			{
				switch (arg)
				{
					case "-": // end of options: use 'stdin'
					{
						isDone = true;
						break;
					}
					case "--":	//end of options: skip it
					{
						++i;
						if (version != 0)
						{
							++version;
						}
						
						break;
					}
					case "-l":	//list
					{
						this.listing = true;
						break;
					}
					case "-o":	//output file
					{
						this.output = argv[++i];

						if (this.output == null || this.output.length() == 0)
						{
							this.usage("'-o' needs argument");
						}
						
						if(this.output.equals("-"))
						{
							this.output = null;
						}

						break;
					}
					case "-p":	//parse only
					{
						this.dumping = false;
						break;
					}
					case "-s":	//strip debug information
					{
						this.stripping = true;
						break;
					}
					case "-v":	//show version
					{
						++version;
						break;
					}
					default:
					{
						this.usage(arg);
						break;
					}
				}
			}
		}
		
		if(i == argc && (this.listing || !this.dumping))
		{
			this.dumping = false;
			argv[--i] = this.output;
		}
		
		if(version != 0)
		{
			System.out.println(LUA_RELEASE + " " + LUA_COPYRIGHT);
			
			if(version == argc - 1)
			{
				throw new IllegalArgumentException("EXIT_SUCCESS");
			}
		}

		return i;
	}
	
	final Proto combine(lua_State L, int n)
	{
		
	}
}
