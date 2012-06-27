/*
 * ScoringScheme.java
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
 * This abstract class is the superclass of all scoring schemes. It defines basic
 * operations that must be provided by all subclasses. Scoring schemes are used by
 * sequence alignment algorithms to compute the score of an alignment.
 *
 * @author Sergio A. de Carvalho Jr.
 * @see PairwiseAlignmentAlgorithm
 */
public abstract class ScoringScheme
{
	/**
	 * Determines whether this scoring scheme ignores the case of characters when
	 * computing their score. It is set by the constructor and cannot be changed
	 * afterwards.
	 */
	protected boolean case_sensitive;

	/**
	 * Creates a new instance of an scoring scheme. The case of characters is significant
	 * when subsequently computing their score.
	 */
	public ScoringScheme ()
	{
		this (true);
	}

	/**
	 * Creates a new instance of an scoring scheme. If <CODE>case_sensitive</CODE> is
	 * <CODE>true</CODE>, the case of characters is significant when subsequently
	 * computing their score; otherwise the case is ignored.
	 *
	 * @param case_sensitive <CODE>true</CODE> if the case of characters must be
	 * significant, <CODE>false</CODE> otherwise
	 */
	public ScoringScheme (boolean case_sensitive)
	{
		this.case_sensitive = case_sensitive;
	}

	/**
	 * Tells whether this scoring scheme ignores the case of characters when computing
	 * their score.
	 *
	 * @return <CODE>true</CODE> if the case of characters is significant,
	 * <CODE>false</CODE> otherwise
	 */
	public boolean isCaseSensitive ()
	{
		return this.case_sensitive;
	}

	/**
	 * Returns the score of a substitution of character <CODE>a</CODE> for character
	 * <CODE>b</CODE> according to this scoring scheme. If this substitution is not
	 * defined, an exception is raised.
	 *
	 * @param a first character
	 * @param b second character
	 * @return score of substitution of <CODE>a</CODE> for <CODE>b</CODE>
	 * @throws IncompatibleScoringSchemeException if this substitution is not defined
	 */
	public abstract int scoreSubstitution (char a, char b)
		throws IncompatibleScoringSchemeException;

	/**
	 * Returns the score of an insertion of character <CODE>a</CODE> according to this
	 * scoring scheme. If this character is not recognised, an exception is raised.
	 *
	 * @param a the character to be inserted
	 * @return score of insertion of <CODE>a</CODE>
	 * @throws IncompatibleScoringSchemeException if character is not recognised by this
	 * scoring scheme
	 */
	public abstract int scoreInsertion (char a)
		throws IncompatibleScoringSchemeException;

	/**
	 * Returns the score of a deletion of character <CODE>a</CODE> according to this
	 * scoring scheme. If this character is not recognised, an exception is raised.
	 *
	 * @param a the character to be deleted
	 * @return score of insertion of <CODE>a</CODE>
	 * @throws IncompatibleScoringSchemeException if character is not recognised by this
	 * scoring scheme
	 */
	public abstract int scoreDeletion (char a)
		throws IncompatibleScoringSchemeException;

	/**
	 * Returns the maximum absolute score that this scoring scheme can return for any
	 * substitution, deletion or insertion.
	 *
	 * @return maximum absolute score that can be returned
	 */
	public abstract int maxAbsoluteScore ();

	/**
	 * Returns <CODE>true</CODE> if this scoring scheme supports partial matches,
	 * <CODE>false</CODE> otherwise. A partial match is a situation when two characters
	 * are not equal but, for any reason, are regarded as similar by this scoring scheme,
	 * which then returns a positive score. This is common when for scoring schemes
	 * that implement amino acid scoring matrices.
	 *
	 * @return <CODE>true</CODE> if this scoring scheme supports partial matches,
	 * <CODE>false</CODE> otherwise
	 */
	public abstract boolean isPartialMatchSupported ();
}
