package com.ktds.erpbarcode.barcode;

import java.util.List;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktds.erpbarcode.GlobalData;
import com.ktds.erpbarcode.barcode.model.BarcodeHttpController;
import com.ktds.erpbarcode.barcode.model.BarcodeInfoConvert;
import com.ktds.erpbarcode.barcode.model.BarcodeListInfo;
import com.ktds.erpbarcode.common.ErpBarcodeException;
import com.ktds.erpbarcode.common.ErpBarcodeExceptionConvert;

public class SAPBarcodeService {

	private static final String TAG = "SAPBarcodeService";
	
	public static final int STATE_NONE = 0;        // we're doing nothing
	public static final int STATE_NOT_FOUND = 1;   // 조회건수 0건..
    public static final int STATE_SUCCESS = 2;     // 조회 성공..
    public static final int STATE_ERROR = -1;      // 오류발생..
    private final Handler mHandler;
    private SAPBarcodeThread mSAPBarcodeThread = null;
    private int mState;
    
    private String mJobGubun = "";
    
    public SAPBarcodeService(Handler handler) {
        mHandler = handler;
        mState = STATE_NONE;
        mJobGubun = GlobalData.getInstance().getJobGubun();
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    public synchronized int getState() {
        return mState;
    }
    
    /**
     * ErpBarcodeException Class를 JSON String으로 에러 메지지 전송한다.
     * 나중에 사용할 예정.
     * 
     * @param errorMessage
     */
    public synchronized void handlerSendMessage(int state, ErpBarcodeException erpbarException) {
    	String jsonString = ErpBarcodeExceptionConvert.erpBarcodeExceptionToJsonString(erpbarException);
    	
    	Message msg = mHandler.obtainMessage(state);
		Bundle bundle = new Bundle();
		bundle.putString("message", jsonString);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_ERROR);
    }
    public synchronized void handlerSendMessage(int state, String errorMessage) {
    	handlerSendMessage(state, new ErpBarcodeException(-1, errorMessage));
    }

    public synchronized void search(String locCd, String deviceId, String barcode) {
    	String orgCode = "";
    	
    	// Start the thread to listen on a BluetoothServerSocket
        if (mSAPBarcodeThread == null) {
        	mSAPBarcodeThread = new SAPBarcodeThread(orgCode, locCd, deviceId, barcode);
        	mSAPBarcodeThread.start();
        }
    }
    
    public synchronized void search(String orgCode, String locCd, String deviceId, String barcode) {
    	Log.i(TAG, "search Start...");
    	
    	// Start the thread to listen on a BluetoothServerSocket
        if (mSAPBarcodeThread == null) {
        	mSAPBarcodeThread = new SAPBarcodeThread(orgCode, locCd, deviceId, barcode);
        	mSAPBarcodeThread.start();
        }
    }

    private class SAPBarcodeThread extends Thread {
    	private String _Barcode;
    	private String _OrgCode;
    	private String _LocCd;
    	private String _DeviceId;
    	
    	public SAPBarcodeThread(String orgCode, String locCd, String deviceId, String barcode) {
    		_Barcode = barcode;
    		_OrgCode = orgCode;
    		_LocCd = locCd;
    		_DeviceId = deviceId;    		
    	}
        public void run() {
            // TODO - PDABarcodeService와 합치기
        	boolean flag = Pattern.matches("^[a-zA-Z0-9]*$", _Barcode);
        	if(!flag){
        		handlerSendMessage(STATE_ERROR, "처리할 수 없는 설비바코드입니다.");
            	return;
        	}
        	
            if (!_Barcode.isEmpty()) {
            	if (_Barcode.length()<16 || _Barcode.length()>18) {
                	handlerSendMessage(STATE_ERROR, "처리할 수 없는 설비바코드입니다.");
                	return;
                }
            }

			List<BarcodeListInfo> barcodeListInfos = null;
            try {
				BarcodeHttpController barcodehttp = new BarcodeHttpController();
				barcodeListInfos = barcodehttp.getSAPBarcodeDataToBarcodeListInfos(_OrgCode, _LocCd, _DeviceId, _Barcode, "H");
				
				if (barcodeListInfos == null) {
					throw new ErpBarcodeException(-1, "설비바코드 조회 결과가 없습니다. ");
				}
				
			} catch (ErpBarcodeException e) {
				Log.d(TAG, "SAPBarcodeThread  서버로 설비바코드 조회 요청중 오류가 발생했습니다.==>" + e.getErrMessage());
				handlerSendMessage(STATE_ERROR, e);
				return;
			}
            
            if (barcodeListInfos.size() == 0) {
            	handlerSendMessage(STATE_NOT_FOUND, "조회 건수가 0건입니다.");
		        return;
			}
            
            //-------------------------------------------------------
            // 여기서부터는 Validation Check 처리.
            //-------------------------------------------------------
            
            // 설비 상태 별 Validation
            BarcodeListInfo barcodeInfo = barcodeListInfos.get(0);
            if (_Barcode.equals(barcodeInfo.getBarcode()) || !mJobGubun.startsWith("현장점검") && !mJobGubun.equals("설비정보") && !mJobGubun.equals("장치바코드하위설비조회")) {
	            try {
	            	SuportLogic.chkZPSTATU(barcodeInfo.getFacStatus(), barcodeInfo.getFacStatusName(), barcodeInfo.getProductCode());
	            } catch (ErpBarcodeException e) {
					handlerSendMessage(STATE_ERROR, e);
	            	return;
				}
            }
            
            //-------------------------------------------------------
            // 2012.07.04.14:49
            // 안녕하세요? 강준석입니다.
            // 다음과 같은 Validation 추가 부탁 드립니다. (전병준 매니저님과 협의 완료)
			//
            // 1. 입고 처리 시
            //  . 설비처리구분이 부외실물이고 (ZKEQUI = ‘B’) 이고 자산번호가 공란(ZANLN1 <> ‘ ‘)이 아니면 에러 처리함.
            //  . 메세지: 자본화된 설비바코드는 입고 처리가 불가능 합니다. 바코드(스캔된 바코드 번호)를 제외하고 입고 처리 해 주시기 바랍니다.
        	//
            // 1. 입고: 로직 수정합니다. 자사번호가 공란일 경우 에러 처리함. 단, 설비처리구분이 부외실물일 경우는 예외임.
            //    T10A9006164001416 (ZANLN1 = ' ' ZKEQUI = ‘ ’) -> 에러 처리되어야 함.
            //    T10A9006161002199 ( ZANLN1 = ' ',  ZKEQUI = ‘B’ ) -> 처리되어야 함.
            // 
            // 
            //    T10A9006164001416 (ZANLN1 = ' ' ZKEQUI = ‘ ’) 바코드를 입고 처리할때 PDA에서 선택한 상태가 납품입고 일 경우 에러 처리되어야 합니다.
			//
            // 2. 송부 스캔 시
            //  . 접수 조직 입력 시 송부할려는 바코드의 조직과 동일하면 에러 처리함.
            //  . 메세지: 설비바코드와 동일한 조직으로 송부 스캔하실 수 없습니다. 출고/입고 메뉴로 처리해 주시기 바랍니다.
			//
            // 3. 실장 처리 시
            //  . 상위바코드가 스캔되었을 경우 스캔된 설비의 상태가 '0200' 불용대기, '0210' 불용요청, '0240' 불용확정, '0260' 사용중지 인 경우 에러 처리함.
            //  . 메세지: 해당 상태(상위바코드의 상태)의 바코드(스캔된 바코드 번호)에는 설비를 실장 하실 수 없습니다.
			//
            // 4. 상태변경 시
            //  . 불용요청 상태는 선택 조건에서 제외해 주시기 바랍니다.
            //  . 불용요청은 입고 처리시에만 선택해서 처리하기로 하였습니다. 
            //-------------------------------------------------------

            // 고장등록 자산화 여부 체크하지 않음 - request by 박장수 2013.06.25 
            if (mJobGubun.equals("개조개량의뢰") || mJobGubun.equals("철거")) {
            	// 개조개량의뢰 '제조사물자' 스캔 불가 처리 
                if (mJobGubun.equals("개조개량의뢰") && barcodeInfo.getZkequi().equals("M")) {
                	handlerSendMessage(STATE_ERROR, "'제조사물자'인 설비바코드는\n\r'" + mJobGubun + "' 작업을\n\r하실 수 없습니다.");
                	return;
                }
                
                // 설비처리구분 : B - 부외실물, N - 일반물자, M - 부외실물(제조사물자 체크)
                // 자산번호 == ""
                // M도 동일하게 처리 - request by 오종윤 2012.09.24
                if (!barcodeInfo.getZkequi().equals("B") && !barcodeInfo.getZkequi().equals("M") 
                		&& barcodeInfo.getZanln1().isEmpty()) {
                	handlerSendMessage(STATE_ERROR, "자산화가 안 된 설비바코드는\n\r'" + mJobGubun + "' 작업을\n\r하실 수 없습니다.");
                	return;
                }
            } else if (mJobGubun.equals("탈장")) {
                // 그럼 창고설비는 탈장을 하실 수 없습니다라는 메시지라도 뿌려줘야죠
                // 가치경영실 자산혁신팀  정진우 - 2013.06.04
                if (barcodeInfo.getLocCd().startsWith("VS")) {
                	handlerSendMessage(STATE_ERROR, "'창고(" + barcodeInfo.getLocCd() + ")'\n\r설비바코드는\n\r'" + mJobGubun + "' 작업을\n\r하실 수 없습니다.");
                	return;
                }
            } else if (mJobGubun.equals("인계") || mJobGubun.equals("인수") || mJobGubun.equals("시설등록")) {
                if (mJobGubun.equals("인계") || mJobGubun.equals("인수")) {
                    if (barcodeInfo.getO_data_c().equals("2")) {
                    	handlerSendMessage(STATE_ERROR, "해당 설비바코드는 '인계'\n\r대상이 아닙니다.\n\r'시설등록'으로 처리 하시기 바랍니다.");
                    	return;
                    }
                    // DR-2015-06633
                    if(!barcodeInfo.getZanln1().equals("") && !barcodeInfo.getAnsdt().equals("") && !barcodeInfo.getZsetup().equals("0")){
                    	handlerSendMessage(STATE_ERROR, "구품 설비는 철거 스캔 선행 후\n\r인계 작업을 진행 하시기 바랍니다.");
                    	//return;
                    }
                } else if (mJobGubun.equals("시설등록")) {
                    if (barcodeInfo.getO_data_c().isEmpty() || barcodeInfo.getO_data_c().equals("1")) {
                    	handlerSendMessage(STATE_ERROR, "해당 설비바코드는 '시설등록'\n\r대상이 아닙니다.\n\r'인계'로 처리 하시기 바랍니다.");
                    	return;
                    }
                }
            }else if(mJobGubun.equals("실장")){
            	if (barcodeInfo.getO_data_c().equals("2") && barcodeInfo.getZanln1().equals("") && (barcodeInfo.getZkequi().equals("") || barcodeInfo.getZkequi().equals("N"))) {
            		handlerSendMessage(STATE_ERROR, "해당 설비바코드는 '신품'이므로\n\r실장처리가 불가합니다.\n\r'시설등록'으로 처리 하시기 바랍니다.");
                	return;
            	}
            }
            
            //-------------------------------------------------------
            // 최종 JsonString 자료로 변환한다.
            //-------------------------------------------------------
            String jsonarrayMessage = BarcodeInfoConvert.barcodeInfosToJsonArrayString(barcodeListInfos);
 
            //-------------------------------------------------------
            // 조회 결과를 요청한 Handler로 전송한다.
            //-------------------------------------------------------
            Message msg = mHandler.obtainMessage(STATE_SUCCESS);
    		Bundle bundle = new Bundle();
    		bundle.putString("message", jsonarrayMessage);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            
            setState(STATE_SUCCESS);
        }
    }
}
