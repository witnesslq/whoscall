package org.ansj.test;

import org.ansj.splitWord.analysis.NlpAnalysis;

public class NlpAnalysisTest {
	public static void main(String[] args) {
		System.out.println(NlpAnalysis.parse("普通用户 名称 适用范围 厂家 武汉傲龙教育科技有限公司 ※ 联系方式 李炜 女士 电话:18064129619 传真: 手机: ◆ 动态信息 傲龙教育招代理啦 傲龙网校是一家提供..."));
	}
}
