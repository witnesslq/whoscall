package com.tianlupan.whoscall;

import java.io.IOException;
import java.util.List;

import com.tianlupan.whoscall.db.IDatabase;
import com.tianlupan.whoscall.db.TempSave;
import com.tianlupan.whoscall.model.NTRecordSet;

import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.tianlupan.whoscall.mining.SearchResult;

public class AnsjServlet {
	
	private final static int FAIL_REASON_NOT_VALID_NUMBER_CODE=101;
	private final static String FAIL_REASON_NOT_VALID_NUMBER="号码不正确";
	
	private final static int ERROR_CODE_ERROR=102;
	private final static String ERROR_CODE_ERROR_MSG="发生错误";
	

	private static String faultMsg(int code,String reason)
	{
		return "{found:false,failReason:"+code+",failMessage:\""+reason+"\"}";
	}
	
	
	interface OnCallback{
		void onResult(String result);
		void onError(int errorCode, String errorMsg);
		void onFinish();
	}

	/**
	 * 用户直接查询号码标记，如面跟着号码，如tellme:18064120000
	 */

	public static void processRequest(String input, int method,OnCallback onCallback) throws IOException {
	
		
		if(TextUtils.isEmpty(input))
		{
			onCallback.onError(FAIL_REASON_NOT_VALID_NUMBER_CODE,FAIL_REASON_NOT_VALID_NUMBER);
		}

		String lowerInput=input.replace("-", "").trim().toLowerCase();
		//电话号码最少五位
		if(lowerInput.length()>=5 && TextUtils.isNumber(lowerInput))
		{
		
			PhoneNumber phoneNumber=new PhoneNumber(input);
			
			if(!phoneNumber.isValid())
			{
				onCallback.onError(FAIL_REASON_NOT_VALID_NUMBER_CODE,FAIL_REASON_NOT_VALID_NUMBER);
			}

			System.out.println("用户查询号码=" + input + " method=" + (method == 1 ? "NLP" : "TO"));
			IDatabase database= TempSave.getInstance();

			String phoneHistory=database.get(lowerInput);
			if(!TextUtils.isEmpty(phoneHistory))
			{
				String nt=database.get(input);
				System.out.println("用户查询号码=" + input + ", 从缓存中返回:" + nt+", 缓存会在服务器重启时清除");
				onCallback.onResult(nt);
				onCallback.onFinish();
			}
			else {
				/*
				ISearchEngine baiduFetch = new BaiduFetch();
				JSONObject jsonObject = baiduFetch.fectchNumber(phoneNumber.getSearchFormat());
				MyStaticValue.Log4j.debug("百度格式化后的json="+jsonObject);
				//System.out.println("result=" + jsonObject.toString());
				String ntResult= NTRecordSet.parseJson(jsonObject,method);			
				MyStaticValue.Log4j.debug("处理后并返回给用户的结果json="+ntResult);
				database.save(phoneNumber.getSearchFormat(), ntResult);
				 
				 */
				SearchResult searchResult=new SearchResult(phoneNumber.getSearchFormat());
				searchResult.loadFromSearchEngine();
				
				 //原来的可以多次chuncked通知
				/*
				onCallback.onResult(searchResult.getPhoneResult().toString());
				
				if(searchResult.isMinableDomain())
					if(searchResult.mine())
						onCallback.onResult(searchResult.getPhoneResult().toString());
				*/
				if(searchResult.isMinableDomain())
					searchResult.mine();
				onCallback.onResult(searchResult.getPhoneResult().toString());
				onCallback.onFinish();
				
				database.save(phoneNumber.getSearchFormat(), searchResult.getPhoneResult().toString());
				
				//return ntResult;
			}

		}
		else if(input.trim().startsWith("{"))
		{
			onCallback.onResult(NTRecordSet.parse(input));
			onCallback.onFinish();
		}
		
		else {
			//此处处理请求，解析字符串
			List<Term> terms =  method==1 ?  NlpAnalysis.parse(input) : ToAnalysis.parse(input);
			//修正或合并，关键点
			//NTUtil.recognizeNT(terms,method);
			if(method==1)
			{
				NTUtil.recognizeNT(terms, method);
			}
			if(method==2)
			{
				new NatureRecognition(terms).recognition(); //词性标注
				NTUtil.recognizeNT(terms,method);
			}
			
			
			if (terms != null) {
				onCallback.onResult(terms.toString());
				onCallback.onFinish();
			}
			else
				onCallback.onError(ERROR_CODE_ERROR,ERROR_CODE_ERROR_MSG);
			
		}
	}

}
