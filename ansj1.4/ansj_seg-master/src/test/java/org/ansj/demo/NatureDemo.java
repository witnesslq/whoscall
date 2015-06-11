package org.ansj.demo;

import java.io.IOException;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.tianlupan.whoscall.*;

/**
 * 词性标注
 * 
 * @author ansj
 * 
 */
public class NatureDemo {
	public static void main(String[] args) throws IOException {
		
		 NTUtil.initNTNZ();
		
		List<Term> terms = ToAnalysis.parse("Ansj中文分词是一个真正的ict的实现.并且加入了自己的一些数据结构和算法的分词.实现了高效率和高准确率的完美结合!");
		new NatureRecognition(terms).recognition(); //词性标注
		System.out.println(terms);
		
		
		 terms = ToAnalysis.parse("欢迎访问武汉傲龙教育科技有限公司，我们的电话是：18064129619，我是田鲁攀 ");
		//new NatureRecognition(terms).recognition(); //词性标注
		
		 NTUtil.recognizeNT(terms);
		
		System.out.println(terms);
		
	}
}
