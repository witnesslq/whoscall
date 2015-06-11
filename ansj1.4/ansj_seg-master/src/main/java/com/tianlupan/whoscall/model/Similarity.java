package com.tianlupan.whoscall.model;


import java.text.NumberFormat;
import java.util.Locale;


/**

 * 比较两个字符串的相似度

 */

public class Similarity {

	public static void main(String[] args) {

		String strA = "ADsT武汉傲龙教育科技有限公司";

		String strB = "ADsT武汉傲龙教育";

		double result=SimilarDegree(strA, strB);

		if(result>=0.7){
			System.out.println("相似度很高！" +similarityResult(result)+result);

		}else{

			System.out.println("相似度不高"+similarityResult(result)+result);

		}

		System.out.println();

	}



	/**

	 * 相似度转百分比

	 */

	public static String similarityResult(double resule){

		return  NumberFormat.getPercentInstance(new Locale( "en ", "US ")).format(resule);

	}



	/**
	 * 相似度比较，此处应该弄的更准确些，不能简单的比较字符出现此次，更多的是关联性，连续得分更高些
	 * @param strA
	 * @param strB
	 * @return
	 */
	public static double SimilarDegree(String strA, String strB){

		
		Similarity lt = new Similarity();

	  return lt.getSimilarityRatio(strA, strB) ;

	}

	

	 private int compare(String str, String target) {
		  int d[][]; // 矩阵
		  int n = str.length();
		  int m = target.length();
		  int i; // 遍历str的
		  int j; // 遍历target的
		  char ch1; // str的
		  char ch2; // target的
		  int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		  if (n == 0) {
		   return m;
		  }
		  if (m == 0) {
		   return n;
		  }
		  d = new int[n + 1][m + 1];
		  for (i = 0; i <= n; i++) { // 初始化第一列
		   d[i][0] = i;
		  }

		  for (j = 0; j <= m; j++) { // 初始化第一行
		   d[0][j] = j;
		  }

		  for (i = 1; i <= n; i++) { // 遍历str
		   ch1 = str.charAt(i - 1);
		   // 去匹配target
		   for (j = 1; j <= m; j++) {
		    ch2 = target.charAt(j - 1);
		    if (ch1 == ch2) {
		     temp = 0;
		    } else {
		     temp = 1;
		    }

		    // 左边+1,上边+1, 左上角+temp取最小
		    d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
		   }
		  }
		  return d[n][m];
		 }

		 

		 private int min(int one, int two, int three) {
		  return (one = one < two ? one : two) < three ? one : three;
		 }

		 

		 /**

		 * 获取两字符串的相似度

		 * 

		 * @param str

		 * @param target

		 * @return

		 */

		 public float getSimilarityRatio(String str, String target) {
		  return 1 - (float)compare(str, target)/Math.max(str.length(), target.length());
		 }


}