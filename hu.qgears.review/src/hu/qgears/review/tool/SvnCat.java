package hu.qgears.review.tool;

import hu.qgears.commons.Pair;

import java.util.concurrent.Future;

/**
 * Download a source file from an SVN server.
 * @author rizsi
 *
 */
public class SvnCat {
	public byte[] execute(String svnurl, String revision) throws Exception
	{
		Process p=Runtime.getRuntime().exec(new String[]{"/usr/bin/svn", "cat",
				"-r"+revision, svnurl});
		Future<Pair<byte[], byte[]>> fut=UtilProcess.streamOutputsOfProcess(p);
		Pair<byte[], byte[]> pa=fut.get();
		return pa.getA();
	}
}
