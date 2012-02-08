package org.patika.mada.util;

import org.patika.mada.dataXML.Reference;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.Xref;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class XRef
{
	/**
	 * Database.
	 */
	private String db;

	/**
	 * Reference.
	 */
	private String ref;

	//----------------------------------------------------------------------------------------------
	// Section: Constructors that register db name. These constructors can only be used while
	// reading in new biopax file.
	//----------------------------------------------------------------------------------------------

	/**
	 * @param db
	 * @param ref
	 */
	public XRef(String db, String ref)
	{
		this.db = db;
		this.ref = ref;

		registerDB(db);
	}

	public XRef(xref r)
	{
		this(r.getDB(), r.getID());
	}

	public XRef(Xref r)
	{
		this(r.getDb(), r.getId());
	}

	//----------------------------------------------------------------------------------------------
	// Section: Constructors that do not register db name. These constructors can be used when
	// creating temporary XRef objects, that are not associated with graph objects.
	//----------------------------------------------------------------------------------------------

	public XRef(Reference r)
	{
		this.db = r.getDb();
		this.ref = r.getValue();
	}

	public XRef(String s)
	{
		int sepLoc = s.indexOf(SEPARATOR);

		assert sepLoc > 0 && sepLoc < s.length() - 1 : "s: " + s;

		this.db = s.substring(0, sepLoc);
		this.ref = s.substring(sepLoc + 1);
	}

	public String getDb()
	{
		return db;
	}

	public String getRef()
	{
		return ref;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof XRef)
		{
			XRef x = (XRef) obj;
			return toString().equals(x.toString());
		}
		return false;
	}

	public int hashCode()
	{
		return this.toString().hashCode();
	}

	public String toString()
	{
		return db + SEPARATOR + ref;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Database names register
	//----------------------------------------------------------------------------------------------

	/**
	 * This set is used for registering encountered db names. This must be cleared before loading a
	 * new biopax graph.
	 */
	private static Set<String> dbSet = new HashSet<String>();

	public static Set<String> getDBSet()
	{
		return dbSet;
	}

	public static void clearDBSet()
	{
		dbSet.clear();
	}
	
	public static void registerDB(String db)
	{
		dbSet.add(db);
	}

	/**
	 * Gets the first xref of the given type.
	 * @param refs
	 * @param db
	 * @return
	 */
	public static XRef getFirstRef(Collection<XRef> refs, String db)
	{
		for (XRef ref : refs)
		{
			if (db == null || ref.getDb().equalsIgnoreCase(db))
			{
				return ref;
			}
		}
		return null;
	}

	/**
	 * Gets the first ref whose type is the first element of the db_priority array. If there is no
	 * such ref, then checks for the second db, and goes on.
	 * @param refs
	 * @param db_priority
	 * @return
	 */
	public static XRef getFirstRef(Collection<XRef> refs, String[] db_priority)
	{
		for (String db : db_priority)
		{
			XRef ref = getFirstRef(refs, db);

			if (ref != null) return ref;
		}
		return null;
	}

	public static final String SEPARATOR = ":";

	// Some database constants

	public static final String CPATH = "CPATH";
	public static final String UNIPROT = "UNIPROT";
	public static final String ENTREZ_GENE = "ENTREZ_GENE";
}
