package com.ktds.erpbarcode.barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.media.BarcodeSoundPlay;
import com.ktds.erpbarcode.common.widget.SpinnerInfo;


public class SuportLogic {
	
	public static String getPartType(String partTypeCode, String devType) {
		if (devType.equals("30")) {
			return "E";
		} else if (partTypeCode.equals("10")) {
			return "R";
		} else if (partTypeCode.equals("20")) {
			return "S";
		} else if (partTypeCode.equals("30")) {
			return "U";
		} else if (partTypeCode.equals("40")) {
			return "N";
		}
		return "";
	}
	
	public static String getPartTypeCode(String partType) {
    	String jobGubun = GlobalData.getInstance().getJobGubun();

		if (partType.equals("R"))
			return "10";
		else if (partType.equals("S"))
			return "20";
		else if (partType.equals("U"))
			return "30";
		else if (partType.equals("E")) {
			if (!jobGubun.startsWith("현장점검") && !jobGubun.equals("인계") && !jobGubun.equals("인수") && !jobGubun.equals("시설등록") && !jobGubun.equals("바코드대체요청") && !jobGubun.equals("부외실물등록요청")) return "99";       // 단품일 경우에는 부품종류가 null 로 정의되어 있기 때문에 SAP에서 처리 시 오류 발생 가능성이 있습니다. 이에 유동처리(탈장, 실장, 입고, 출고, 송부, 접수) 시 단품일 경우에는 부품종류(PARTTYPE) 를 '99' 로 지정하여 SAP 로 I/F 해 주시기 바랍니다. 처리 완료시 Test를 위해 Feedback 부탁 드리겠습니다. request by 강준석 2012.02.20
			return "";
		}
			
		else
//			return "N/A";
			return "";
//		TODO. N/A or "" matsua 17.04.03
	}
	
	public static String getNodeStringType(String val) {
        if (val.equals("L")) {
        	return "위치";
        } else if (val.equals("D")) {
        	return "장치";
        } else if (val.equals("R")) {
        	return "Rack";
        } else if (val.equals("S")) {
        	return "Shelf";
        } else if (val.equals("U")) {
        	return "Unit";
        } else if (val.equals("E")) {
        	return "";
        }
        return "";
    }
	
	public static String getDevTypeName(String devType) {
		if (devType.equals("10"))
			return "대표자재";
		else if (devType.equals("20"))
			return "조립품";
		else if (devType.equals("30"))
			return "단품";
		else if (devType.equals("40"))
			return "부품";
		else if (devType.equals("50"))
			return "케이블";
		else
			return "N/A";
	}

	public static List<SpinnerInfo> initFacStatusArray(String jobGubun) {
    	
		List<SpinnerInfo> items = new ArrayList<SpinnerInfo>();
    	
		if (jobGubun.equals("설비상태변경")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
			items.add(new SpinnerInfo("0070", "미운용"));
			items.add(new SpinnerInfo("0200", "불용대기"));
		} else if (jobGubun.equals("입고(팀내)")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
			items.add(new SpinnerInfo("0200", "불용대기"));
		} else if (jobGubun.equals("접수(팀간)")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
		} else if (jobGubun.equals("수리완료")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
			items.add(new SpinnerInfo("0200", "불용대기"));
		} else if (jobGubun.equals("개조개량완료")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
		} else if (jobGubun.equals("고장등록취소")) {
			items.add(new SpinnerInfo("0100", "유휴"));
			items.add(new SpinnerInfo("0110", "예비"));
			items.add(new SpinnerInfo("0070", "미운용"));
			items.add(new SpinnerInfo("0200", "불용대기"));
		}
		return items;
    }
	
	public static String getFACStatusCode(String name) {
		Map<String, String> facStatus = new HashMap<String, String>();
		
		facStatus.put("납품입고",    "0020");
		facStatus.put("납품취소",    "0021");
		facStatus.put("인수예정",    "0040");
		facStatus.put("시설등록완료", "0045");
		facStatus.put("인계완료",    "0050");
		facStatus.put("운용",       "0060");
		facStatus.put("미운용",      "0070");
		facStatus.put("탈장",       "0080");

		facStatus.put("유휴",       "0100");
		facStatus.put("예비",       "0110");
		facStatus.put("고장",       "0120");
		facStatus.put("수리의뢰",    "0130");
		facStatus.put("이동중",      "0140");

		facStatus.put("수리완료송부", "0160");
		facStatus.put("개조의뢰",    "0170");
		facStatus.put("개조완료송부", "0171");

		facStatus.put("철거확정",    "0190");
		facStatus.put("불용대기",    "0200");
		facStatus.put("불용요청",    "0210");

		facStatus.put("불용확정",    "0240");
		facStatus.put("사용중지",    "0260");
		facStatus.put("출고중",      "0270");
		facStatus.put("분실위험",    "0081");

		// 추가 - request by 김두영 2012.06.04
		facStatus.put("인계작업취소",   "0041");
		facStatus.put("인수거부",      "0042");
		facStatus.put("시설등록취소",   "0046");

		// 삭제대상
		facStatus.put("투입(출고)",    "0010");
		facStatus.put("교체요청",      "0030");
		facStatus.put("실장중",       "0090");
		facStatus.put("수리진행",      "0150");
		facStatus.put("철거중",       "0180");
		facStatus.put("불용품",       "0220");
		facStatus.put("불용반려",      "0230");
		facStatus.put("손망실",       "0250");
		
		String facStatusCode = "";
		try {
			facStatusCode = facStatus.get(name);
		} catch (Exception e) {
			facStatusCode = "";
		}
		
		if (facStatusCode == null) {
			facStatusCode = "";
		}
		
		return facStatusCode;
	}
	
	public static String getFACStatusName(String code) {
		Map<String, String> facStatus = new HashMap<String, String>();
		
		facStatus.put("0020", "납품입고");
		facStatus.put("0021", "납품취소");
		facStatus.put("0040", "인수예정");
		facStatus.put("0045", "시설등록완료");
		facStatus.put("0050", "인계완료");
		facStatus.put("0060", "운용");
		facStatus.put("0070", "미운용");
		facStatus.put("0080", "탈장");

		facStatus.put("0100", "유휴");
		facStatus.put("0110", "예비");
		facStatus.put("0120", "고장");
		facStatus.put("0130", "수리의뢰");
		facStatus.put("0140", "이동중");

		facStatus.put("0160", "수리완료송부");
		facStatus.put("0170", "개조의뢰");
		facStatus.put("0171", "개조완료송부");

		facStatus.put("0190", "철거확정");
		facStatus.put("0200", "불용대기");
		facStatus.put("0210", "불용요청");

		facStatus.put("0240", "불용확정");
		facStatus.put("0260", "사용중지");
		facStatus.put("0270", "출고중");
		facStatus.put("0081", "분실위험");

		// 추가 - request by 김두영 2012.06.04
		facStatus.put("0041", "인계작업취소");
		facStatus.put("0042", "인수거부");
		facStatus.put("0046", "시설등록취소");

		// 삭제대상
		facStatus.put("0010", "투입(출고)");
		facStatus.put("0030", "교체요청");
		facStatus.put("0090", "실장중");
		facStatus.put("0150", "수리진행");
		facStatus.put("0180", "철거중");
		facStatus.put("0220", "불용품");
		facStatus.put("0230", "불용반려");
		facStatus.put("0250", "손망실");
		
		String facStatusName = "";
		try {
			facStatusName = facStatus.get(code);
		} catch (Exception e) {
			facStatusName = "";
		}
		
		if (facStatusName == null) {
			facStatusName = "";
		}
		
		return facStatusName;
	}

    // 설비상태 체크하여 바코드 스캔시 Validation
    public static void chkZPSTATU(String ZPSTATU, String ZDESC, String submt) throws ErpBarcodeException
    {
        /*
        "0020", "납품입고" -> 불가
        "0021", "납품취소" -> 불가
        "0040", "인수예정" -> 불가
        "0045", "시설등록완료" -> 불가
        "0050", "인계완료" -> 불가
        "0060", "운용"
        "0070", "미운용"
        "0080", "탈장"
        "0100", "유휴"
        "0110", "예비"
        "0120", "고장" -> 불가
        "0130", "수리의뢰" -> 불가
        "0140", "이동중" -> 불가
        "0160", "수리완료송부" -> 불가 by 김소연 -> 가능 by 정진우 
        "0170", "개조의뢰" -> 불가
        "0171", "개조완료송부"
        "0190", "철거확정"
        "0200", "불용대기"
        "0210", "불용요청" -> 불가
        "0230", "불용반려"
        "0240", "불용확정"-> 불가
        "0260", "사용중지" -> 불가
        "0270", "출고중" -> 불가 by 김소연 -> 가능 by 정진우 
        "0081", "분실위험"
         
        // 추가 - request by 김두영 2012.06.04
        "0041", "인계작업취소"
        "0042", "인수거부"
        "0046", "시설등록취소"
         
        // 삭제대상
        "0010", "투입(출고)"
        "0030", "교체요청"
        "0090", "실장중"
        "0150", "수리진행"
        "0180", "철거중" -> 이런 상태값도 있나요? 삭제대상 상태입니다.(철거확정 상태값 존재합니다.)
        "0220", "불용품"
        "0250", "손망실" -> 이런 상태값도 있나요? 삭제대상 상태입니다.(손망실은 불용사유 중 하나일 뿐, 불용 관련 상태값은 불용대기,불용요청,불용확정 중 하나일 뿐입니다.)
         * **/

    	String jobGubun = GlobalData.getInstance().getJobGubun();
        boolean errorFlag = false;
        if (jobGubun.equals("설비정보") || jobGubun.equals("장치바코드정보") || jobGubun.equals("장치바코드하위설비조회") || ZPSTATU == null)               // 설비정보조회, 장치바코드하위설비조회는 무조건 통과
        {
        }
        else if (ZPSTATU.equals("0240") || ZPSTATU.equals("0260") || ZPSTATU.equals("0021"))      // 불용확정, 사용중지, 납품취소 무조건 오류 처리
        {
        	String message = "설비의 상태가 '" + ZDESC + "'인 설비는\n\r스캔 하실 수 없습니다.";
            if (ZPSTATU.equals("0240") && isGbic0240(submt)) 
            {
                message = "광모듈(GBIC) 설비 '" + submt + "' (은)는\n\r바코드 비관리 대상으로 결정되어\n\r'불용확정' 상태로\n\r변경되었습니다.";
        	}
        	throw new ErpBarcodeException(-1, message, BarcodeSoundPlay.SOUND_ERROR);
        }
        else if(ZPSTATU.equals("0140") && jobGubun.equals("인스토어마킹완료")){
        	String message = "상태가 '이동중'인 설비가 존재합니다.\n'이동중'상태 설비는 인스토어마킹\n완료후 '송부취소(팀간)'\n또는 접수(팀간)'\n SCAN을 수행하시기 바랍니다.";
        	throw new ErpBarcodeException(-1, message, BarcodeSoundPlay.SOUND_ERROR);
        }
        else if (ZPSTATU.equals("0140") && !jobGubun.equals("접수(팀간)") && !jobGubun.equals("송부취소(팀간)")) {
        	String message = "설비의 상태가 '" + ZDESC + "'인 설비는\n\r'접수(팀간)' 또는 '송부취소(팀간)'\n\r메뉴를 사용하시기 바랍니다.";
        	throw new ErpBarcodeException(-1, message, BarcodeSoundPlay.SOUND_ERROR);
        } 
        else if (jobGubun.equals("인계") || jobGubun.equals("시설등록"))
        {
            String[] arrZPSTATU = { "0120", "0130", "0140", "0160", "0170", "0171", "0200", "0210" };
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("고장등록"))
        {
            String[] arrZPSTATU = { "0020", "0040", "0045", "0050", "0120", "0130", "0140", "0160", "0170", "0171", "0210"};
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("고장등록취소"))
        {
            errorFlag = (!ZPSTATU.equals("0120"));
        }
        else if (jobGubun.equals("수리의뢰") || jobGubun.equals("개조개량의뢰"))
        {
            String[] arrZPSTATU = { "0020", "0040", "0120", "0130", "0140", "0170", "0190", "0210"};
            //						납품입고, 인수예정, 고장, 수리의뢰, 이동중, 개조의뢰, 철거확정, 불용요청
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("수리의뢰취소"))
        {
            errorFlag = (!ZPSTATU.equals("0130"));
        }
        else if (jobGubun.equals("개조개량의뢰취소"))
        {
            errorFlag = (!ZPSTATU.equals("0170"));
        }
        else if (jobGubun.equals("철거"))
        {
            String[] arrZPSTATU = { "0020", "0120", "0140", "0210", "0040", "0045", "0050"};
            //        납품입고, 고장, 이동중, 불용요청, 인수예정, 시설등록완료, 인계완료
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("배송출고"))
        {
            String[] arrZPSTATU = { "0060", "0190", "0210"};
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("설비상태변경"))
        {
            String[] arrZPSTATU = { "0020", "0040", "0041", "0050", "0120", "0130", "0140", "0160", "0170", "0250" };
            errorFlag = getZPSTATUErrorFlag(ZPSTATU, arrZPSTATU);
        }
        else if (jobGubun.equals("송부취소(팀간)"))
        {
            errorFlag = (!ZPSTATU.equals("0140"));
        }

        if (errorFlag)
        {
        	throw new ErpBarcodeException(-1, "설비의 상태가 '" + ZDESC + "'인 설비는\n\r'" + jobGubun + "' 작업을\n\r하실 수 없습니다.");
        }
    }

    public static boolean getZPSTATUErrorFlag(String ZPSTATU, String[] arrZPSTATU) {
        for (String val : arrZPSTATU)
        {
            if (ZPSTATU.equals(val))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean validationBarcode(String barcode) {
		if (barcode.indexOf(":") >= 0 
				|| barcode.indexOf("'") >= 0 
				|| barcode.indexOf("`") >= 0 
				|| barcode.indexOf("\"") >= 0 
				|| barcode.indexOf("*") >= 0 
				|| barcode.length() < 16 ) {
			return false;
		}
		return true;
	}
    
    /**
     * 광모듈 바코드들 체크..
     * @param barcode
     * @return
     */
    public static boolean isGbicBarcodes(String barcode) {
    	String[] arr_gbic_arcodes = {"K9094596","K9022543","K9052361","K9059332","K9061175","K9061178","K9061179","K9073765","K9073766","K9073767","K9075259","K9075265","K9075266","K9075267","K9076820","K9076821","K9076825","K9076826","K9076828","K9076829","K9076830","K9076837","K9076840","K9076841","K9076842","K9076843","K9075137","K9085400","K9085401","K9085453","K9085454","K9085455","K9085456","K9085457","K9085458","K9085459","K9085460","K9085461","K9085462","K9085463","K9085347","K9085385","K9085386","K9088021","K9088022","K9088023","K9089811","K9089812","K9089813","K9089814","K9088287","K9088289","K9088290","K9088291","K9088292","K9091744","K9092536","K9092538","K9092539","K9093156","K9093157","K9093158","K9093159","K9093160","K9093161","K9092676","K9092022","K9092023","K9092025","K9092795","K9092796","K9092797","K9092798","K9092799","K9092852","K9092853","K9092854","K9092855","K9092856","K9092857","K9092858","K9092859","K9092860","K9092861","K9092862","K9092863","K9092206","K9092207","K9092208","K9092209","K9092210","K9092211","K9092212","K9092213","K9092214","K9092215","K9092882","K9093003","K9093004","K9093006","K9093008","K9093009","K9093010","K9093011","K9093012","K9095952","K9094323","K9096158","K9096159","K9094024","K9094822","K9094823","K9098240","K9098241","K9098243","K9098252","K9098258","K9098260","K9098264","K9097759","K9097760","K9098281","K9098283","K9098304","K9098305","K9098326","K9098327","K9098329","K9097848","K9097849","K9097850","K9097851","K9097852","K9097853","K9097854","K9097855","K9097856","K9097857","K9097858","K9097861","K9097862","K9097863","K9097864","K9097865","K9097873","K9097874","K9097875","K9097885","K9097887","K9097888","K9097889","K9097890","K9097891","K9097892","K9097893","K9098382","K9098383","K9098384","K9098385","K9098387","K9096767","K9096768","K9096769","K9096771","K9096772","K9096774","K9096775","K9096779","K9096780","K9096782","K9097897","K9097901","K9097905","K9097910","K9097912","K9097913","K9097914","K9097916","K9097918","K9097919","K9097920","K9098417","K9098423","K9098425","K9097923","K9097924","K9097925","K9097977","K9097980","K9098064","K9098077","K9098078","K9098079","K9098080","K9098081","K9098082","K9098083","K9098084","K9098085","K9098086","K9098087","K9098088","K9098089","K9098090","K9098091","K9098092","K9098093","K9098094","K9098095","K9098096","K9098097","K9098098","K9098099","K9098100","K9098101","K9098102","K9098103","K9098104","K9098105","K9098106","K9098107","K9098108","K9098109","K9098132","K9098135","K9098136","K9098137","K9098138","K9098139","K9097639","K9097644","K9098172","K9097658","K9097660","K9097661","K9097662","K9097664","K9097666","K9097668","K9097669","K9097670","K9097671","K9097673","K9097679","K9097681","K9097682","K9098174","K9098179","K9098190","K9117521","K9117523","K9117525","K9117526","K9117527","K9117529","K9117531","K9117538","K9117548","K9117549","K9117558","K9117560","K9117561","K9117623","K9117624","K9119579","K9119580","K9119587","K9119590","K9119096","K9121147","K9121158","K9121159","K9121161","K9122057","K9121361","K9122059","K9122060","K9121403","K9121404","K9121405","K9121406","K9121414","K9121415","K9121416","K9121417","K9121442","K9121443","K9121444","K9121448","K9121449","K9121450","K9121451","K9121452","K9121453","K9121457","K9121458","K9121459","K9121465","K9121466","K9120283","K9121129","K9095504","K9095505","K9095507","K9096759","K9096760","K9094071","K9094072","K9097451","K9097431","K9085220","K9085219","K9096758","K9092794","K9052320","K9085798","K9126337","K9056732","K9009823","K9092695","K9122559","K9095507","K9077881","K9085058","K9090073","K9089773","K9097927","K9097928","K9118716","K9090958","K9092016","K9175247","K9174435","K9089772","K9077882","K9089774","K9089684","K9096753","k9096736","K9085811","K9175323","K9175047","K9127564","K9127565","K9098170","K9098266","K9089896","K9175310","K9096758","K9118717","K9159045","K9159319","K9160676","K9161966","K9162368","K9168191","K9168999","K9056430","K9053767","K9087446","K9088253","K9096280","K9175240","K9077604","K9162622","K9009375","K9085795","K9093793","K9119315","K9179942", "K9179945", "K9179950"};
    	for (String _gbic_code : arr_gbic_arcodes) {
    		if (_gbic_code.equals(barcode)) {
        		return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 불용확정 바코드들 체크
     * @param barcode
     * @return
     */
    public static boolean isGbic0240(String barcode) {
    	String[] arr_gbic_0240 = {"K9022543","K9052361","K9059332","K9061175","K9061178","K9061179","K9073765","K9073766","K9073767","K9075259","K9075265","K9075266","K9075267","K9076820","K9076821","K9076825","K9076826","K9076828","K9076829","K9076830","K9076837","K9076840","K9076841","K9076842","K9076843","K9075137","K9085400","K9085401","K9085453","K9085454","K9085455","K9085456","K9085457","K9085458","K9085459","K9085460","K9085461","K9085462","K9085463","K9085347","K9085385","K9085386","K9088021","K9088022","K9088023","K9089811","K9089812","K9089813","K9089814","K9088287","K9088289","K9088290","K9088291","K9088292","K9091744","K9092536","K9092538","K9092539","K9093156","K9093157","K9093158","K9093159","K9093160","K9093161","K9092676","K9092022","K9092023","K9092025","K9092795","K9092796","K9092797","K9092798","K9092799","K9092852","K9092853","K9092854","K9092855","K9092856","K9092857","K9092858","K9092859","K9092860","K9092861","K9092862","K9092863","K9092206","K9092207","K9092208","K9092209","K9092210","K9092211","K9092212","K9092213","K9092214","K9092215","K9092882","K9093003","K9093004","K9093006","K9093008","K9093009","K9093010","K9093011","K9093012","K9095952","K9094323","K9096158","K9096159","K9094024","K9094822","K9094823","K9098240","K9098241","K9098243","K9098252","K9098258","K9098260","K9098264","K9097759","K9097760","K9098281","K9098283","K9098304","K9098305","K9098326","K9098327","K9098329","K9097848","K9097849","K9097850","K9097851","K9097852","K9097853","K9097854","K9097855","K9097856","K9097857","K9097858","K9097861","K9097862","K9097863","K9097864","K9097865","K9097873","K9097874","K9097875","K9097885","K9097887","K9097888","K9097889","K9097890","K9097891","K9097892","K9097893","K9098382","K9098383","K9098384","K9098385","K9098387","K9096767","K9096768","K9096769","K9096771","K9096772","K9096774","K9096775","K9096779","K9096780","K9096782","K9097897","K9097901","K9097905","K9097910","K9097912","K9097913","K9097914","K9097916","K9097918","K9097919","K9097920","K9098417","K9098423","K9098425","K9097923","K9097924","K9097925","K9097977","K9097980","K9098064","K9098077","K9098078","K9098079","K9098080","K9098081","K9098082","K9098083","K9098084","K9098085","K9098086","K9098087","K9098088","K9098089","K9098090","K9098091","K9098092","K9098093","K9098094","K9098095","K9098096","K9098097","K9098098","K9098099","K9098100","K9098101","K9098102","K9098103","K9098104","K9098105","K9098106","K9098107","K9098108","K9098109","K9098132","K9098135","K9098136","K9098137","K9098138","K9098139","K9097639","K9097644","K9098172","K9097658","K9097660","K9097661","K9097662","K9097664","K9097666","K9097668","K9097669","K9097670","K9097671","K9097673","K9097679","K9097681","K9097682","K9098174","K9098179","K9098190","K9117521","K9117523","K9117525","K9117526","K9117527","K9117529","K9117531","K9117538","K9117548","K9117549","K9117558","K9117560","K9117561","K9117623","K9117624","K9119579","K9119580","K9119587","K9119590","K9119096","K9121147","K9121158","K9121159","K9121161","K9122057","K9121361","K9122059","K9122060","K9121403","K9121404","K9121405","K9121406","K9121414","K9121415","K9121416","K9121417","K9121442","K9121443","K9121444","K9121448","K9121449","K9121450","K9121451","K9121452","K9121453","K9121457","K9121458","K9121459","K9121465","K9121466","K9120283","K9121129","K9095504","K9095505","K9095507","K9179942", "K9179945", "K9179950"};

    	for (String _gbic_240 : arr_gbic_0240) {
    		if (_gbic_240.equals(barcode)) {
        		return true;
    		}
    	}
    	return false;
    }
}
