package com.androbotus.robotmq;

import junit.framework.Assert;

import org.junit.Test;

import com.androbotus.mq2.util.NumUtils;

public class NumUtilsTest {
	@Test
	public void testToByteIntConvertion(){
		int initial = 64345;
		byte[] bytes = NumUtils.convertIntToByte(initial);
		int num = NumUtils.convertByteToInt(bytes);
		Assert.assertTrue(initial == num);
	}
}
