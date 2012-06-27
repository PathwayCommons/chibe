/*
 * NeedlemanWunsch.java
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
 */

package org.ivis.util.alignment;

import java.io.Reader;
import java.io.IOException;

/**
 * This class implements the classic global alignment algorithm (with linear gap penalty
 * function) due to S.B.Needleman and C.D.Wunsch (1970).
 *
 * <P>It is based on a dynamic programming approach. The idea consists of, given two
 * sequences A and B of sizes n and m, respectively, building an (n+1 x m+1) matrix M that
 * contains the similarity of prefixes of A and B. Every position M[i,j] in the matrix
 * holds the score between the subsequences A[1..i] and B[1..j]. The first row and column
 * represent alignments with spaces.</P>
 *
 * <P>Starting from row 0, column 0, the algorithm computes each position M[i,j] with the
 * following recurrence:</P>
 *
 * <CODE><BLOCKQUOTE><PRE>
 * M[0,0] = 0
 * M[i,j] = max { M[i,j-1]   + scoreInsertion (B[j]),
 *                M[i-1,j-1] + scoreSubstitution (A[i], B[j]),
 *                M[i-1,j]   + scoreDeletion(A[i])             }
 * </PRE></BLOCKQUOTE></CODE>
 *
 * <P>In the end, the value at the last position (last row, last column) will contain
 * the similarity between the two sequences. This part of the algorithm is accomplished
 * by the {@link #computeMatrix computeMatrix} method. It has quadratic space complexity
 * since it needs to keep an (n+1 x m+1) matrix in memory. And since the work of computing
 * each cell is constant, it also has quadratic time complexity.</P>
 *
 * <P>After the matrix has been computed, the alignment can be retrieved by tracing a path
 * back in the matrix from the last position to the first. This step is performed by
 * the {@link #buildOptimalAlignment buildOptimalAlignment} method, and since the path can
 * be roughly as long as (m + n), this method has O(n) time complexity.</P>
 *
 * <P>If the similarity value only is needed (and not the alignment itself), it is easy to
 * reduce the space requirement to O(n) by keeping just the last row or column in memory.
 * This is precisely what is done by the {@link #computeScore computeScore} method. Note
 * that it still requires O(n<SUP>2</SUP>) time.</P>
 *
 * <P>For a more efficient approach to the global alignment problem, see the
 * {@linkplain CrochemoreLandauZivUkelson} algorithm. For local alignment, see the
 * {@linkplain SmithWaterman} algorithm.</P>
 *
 * @author Sergio A. de Carvalho Jr.
 * @see SmithWaterman
 * @see CrochemoreLandauZivUkelson
 * @see CrochemoreLandauZivUkelsonLocalAlignment
 * @see CrochemoreLandauZivUkelsonGlobalAlignment
 *
 * Modified by Ugur Dogrusoz to change initial and terminating conditions to
 * ignore beginning and end gaps. The reason for this is, here we are emulating
 * a circular alignment by duplicating the first of the two sequences to be
 * aligned. Because of this gaps at the beginning and end should not be taken
 * into account. We initialize row 0 and column 0 to all 0's. The maximum score
 * is searched for the last row and column rather than directly looking at the
 * lower-right entry.
 * Details: http://www.stanford.edu/class/cs262/notes/lecture3.pdf
 */
public class NeedlemanWunsch extends PairwiseAlignmentAlgorithm
{
	/**
	 * The first sequence of an alignment.
	 */
	protected CharSequence seq1;

	/**
	 * The second sequence of an alignment.
	 */
	protected CharSequence seq2;

	/**
	 * The dynamic programming matrix. Each position (i, j) represents the best score
	 * between the firsts i characters of <CODE>seq1</CODE> and j characters of
	 * <CODE>seq2</CODE>.
	 */
	protected int[][] matrix;

	/**
	 * Loads sequences into {@linkplain CharSequence} instances. In case of any error,
	 * an exception is raised by the constructor of <CODE>CharSequence</CODE> (please
	 * check the specification of that class for specific requirements).
	 *
	 * @param input1 Input for first sequence
	 * @param input2 Input for second sequence
	 * @throws IOException If an I/O error occurs when reading the sequences
	 * @throws InvalidSequenceException If the sequences are not valid
	 * @see CharSequence
	 */
	protected void loadSequencesInternal (Reader input1, Reader input2)
		throws IOException, InvalidSequenceException
	{
		// load sequences into instances of CharSequence
		this.seq1 = new CharSequence(input1);
		this.seq2 = new CharSequence(input2);
	}

	/**
	 * Frees pointers to loaded sequences and the dynamic programming matrix so that their
	 * data can be garbage collected.
	 */
	protected void unloadSequencesInternal ()
	{
		this.seq1 = null;
		this.seq2 = null;
		this.matrix = null;
	}

	/**
	 * Builds an optimal global alignment between the loaded sequences after computing the
	 * dynamic programming matrix. It calls the <CODE>buildOptimalAlignment</CODE> method
	 * after the <CODE>computeMatrix</CODE> method computes the dynamic programming
	 * matrix.
	 *
	 * @return an optimal global alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme is not compatible
	 * with the loaded sequences.
	 * @see #computeMatrix
	 * @see #buildOptimalAlignment
	 */
	protected PairwiseAlignment computePairwiseAlignment ()
		throws IncompatibleScoringSchemeException
	{
		// compute the matrix
		computeMatrix ();

		// build and return an optimal global alignment
		PairwiseAlignment alignment = buildOptimalAlignment ();

		// allow the matrix to be garbage collected
		matrix = null;

		return alignment;
	}

	/**
	 * Computes the dynamic programming matrix.
	 *
	 * @throws IncompatibleScoringSchemeException If the scoring scheme is not compatible
	 * with the loaded sequences.
	 */
	protected void computeMatrix () throws IncompatibleScoringSchemeException
	{
		int r, c, rows, cols, ins, del, sub;

		rows = seq1.length()+1;
		cols = seq2.length()+1;

		matrix = new int [rows][cols];

		// initiate first row
		matrix[0][0] = 0;
		for (c = 1; c < cols; c++)
			matrix[0][c] = 0; //UD: init condition to trim beginning and end gaps

		// calculates the similarity matrix (row-wise)
		for (r = 1; r < rows; r++)
		{
			// initiate first column
			matrix[r][0] = 0; //UD: init condition to trim beginning and end gaps

			for (c = 1; c < cols; c++)
			{
				ins = matrix[r][c-1] + scoreInsertion(seq2.charAt(c));
				sub = matrix[r-1][c-1] + scoreSubstitution(seq1.charAt(r),seq2.charAt(c));
				del = matrix[r-1][c] + scoreDeletion(seq1.charAt(r));

				// choose the greatest
				matrix[r][c] = max (ins, sub, del);
			}
		}
	}

	/**
	 * Builds an optimal global alignment between the loaded sequences. Before it is
	 * executed, the dynamic programming matrix must already have been computed by
	 * the <CODE>computeMatrix</CODE> method.
	 *
	 * @return an optimal global alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme
	 * is not compatible with the loaded sequences.
	 * @see #computeMatrix
	 */
	protected PairwiseAlignment buildOptimalAlignment ()
		throws IncompatibleScoringSchemeException
	{
		StringBuffer	gapped_seq1, score_tag_line, gapped_seq2;
		int				r, c, sub, max_score;

		gapped_seq1 	= new StringBuffer();
		score_tag_line	= new StringBuffer();
		gapped_seq2 	= new StringBuffer();

		//UD: find max in last row or last column, make sure it's not a gap
		//UD: this should be guarenteed assuming gap penalty is not less than
		//UD: mismatch penalty

		max_score = Integer.MIN_VALUE;
		r = -1;
		c = -1;

		// search last column

		for (int i = 1; i < matrix.length; i++)
		{
			int j = matrix[i].length - 1;

			if (matrix[i][j] > max_score &&
				matrix[i][j] == matrix[i-1][j-1] + scoreSubstitution(seq1.charAt(i), seq2.charAt(j)))
			{
				max_score = matrix[r = i][c = j];
			}
		}

		// search last row

		int i = matrix.length - 1;

		for (int j = 1; j < matrix[i].length; j++)
		{
			if (matrix[i][j] > max_score &&
				matrix[i][j] == matrix[i-1][j-1] + scoreSubstitution(seq1.charAt(i), seq2.charAt(j)))
			{
				max_score = matrix[r = i][c = j];
			}
		}

		assert r > 0 && c > 0;

		while ((r > 0) || (c > 0))
		{
			if (r == 0 || c == 0)
			{
				break;
			}

			if ((r > 0) && (c > 0))
			{
				sub = scoreSubstitution(seq1.charAt(r), seq2.charAt(c));

				if (matrix[r][c] == matrix[r-1][c-1] + sub)
				{
					// substitution was used
					gapped_seq1.insert (0, seq1.charAt(r));
					if (seq1.charAt(r) == seq2.charAt(c))
						if (useMatchTag())
							score_tag_line.insert (0, MATCH_TAG);
						else
							score_tag_line.insert (0, seq1.charAt(r));
					else if (sub > 0)
						score_tag_line.insert (0, APPROXIMATE_MATCH_TAG);
					else
						score_tag_line.insert (0, MISMATCH_TAG);
					gapped_seq2.insert (0, seq2.charAt(c));
					r = r - 1; c = c - 1;

					// skip to the next iteration
					continue;
				}
			}

			if (c > 0)
				if (matrix[r][c] == matrix[r][c-1] + scoreInsertion(seq2.charAt(c)))
				{
					// insertion was used
					gapped_seq1.insert (0, GAP_CHARACTER);
					score_tag_line.insert (0, GAP_TAG);
					gapped_seq2.insert (0, seq2.charAt(c));
					c = c - 1;

					// skip to the next iteration
					continue;
				}

			// must be a deletion
			gapped_seq1.insert (0, seq1.charAt(r));
			score_tag_line.insert (0, GAP_TAG);
			gapped_seq2.insert (0, GAP_CHARACTER);
			r = r - 1;
		}

		return new PairwiseAlignment (gapped_seq1.toString(), score_tag_line.toString(),
										gapped_seq2.toString(), max_score);
	}

	/**
	 * Computes the score of the best global alignment between the two sequences using the
	 * scoring scheme previously set. This method calculates the similarity value only
	 * (doesn't build the whole matrix so the alignment cannot be recovered, however it
	 * has the advantage of requiring O(n) space only).
	 *
	 * @return score of the best global alignment between the loaded sequences
	 * @throws IncompatibleScoringSchemeException If the scoring scheme is not compatible
	 * with the loaded sequences.
	 */
	protected int computeScore () throws IncompatibleScoringSchemeException
	{
		int[]	array;
		int		r, c, rows, cols, tmp, ins, del, sub;

		rows = seq1.length()+1;
		cols = seq2.length()+1;

		if (rows <= cols)
		{
			// goes columnwise
			array = new int [rows];

			// initiate first column
			array[0] = 0;
			for (r = 1; r < rows; r++)
				array[r] = array[r-1] + scoreDeletion(seq1.charAt(r));

			// calculate the similarity matrix (keep current column only)
			for (c = 1; c < cols; c++)
			{
				// initiate first row (tmp hold values
				// that will be later moved to the array)
				tmp = array[0] + scoreInsertion(seq2.charAt(c));

				for (r = 1; r < rows; r++)
				{
					ins = array[r] + scoreInsertion(seq2.charAt(c));
					sub = array[r-1] + scoreSubstitution(seq1.charAt(r), seq2.charAt(c));
					del = tmp + scoreDeletion(seq1.charAt(r));

					// move the temp value to the array
					array[r-1] = tmp;

					// choose the greatest
					tmp = max (ins, sub, del);
				}

				// move the temp value to the array
				array[rows - 1] = tmp;
			}

			return array[rows - 1];
		}
		else
		{
			// goes rowwise
			array = new int [cols];

			// initiate first row
			array[0] = 0;
			for (c = 1; c < cols; c++)
				array[c] = array[c-1] + scoreInsertion(seq2.charAt(c));

			// calculate the similarity matrix (keep current row only)
			for (r = 1; r < rows; r++)
			{
				// initiate first column (tmp hold values
				// that will be later moved to the array)
				tmp = array[0] + scoreDeletion(seq1.charAt(r));

				for (c = 1; c < cols; c++)
				{
					ins = tmp + scoreInsertion(seq2.charAt(c));
					sub = array[c-1] + scoreSubstitution(seq1.charAt(r), seq2.charAt(c));
					del = array[c] + scoreDeletion(seq1.charAt(r));

					// move the temp value to the array
					array[c-1] = tmp;

					// choose the greatest
					tmp = max (ins, sub, del);
				}

				// move the temp value to the array
				array[cols - 1] = tmp;
			}

			return array[cols - 1];
		}
	}
}
