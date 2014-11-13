package hu.qgears.review.tool;

import hu.qgears.commons.Pair;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UtilProcess {
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
					InputStream is=p.getInputStream();
					int n;
					byte[] cbuf=new byte[1024];
					while((n=is.read(cbuf))>-1)
					{
						ret.write(cbuf, 0, n);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				InputStream is=p.getErrorStream();
				int n;
				byte[] cbuf=new byte[1024];
				while((n=is.read(cbuf))>-1)
				{
					ret.write(cbuf, 0, n);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally
			{
				retfut.setB(ret.toByteArray());
			}
		};}
		.start();
		return retfut;
	}
}
