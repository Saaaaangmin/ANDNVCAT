package kr.co.nicevan.androidnvcat.shared;



import android.util.Base64;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;


public class NiceUniQr{

	char[] result_0 = new char[300];
	char[] TLV = new char[1024];
	char FS = 0x1C;
	char[] PAN = new char[1024];
	int TLV_Count = 0;
	int Check_61 = 0;

	public class NICE_QR{
		public String Input_QR = null;
		public String Output_AID_FIRST = null;
		public String Output_PAN = null;
		public String Output_EMV_NICE = null;
		public String Output_TLV = null;
		public String Output_UPLAN = null;
	}

	/**
	 * hex string to byte[]
	 *
	 * @param hex HEX String
	 * @return converted byte array from hex string
	 */
//	 private static byte[] hexToByteArray(String hex) {
//	  hex = hex.replaceAll("\"", "\\\""); /*" */
//	  if (hex == null || hex.length() == 0) {
//	    return null;
//	  }
//	  byte[] ba = new byte[hex.length() / 2];
//	  for (int i = 0; i < ba.length; i++) {
//	    ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
//	  }
//	  return ba;
//	}

	/**
	 * byte[] to hex sting
	 *
	 * @param byteArray byte array
	 * @return converted hex string from byte array
	 */
	public static String byteArrayToHex(byte[] byteArray) {
		if (byteArray == null || byteArray.length == 0) {
			return null;
		}
		StringBuilder stringBuffer = new StringBuilder(byteArray.length * 2);
		String hexNumber;
		for (byte aBa : byteArray) {
			hexNumber = "0" + Integer.toHexString(0xff & aBa).toUpperCase(); //toupperCase()를 추가하면 16진수 대문자 형태.

			stringBuffer.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return stringBuffer.toString();
	}


	public static void Hex2Hex(char[] inputdata, char[] outputdata)
	{
		int k =0;
		int i=0;
		int j=0;

		for(i=0, j=0; inputdata[i] != '\0'; i = i+2)
		{
			k = (16 * Character.getNumericValue(inputdata[i]) + 1 * Character.getNumericValue(inputdata[i+1]));
			outputdata[j] = (char)k;
			j++;
		}
	}


	public int Search_Dic(char[] temp, char[] Nice_Format, char[] Uplan_Code)
	{

		int count = 0;
		int P = 0;

		char[] temp1 = new char[200];
		char[] temp2 = new char[200];
		char[] temp3 = new char[200];
		char[] temp4 = new char[200];
		char[] temp5 = new char[200];
		char[] temp6 = new char[200];
		char[] temp7 = new char[200];

		System.arraycopy(temp, 0, temp1, 0, 2);

		Hex2Hex(temp1, temp2);

		int num = temp2[0] + temp2[1];

		switch(num)
		{
			case 97: //0x61
				if(Check_61 == 0) // 첫 번째 Application Template
				{
					Check_61 = 1;
					System.arraycopy(temp, 2, temp3, 0, 2);
					Hex2Hex(temp3, temp4);
					count = (2 * (temp4[0] + temp4[1]));
					TLV[TLV_Count] = FS;
					System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);// 0x61도 하겠지???
					TLV_Count += count + 4 + 1;

					return 4; //하위 태그로 이어지기 위해
				}
				else // 두 번째 Application Template -> 건너뛰도록 count 리턴.
				{
					System.arraycopy(temp, 2, temp3, 0, 2);
					Hex2Hex(temp3, temp4);
					count = (2 * (temp4[0] + temp4[1]));

					return count + 4 + 1;
				}

			case 79 : //0x4f
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;

				//두 자리 이내 정수값(태그 내 데이터 길이)을 아스키값으로 변환

				temp4[0] = (char)((count / 10) + 48);
				temp4[1] = (char)((count % 10) + 48);

				System.arraycopy(temp4, 0, Nice_Format, 190, 2);
				System.arraycopy(temp, 4, Nice_Format, 192, count);

				return count + 4;

			case 87 : //0x57
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;
				System.arraycopy(temp, 4, PAN, 0, count); //PAN 획득

				for(P=0; P<count; P++)
				{
					if(PAN[P] == 'D')
					{
						PAN[P] = '=';
						break;
					}
				}

				//0x57(PAN 데이터)가 'F'로 끝나지 않으면(데이터 수가 홀수일 경우) F패딩
				if(PAN[count-1] != 'F')
					PAN[count] = 'F';

				return count+4;

			case 95 : //0x5F..  1.5F34 ..// 나이스포맷에 넣어야하나 ???
				System.arraycopy(temp, 2, temp2, 0, 2);

				String str1 = new String(temp2);

				if(str1.startsWith("34"))
				{
					System.arraycopy(temp, 4, temp3, 0, 2);
					Hex2Hex(temp3, temp4);
					count = (2 * (temp4[0] + temp4[1]));
					TLV[TLV_Count] = FS;
					System.arraycopy(temp, 0, TLV, TLV_Count+1, count+6);
					TLV_Count += count + 6 + 1;
					System.arraycopy(temp, 6, Nice_Format, 22, count);

					return count+6;
				}
				else if(str1.startsWith("2D")) //LJY20241008 : TAG (5F>2D) [Language Preference] - EMV데이터 넣을 곳이 없음
				{
					System.arraycopy(temp, 4, temp3, 0, 2);
					Hex2Hex(temp3, temp4);
					count = (2 * (temp4[0] + temp4[1]));
					TLV[TLV_Count] = FS;
					System.arraycopy(temp, 0, TLV, TLV_Count+1, count+6);
					TLV_Count += count + 6 + 1;

					return count+6;
				}
				else
				{
					return -2;
				}

			case 159 : ////0x9f.. 1. 9F60, 2.9F26, 3.9F27, 4.9F10, 5.9F36, 6.9F37 // C++소스에서 case (-97) 동일.
				System.arraycopy(temp, 2, temp5, 0, 2);
				System.arraycopy(temp, 4, temp6, 0, 2);

				Hex2Hex(temp6, temp7);
				count = (2 * (temp7[0] + temp7[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+6);
				TLV_Count += count + 6 + 1;

				//9F60 : Uplan code 부분. 원본 쿠폰번호 형태로 Out하는 것으로 결정!
				String str5 = new String(temp5);
				if(str5.startsWith("60"))
				{
					//LJY20220711 : 네이버페이
					if(Nice_Format[192] == 'A')
					{
						//기입된 원본 쿠폰번호 형태로 출력해야 할 때
						char[] temp_Uplan = new char[100];
						System.arraycopy(temp, 14, temp_Uplan, 0, count-8);// "30303031"(=="0001") 8자리 제외.
						Hex2Hex(temp_Uplan, Uplan_Code);
					}
					else
					{
						System.arraycopy(temp, 6, Uplan_Code, 0, count);
					}

					return count+6;
				}
				else if(str5.startsWith("26"))
				{
					System.arraycopy(temp, 6, Nice_Format, 46, count);

					return count+6;
				}
				else if(str5.startsWith("27"))
				{
					System.arraycopy(temp, 6, Nice_Format, 62, count);

					return count+6;
				}
				else if(str5.startsWith("10"))
				{
					//두 자리 이내 정수값(태그 내 데이터 길이)을 아스키값으로 변환
					temp7[0] = (char)((count / 10) + 48);
					temp7[1] = (char)((count % 10) + 48);

					System.arraycopy(temp7, 0, Nice_Format, 64, 2);
					System.arraycopy(temp, 6, Nice_Format, 66, count);

					return count+6;
				}
				else if(str5.startsWith("36"))
				{
					System.arraycopy(temp, 6, Nice_Format, 138, count);

					return count+6;
				}
				else if(str5.startsWith("37"))
				{
					System.arraycopy(temp, 6, Nice_Format, 130, count);

					return count+6;
				}
				else if(str5.startsWith("08")) //LJY20241008 : TAG (9F>08) [Application Version Number] - AVN
				{
					System.arraycopy(temp, 6, Nice_Format, 224, count);

					return count+6;
				}
				else
				{
					return -2;
				}

			case 99 : //0x63
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;

				return 4; //하위 태그로 이어지기 위해

			case (130) : //0x82 //C++ 소스에서 에서 case (-126) 동일
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;

				System.arraycopy(temp, 4, Nice_Format, 160, count);

				//LJY20241008 : TAG (82) [Application Interchange Profile] - AIP
				//"0008" ~ "0015"이면 카드사 공통QR이며, POS에서 해당값을 보고 카드사 공통QR 구분 가능
				return count+4;

			//Common Data Template //2019.01.07 추가
			case 62 :
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;

				return 4; //하위 태그로 이어지기 위해

			case (223) : //0xDF... DF61 //C++ 소스에서 case (-33) 동일
				System.arraycopy(temp, 2, temp2, 0, 2);

				String str3 = new String(temp2);
				if(str3.startsWith("61"))
				{
					System.arraycopy(temp, 4, temp3, 0, 2);
					Hex2Hex(temp6, temp7);
					count = (2 * (temp4[0] + temp4[1]));
					TLV[TLV_Count] = FS;
					System.arraycopy(temp, 0, TLV, TLV_Count+1, count+6);
					TLV_Count += count + 6 + 1;

					return count+6;
				}
				else
				{
					return -2;
				}

			case (80) : //LJY20241008 : TAG (61>50) [Application Label] - 카드사 공통QR 생성기관 구분용으로 활용
				System.arraycopy(temp, 2, temp3, 0, 2);
				Hex2Hex(temp3, temp4);
				count = (2 * (temp4[0] + temp4[1]));
				TLV[TLV_Count] = FS;
				System.arraycopy(temp, 0, TLV, TLV_Count+1, count+4);
				TLV_Count += count + 4 + 1;

				return count+4;

			default :
				return -2;
		}


	}


	public int Uni_QR(NICE_QR QR_DATA)
	{

		System.out.println("NiceUniQr.jar : NiceUniQr_3.0.0.3_JDK1.8_20241008");

		char[] Decode_Data = new char[1000]; //디코딩된 데이터
		char[] Decode_Data_Temp;
		char[] Nice_Format = new char[258];  //LJY20220711 : 사이즈 변경 257 > 258//NICE QR 전문
		char[] Uplan_Code = new char[100];    //UPLAN 코드
//		char[] Decimal_Data = new char[100];
		char[] Temp = new char[1000];
		int Count = 0;
		int Return_Dic = 0;
		char[] YYMMDD = new char[6];
		char[] AID_FIRST = new char[2];

		//YYMMDD 설정
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Calendar time = Calendar.getInstance();

		String YYMMDD_str = sdf.format(time.getTime());
		YYMMDD = YYMMDD_str.toCharArray();

		//스페이스로 초기화(자동패딩 위해서)
		Arrays.fill(Nice_Format, ' ');
		Nice_Format[257] = (char)0x00; //LJY20220711 : 마지막 널

		//LJY20220711 : 네이버페이 (인풋 널값일 때 예외처리)
		if(QR_DATA.Input_QR == null)
		{
			TLV_Count = 0;
			Check_61 = 0;

			return -2;
		}

		String str1 = QR_DATA.Input_QR;

		byte[] dst = Base64.decode(str1, Base64.DEFAULT);

		String Decode_Data_str = byteArrayToHex(dst);
		Decode_Data_Temp = Decode_Data_str.toCharArray();
		System.arraycopy(Decode_Data_Temp, 0, Decode_Data, 0, Decode_Data_Temp.length);

		//NICE_QR전문 초기 생성
		System.arraycopy("0253123456789012".toCharArray(), 0, Nice_Format, 0, "0253123456789012".length());
		System.arraycopy(YYMMDD, 0, Nice_Format, 16, YYMMDD.length);
		System.arraycopy("01100500001000          ".toCharArray(), 0, Nice_Format, 22, "01100500001000          ".length());
		System.arraycopy("000000080001010100".toCharArray(), 0, Nice_Format, 142, "000000080001010100".length());
		System.arraycopy("000000E0E8C022000000000000".toCharArray(), 0, Nice_Format, 164, "000000E0E8C022000000000000".length());
		System.arraycopy("00208000000000510                ".toCharArray(), 0, Nice_Format, 224, "00208000000000510                ".length());


		//CPV01 버전 확인 + 최상위 템플릿 태그 61 확인
		if(!Decode_Data_str.startsWith("8505435056303161"))
		{
			return -1;
		}

		Count += 14; //  Count == 14


		System.arraycopy(Decode_Data, 0, TLV, TLV_Count, Count);
		TLV_Count += 14;


		/*      반복해야 할 부분    */
		while(! (Decode_Data[Count] == '\0'))
		{
			System.arraycopy(Decode_Data, Count, Temp, 0, Decode_Data_str.length() - Count);

			Return_Dic = Search_Dic(Temp, Nice_Format, Uplan_Code);

			if(Return_Dic<0) //파싱 에러
			{
				return Return_Dic;
			}
			else
				Count += Return_Dic;
		}


		//QR전문에 9F34가 없다면 "12345678" 대입
		if(Nice_Format[130] == 0x20)
		{
			System.arraycopy("12345678".toCharArray(), 0, Nice_Format, 130, "12345678".length());
		}


		//AID 구분자 리턴 기능 추가(Output_AID_FIRST)    //롯데면세점 요청사항 : 2018.12.17
		System.arraycopy(Nice_Format, 192, AID_FIRST, 0, 1);
		//LJY20220711 : 네이버페이
		if((new String(Uplan_Code)).startsWith("4E50"))
		{
			AID_FIRST[0] = 'N';
		}

		QR_DATA.Output_AID_FIRST = new String(AID_FIRST);
		QR_DATA.Output_EMV_NICE = new String(Nice_Format);
		QR_DATA.Output_PAN = new String(PAN);
		QR_DATA.Output_TLV = new String(TLV);
		QR_DATA.Output_UPLAN = new String(Uplan_Code);


		return 1;
	}

	//LJY20220718 : 추가
	public int Uni_QR2(String Input_QR, char[] Output_AID_FIRST, char[] Output_PAN, char[] Output_EMV_NICE, char[] Output_TLV, char[] Output_UPLAN)
	{
		System.out.println("NiceUniQr.jar : NiceUniQr_3.0.0.3_JDK1.8_20220718");

		char[] Decode_Data = new char[1024];  //디코딩된 데이터
		char[] Nice_Format = new char[1024];  //NICE QR 전문
		char[] Uplan_Code = new char[1024];   //UPLAN 코드
		char[] Decimal_Data = new char[1024];
		char[] Temp = new char[1024];
		char[] YYMMDD = new char[8];
		char[] AID_FIRST = new char[2];

		Arrays.fill(Decode_Data, (char)0x00);
		Arrays.fill(Nice_Format, (char)0x00);
		Arrays.fill(Uplan_Code, (char)0x00);
		Arrays.fill(Decimal_Data, (char)0x00);
		Arrays.fill(Temp, (char)0x00);
		Arrays.fill(YYMMDD, (char)0x00);
		Arrays.fill(AID_FIRST, (char)0x00);
		Arrays.fill(PAN, (char)0x00);
		Arrays.fill(TLV, (char)0x00);

		int Count = 0;
		int Return_Dic = 0;

		//YYMMDD 설정
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Calendar time = Calendar.getInstance();
		String YYMMDD_str = sdf.format(time.getTime());
		YYMMDD = YYMMDD_str.toCharArray();

		//스페이스로 초기화(자동패딩 위해서)
		for(int i=0; i<257; i++)
			Nice_Format[i] = ' ';

		//LJY20220711 : 네이버페이 (인풋 널값일 때 예외처리)
		if(Input_QR == null)
		{
			TLV_Count = 0;
			Check_61 = 0;

			return -2;
		}

		String str1 = Input_QR;
		byte[] dst = Base64.decode(str1, Base64.DEFAULT);
		String Decode_Data_str = byteArrayToHex(dst);
		char[] Decode_Data_Temp = Decode_Data_str.toCharArray();
		System.arraycopy(Decode_Data_Temp, 0, Decode_Data, 0, Decode_Data_Temp.length);

		//NICE_QR전문 초기 생성
		System.arraycopy("0253123456789012".toCharArray(), 0, Nice_Format, 0, "0253123456789012".length());
		System.arraycopy(YYMMDD, 0, Nice_Format, 16, YYMMDD.length);
		System.arraycopy("01100500001000          ".toCharArray(), 0, Nice_Format, 22, "01100500001000          ".length());
		System.arraycopy("000000080001010100".toCharArray(), 0, Nice_Format, 142, "000000080001010100".length());
		System.arraycopy("000000E0E8C022000000000000".toCharArray(), 0, Nice_Format, 164, "000000E0E8C022000000000000".length());
		System.arraycopy("00208000000000510                ".toCharArray(), 0, Nice_Format, 224, "00208000000000510                ".length());


		//CPV01 버전 확인 + 최상위 템플릿 태그 61 확인
		if(!Decode_Data_str.startsWith("8505435056303161"))
		{
			return -1;
		}
		Count += 14; //  Count == 14

		System.arraycopy(Decode_Data, 0, TLV, TLV_Count, Count);
		TLV_Count += 14;

		/*      반복해야 할 부분    */
		while(! (Decode_Data[Count] == '\0'))
		{
			System.arraycopy(Decode_Data, Count, Temp, 0, Decode_Data_str.length() - Count);

			Return_Dic = Search_Dic(Temp, Nice_Format, Uplan_Code);
			if(Return_Dic<0) //파싱 에러
				return Return_Dic;
			else
				Count += Return_Dic;
		}

		//QR전문에 9F34가 없다면 "12345678" 대입
		if(Nice_Format[130] == 0x20)
		{
			System.arraycopy("12345678".toCharArray(), 0, Nice_Format, 130, "12345678".length());
		}

		//AID 구분자 리턴 기능 추가(Output_AID_FIRST)    //롯데면세점 요청사항 : 2018.12.17
		System.arraycopy(Nice_Format, 192, AID_FIRST, 0, 1);
		//LJY20220711 : 네이버페이
		if((new String(Uplan_Code)).startsWith("4E50"))
		{
			AID_FIRST[0] = 'N';
		}

		for (int i = 0; i < AID_FIRST.length; i++) {
			if (AID_FIRST[i] == 0x00) {
				System.arraycopy(AID_FIRST, 0, Output_AID_FIRST, 0, i);
				break;
			}
		}

		for (int i = 0; i < Nice_Format.length; i++) {
			if (Nice_Format[i] == 0x00) {
				System.arraycopy(Nice_Format, 0, Output_EMV_NICE, 0, i);
				break;
			}
		}

		for (int i = 0; i < PAN.length; i++) {
			if (PAN[i] == 0x00) {
				System.arraycopy(PAN, 0, Output_PAN, 0, i);
				break;
			}
		}

		for (int i = 0; i < TLV.length; i++) {
			if (TLV[i] == 0x00) {
				System.arraycopy(TLV, 0, Output_TLV, 0, i);
				break;
			}
		}

		for (int i = 0; i < Uplan_Code.length; i++) {
			if (Uplan_Code[i] == 0x00) {
				System.arraycopy(Uplan_Code, 0, Output_UPLAN, 0, i);
				break;
			}
		}

		return 1;
	}
}