package org.gvt.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for downloading files from a URL.
 * @author Ozgun Babur
 */
public class Download
{
	public static boolean downlaodTextFile(String url, String saveFile)
	{
		try
		{
			URLConnection con = new URL(url).openConnection();

			BufferedReader reader = new BufferedReader(
				new InputStreamReader(con.getInputStream()));

			BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));

			String currentRead;

			int cnt = 0;
			while((currentRead = reader.readLine()) != null)
			{
				writer.write(currentRead + "\n");
				cnt++;
			}

			reader.close();
			writer.close();
			return cnt > 0;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static boolean downloadAsIs(String address, String saveFile)
	{
		if (address.startsWith("ftp://"))
			return downloadAsIsViaFTP(address, saveFile);

		try
		{
			URL url = new URL(address);
			URLConnection con = url.openConnection();

			InputStream in = con.getInputStream();

			// Open the output file
			OutputStream out = new FileOutputStream(saveFile);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];

			int total = 0;
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				total += len;
			}

			// Close the file and stream
			in.close();
			out.close();

			return total > 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean downloadAndUncompress(String address, String saveFile)
	{
		if (address.startsWith("ftp://"))
			return downloadAndUncompressViaFTP(address, saveFile);

		try
		{
			URL url = new URL(address);
			URLConnection con = url.openConnection();

//			if (con instanceof )

			GZIPInputStream in = new GZIPInputStream(con.getInputStream());

			// Open the output file
			OutputStream out = new FileOutputStream(saveFile);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];

			int lines = 0;
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				lines++;
			}

			// Close the file and stream
			in.close();
			out.close();

			return lines > 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private static boolean downloadAndUncompressViaFTP(String address, String saveFile)
	{
		try
		{
			FTPClient fc = new FTPClient();

			String host = address.substring(6, address.indexOf("/", 7));
			String fileloc = address.substring(address.indexOf("/", 7));

			fc.connect(host);
			fc.enterLocalPassiveMode();
			fc.setRemoteVerificationEnabled(false);
			fc.login("anonymous", "");
			fc.setFileType(FTP.BINARY_FILE_TYPE);

			InputStream is = new GZIPInputStream(fc.retrieveFileStream(fileloc));
			OutputStream out = new FileOutputStream(saveFile);

			int length = 0;

			byte[] buffer = new byte[65536];
			int noRead;

			while ((noRead = is.read(buffer)) > 0) {
				out.write(buffer, 0, noRead);
				length += noRead;
			}

			is.close();
			fc.disconnect();
			out.close();

			return length > 0;
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			return false;
		}
	}

	private static boolean downloadAsIsViaFTP(String address, String saveFile)
	{
		try
		{
			FTPClient fc = new FTPClient();

			String host = address.substring(6, address.indexOf("/", 7));
			String fileloc = address.substring(address.indexOf("/", 7));

			fc.connect(host);
			fc.enterLocalPassiveMode();
			fc.setRemoteVerificationEnabled(false);
			fc.login("anonymous", "");
			fc.setFileType(FTP.BINARY_FILE_TYPE);

			InputStream is = fc.retrieveFileStream(fileloc);
			OutputStream out = new FileOutputStream(saveFile);

			int length = 0;

			byte[] buffer = new byte[65536];
			int noRead;

			while ((noRead = is.read(buffer)) > 0) {
				out.write(buffer, 0, noRead);
				length += noRead;
			}

			is.close();
			fc.disconnect();
			out.close();

			return length > 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static String[] listFtpFiles(String ftpDir) throws IOException
	{
		java.util.List<String> list = new ArrayList<String>();

		FTPClient fc = new FTPClient();

		String host = ftpDir.substring(6, ftpDir.indexOf("/", 7));

		String dir = ftpDir.substring(ftpDir.indexOf("/", 7));

		fc.connect(host);
		fc.enterLocalPassiveMode();
		fc.setRemoteVerificationEnabled(false);
		fc.login("anonymous", "");

		FTPFile[] ftpFiles = fc.listFiles(dir);
		for (FTPFile file : ftpFiles) {
			list.add(file.getName());
		}

		String[] files = list.toArray(new String[list.size()]);
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].contains(" ")) files[i] = files[i].substring(files[i].indexOf(" ") + 1);
		}
		return files;
	}

}
