package cpath.client.util;

import java.io.IOException;


public class CPathException extends IOException
{

	public CPathException(String s) {
		super(s);
	}
	
	public CPathException(Throwable t) {
		super(t);
	}
	
	public CPathException(String s, Throwable t) {
		super(s,t);
	}
}
