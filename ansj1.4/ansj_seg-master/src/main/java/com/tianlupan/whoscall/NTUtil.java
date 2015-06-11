package com.tianlupan.whoscall;

import org.ansj.domain.Nature;
import org.ansj.domain.Term;
import org.ansj.domain.TermNature;
import org.ansj.domain.TermNatures;
import org.ansj.library.UserDefineLibrary;
import org.ansj.util.*;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

import java.io.*;
import java.util.*;

public class NTUtil {

	public static String libraryDir = "library";

	// 机构名称
	private static HashSet<String> gongsiSet = loadToSet("nt.txt");
	// 机构默认频率
	public static final int NT_DEFAULT_PINLV = 1500;

	// 行业用ND代表属性，这个可以被当前的自动合并NT识别
	private static HashSet<String> hangyeSet = loadToSet("hangye.txt");
	public static final int HANGYE_DEFAULT_PINLV = 5000;

	// 二个字的行业，如装修,用做行业后缀，如武汉傲龙教育， 一加一装饰
	private static HashSet<String> hangyeHouZhuiSet = loadToSet("hangye2.txt");
	public static final int HANGYE_HOUZHUI_DEFAULT_PINLV = 5000;

	// 将来这个应该从文件中读取
	// 修正 n=>nt,比如"工作室"6
	private static HashSet<String> fixNTSet = loadDefaultLibraryNIS();

	private static HashSet<String> baijiaxingSet = loadToSet("baijiaxing.txt");
	private static HashSet<String> chenghuSet = loadToSet("chenghu.txt");

	// 是否已经初始化标记
	private static boolean isInitialed = false;

	// Method=1,nlp,Method=2,To
	public static int defaultMethod = 2;

	public static boolean ntDebug = true;

	// 从默认库中获取NIS => NT
	private static HashSet<String> loadDefaultLibraryNIS() {

		HashSet<String> set = new HashSet<String>();

		String userLibrary = MyStaticValue.userLibrary;

		File file = new File(userLibrary);

		if (!file.canRead()) {
			MyStaticValue.LIBRARYLOG.warning("file in path " + file.getAbsolutePath()
					+ " can not to read!");
			return set;
		}
		String temp = null;
		BufferedReader br = null;
		String[] strs = null;
		try {
			br = IOUtil.getReader(new FileInputStream(file), "UTF-8");
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				} else {
					strs = temp.split("\t");
					if (strs.length == 3 && strs[1].equals("nis")
							&& !StringUtil.isBlank(strs[0])) {
						set.add(strs[0]);
					}
				}
			}
			MyStaticValue.LIBRARYLOG
					.info("load fixSet from default  userLibrary ok path is : "
							+ file.getAbsolutePath());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(br);
			br = null;
		}

		return set;

	}

	public static void main(String[] args) {

	}

	static {
		/**
		 * 配置文件变量
		 */
		try {
			ResourceBundle rb = ResourceBundle.getBundle("library");
			if (rb.containsKey("libraryDir"))
				libraryDir = rb.getString("libraryDir");
			if (rb.containsKey("defaultMethod"))
				defaultMethod = Integer.valueOf(rb.getString("defaultMethod"));
			MyStaticValue.LIBRARYLOG.warning("default method="
					+ (defaultMethod == 1 ? "NLP" : "To"));

			if (rb.containsKey("ntDebug"))
				ntDebug = Boolean.valueOf(rb.getString("ntDebug"));

		} catch (Exception e) {
			e.printStackTrace();
			MyStaticValue.LIBRARYLOG
					.warning("not find library.properties in classpath ,set libraryDir=library by default !");
		}
	}

	/**
	 * 按条加载资源文件并添加进set
	 * 
	 * @param file
	 * @param set
	 */
	private static HashSet<String> loadToSet(String file) {

		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = null;
		String line = null;
		// 用#做注释
		try {
			// 默认目录是/library/nt
			reader = IOUtil.getReader(new FileInputStream(libraryDir + "/nt/"
					+ file), "UTF-8");
			while ((line = reader.readLine()) != null) {
				if (!org.ansj.util.TextUtils.isEmpty(line) && !line.startsWith("#")) {
					set.add(line);
				}
			}
		} catch (IOException ex) {
			log("Init  dictionary failed, dic=" + file);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return set;

	}

	public static HashSet<String> getHangyeSet() {
		return hangyeSet;
	}

	public static HashSet<String> getNTSet() {
		return gongsiSet;
	}

	/**
	 * 把自定义机构名称、行业列表加入初始所有单词总词典
	 */
	public static void initNTNZ() {
		// 只运行一次;
		if (isInitialed)
			return;

		Iterator<String> iterator = gongsiSet.iterator();
		while (iterator.hasNext()) {
			String nt = iterator.next();
			UserDefineLibrary.insertWord(nt, "nt", NT_DEFAULT_PINLV);
		}

		Iterator<String> iteratorHangye = hangyeSet.iterator();
		while (iteratorHangye.hasNext()) {
			String hangye = iteratorHangye.next();
			UserDefineLibrary.insertWord(hangye, "nd", HANGYE_DEFAULT_PINLV);
		}

		Iterator<String> iteratorHangyeHouZhui = hangyeHouZhuiSet.iterator();
		while (iteratorHangyeHouZhui.hasNext()) {
			String hangye = iteratorHangyeHouZhui.next();
			UserDefineLibrary.insertWord(hangye, "nd",
					HANGYE_HOUZHUI_DEFAULT_PINLV);
		}

		isInitialed = true;
	}

	/**
	 * 添加进公司词典
	 * 
	 * @param cMap
	 */
	public static void appendNT(HashMap<String, int[]> nMap) {
		Iterator<String> iterator = gongsiSet.iterator();
		while (iterator.hasNext()) {
			String nt = iterator.next();
			// 当前不支持value值
			nMap.put(nt, new int[] { 0, NT_DEFAULT_PINLV });
		}
	}

	/**
	 * 合并list中的从from直至to的词语,并且设置新词语词性
	 * 
	 * @param list
	 * @param from
	 * @param to
	 * @param termNatures
	 */
	private static void JoinTerm(List<Term> list, Term from, Term to,
			TermNatures termNatures) {
		if (from == null || to == null)
			return;

		int fromIndex = list.indexOf(from);
		int endIndex = list.indexOf(to);

		if (fromIndex < 0 || endIndex < 0 || fromIndex >= endIndex)
			return;

		List<Term> subItems = new ArrayList<Term>();

		StringBuilder builder = new StringBuilder();

		Iterator<Term> iterator = list.iterator();
		int index = -1;
		while (iterator.hasNext()) {
			index++;
			Term term = (Term) iterator.next();
			if (index >= fromIndex && index <= endIndex) {
				subItems.add(term);
				builder.append(term.getName());
				iterator.remove();
			}
		}

		Term term = new Term(builder.toString(), from.getOffe(), termNatures);
		term.setSubTerm(subItems);
		term.setFrom(from.from());
		term.setTo(to.to());
		list.add(fromIndex, term);
	}

	private static void CombineToNT(List<Term> list, Term from, Term to) {
		JoinTerm(list, from, to, TermNatures.NT);
	}

	private static boolean isSeparate(Term term) {
		return (term.getNatureStr().equals("w")
				|| term.getNatureStr().equals("") || term.getNatureStr()
				.equals("null"));
	}

	/**
	 * 合并连续的属性,,如地区:武汉市/ns,洪山区/ns=>武汉市洪山区/ns,新合成词的词性为nt
	 * 
	 * @param list
	 * @param nature
	 *            "ns"或"nt"等,
	 * @return
	 */
	private static void CombineContinusSameTerm(List<Term> list, String nature) {
		boolean firstFound = false;
		boolean lastFound = false;
		Term from = null;
		Term to = null;
		for (int i = 0; i < list.size(); i++) {
			Term term = list.get(i);
			if (term.getNatureStr().equals(nature)) {
				if (!firstFound) {
					firstFound = true;
					from = term;
					lastFound = false;
					to = null;
				} else {
					lastFound = true;
					to = term;
				}
			} else {
				if (firstFound && lastFound) {
					break;
				} else {
					firstFound = false;
					from = null;
					to = null;
					continue;
				}
			}
		}

		if (firstFound && from != null && to != null && from != to) {
			JoinTerm(list, from, to, from.termNatures());
			CombineContinusSameTerm(list, nature);
		}
	}

	private static void combineNwNt(List<Term> list) {
		combineNatureToNature(list, "nw", "nt", 4);
	}

	private static void combineNrNt(List<Term> list) {
		combineNatureToNature(list, "nr", "nt", 4);
	}

	/**
	 * 比如 [格瑞迈德/nrf, 教育/vn, 科技发展(北京)有限公司/nt]
	 * 
	 * @param list
	 */
	private static void combineNrfNt(List<Term> list) {
		combineNatureToNature(list, "nrf", "nt", 4);
	}

	private static void combineNrND(List<Term> list) {
		combineNatureToNature(list, "nr", "nd", 4);
	}

	/**
	 * 如果最开始几个字之后是nt,很可能从头到nt,是一段，如 装修/v, 设计/vn, 培训班/nt => 装修设计培训班
	 * 这个方法需要改进，可以改成类似 倒着合并ND,backCombineND
	 * @param list
	 */
	private static void combineHeadNT(List<Term> list) {
		final int maxPosition = 6;
		for (int i = 0; i < list.size(); i++) {
			Term term = list.get(i);
			if (isSeparate(term) 
					|| term.getOffe() > maxPosition
					|| (term.getNatureStr().equals("v") && term.getName().length()>1)  //NT前面不能是动词，如“欢迎/v来到/v朗翊科技/nt”
					|| term.getName().equals("的")
					) {
				break;
			}
			// 如果名称已经很长了,就不加前面的了
			//如果不是以地区开头的公司名
			if (i>0 
					&& term.getNatureStr().equals("nt")
					&& term.getOffe() <= maxPosition
					&&!(term.getSubTerm() != null
							&& term.getSubTerm().size() > 0 && term
							.getSubTerm().get(0).getNatureStr()
							.equals("ns"))
					&& term.getName().length() < 14) {
				CombineToNT(list, list.get(0), term);
				break;
			}
		}

	}


	/**
	 * 合并常规公司名称, 北京/ns, 形意/nz, 达/v, 设计公司/nt=>北京形意达设计公司/nt
	 * 
	 * @param list
	 */
	private static void combineNsNt(List<Term> list) {
		combineNatureToNature(list, "ns", "nt", 6);
	}

	private static void combineNsND(List<Term> list) {
		combineNatureToNature(list, "ns", "nd", 4, 1);
	}

	private static void combineNwND(List<Term> list) {
		combineNatureToNature(list, "nw", "nd", 4);
	}

	private static void combineNatureToNature(List<Term> list,
			String natureFrom, String natureTo, int nsLastMAXLENGTH) {
		combineNatureToNature(list, natureFrom, natureTo, nsLastMAXLENGTH, 0);
	}

	/**
	 * 合并从一个nature到另一个nature,如常用见的公司名称:北京/ns, 形意/nz, 达/v, 设计公司/nt=>北京形意达设计公司/nt
	 * 
	 * @param list
	 * @param natureFrom
	 * @param natureTo
	 * @param nsLastMAXLENGTH
	 *            起始到最终发现,可容忍的中间字符个数
	 */
	private static void combineNatureToNature(List<Term> list,
			String natureFrom, String natureTo, int nsLastMAXLENGTH,
			int lastMinLength) {

		boolean firstFound = false;

		Term from = null;
		Term to = null;

		int lastLength = 0;
		// final int nsLastMAXLENGTH=6;

		for (int i = 0; i < list.size(); i++) {
			Term term = list.get(i);

			if (!firstFound) {
				if (term.getNatureStr().equals(natureFrom)) {
					firstFound = true;
					from = term;
					to = null;
					lastLength = 0;
				}
			} else {
				if (term.getNatureStr().equals(natureTo)) {
					// 如果长度超过最小限制,比如 武汉/ns 小太阳/nw 品牌设计/nz ,如果lastMinLength<1,
					// 即变成武汉市品牌设计,此关键词不应该被当作机构名称,而应该是行业名称nd
					if (lastLength < lastMinLength) {
						firstFound = false;
						from = null;
						to = null;
						lastLength = 0;
						continue;
					} else {
						to = term;
						break;
					}
				} else {
					if (isSeparate(term)) {
						firstFound = false;
						from = null;
						to = null;
						lastLength = 0;
						continue;
					} else {
						lastLength += term.getName().length();
						if (lastLength > nsLastMAXLENGTH) {
							firstFound = false;
							from = null;
							to = null;
							lastLength = 0;
							continue;
						}
					}

				}
			}
		}

		if (firstFound && from != null && to != null && from != to) {
			CombineToNT(list, from, to);
			// TODO 检查是否需要这句?是不是应该只执行一次
			// combineNsNt(list);
			combineNatureToNature(list, natureFrom, natureTo, nsLastMAXLENGTH,
					lastMinLength);

		}

	}

	/**
	 * 检查是以 地区开头的机构，如武汉市洪山区标志在线工作室
	 * 
	 * @param term
	 * @return 如果是以地区开头的机构返回true,否则均返回false
	 */
	private static boolean isNsNt(Term term) {
		return (term.getSubTerm() != null && term.getSubTerm().size() > 0 && term
				.getSubTerm().get(0).getNatureStr().equals("ns"));
	}

	/**
	 * 合并连续的结构，为ND__NT设计
	 * 
	 * @param list
	 * @param natureFrom
	 * @param natureTo
	 * @param nsLastMAXLENGTH
	 * @param lastMinLength
	 */
	private static void combineNdToNotNSNT(List<Term> list, String natureFrom,
			String natureTo, int nsLastMAXLENGTH, int lastMinLength) {

		boolean firstFound = false;

		Term from = null;
		Term to = null;

		int lastLength = 0;
		// final int nsLastMAXLENGTH=6;

		for (int i = 0; i < list.size(); i++) {
			Term term = list.get(i);
			if (!firstFound) {
				if (term.getNatureStr().equals(natureFrom)) {
					firstFound = true;
					from = term;
					to = null;
					lastLength = 0;
				}
			} else {
				// 如果是NT,并且开头不为NS,才能合并
				if (term.getNatureStr().equals(natureTo) && !isNsNt(term)) {
					// 如果长度超过最小限制,比如 武汉/ns 小太阳/nw 品牌设计/nz ,如果lastMinLength<1,
					// 即变成武汉市品牌设计,此关键词不应该被当作机构名称,而应该是行业名称nd
					if (lastLength < lastMinLength) {
						firstFound = false;
						from = null;
						to = null;
						lastLength = 0;
						continue;
					} else {
						to = term;
						break;
					}
				} else {
					if (isSeparate(term)) {
						firstFound = false;
						from = null;
						to = null;
						lastLength = 0;
						continue;
					} else {
						lastLength += term.getName().length();
						if (lastLength > nsLastMAXLENGTH) {
							firstFound = false;
							from = null;
							to = null;
							lastLength = 0;
							continue;
						}
					}

				}
			}
		}

		if (firstFound && from != null && to != null && from != to) {
			CombineToNT(list, from, to);
			// TODO 检查是否需要这句?是不是应该只执行一次
			// combineNsNt(list);
			combineNatureToNature(list, natureFrom, natureTo, nsLastMAXLENGTH,
					lastMinLength);

		}

	}

	/**
	 * 现在还不要用,会把"电话"等识别为电话, 将来用以将默认识别为N的词语,如"工作室"转换为NT
	 * 
	 * @deprecated
	 * @param list
	 */
	private static void fixNature(List<Term> list) {

		for (int i = 0; i < list.size(); i++) {

			Term term = (Term) list.get(i);

			if (term.getName().equals("/")) {
				list.get(i).setNature(Nature.NULL);
			}

			if (fixNTSet.contains(term.getName())) {
				list.get(i).setNature(new Nature("nt"));
			} else if (hangyeHouZhuiSet.contains(term.getName())) {
				list.get(i).setNature(new Nature("nd"));
			}

			if (gongsiSet.contains(term.getName())) {
				list.get(i).setNature(new Nature("nt"));
			}

		}

	}

	/*
	 * //这是原来的，有些问题，先注起来 // //倒着检查是否是NT，如 创佳/nz, 律师事务所/nt 合成 创佳律师事务所 /nt public
	 * static void backCombine2NT(List<Term> list, int nsLastMAXLENGTH) {
	 * boolean firstFound = false;
	 * 
	 * Term from = null; Term to = null;
	 * 
	 * int nsLastLength = 0; // final int nsLastMAXLENGTH=6;
	 * 
	 * for (int i = list.size() - 1; i >= 0; i--) { Term term = list.get(i); if
	 * (!firstFound) { // 当前为机构，并且不能是合成的机构，如武汉傲龙教育科技有限公司 if
	 * (term.getNatureStr().equals("nt") && ntMap.containsKey(term.getName())) {
	 * firstFound = true; from = term; to = null; } } else { if
	 * (isSeparate(term)) { if (nsLastLength < nsLastMAXLENGTH) { break; } else
	 * { firstFound = false; from = null; to = null; nsLastLength = 0; continue;
	 * } } else { nsLastLength += term.getName().length(); if (nsLastLength >
	 * nsLastMAXLENGTH) { firstFound = false; from = null; to = null;
	 * nsLastLength = 0; continue; } else { to = term; } } } }
	 * 
	 * if (firstFound && from != null && to != null && from != to) {
	 * CombineToNT(list, to, from); backCombine2NT(list, nsLastMAXLENGTH); } }
	 */

	/**
	 * 倒着检查是否是NT，如 创佳/nz, 律师事务所/nt 合成 创佳律师事务所 /nt
	 * 
	 * @param list
	 * @param nsLastMAXLENGTH
	 */
	public static void backCombine2NT(List<Term> list, int nsLastMAXLENGTH) {
		boolean firstFound = false;

		Term from = null;
		Term to = null;

		int nsLastLength = 0;
		// final int nsLastMAXLENGTH=6;

		for (int i = list.size() - 1; i >= 0; i--) {
			Term term = list.get(i);
			if (!firstFound) {
				// 当前为机构，并且不是以NS地区开头的已合成公司名，如武汉傲龙教育科技有限公司
				if (term.getNatureStr().equals("nt")
						&& !(term.getSubTerm() != null
								&& term.getSubTerm().size() > 0 && term
								.getSubTerm().get(0).getNatureStr()
								.equals("ns"))) {
					firstFound = true;
					from = term;
					to = null;
				}
			} else {
				// 不是分隔符，并且不是“欢迎”等动词
				if (!isSeparate(term)
						&& !(term.getName().equals("的"))  //不能是 的，如“创立的博文教育”，不能把“的”包进去了
						//不能是二个字或以上的动词
						&& !(term.getName().length() >= 2 && term
								.getNatureStr().equals("v"))) {
					nsLastLength += term.getName().length();
					if (nsLastLength > nsLastMAXLENGTH) {
						firstFound = false;
						from = null;
						to = null;
						nsLastLength = 0;
						continue;
					} else {
						to = term;
					}
				} else {
					if (nsLastLength > 0) {
						break;
					} else {
						firstFound = false;
						from = null;
						to = null;
						nsLastLength = 0;
						continue;
					}
				}
			}
		}

		if (firstFound && from != null && to != null && from != to) {
			CombineToNT(list, to, from);
			backCombine2NT(list, nsLastMAXLENGTH);
		}
	}

	/**
	 * 倒着检查是否是NT，如 创佳/nz, 自助烤肉/nd 合成 创佳自助烤肉 /nt
	 * 
	 * @param list
	 * @param nsLastMAXLENGTH
	 */
	public static void backCombineND2NT(List<Term> list, int nsLastMAXLENGTH) {
		boolean firstFound = false;

		Term from = null;
		Term to = null;

		int nsLastLength = 0;
		// final int nsLastMAXLENGTH=6;

		for (int i = list.size() - 1; i >= 0; i--) {
			Term term = list.get(i);
			if (!firstFound) {
				// 当前为行业，并且不是以NS地区开头的已合成公司名，如武汉傲龙教育科技有限公司
				// 现在还还没合成的nd,如 武汉/ns，商标设计/nd=>合成 武汉商标设计/nd ,以后准备添加
				if (term.getNatureStr().equals("nd")
						&& !(term.getSubTerm() != null
								&& term.getSubTerm().size() > 0 && term
								.getSubTerm().get(0).getNatureStr()
								.equals("ns"))) {
					firstFound = true;
					from = term;
					to = null;
				}
			} else {
				// 不是分隔符，并且不是“欢迎”等动词
				// 不能是nt
				if (!isSeparate(term)
						&& term.getName().length() <= 2
						&& !(term.getName().length() >= 2 && (term
								.getNatureStr().equals("v") || term
								.getNatureStr().equals("ad"))) //[更多/ad, 快递/nd]
						&& !term.getNatureStr().equals("nt")
						&& !term.getNatureStr().equals("ns")) {
					nsLastLength += term.getName().length();
					if (nsLastLength > nsLastMAXLENGTH) {
						firstFound = false;
						from = null;
						to = null;
						nsLastLength = 0;
						continue;
					} else {
						to = term;
					}
				} else {
					//向前的长度至少是2，避免出现  "与/p资本运营/nd"=>与资本运营/nt 的情况
					if (nsLastLength > 1) {
						break;
					} else {
						firstFound = false;
						from = null;
						to = null;
						nsLastLength = 0;
						continue;
					}
				}
			}
		}

		if (firstFound && from != null && to != null && from != to) {
			CombineToNT(list, to, from);
			backCombineND2NT(list, nsLastMAXLENGTH);
		}
	}

	/**
	 * 合并分店，如：豆豆佳餐厅(武珞路店), 豆豆佳餐厅武珞路店
	 * 
	 * @param list
	 */
	private static void combineFendian(List<Term> list) {
		final String TAG_FENDIAN = "店";
		// boolean ntFound=false;

		Term ntTerm = null;

		Term lastTerm = null;

		LOOP_NT: for (int i = 0; i < list.size(); i++) {

			Term term = list.get(i);
			// 如果是nt并且不是最后一个
			if (term.getNatureStr().equals("nt") && (i < list.size() - 1)) {
				ntTerm = term;
				boolean startWithQuote = false;

				Term termNext = list.get(i + 1);
				if (termNext.getName().equals("(")
						|| termNext.getName().equals("（"))
					startWithQuote = true;

				int loopStart = startWithQuote ? i + 2 : i + 1;
				// 如果nt后面就只跟了一个括号，视为无效
				if (loopStart > list.size() - 1)
					break;

				// 如果后面以"("开头
				// 分店名称最大长度
				int FENDIAN_MAX_LENGTH = 8;

				int fendianLength = 0;

				LOOP_FENDIAN: for (int j = loopStart; j < list.size(); j++) {
					String termName = list.get(j).getName();

					if (termName.equals(TAG_FENDIAN)
							|| termName.endsWith(TAG_FENDIAN)) {
						lastTerm = list.get(j);
						// 检查后面是否有括号
						if (startWithQuote && (j < list.size() - 1)) {
							Term t2 = list.get(j + 1);
							if (t2.getName().equals(")")
									|| t2.getName().equals("）")) {
								lastTerm = t2;
							}
						}

						break LOOP_NT;

					}

					fendianLength += termName.length();
					// 如果超出分店长度限制或遇到除指定括号分隔符以外的分隔符
					if ((fendianLength > FENDIAN_MAX_LENGTH && lastTerm == null)
							|| isSeparate(list.get(j))) {
						ntTerm = null;
						lastTerm = null;
						break LOOP_FENDIAN;
					}

				}

			}
		}

		if (ntTerm != null && lastTerm != null && ntTerm != lastTerm) {
			JoinTerm(list, ntTerm, lastTerm, TermNatures.NT);
			combineFendian(list);
		}

	}

	/**
	 * 李/nr 经理/n => 李经理/nr 目前用nr1与自动合成的nr（如田鲁攀）相识别，这个是有职位信息的
	 * 
	 * @param list
	 * 
	 */
	private static void recognizeNR(List<Term> list) {
		for (int i = 0; i < list.size(); i++) {
			Term term = list.get(i);
			int termLength = term.getName().length();

			if ((i < list.size() - 1) && (termLength == 1 || termLength == 2)
					&& baijiaxingSet.contains(term.getName())) {
				// 认为是姓，如果后面是称谓，则合并为NR
				Term nextTerm = list.get(i + 1);
				int nextTermLength = nextTerm.getName().length();
				if ((nextTermLength == 1 || nextTermLength == 2)
						&& chenghuSet.contains(nextTerm.getName())) {
					// 可以合并了
					JoinTerm(list, term, nextTerm, new TermNatures(
							new TermNature("nr", 1000)));
					recognizeNR(list);
					break;
				}
			}
		}
		
		//合并 人名 李真真/nr  老师/n =>李真真 老师/nr
		LOOP:
		for(int i=0;i<list.size();i++)
		{
			Term term = list.get(i);
			int termLength = term.getName().length();
			//人名
			if((i<list.size()-1)  && term.getNatureStr().equalsIgnoreCase("nr") &&  termLength>=2 && termLength<=4)
			{
				boolean startWithKuohao=false;
				for(int j=i+1;j<list.size();j++)
				{
					//如果后面是空格是可以的或括号是可以的
					Term term2=list.get(j);
					String termName=term2.getName();
					
					if(chenghuSet.contains(termName))
					{
					         Term endTerm=term2;					
						
						if(startWithKuohao && j<list.size()-1)
						{
							Term termNext=list.get(j+1);
							if(termNext.getName().equalsIgnoreCase(")") || termNext.getName().equalsIgnoreCase("）"))
							{
								endTerm=termNext;
							}
						}
						
						// 可以合并了
						JoinTerm(list, term, endTerm, new TermNatures(
								new TermNature("nr", 1000)));
						recognizeNR(list);
						break LOOP;
					}					
					else if(termName.equals(" "))
					{
					   continue;
					}
					else if(!startWithKuohao &&  (termName.equalsIgnoreCase("(") || termName.equalsIgnoreCase("（")))
					{
						startWithKuohao=true;
						continue;
					}
					else {
						continue LOOP;
					}		
				}
			}
		}
	}

	/**
	 * 识别NW 为机构，前提:nw比需以一个武汉/ns等区域开头，并且长度小于4;本Item长度至少为6
	 * 
	 * @param list
	 * @return
	 */
	private static void regcongnizeNW2NT(List<Term> list) {
		for (Term term : list) {
			if (term.getNatureStr().equals("nw") && term.getName() != null
					&& term.getName().length() >= 6
					&& term.getSubTerm() != null
					&& term.getSubTerm().size() > 1
					&& term.getSubTerm().get(0).getNatureStr().equals("ns")
					&& term.getSubTerm().get(0).getName().length() < 4
					&& !term.getSubTerm().get(1).getNatureStr().equals("ns")) {
				term.setNature(new Nature("nr"));
			}
		}
	}

	public static void fixToAnalysisNT(List<Term> terms) {
		fixToAnalysisNT(terms, 0);
	}

	/**
	 * 修正ToAnalysic中不正常识别的机构，如 广州/ns, 艺术/n, 学院/n => 广州/ns, 艺术学院/nt
	 * 
	 * @param terms
	 */
	private static void fixToAnalysisNT(List<Term> terms, int startTermIndex) {
		// 最大检查长度，20个字符
		final int MAX_CHECK = 20;

		// boolean firstFound=false;

		Term to = null;

		// 遇到nt,nr则认为已经到达边界,或者是分隔符，
		HashSet<String> skipType = new HashSet<String>();
		skipType.add("nt");
		skipType.add("nr");
		skipType.add("ns");

		String combineString = "";

		int toIndex = -1;

		for (int i = startTermIndex; i < terms.size(); i++) {
			Term term = terms.get(i);
			// log("term="+term);
			if (isSeparate(term) || skipType.contains(term.getNatureStr())) {
				// log("term="+term+" skip");
				break;
			} else {
				combineString += term.getName();

				if (combineString.length() > MAX_CHECK) {
					// log(combineString + " is too long");
					break;
				} else if (gongsiSet.contains(combineString)) {
					toIndex = i;
					to = term;
					// log(combineString + " is ok");
				} else {
					// log(combineString + " is not nt");
				}

			}
		}

		if (to != null) {
			Term first = terms.get(startTermIndex);
			// 如果仅一个，则更改类型为nt
			if (to == first) {
				to.setNature(new Nature("nt"));

				if (startTermIndex < terms.size() - 1)
					fixToAnalysisNT(terms, startTermIndex + 1);

			} else {
				// 检查to是第几个
				boolean shouldNexLoop = (toIndex < terms.size() - 1);
				JoinTerm(terms, first, to, TermNatures.NT);
				if (shouldNexLoop)
					fixToAnalysisNT(terms, startTermIndex + 1);
			}
		} else {
			if (startTermIndex < terms.size() - 1)
				fixToAnalysisNT(terms, startTermIndex + 1);
		}
	}

	public static void recognizeNT(List<Term> list) {
		recognizeNT(list, 1);
	}

	// 需要处理 [【/w, 豆豆/nz, 智慧/n, 旅馆/nt, (, 武汉/ns, 工大/j, 路/n, 店/n, ), 】/w

	private static void log(String msg) {
		if (ntDebug) {
			MyStaticValue.Log4j.debug(msg);
		}
	}

	/**
	 * 筛选修复最为可能是真实人名的NR识别<BR />
	 * 标准：人名前是空格（或是行头）、符号、职位视为有效;<BR />
	 * 注意：这个要放在最后，在Parse排名之前;
	 * 
	 * @param terms
	 */
	private static void fixNR(List<Term> terms) {
		// 发现的不太可能是人名的NR更改词性为Nature.NULL
		// final String BAD_NR_CHANGE_TO="n";
		for (int i = 0; i < terms.size(); i++) {
			if (terms.get(i).getNatureStr().equals("nr")) {
				Term prevTerm = i > 0 ? terms.get(i - 1) : null;
				Term nextTerm = (i < terms.size() - 1 ? terms.get(i + 1) : null);
				Term term = terms.get(i);

				// 以联系人：职位开头，直接视为有效；另外如果NR以符号开头，并且不以名词结束也视为有效；
				if(isNrPrevContact(terms, i) || (isNrPrevValid(prevTerm) && isNrNextValid(nextTerm))) {
					// 如果名称是路结尾
					if (!org.ansj.util.TextUtils.isEmpty(term.getName())
							&& (term.getName().endsWith("路") || term.getName()
									.endsWith("街"))) {

						// 如果是古田一路，或者后面是xx路78号
						if ((term.getName().length() > 3)
								|| (nextTerm != null && !isSeparate(nextTerm))) {
							log("不是有效的人名，更改为地名，term=" + term.getName());
							// 注意：不符合规则的更改词性为nr1,防止被解析的时侯当作噪音
							terms.get(i).setNature(new Nature("ns"));
						}
					}
					else {
						// 如果是有效的NR
					}
				}
				else {
					log("不是有效的人名，term=" + term.getName());
					// 注意：不符合规则的更改词性为nr1,防止被解析的时侯当作噪音
					terms.get(i).setNature(new Nature("nr1"));
				}
			}
		}
	}

	/**
	 * 检查人名前面是否为联系人：
	 * 
	 * @param prevTerm
	 * @return
	 */
	private static boolean isNrPrevContact(List<Term> list, int nrIndex) {
		if (nrIndex <= 0)
			return false;
		for (int i = nrIndex - 1; i >= 0; i--) {
			Term prevTerm = list.get(i);
			String text = prevTerm.getName();
			if (org.ansj.util.TextUtils.isEmpty(text) || text.endsWith(":")
					|| text.endsWith("："))
				continue;
			else {

				return isNrJob(prevTerm);
			}
		}
		return false;
	}

	/**
	 * 检查人名前名是否有效，比如是职位，联系人：等
	 * 
	 * @param prevTerm
	 * @return
	 */
	private static boolean isNrPrevValid(Term prevTerm) {
		// 如果是一行的头，视为有效
		if (prevTerm == null)
			return true;
		// 如果是分隔符，视为有效
		if (isSeparate(prevTerm))
			return true;
		// 如果前面是电话号码，或为英文，视为有效，如18065222222田XX
		//如果词性为p,"介词"?，也视为有效
		if (prevTerm.getNatureStr().equalsIgnoreCase("m")
				|| prevTerm.getNatureStr().equalsIgnoreCase("en")
				||prevTerm.getNatureStr().equalsIgnoreCase("p"))
			return true;
		
		//如果是职位或联系人:
		if (isNrJob(prevTerm))
			return true;
		return false;
	}

	private static boolean isNrJob(Term prevTerm) {
		// 如果是职位，视为有效；或者是联系人，联系田xxx 也视为有效
		String[] jobs = { "长", "经理", "总裁", "总", "总监", "助理", "主管", "主任", "师",
				"员", "顾问", "秘书", "出纳", "会计", "人", "联系" };
		for (int i = 0; i < jobs.length; i++) {
			if (!org.ansj.util.TextUtils.isEmpty(prevTerm.getName())
					&& prevTerm.getName().endsWith(jobs[i])) {
				return true;
			}
		}

		return false;
	}

	private static boolean isNrNextValid(Term nextTerm) {
		if (nextTerm != null) {
			if (nextTerm.getNatureStr().equals("n"))
				return false;
		}
		return true;
	}

	public static void recognizeNT(List<Term> list, int method) {

		log("================================");
		log("========     机构，行业识别     ===========");
		log("recognizeNT 初始的list=" + list);

		fixNature(list);

		if (method == 2) {
			fixToAnalysisNT(list);
			log("修正To机构词性=" + list);
		}

		/*
		 * combineNdToNotNSNT(list, "nd", "nt", 4, 0); log("list12A=" +
		 * list.toString());
		 */

		CombineContinusSameTerm(list, "ns");
		log("NSNS=>NS," + list.toString());

		CombineContinusSameTerm(list, "nd");
		log("NDND=>ND," + list.toString());

		CombineContinusSameTerm(list, "nt");
		log("NTNT=>NT," + list.toString());

		combineNsNt(list);
		log("NS______NT=>NT," + list.toString());
		// combineNsNt(list);

		combineNrNt(list);
		log("NR______NT=>NT," + list.toString());

		combineNrfNt(list);
		log("NRF______NT=>NT," + list.toString());

		combineNrND(list);
		log("NR____ND=>NT," + list.toString());

		combineNwNt(list);
		log("list6=" + list.toString());

		combineHeadNT(list);
		log("list7=" + list.toString());
		combineNsNt(list);
		log("list8=" + list.toString());
		combineNwND(list);
		log("list9=" + list.toString());

		combineNsND(list);
		log("list10,NS_ND=>ND," + list.toString());

		CombineContinusSameTerm(list, "nd");
		log("NDND=>ND," + list.toString());

		// 视觉设计/nd, 及/c, 形象/n, 顾问公司/nt
		// ND__NT
		combineNdToNotNSNT(list, "nd", "nt", 4, 0);
		log("list12,ND__NT=>NT," + list.toString());

		CombineContinusSameTerm(list, "nt");
		log("NTNT=>NT," + list.toString());

		// 倒着检查ND,如 创佳/nz, 自助烤肉/nd => 创佳自助烤肉/nt
		backCombineND2NT(list, 5);
		log("list14,backCombineND2NT=" + list.toString());

		// 倒着检查是否是NT，如 创佳/nz, 律师事务所/nt 合成 创佳律师事务所 /nt
		backCombine2NT(list, 5);
		log("list15=" + list.toString());

		combineFendian(list);
		log("NT(__店)=>NT" + list.toString());

		recognizeNR(list);
		log("recognizeNR=" + list.toString());

		// TODO 删除nt ：前面的 联络电话:人事部/nt
		// TODO 优化 合并 商标/n, 设计工作室/nt,

		// TODO 识别 武汉京明不动产/nw
		// 把一些
		regcongnizeNW2NT(list);

		// 去除不正确的NR
		fixNR(list);
		log("fixNR=" + list.toString());
		
		//TODO 北清光大管理顾问(北京)有限公司 

		// 联系我们_同创佳景
		// Contract us... 电话:010-56245004 手机:13311273178 大连公司 同创佳景(大连)营销策划有限公司
		// 地址:地址:大连市西岗区博爱街32号“帅客大酒店旁铜门一层二层”...
		// 大连公司/nt, , 同/p, 创佳/nz, 景/ng, (, 大连/ns, ), 营销策划有限公司/nt,

		// 联系方式 - 英佰视觉
		// [联系方式/nz, , -, , 英佰/nw, 视觉/n,
		// 86-10-56421231 市场部邮箱/enbestbrand@163.com
		// 人力资源邮箱/enbestbrand_hr@163.com 欢迎致电英佰盛视:15101651716 010-56421231
		// 备案号:ICP备095112号 86-...
		// TODO 视觉从hy2表中查询hycount>10的，包括就认为有可能， 英佰/nw+ 视觉/nd

		// 创洁/nw, 教育/vn, 也是一样 nw+nd=nt

		// combineNsNZ(list);
		/*
		 * Term term1=list.get(0); Term term2=list.get(1); Term
		 * term3=list.get(2); Term term4=list.get(3);
		 */

		// CombineToNT(list, term2, term4);

	}

}
