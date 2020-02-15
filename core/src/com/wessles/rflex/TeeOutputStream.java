package com.wessles.rflex;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Classic splitter of OutputStream. Named after the unix 'tee'
 * command. It allows a stream to be branched off so there
 * are now two streams.
 *
 * @version $Id: TeeOutputStream.java 659817 2008-05-24 13:23:10Z niallp $
 */
public class TeeOutputStream extends ProxyOutputStream {

	/**
	 * the second OutputStream to write to
	 */
	protected OutputStream branch;

	/**
	 * Constructs a TeeOutputStream.
	 *
	 * @param out    the main OutputStream
	 * @param branch the second OutputStream
	 */
	public TeeOutputStream(OutputStream out, OutputStream branch) {
		super(out);
		this.branch = branch;
	}

	/**
	 * Write the bytes to both streams.
	 *
	 * @param b the bytes to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(byte[] b) throws IOException {
		super.write(b);
		this.branch.write(b);
	}

	/**
	 * Write the specified bytes to both streams.
	 *
	 * @param b   the bytes to write
	 * @param off The start offset
	 * @param len The number of bytes to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		this.branch.write(b, off, len);
	}

	/**
	 * Write a byte to both streams.
	 *
	 * @param b the byte to write
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		super.write(b);
		this.branch.write(b);
	}

	/**
	 * Flushes both streams.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void flush() throws IOException {
		super.flush();
		this.branch.flush();
	}

	/**
	 * Closes both streams.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		super.close();
		this.branch.close();
	}

}