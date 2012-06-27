/*
 * IncompatibleScoringSchemeException.java
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

/**
 * Signals that an scoring scheme is not compatible with the sequences being aligned.
 *
 * @author Sergio A. de Carvalho Jr.
 * @see ScoringScheme
 * @see PairwiseAlignmentAlgorithm
 */
public class IncompatibleScoringSchemeException extends Exception
{
	/**
	 * Constructs an <CODE>IncompatibleScoringSchemeException</CODE> with null as its
	 * error detail message.
	 */
	public IncompatibleScoringSchemeException ()
	{
		super();
	}

	/**
	 * Constructs an <CODE>IncompatibleScoringSchemeException</CODE> with the specified
	 * detail message.
	 *
	 * @param message an error message
	 */
	public IncompatibleScoringSchemeException (String message)
	{
		super(message);
	}

	/**
	 * Constructs an <CODE>IncompatibleScoringSchemeException</CODE> with the specified
	 * cause (and a detail message that typically contains the class and detail message
	 * of cause).
	 *
	 * @param cause a cause
	 */
	public IncompatibleScoringSchemeException (Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs an <CODE>IncompatibleScoringSchemeException</CODE> with the specified
	 * detail message and cause.
	 *
	 * @param message an error message
	 * @param cause a cause
	 */
	public IncompatibleScoringSchemeException (String message, Throwable cause)
	{
		super(message, cause);
	}
}
