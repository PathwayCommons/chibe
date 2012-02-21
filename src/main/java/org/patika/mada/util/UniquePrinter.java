package org.patika.mada.util;

import java.util.Set;
import java.util.HashSet;

/**
 * Handles multiple copy prints. When something is requested to print second times, it is ignored.
 *
 * @author Ozgun Babur
 */
public class UniquePrinter
{
	private static Set<Object> memory = new HashSet<Object>();

	public static void resetMemory()
	{
		memory.clear();
	}

	public static void print(String pre, Object o)
	{
		if (!memory.contains(o))
		{
			System.out.println(pre + o.toString());
			memory.add(o);
		}
	}

	/**
	 * Just for printing the system properties to the console. I sometimes need this for debugging
	 * something.
	 */
	public static void main(String[] args)
	{
		System.getProperties().list(System.out);
	}
}
