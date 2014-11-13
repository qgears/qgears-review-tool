package hu.qgears.review.test;

import java.text.ParseException;

import hu.qgears.review.util.UtilSimpleString;

public class TestUtilSimpleString {
	public static void main(String[] args) throws ParseException {
		new TestUtilSimpleString().test1();
		new TestUtilSimpleString().test2();
	}
	public void test1() throws ParseException
	{
		String example="alma #####\n %% %%%% %%NB";
		UtilSimpleString uss=new UtilSimpleString("#####\n", "%%", "NB");
		String a=uss.escape(example);
		System.out.println(""+a);
		String v=uss.unescape(a);
		if(!v.equals(example))
		{
			throw new RuntimeException("Inverting is not correct!");
		}
	}
	public void test2() throws ParseException
	{
		String example="alma \n korte";
		UtilSimpleString uss=new UtilSimpleString("\n", "\\", "n");
		String a=uss.escape(example);
		System.out.println(""+example+" -> "+a);
		String v=uss.unescape(a);
		if(!v.equals(example))
		{
			throw new RuntimeException("Inverting is not correct!");
		}
	}
}
