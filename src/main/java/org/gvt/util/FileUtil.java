package org.gvt.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Ozgun Babur
 */
public class FileUtil
{
	public static ZipEntry findEntryContainingNameInZIPFile(String zipFileName,
		String partOfEntryName)
	{
		try
		{
			ZipFile zipFile = new ZipFile(zipFileName);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements())
			{
				ZipEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory())
				{
					String fileName = zipEntry.getName();
					if (fileName.contains(partOfEntryName))
					{
						return zipEntry;
					}
				}
			}
			zipFile.close();
		} catch (final IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
		return null;
	}

	public static String readEntryContainingNameInTARGZFile(String zipFileName,
		String partOfEntryName)
	{
		try
		{
			GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(zipFileName));
			TarArchiveInputStream is = new TarArchiveInputStream(gzipInputStream);
			ArchiveEntry entry;

			while ((entry = is.getNextEntry()) != null)
			{
				if (entry.isDirectory()) continue;
				else
				{
					if (entry.getName().contains(partOfEntryName))
					{
						byte[] content = new byte[(int) entry.getSize()];
						int len = is.read(content, 0, content.length);

						if (len == content.length);
						{
							String s = new String(content, "UTF-8");
							return s;
						}
					}
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}

		return null;
	}

	public static void main(String[] args)
	{
		String s = readEntryContainingNameInTARGZFile("/home/ozgun/Downloads/gdac.broadinstitute.org_OV-TP." +
			"CopyNumber_Gistic2.Level_4.2013052300.0.0.tar.gz", "amp_genes");

		System.out.println("s = " + s);
	}
}
