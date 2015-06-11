package org.nlpcn.commons.lang.bloomFilter;

import org.nlpcn.commons.lang.bloomFilter.filter.*;
import org.nlpcn.commons.lang.bloomFilter.iface.Filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * BlommFilter 实现 1.构建hash算法 2.散列hash映射到数组的bit位置 3.验证
 * 
 * @author Ansj
 */
public class BloomFilter {

	private static int length = 5;

	Filter[] filters = new Filter[length];

	public BloomFilter(int m) throws Exception {
		float mNum = m / 5;
		long size = (long) (1L * mNum * 1024 * 1024 * 8);
		filters[0] = new JavaFilter(size);
		filters[1] = new PHPFilter(size);
		filters[2] = new JSFilter(size);
		filters[3] = new PJWFilter(size);
		filters[4] = new SDBMFilter(size);
	}

	public void add(String str) {
		for (int i = 0; i < length; i++) {
			filters[i].add(str);
		}
	}

	public boolean contains(String str) {
		for (int i = 0; i < length; i++) {
			if (!filters[i].contains(str)) {
				return false;
			}
		}
		return true;
	}

	public boolean containsAndAdd(String str) {
		boolean flag = this.contains(str) ;
		if(!flag){
			this.add(str);
		}
		return flag ;
	}

	public static void main(String[] args) throws Exception {
		BloomFilter bf = new BloomFilter(32);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\Users\\caiqing\\workspace\\CQ\\library\\dictionary-utf8.TXT"), "UTF-8"));
		String str = null;
		System.out.println("begin");
		long start = System.currentTimeMillis();
		while ((str = br.readLine()) != null) {
			if (bf.containsAndAdd(str)) {
				System.out.println("containsAndAdd:" + str);
			}
		}

		br.close();

		br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\Users\\caiqing\\workspace\\CQ\\library\\dictionary-utf8.TXT"), "UTF-8"));
		System.out.println("begin-find");
		start = System.currentTimeMillis();
		while ((str = br.readLine()) != null) {
			if (!bf.contains(str)) {
				System.out.println("contains:" + str);
			}
		}

		System.out.println(System.currentTimeMillis() - start);
		br.close();
	}
}
