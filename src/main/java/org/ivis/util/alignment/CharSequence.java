/*
 * CharSequence.java
 *
 * Copyright 2003 Sergio Anibal de Carvalho Junior
 *
 * This file is part of NeoBio.
 *
 * NeoBio is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * NeoBio is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NeoBio;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Proper attribution of the author as the source of the software would be appreciated.
 *
 * Sergio Anibal de Carvalho Junior		mailto:sergioanibaljr@users.sourceforge.net
 * Department of Computer Science		http://www.dcs.kcl.ac.uk
 * King's College London, UK			http://www.kcl.ac.uk
 *
 * Please visit http://neobio.sourceforge.net
 *
 * This project was supervised by Professor Maxime Crochemore.
 *
 */

package org.ivis.util.alignment;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * This class implements a sequence of characters stored as an array that provides random
 * access to any position in constant time.
 *
 * <P>The input can come from any source, provided it is encapsulated in a proper
 * <CODE>Reader</CODE> instance. The stream is expected to be ready (i.e. the next
 * <CODE>read</CODE> operation must return the first character of the sequence) and it is
 * not closed when its end is reached, so the client is allowed to reset it and maybe use
 * it for another purpose.</P>
 *
 * <P>Sequences can contain letters only although lines started with the
 * <CODE>COMMENT_CHAR</CODE> character ('>') are regarded as comments and are completely
 * skipped. White spaces (including tabs, line feeds and carriage returns) are also
 * ignored throughout.</P>
 *
 * <P>This class is used by two sequence alignment algorithms: {@linkplain SmithWaterman}
 * and {@linkplain NeedlemanWunsch}.</P>
 *
 * @author Sergio A. de Carvalho Jr.
 * @see SmithWaterman
 * @see NeedlemanWunsch
 */
public class CharSequence
{
	/**
	 * The character used to start a comment line in a sequence file. When this character
	 * is found, the rest of the line is ignored.
	 */
	protected static final char COMMENT_CHAR = '>';

	/**
	 * Stores the sequence as an array of characters.
	 */
	protected char sequence[];

	/**
	 * Creates a new instance of a <CODE>CharSequence</CODE>, loading the sequence data
	 * from the <CODE>Reader</CODE> input stream.
	 *
	 * @param reader source of characters for this sequence
	 * @throws IOException if an I/O exception occurs when reading the input
	 * @throws InvalidSequenceException if the input does not contain a valid sequence
	 */
	public CharSequence (Reader reader) throws IOException, InvalidSequenceException
	{
		int ch;
		char c;

		BufferedReader input = new BufferedReader(reader);

		StringBuffer buf = new StringBuffer();

		// read characters
		while ((ch = input.read()) != -1)
		{
			// conver to char
			c = (char) ch;

			// skip line if comment character is found
			if (c == COMMENT_CHAR)
				input.readLine();

			// accept letters only
			else if (Character.isLetter(c))
				buf.append(c);

			// anything else, except whitespaces, will throw an exception
			else if (!Character.isWhitespace(c))
				throw new InvalidSequenceException
					("Sequences can contain letters only.");
		}

		// check if read anything!
		if (buf.length() > 0)
			sequence = new char[buf.length()];
		else
			throw new InvalidSequenceException ("Empty sequence.");

		// copy data to
		buf.getChars(0, buf.length(), sequence, 0);
	}

	/**
	 * Returns the number of characters of this sequence.
	 *
	 * @return int number of characters of this sequence
	 */
	public int length ()
	{
		return sequence.length;
	}

	/**
	 * Returns the character at a given position. For the client, the first character is
	 * at position 1, while the last character is at position <CODE>length()</CODE>. This
	 * is convinient for sequence alignment algorithms based on a classic dynamic
	 * programming matrix since the sequences usually start at row/column 1. This method
	 * does not check boundaries, therefore an <CODE>ArrayIndexOutOfBoundsException</CODE>
	 * may be raised if <CODE>pos</CODE> is out of bounds.
	 *
	 * @param pos position of character (from 1 to <CODE>length()</CODE> inclusive)
	 * @return the character
	 */
	public char charAt (int pos)
	{
		// convert from one-based to zero-based index
		return sequence[pos-1];
	}

	/**
	 * Returns a string representation of the sequence.
	 *
	 * @return a string representation of the sequence
	 */
	public String toString ()
	{
		return new String(sequence);
	}
}
