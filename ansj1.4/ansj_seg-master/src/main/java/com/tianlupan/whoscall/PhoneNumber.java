package com.tianlupan.whoscall;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumber {

	// 用于匹配手机号码
	private final static String REGEX_MOBILEPHONE = "^0?1[34578]\\d{9}$";

	// 用于匹配固定电话号码
	private final static String REGEX_FIXEDPHONE = "^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$";

	// 用于获取固定电话中的区号
	private final static String REGEX_ZIPCODE = "^(010|02\\d|0[3-9]\\d{2})\\d{6,8}$";
	
	//用于保存热线电话
	private final static String REGEX_HOTLINE="^(4|8)00\\d{7}$";

	// 区配特殊,120 12315 95555等
	private final static String REGEX_SPECIALPHONE = "^(110|120|119|114|117|1\\d{4}|9\\d{4})$";

	private static Pattern PATTERN_MOBILEPHONE;
	private static Pattern PATTERN_FIXEDPHONE;
	private static Pattern PATTERN_ZIPCODE;
	private static Pattern PATTERN_SPECIALPHONE;
	private static Pattern PATTERN_HOTLINE;

	static {
		PATTERN_FIXEDPHONE = Pattern.compile(REGEX_FIXEDPHONE);
		PATTERN_MOBILEPHONE = Pattern.compile(REGEX_MOBILEPHONE);
		PATTERN_ZIPCODE = Pattern.compile(REGEX_ZIPCODE);
		PATTERN_SPECIALPHONE = Pattern.compile(REGEX_SPECIALPHONE);
		PATTERN_HOTLINE=Pattern.compile(REGEX_HOTLINE);
	}

	public static enum PhoneType {
		/**
		 * 手机
		 */
		CELLPHONE,

		/**
		 * 固定电话
		 */
		FIXEDPHONE,
		
		/**
		 * 企业热线，400或800
		 */
		HOTLINE,

		/**
		 * 特殊号码，比如110,10000,12315,12345,119,120,95588
		 */
		SPECIALPHONE,
		/**
		 * 非法格式号码
		 */
		INVALIDPHONE
	}

	private PhoneType type;
	/**
	 * 如果是手机号码，则该字段存储的是手机号码 前七位；如果是固定电话，则该字段存储的是区号
	 */
	private String code;
	private String number;

	private String searchFormat;

	private Long dbFormat = -1L;

	public PhoneNumber(String _number) {

		if (TextUtils.isEmpty(_number)) {
			throw new IllegalArgumentException("手机号码不能为空");
		}

		String number = _number;

		if (number.contains("-")) {
			number = number.replace("-", "");
		}

		if (number != null && number.length() > 0) {
			if (isCellPhone(number)) {
				// 如果手机号码以0开始，则去掉0
				if (number.charAt(0) == '0') {
					number = number.substring(1);
				}

				create(PhoneType.CELLPHONE, number.substring(0, 7), number);
			} else if (isFixedPhone(number)) {
				// 获取区号
				String zipCode = getZipFromHomephone(number);
				create(PhoneType.FIXEDPHONE, zipCode, number);
			}
			else if(isHotLinePhone(number))
			{
				create(PhoneType.HOTLINE, null, _number);
			}
			else if (isSpecialPhone(number)) {
				create(PhoneType.SPECIALPHONE, null, _number);
			} else {
				create(PhoneType.INVALIDPHONE, null, _number);
			}
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if(getType()==PhoneType.INVALIDPHONE)
			return false;
		

		if (obj instanceof PhoneNumber) {
			PhoneNumber phoneNumber = (PhoneNumber) obj;
			
			if(phoneNumber.getType()==PhoneType.INVALIDPHONE)
				return false;

			if (phoneNumber.getDbFormat().equals(getDbFormat()))
				return true;
			
			final int MIN_LENGTH = 7;
			// 如果后面几位一致，也认为是一样的
			if (!TextUtils.isEmpty(phoneNumber.getNumber())
					&& !TextUtils.isEmpty(getNumber())
					&& phoneNumber.getNumber().length() >= MIN_LENGTH
					&& getNumber().length() >= MIN_LENGTH) {
				if (phoneNumber.getNumber().length() > getNumber().length() && phoneNumber.getNumber().endsWith(getNumber()))
						return true;
				else if ( getNumber().length()  >phoneNumber.getNumber().length() && getNumber().endsWith(phoneNumber.getNumber()))
						return true;
				else return false;
			}
		}

		return false;

	}

	private void create(PhoneType _type, String _code, String _number) {
		this.type = _type;
		this.code = _code;
		this.number = _number;

		createFormat();

	}

	private void createFormat() {

		switch (type) {
		case CELLPHONE:
			searchFormat = number;
			dbFormat = Long.valueOf(number);
			break;
		case FIXEDPHONE:
			if (code != null) {
				searchFormat = code + "-" + number.substring(code.length());
				// 去掉开头的0
				dbFormat = Long.valueOf(number.substring(0));

			} else {
				// TODO 此处应该根据网络等信息自动添加用户的固话区号
				searchFormat = number;
				dbFormat = Long.valueOf(searchFormat);
			}
			break;
		case HOTLINE:
			searchFormat=number;
			dbFormat=Long.valueOf(number);
		case SPECIALPHONE:
			searchFormat = number;
			dbFormat = Long.valueOf(number);
			break;
		case INVALIDPHONE:
			searchFormat = "";
			dbFormat = -1L;
		default:
			break;
		}

	}

	public PhoneType getType() {
		return type;
	}

	public boolean isValid() {
		return type != PhoneType.INVALIDPHONE;
	}

	public String getCode() {
		return code;
	}

	public String getNumber() {
		return number;
	}
	
	public String getFixedNumberWithoutCode(){
		if(type!=PhoneType.FIXEDPHONE)
			return null;
		if(!TextUtils.isEmpty(code))
			return number.substring(code.length());
		else return number;		
	}

	/**
	 * 用以在百度搜索的格式类型，如027-86642326 18064129666 95555等
	 * 
	 * @return
	 */
	public String getSearchFormat() {
		return searchFormat;
	}

	/**
	 * 用以保存在Mongodb中的数据格式，Long类型，去掉开头的0
	 * 
	 * @return
	 */
	public Long getDbFormat() {
		return dbFormat;
	}

	public String toString() {
		return String.format("[number:%s, type:%s, code:%s]", number,
				type.name(), code);
	}

	/**
	 * 判断是否为手机号码
	 * 
	 * @param number
	 *            手机号码
	 * @return
	 */
	private boolean isCellPhone(String number) {
		Matcher match = PATTERN_MOBILEPHONE.matcher(number);
		return match.matches();
	}

	private boolean isSpecialPhone(String number) {
		Matcher match = PATTERN_SPECIALPHONE.matcher(number);
		return match.matches();
	}

	/**
	 * 判断是否为固定电话号码
	 * 
	 * @param number
	 *            固定电话号码
	 * @return
	 */
	private boolean isFixedPhone(String number) {
		Matcher match = PATTERN_FIXEDPHONE.matcher(number);
		return match.matches();
	}
	
	
	private boolean isHotLinePhone(String number)
	{
		return PATTERN_HOTLINE.matcher(number).matches();
	}

	/**
	 * 获取固定号码号码中的区号
	 * 
	 * @param strNumber
	 * @return
	 */
	private String getZipFromHomephone(String strNumber) {
		Matcher matcher = PATTERN_ZIPCODE.matcher(strNumber);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}
	
	public static void main(String[] args) {
		PhoneNumber phoneNumber=new PhoneNumber("42326");
		System.out.println(phoneNumber.getFixedNumberWithoutCode());
	}

}
