package com.ktds.erpbarcode.common.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.ktds.erpbarcode.SessionUserData;
import com.ktds.erpbarcode.common.ErpBarcodeException;

public class HttpAddressConfig {
	//-----------------------------------------------------------
    // 서버 Host 종류.
	//-----------------------------------------------------------
	public static final String APP_SERVER = "app-server";
	public static final String QA_SERVER = "qa-server";
	
	public static final String APP_DOWNLOAD_URL = "http://erpbarcode.kt.com/nbase";
	public static final String QA_DOWNLOAD_URL = "http://nbaseqa.kt.com/nbase";
	
	
	//-----------------------------------------------------------
    // 프로젝트명
	//-----------------------------------------------------------
	public static final String PROJECT_ERPBARCODE = "nbase";
	

	//-----------------------------------------------------------
	// GET
	//-----------------------------------------------------------
	public static final String PATH_GET_LOGIN = "login";
	public static final String PATH_GET_LOGOUT = "logout";
	public static final String PATH_GET_LOGIN_MAKE_CERTIFICATION = "Login_Make_Certification";   // SMS 본인인증
	public static final String PATH_GET_PROGRAM_VERSION = "ProgramVersion";              // 프로그램 버젼.
	
	public static final String PATH_GET_LOC_CHECK = "locCheck";
	public static final String PATH_GET_LOC_ADDNAME = "Location_addNm";					 // 위치주소검색
	public static final String PATH_GET_LOC_WM_CHECK = "LOC_WM_Check";                   // 물류센터 전용 위치바코드 권한 체크 ( 출고는 제외 ) - ZMMO_LOCATION_USER_CHECK
	public static final String PATH_GET_MULTIINFO = "MultiInfo";
	public static final String PATH_GET_REMOVAL_WBS = "Removal_WBS";                     // 철거 WBS - ZPMN_REMOVAL_WBS_SEARCH
	public static final String PATH_GET_WBS = "WBS";                                     // WBS   - ZPMN_TAKEOVER_04
	
	public static final String PATH_GET_FACINFO_INQUERY = "FacInfoInquery";
	public static final String PATH_GET_ITEMINFO = "아이템정보";
	public static final String PATH_GET_SURVEYMASTER = "자재마스터";                        // 자재마스터 조회.
	public static final String PATH_GET_ASSET = "ASSET";                                 // 자산분류 조회.
	public static final String PATH_GET_NOTICE = "공지사항";                               // 공지사항 조회.
	
	public static final String PATH_GET_ORGTREE_SEARCH = "OrgTreeSearch";                // 최상위 운용조직 목록 조회.
	public static final String PATH_GET_ORGNAME_SEARCH = "OrgNameSearch";                // 운용조직명으로 조회.
	public static final String PATH_GET_DEPTMOVEREQUEST = "DeptMoveRequest";             // 부서간 이동 조회.
	public static final String PATH_GET_LOGICAL_LOCATION_CODE = "LogicalLocationCode";   // 논리위치바코드(창고위치) 조회.
	public static final String PATH_GET_FACINFOINQUERY_DETAIL = "FacInfoInquery_Detail"; // 설비정보 상세 조회.
	public static final String PATH_GET_SPOTCHECK_GETLOCCODEBYDEVICEID = "SpotCheckGetLoccodeByDeviceId"; // 현장점검 장치아이디 하위 설비의 위치코드 리스트 가져오기.
	public static final String PATH_GET_SPOTCHECKGETSERIAL = "SpotCheckGetSerial";       // 현장점검 serial 번호 가져오기
	public static final String PATH_GET_GETPLANT = "GetPlant";                           // 플랜트 얻기 - ZMMO_PLANT_SEARCH
	public static final String PATH_GET_GETSL = "GetSL";                                 // 저장위치 얻기 - ZMMO_PLANT_SL_SEARCH
	public static final String PATH_GET_GETIBLNR = "GetIBLNR";                           // 실사문서 얻기 - ZMMO_IBLNR_SEARCH
	public static final String PATH_GET_GETPRODUCTSURVEYLIST = "GetProductSurveyList";   // 실사자재 얻기 - ZMMO_ZEILI_SEARCH
	public static final String PATH_GET_GETPRODUCTSURVEYSCANLIST = "GetProductSurveyScanList";  // 실사 스캔 리스트 얻기 - ZMMO_PHYSINV_BARCODE_DISPLAY 
	
	public static final String PATH_GET_TRANSFERSCAN = "TransferScan";                   // 인계 - ZPMN_TAKEOVER_03
	public static final String PATH_GET_ARGUMENTSCAN = "ArgumentScan";                   // 인수 - ZPMN_TAKEOVER_21
	public static final String PATH_GET_ARGUMENTSCAN_CONFIRM = "ArgumentScan_Confirm";   // 인수확정 조회  - ZPMN_TAKEOVER_15
	public static final String PATH_GET_INSTCONFSCAN = "InstConfScan";                   // 시설등록 조회 - ZPMN_TAKEOVER_21
	public static final String PATH_GET_DEVICEIDBELOWFACLIST = "DeviceIdBelowFacList";   // 장치바코드 하위 설비 중 운용이면서 유닛 제외 리스트 가져오기
	public static final String PATH_GET_INSTOREMARKING = "InstoreMarking";               // 인스토어마킹 정보 조회.
	public static final String PATH_GET_IM_COMPLETESCAN = "IM_CompleteScan";             // 인스토어마킹 완료 조회
	
	
	public static final String PATH_GET_OUTINTOSTATUSCHANGELIST = "OutIntoStatusChangeList";    // 납품취소 리스트
	public static final String PATH_GET_OUTINTOSTATUSCHANGECHECK = "OutIntoStatusChangeCheck";  // 납품취소 가능 여부 리스트
	public static final String PATH_GET_FACINFOINQUERY_MM = "FacInfoInquery_MM";
	public static final String PATH_GET_GETOUTOFSERVICELIST = "GetOutOfServiceList";     // 고장정보조회 - ZPMN_REPAIR_EQUIP_DETAIL
	public static final String PATH_GET_GETREPAIRRECEIPT_IM = "GetRepairReceipt_IM";   	 //수리완료접수_인스토어마킹조회
	public static final String PATH_GET_GETREPAIRRECEIPTLIST = "GetRepairReceiptList";   // 수리완료정보조회 - ZPMN_REPAIR_EQUIP_DETAIL
	

	public static final String PATH_GET_RMDLIMPR_RQFORM_NEW = "RmdlImpr_rqForm_New";     // 개조개량의뢰스캔대상 바코드 정보 ZPMN_UPGRADE_MATNR_INFO
	public static final String PATH_GET_RMDLIMPR_CPFORM_NEW = "RmdlImpr_cpForm_New";     // 개조개량완료스캔대상 바코드 정보 ZPMN_UPGRADE_COMPLETE_DATA	
	public static final String PATH_GET_FACINFO_INQUERY_SPOTCHECK = "FacInfoInquerySpotCheck";	// 현장점검용 설비정보조회
	public static final String PATH_GET_GBIC_LIST = "gbic_list";	// gbic data list 조회
	public static final String PATH_GET_FAILURE_LIST = "failure_list";	// failure list 조회
	public static final String PATH_GET_BREAKDOWN_CHECK = "breakDown_check";	// 고장등록 가능여부 조회
	
	//-----------------------------------------------------------
	// POST
	//-----------------------------------------------------------
	public static final String PATH_POST_LOGIN_SEND_CERTIFICATION = "Login_Send_Certification";   // SMS 인증번호 전송
	
	
	public static final String PATH_POST_SPOT_CHECK = "SpotCheck";  	                 // 현장점검 전송
	public static final String PATH_POST_INVENTORYSURVEYSEND = "InventorySurveySend";    // 임대단말실사전송
	public static final String PATH_POST_PRODUCTSURVEYSEND = "ProductSurveySend";        // 상품단말실사전송
	
	public static final String PATH_POST_BUYOUTINTO = "BuyOutInto";
	public static final String PATH_POST_OUTINTOSTATUS_CHANGE = "OutIntoStatusChange";
	public static final String PATH_POST_EQUIPMENTASSEMBLING = "EquipmentAssembling";
	public static final String PATH_POST_EQUIPMENTPUT = "EquipmentPut";
	public static final String PATH_POST_DELIVERY_MM = "Delivery_MM";					 // 배송출고
	public static final String PATH_POST_STATUSCHANGE = "StatusChange";
	public static final String PATH_POST_OUTINTO = "OutInto";
	public static final String PATH_POST_DELIVERY = "Delivery";
	public static final String PATH_POST_UNMOUNT = "Unmount";                            // 탈장
	public static final String PATH_POST_SECTION_CHIEF = "SectionChief";                 // 실장처리
	public static final String PATH_POST_SEND_DEVICE_NEW = "SendDevice_NEW";             // 송부스캔 신규
	public static final String PATH_POST_SEND_DEVICE_CANCEL = "SendDeviceCancel";        // 송부취소(팀간)
	public static final String PATH_POST_RECEPTSCAN_NEW = "ReceptScan_NEW";              // 접수스캔 신규
	
	public static final String PATH_POST_TRANSFERSCAN = "PostTransferScan";              // 인계스캔
	public static final String PATH_POST_ARGUMENTSCAN_SENDMAIL = "PostArgumentScan_SendMail";	// 재스캔요청
	public static final String PATH_POST_ARGUMENTSCAN = "PostArgumentScan";              // 인수스캔
	public static final String PATH_POST_INSTCONFSCAN = "PostInstConfScan";              // 시설등록
	
	public static final String PATH_POST_ARGUMENTSCAN_CONFIRM = "PostArgumentScan_Confirm";  // 인수, 시설등록확정 전송
	
	public static final String PATH_POST_TRANSFERSCAN_DELETE = "TransferScan_Delete";    // ERP 재스캔 데이터 삭제
	
	// 고장/수리
	public static final String PATH_POST_OOSREG = "OOSReg";								 // 고장등록
	public static final String PATH_POST_OOSFILEUPLOAD = "OOSFuleUpload";				 // 고장등록 이미지 저장
	public static final String PATH_POST_OUTOFSERVICECANCEL = "OutOfServiceCancel";	     // 고장등록취소 - ZPM_BARCODE_DATA_CREATE
	public static final String PATH_POST_OUTOFSERVICECANCEL2 = "OutOfServiceCancel2";	 // 수리의뢰취소 - ZPM_BARCODE_DATA_CREATE
	public static final String PATH_POST_REPAIRRECEIPT_IM = "PostRepairReceipt_IM";
	public static final String PATH_POST_REPAIRRECEIPT = "PostRepairReceipt";	 		 // 수리완료

	// 개조개량
	public static final String PATH_POST_REMODELIMPROV_RQ_NEW = "PostRemodelImprov_RQ_New";	// 개조개량의뢰,개조개량의뢰취소
	public static final String PATH_POST_REMODELIMPROV_CP_NEW = "PostRemodelImprov_CP_New";	// 개조개량완료
	
	public static final String PATH_POST_REMOVALSCAN = "RemovalScan";					 // 철거
	
	// 인스토어마킹
	public static final String PATH_POST_INSTOREMARKING = "Post_InstoreMarking";         // 바코드대체요청
	public static final String PATH_POST_IM_COMPLETESCAN = "Post_IM_CompleteScan";       // 인스토어마킹완료
	
	//설비S/N변경
	public static final String PATH_POST_SERGE_CHANGE = "Post_Serge_change"; // 설비S/N변경 
	//비밀번호 변경 
	public static final String PATH_POST_PASSWORD_CHANGE = "Post_Password_change"; // 비밀번호 변경
	
	//바코드 관리 - 인스토어마킹관리
	public static final String PATH_POST_ISM_PRINT_STATUS = "Post_Ism_PrintStatus"; 		// 인스토어마킹관리 - 진행상태
	public static final String PATH_POST_ISM_PRINT_PBLS_WHY = "Post_Ism_PrintPblsWhy"; 		// 인스토어마킹관리 - 요청사유
	public static final String PATH_POST_ISM_PRINT_INS_DEL = "Post_Ism_PrintInsDel"; 		// 인스토어마킹관리 - 요청취소사유
	public static final String PATH_POST_ISM_PRINT_LABEL_TP = "Post_Ism_PrintLabelTp"; 		// 공통 - 라벨용지
	public static final String PATH_POST_ISM_PRT_INS_SEARCH = "Post_Ism_PrintSearch"; 		// 인스토어마킹관리 - 조회
	public static final String PATH_POST_ISM_PRT_INS_CANCEL = "Post_Ism_PrintCancel"; 		// 인스토어마킹관리 - 취소
	public static final String PATH_POST_ISM_PRT_INS_GENERATE = "Post_Ism_PrintGenerate"; 	// 인스토어마킹관리 - 발행
	public static final String PATH_POST_ISM_PRT_INS_REPUBLISH = "Post_Ism_PrintRepublish"; // 인스토어마킹관리 - 재발행
	public static final String PATH_POST_ISM_PRN_STATUS = "Post_Ism_PrintStatusMod";		// 인스토어마킹관리 - 출력상태등록
	
	//바코드 관리 - 위치코드 
	public static final String PATH_POST_BARCODE_SIDO = "Post_BarcodeSido"; 				// 위치코드 - 시도
	public static final String PATH_POST_BARCODE_SIGOON = "Post_BarcodeSigoon"; 			// 위치코드 - 시군 
	public static final String PATH_POST_BARCODE_DONG = "Post_BarcodeDong"; 				// 위치코드 - 읍면동 
	public static final String PATH_POST_BARCODE_LOC_REQ = "Post_BarcodeLocRequest"; 		// 위치코드 - 검색
	
	//바코드 관리 - 장치코드 
	public static final String PATH_POST_BARCODE_DEVICE_REQ = "Post_BarcodeDeviceRequest"; 	// 장치코드 - 검색
	public static final String PATH_POST_BARCODE_USER_INFO = "Post_BarcodeUserInfo"; 	// 장치코드 - 사용자조회 
	
	//바코드 관리 - 소스마킹 
	public static final String PATH_POST_BARCODE_SM_REQ = "Post_BarcodeSmRequest"; 			// 소스마킹 - 검색
	
	//비밀번호 변경 
	public static final String PATH_POST_REQUEST_USERAUTH = "Post_RequestUserAuth"; 	// 비밀번호 변경 인증번호 요청 
	public static final String PATH_POST_CONFIRM_USERAUTH = "Post_Confirm_UserAuth"; 	// 비밀번호 변경 인증번호 확인  
	public static final String PATH_POST_SAVE_PASSWORD = "Post_Save_Password"; 			// 비밀번호 변경 저장 
	
	public static final String PATH_POST_LOGIN_DEV_INFO = "login_devide_info";			//중복로그인 방지 최종 접속기기 정보 저장 
	
	//형상구성 추가
	public static final String PATH_POST_FORM_MAKE = "Post_FormMake";                 	// 형상구성(창고내)
	public static final String PATH_POST_FORM_CLEAR = "Post_FormClear";                 // 형상해제(창고내)

	private WebServer mServer;
	private String mHost;
	private String mProject;
	private String mPath;
	
	private HashMap<String, String> mPathInfos;
	
	public HttpAddressConfig(String project, String path) {
		//-----------------------------------------------------------
		// 서버 선택.
		//-----------------------------------------------------------
		if (SessionUserData.getInstance().getAccessServer().isEmpty()) {
			SessionUserData.getInstance().setAccessServer(QA_SERVER);
		}
			
		choiceServer(SessionUserData.getInstance().getAccessServer());
		
		mHost = getHostAddress();
		mProject = project;
		initPathMap();
		mPath = getPathMap(path);
	}
	
	private void choiceServer(String server) {
		if      (server.equals(APP_SERVER))  {mServer = new WebServer("http", "erpbarcodepda.kt.com", 0); }  // 운영서버
		else if (server.equals(QA_SERVER))   {mServer = new WebServer("http", "nbaseqa.kt.com", 0); }  // QA서버	
	}

	public WebServer getServer() {
		return mServer;
	}
	
	public String getHostAddress() {
		String hostAddress;
		if (mServer.getPort() == 0) {
			hostAddress = mServer.getProtocol() + "://" + mServer.getHost();
		} else {
			hostAddress = mServer.getProtocol() + "://" + mServer.getHost() + ":" + mServer.getPort();
		}
		return hostAddress;
	}

	public class WebServer {
		private String protocol;
	    private String host;
	    private int port;
	    
	    public WebServer(String protocol, String host, int port) {
	    	this.protocol = protocol;
	    	this.host = host;
	    	this.port = port;
	    }

		public String getProtocol() {
			return protocol;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}
	}

	private void initPathMap() {
		mPathInfos = new HashMap<String, String>();
		
		//-----------------------------------------------------------
		// GET
		//-----------------------------------------------------------
		mPathInfos.put(PATH_GET_LOGIN, mProject + "/user/pda/login.json");
		mPathInfos.put(PATH_GET_LOGOUT, mProject + "/user/pda/logout.json");
		mPathInfos.put(PATH_GET_LOGIN_MAKE_CERTIFICATION, mProject + "/user/confirmation/no/make.json");
		mPathInfos.put(PATH_GET_PROGRAM_VERSION, mProject + "/pda/program/download/info/get.json");
		
		mPathInfos.put(PATH_GET_LOC_CHECK, mProject + "/based/location/info/get.json");
		mPathInfos.put(PATH_GET_LOC_WM_CHECK, mProject + "/based/location/user/check/get.json");
		mPathInfos.put(PATH_GET_LOC_ADDNAME, mProject + "/based/location/load/addr/get.json");       //위치코드 주소명 검색
		
		mPathInfos.put(PATH_GET_MULTIINFO, mProject + "/deviceId/info/by/pda/get.json");
		mPathInfos.put(PATH_GET_FACINFO_INQUERY, mProject + "/operation/facility/list/get.json");
		mPathInfos.put(PATH_GET_FACINFO_INQUERY_SPOTCHECK, mProject + "/operation/facility/listSpotCheck/get.json");
		mPathInfos.put(PATH_GET_ITEMINFO, mProject + "/based/item/info/list/pda/get.json");
		mPathInfos.put(PATH_GET_REMOVAL_WBS, mProject + "/based/wbs/wbsRemolish/get.json");
		mPathInfos.put(PATH_GET_WBS, mProject + "/based/wbs/list/get.json");
		mPathInfos.put(PATH_GET_SURVEYMASTER, mProject + "/based/item/interface/list/pda/get.json");
		mPathInfos.put(PATH_GET_ASSET, mProject + "/based/asset/classification/list/by/pda/get.json");
		mPathInfos.put(PATH_GET_NOTICE, mProject + "/board/pda/notice/list/get.json");
		
		mPathInfos.put(PATH_GET_ORGTREE_SEARCH, mProject + "/based/organization/list/get.json");
		mPathInfos.put(PATH_GET_ORGNAME_SEARCH, mProject + "/based/organization/autocomplete/list/get.json");
		mPathInfos.put(PATH_GET_DEPTMOVEREQUEST, mProject + "/movement/request/dept/movement/list/get.json");
		mPathInfos.put(PATH_GET_LOGICAL_LOCATION_CODE, mProject + "/based/location/storage/list/pda/get.json");
		mPathInfos.put(PATH_GET_FACINFOINQUERY_DETAIL, mProject + "/operation/facility/detail/list/get.json");
		mPathInfos.put(PATH_GET_SPOTCHECK_GETLOCCODEBYDEVICEID, mProject + "/operation/spot/check/devid/loc/list/get.json");
		mPathInfos.put(PATH_GET_SPOTCHECKGETSERIAL, mProject + "/operation/manage/spot/check/by/pda/get.json");
		mPathInfos.put(PATH_GET_GETPLANT, mProject + "/movement/plant/search/list/get.json");
		mPathInfos.put(PATH_GET_GETSL, mProject + "/movement/lgort/search/list/get.json");
		mPathInfos.put(PATH_GET_GETIBLNR, mProject + "/survey/doc/no/list/get.json");
		mPathInfos.put(PATH_GET_GETPRODUCTSURVEYLIST, mProject + "/survey/list/get.json");
		mPathInfos.put(PATH_GET_GETPRODUCTSURVEYSCANLIST, mProject + "/survey/detail/list/get.json");
		
		mPathInfos.put(PATH_GET_TRANSFERSCAN, mProject + "/construction/before/transition/list/get.json");
		mPathInfos.put(PATH_GET_ARGUMENTSCAN, mProject + "/construction/after/transition/list/get.json");
		mPathInfos.put(PATH_GET_ARGUMENTSCAN_CONFIRM, mProject + "/construction/argument/list/get.json");
		mPathInfos.put(PATH_GET_INSTCONFSCAN, mProject + "/construction/after/transition/list/get.json");
		mPathInfos.put(PATH_GET_DEVICEIDBELOWFACLIST, mProject + "/operation/devid/low/fac/list/get.json");
		mPathInfos.put(PATH_GET_INSTOREMARKING, mProject + "/barcode/facility/info/instore/get.json");
		
		mPathInfos.put(PATH_GET_OUTINTOSTATUSCHANGELIST, mProject + "/operation/deliver/facility/list/get.json");
		mPathInfos.put(PATH_GET_OUTINTOSTATUSCHANGECHECK, mProject + "/operation/deliver/facility/status/check/get.json");
		mPathInfos.put(PATH_GET_FACINFOINQUERY_MM, mProject + "/operation/facility/mm/list/get.json");
		mPathInfos.put(PATH_GET_GETOUTOFSERVICELIST, mProject + "/repair/request/document/list/get.json");
		mPathInfos.put(PATH_GET_GETREPAIRRECEIPT_IM, mProject + "/operation/repair/barcode/get.json");	// 수리완료접수_인스토어마킹조회
		mPathInfos.put(PATH_GET_GETREPAIRRECEIPTLIST, mProject + "/operation/repair/facility/get.json");	// 수리완료정보조회 - ZPMN_REPAIR_EQUIP_DETAIL

        
		mPathInfos.put(PATH_GET_RMDLIMPR_RQFORM_NEW, mProject + "/construction/convert/instructions/request/scan/barcode/info/get.json");     // 개조개량의뢰스캔대상 바코드 정보 ZPMN_UPGRADE_MATNR_INFO
		mPathInfos.put(PATH_GET_RMDLIMPR_CPFORM_NEW, mProject + "/construction/convert/instructions/finsh/scan/barcode/info/get.json");       // 개조개량완료스캔대상 바코드 정보 ZPMN_UPGRADE_COMPLETE_DATA
		mPathInfos.put(PATH_GET_IM_COMPLETESCAN, mProject + "/barcode/generation/new/get.json");       // 인스토어마킹 완료 조회
		mPathInfos.put(PATH_GET_GBIC_LIST, mProject + "/based/item/gbic/list/get.json");       // gbic list 조회 
		mPathInfos.put(PATH_GET_FAILURE_LIST, mProject + "/repair/history/list/get.json");		// failure list 조회  
		mPathInfos.put(PATH_GET_BREAKDOWN_CHECK, mProject + "/repair/control/breakdown/get.json"); //고장등록 가능여부 조회 
		
		//-----------------------------------------------------------
		// POST
		//-----------------------------------------------------------
		mPathInfos.put(PATH_POST_LOGIN_DEV_INFO, mProject + "/user/setLoginDevInfo.json");
		mPathInfos.put(PATH_POST_LOGIN_SEND_CERTIFICATION, mProject + "/user/confirmation/date/mod.json");
		
		mPathInfos.put(PATH_POST_SPOT_CHECK, mProject + "/operation/spot/check/reg.json");
		mPathInfos.put(PATH_POST_INVENTORYSURVEYSEND, mProject + "/movement/physical/count/reg.json");
		mPathInfos.put(PATH_POST_PRODUCTSURVEYSEND, mProject + "/survey/reg.json");
		
		mPathInfos.put(PATH_POST_BUYOUTINTO, mProject + "/delivery/enter/stock/exec.json");
		mPathInfos.put(PATH_POST_OUTINTOSTATUS_CHANGE, mProject + "/operation/deliver/facility/status/mod.json");
		mPathInfos.put(PATH_POST_EQUIPMENTASSEMBLING, mProject + "/operation/equipment/assembling/pda/reg.json");
		mPathInfos.put(PATH_POST_EQUIPMENTPUT, mProject + "/operation/equipment/input/pda/reg.json");
		mPathInfos.put(PATH_POST_DELIVERY_MM, mProject + "/operation/movement/release/otd/exec.json");
		mPathInfos.put(PATH_POST_STATUSCHANGE, mProject + "/operation/facility/status/modify.json");
		mPathInfos.put(PATH_POST_OUTINTO, mProject + "/operation/enter/stock/exec.json");
		mPathInfos.put(PATH_POST_DELIVERY, mProject + "/operation/movement/release/exec.json");
		mPathInfos.put(PATH_POST_UNMOUNT, mProject + "/operation/unmount/exec.json");
		mPathInfos.put(PATH_POST_SECTION_CHIEF, mProject + "/operation/mount/facility/reg.json");
		mPathInfos.put(PATH_POST_SEND_DEVICE_NEW, mProject + "/movement/scan/dept/movement/send.json");
		mPathInfos.put(PATH_POST_SEND_DEVICE_CANCEL, mProject + "/movement/scan/cancel/dept/movement/send.json");
		mPathInfos.put(PATH_POST_RECEPTSCAN_NEW, mProject + "/movement/scan/receipt/dept/movement/send.json");
		
		mPathInfos.put(PATH_POST_TRANSFERSCAN, mProject + "/construction/before/transition/execute.json");
		mPathInfos.put(PATH_POST_ARGUMENTSCAN_SENDMAIL, mProject + "/construction/request/rescan/by/pda/trs.json");	// 재스캔요청
		mPathInfos.put(PATH_POST_ARGUMENTSCAN, mProject + "/construction/after/transition/execute.json");
		mPathInfos.put(PATH_POST_INSTCONFSCAN, mProject + "/construction/install/confirm/reg.json");
		
		mPathInfos.put(PATH_POST_TRANSFERSCAN_DELETE, mProject + "/construction/delete/transition/execute.json");
		mPathInfos.put(PATH_POST_ARGUMENTSCAN_CONFIRM, mProject + "/construction/save/argument/decision/reg.json");
		
		mPathInfos.put(PATH_POST_OOSREG, mProject + "/repair/oos/reg.json");									// 고장등록
		mPathInfos.put(PATH_POST_OOSFILEUPLOAD, mProject + "/fac/file/upload.up");								// 고장등록 이미지 저장
		
		mPathInfos.put(PATH_POST_OUTOFSERVICECANCEL, mProject + "/repair/oos/cancel.json");						// 고장등록 취소
		mPathInfos.put(PATH_POST_OUTOFSERVICECANCEL2, mProject + "/repair/request/document/cancel.json");		// 수리의뢰취소
		mPathInfos.put(PATH_POST_REPAIRRECEIPT_IM, mProject + "/barcode/generation/complete/scan/reg.json");	// 수리완료 인스토어마킹 전송
		mPathInfos.put(PATH_POST_REPAIRRECEIPT, mProject + "/operation/repair/facility/reg.json");		        // 수리완료 전송

		mPathInfos.put(PATH_POST_REMODELIMPROV_RQ_NEW, mProject + "/construction/convert/instructions/request/scan/reg.json");	    // 개조개량의뢰, 개조개량의뢰취소
		mPathInfos.put(PATH_POST_REMODELIMPROV_CP_NEW, mProject + "/construction/convert/instructions/complete/scan/pda/reg.json");	// 개조개량완료

		mPathInfos.put(PATH_POST_REMOVALSCAN, mProject + "/movement/remolish/reg.json");						// 철거
		mPathInfos.put(PATH_POST_INSTOREMARKING, mProject + "/barcode/generation/request/reg.json");			// 바코드대체요청
		mPathInfos.put(PATH_POST_IM_COMPLETESCAN, mProject + "/barcode/generation/complete/scan/reg.json");		// 인스토어마킹완료스캔
		
		mPathInfos.put(PATH_POST_SERGE_CHANGE, mProject + "/operation/serial/number/mod.json");		// 설비S/N변경
		mPathInfos.put(PATH_POST_PASSWORD_CHANGE, mProject + "/operation/passwd/mod.json");			//비밀번호 변경 
		
		mPathInfos.put(PATH_POST_ISM_PRINT_STATUS, mProject + "/based/code/cache/list/type/BC_TRA_ST/get.json");				//프린트연동 추가 - 진행상태
		mPathInfos.put(PATH_POST_ISM_PRINT_PBLS_WHY, mProject + "/based/code/cache/list/type/PBLS_WHY/get.json");				//프린트연동 추가 - 요청사유
		mPathInfos.put(PATH_POST_ISM_PRINT_INS_DEL, mProject + "/based/code/cache/list/type/INS_DEL/get.json");					//프린트연동 추가 - 요청취소사유
		mPathInfos.put(PATH_POST_ISM_PRINT_LABEL_TP, mProject + "/based/code/cache/list/type/LABEL_TP/REF_VAL2/Y/get.json");	//프린트연동 추가 - 라벨용지
		mPathInfos.put(PATH_POST_ISM_PRT_INS_SEARCH, mProject + "/barcode/generation/list/get.json");		//인스토어마킹관리 - 조회
		mPathInfos.put(PATH_POST_ISM_PRT_INS_CANCEL, mProject + "/barcode/generation/remove.json");			//인스토어마킹관리 - 조회
		mPathInfos.put(PATH_POST_ISM_PRT_INS_GENERATE, mProject + "/barcode/generation/generate.json");		//인스토어마킹관리 - 발행
		mPathInfos.put(PATH_POST_ISM_PRT_INS_REPUBLISH, mProject + "/barcode/generation/republish.json");	//인스토어마킹관리 - 재발행
		mPathInfos.put(PATH_POST_ISM_PRN_STATUS, mProject + "/barcode/generation/status/publish/mod.json");	//인스토어마킹관리 - 출력상태변경
		
		mPathInfos.put(PATH_POST_BARCODE_SIDO, mProject + "/based/location/list/addr/sido/get.json");					// 위치코드 - 시도
		mPathInfos.put(PATH_POST_BARCODE_SIGOON, mProject + "/based/location/list/addr/siGoon/get.json");				// 위치코드 - 시군 
		mPathInfos.put(PATH_POST_BARCODE_DONG, mProject + "/based/location/autocomplete/legal/dong/list/get.json");		// 위치코드 - 읍면동 
		mPathInfos.put(PATH_POST_BARCODE_LOC_REQ, mProject + "/based/location/info/list/get.json");		// 위치코드 - 검색
		mPathInfos.put(PATH_POST_BARCODE_DEVICE_REQ, mProject + "/deviceId/list/get.json");		// 장치코드 - 검색
		mPathInfos.put(PATH_POST_BARCODE_USER_INFO, mProject + "/pda/user/list/get.json");		// 장치코드 - 사용자조회
		mPathInfos.put(PATH_POST_BARCODE_SM_REQ, mProject + "/barcode/sourcemarking/list/po/no/get.json");		// 소스마킹 - 검색	
		
		mPathInfos.put(PATH_POST_REQUEST_USERAUTH, mProject + "/user/password/confirmation/no/make.json");			//비밀번호 변경 인증번호 요청 
		mPathInfos.put(PATH_POST_CONFIRM_USERAUTH, mProject + "/user/checkCertificationNumber.json");				//비밀번호 변경 인증번호 확인 
		mPathInfos.put(PATH_POST_SAVE_PASSWORD, mProject + "/user/password/by/user/executePasswordUpdate.json");	//비밀번호 저장
		
		mPathInfos.put(PATH_POST_FORM_MAKE, mProject + "/operation/mount/facility/formMake.json");		// 형상구성(창고내)
		mPathInfos.put(PATH_POST_FORM_CLEAR, mProject + "/operation/mount/facility/formClear.json");	// 형상해제(창고내)
		
	}
	
	private String getPathMap(String pathName) {
		return mPathInfos.get(pathName);
	}
	
	public boolean isUrlAddress() {
		boolean isSuccess = true;
		try {
			getUrlAddress();
		} catch (ErpBarcodeException e) {
			isSuccess = false;
		}
		return isSuccess;
	}
	
	public String getUrlAddress() throws ErpBarcodeException {
		String urlString = mHost + "/" + mPath;
		try {
			URL u = new URL(urlString);
			urlString = u.toString() + "?securityYN=Y&call=ANDROID";
		} catch (MalformedURLException e) {
			throw new ErpBarcodeException(-1, "URL 생성중 오류(MalformedURLException)가 발생했습니다. ");
		}
		return urlString;
	}
	
	public String getNotSecurityUrlAddress() throws ErpBarcodeException {
		String urlString = mHost + "/" + mPath;
		try {
			URL u = new URL(urlString);
			urlString = u.toString() + "?securityYN=N&call=ANDROID";
		} catch (MalformedURLException e) {
			throw new ErpBarcodeException(-1, "URL 생성중 오류(MalformedURLException)가 발생했습니다. ");
		}
		return urlString;
	}
	
}
