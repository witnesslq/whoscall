package org.ansj.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.junit.Test;
import org.nlpcn.commons.lang.util.IOUtil;

public class SpeedTest {
	@Test
	public void basicTest() throws IOException {
		/*
		ToAnalysis.parse("test---aaaa中国孙健测试");
		BufferedReader reader = IOUtil.getReader("/Users/ansj/Documents/temp/test.txt", IOUtil.UTF8);

		String temp = null;

		List<String> all = new ArrayList<String>();

		int i = 0;
		while ((temp = reader.readLine()) != null) {

			i++;
			if (i < 0) {
				continue;
			}
			all.add(temp);
			if (all.size() > 10000) {
				break;
			}

		}
		long start = System.currentTimeMillis();
		int allCount = 0;
		for (int j = 0; j < 1; j++) {
			for (String string : all) {
				allCount += string.length();

				ToAnalysis.parse(string);
			}
		}

		long end = System.currentTimeMillis();
		System.out.println(start - end);
		System.out.println("共 " + allCount + " 个字符，每秒处理了:" + (allCount * 1000.0 / (end - start)));
		*/
	}
}
