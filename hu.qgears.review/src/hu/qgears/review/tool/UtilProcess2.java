package hu.qgears.review.tool;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilProcess;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/**
 * TODO move this functionality to hu.qgears.commons
 * 
 * @author rizsi
 *
 */
public class UtilProcess2 {
	private static final Logger LOG = Logger.getLogger(UtilProcess2.class);
	
	private static class PairFuture implements Future<Pair<byte[], byte[]>>
	{

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
		volatile private byte[] a;
		volatile private byte[] b;
		@Override
		public boolean isDone() {
			return a!=null&&b!=null;
		}

		@Override
		public Pair<byte[], byte[]> get() throws InterruptedException,
				ExecutionException {
			synchronized (this) {
				while(a==null||b==null)
				{
					this.wait();
				}
			}
			return new Pair<byte[], byte[]>(a,b);
		}

		@Override
		public Pair<byte[], byte[]> get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			synchronized (this) {
				if(a==null||b==null)
				{
					unit.timedWait(this, timeout);
					this.wait();
				}
				if(a==null||b==null)
				{
					throw new TimeoutException();
				}
			}
			return new Pair<byte[], byte[]>(a,b);
		}

		public void setA(byte[] string) {
			synchronized (this) {
				a=string;
				this.notifyAll();
			}
		}

		public void setB(byte[] string) {
			synchronized (this) {
				b=string;
				this.notifyAll();
			}
		}
		
	}
	public static Future<Pair<byte[], byte[]>> streamOutputsOfProcess(final Process p)
	{
		final PairFuture retfut=new PairFuture();
		new Thread(){
			public void run() {
				ByteArrayOutputStream ret=new ByteArrayOutputStream();
				try {
					UtilProcess.streamErrorOfProcess(p.getInputStream(), ret);
				} catch (Exception e) {
					LOG.error("Error streaming std out",e);
				}finally
				{
					retfut.setA(ret.toByteArray());
				}
			};
		}
		.start();
		new Thread(){public void run() {
			ByteArrayOutputStream ret=new ByteArrayOutputStream();
			try {
				UtilProcess.streamErrorOfProcess(p.getErrorStream(), ret);
			} catch (Exception e) {
				LOG.error("Error streaming std err",e);
			}finally
			{
				retfut.setB(ret.toByteArray());
			}
		};}
		.start();
		return retfut;
	}
}
