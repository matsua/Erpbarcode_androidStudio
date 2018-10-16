/*
 * KTSyncData.java
 *
 * �?your company here>, 2003-2008
 * Confidential and proprietary.
 */

package com.ktds.erpbarcode.env.bluetooth;


/**
 * 
 */
public class KTSyncData {
	public static KScan mKScan = null;
	
	public static boolean bInitializeDone = false;
//
	public static boolean bForceTerminate = false;
//
	public static boolean bIsOver_233 = false;
//	
    public static boolean bIsRunning = false;
//
    public static boolean bIsReadyForMenu = false;
    //public static boolean bIsReadyToClose = false;    
//
    public static boolean bIsConnected = false;
    public static boolean bIsBackground = false; 
    public static boolean bIsTerminalMode = false;
//Command related
    public static int   state = 0;
    public static boolean bIsCommandDone = true;
    public static int   LongNumbers = 0;
    public static int   LongNumbersEx = 0;    
    public static byte[] StringData = new byte[256];
    public static int   StringLength;
//Buffer related
    public static int   total = 0;
    public static int   writePtr = 0;
    public static byte[] RxBuffer = new byte[4*1024];
//

    public static boolean bIsSynchronizeOn = false;
    public static boolean bIsSyncFinished = false;
//
    public static boolean bIsKDC300 = false;    
    public static boolean bIsTwoBytesCount = false;
    public static byte[] SerialNumber =  new byte[15];
    public static byte[] FWVersion =  new byte[15];    
    public static byte[] MACAddress =  new byte[15];
    public static byte[] BTVersion =  new byte[15];
    public static byte[] FWBuild = new byte[15];
    public static byte[] DateTime = new byte[10];
    public static byte[] FCCNumber = new byte[16+1];
//
    public static int BarcodeType;
    public static byte[] BarcodeBuffer = new byte[2048*2];
//
    public  static int Options;
    //Laser
    	public static final int OPTIONS_MASK 		= 0x0000C010;
    	public static final int EAN8AS13_MASK 		= 0x00000400;
    	public static final int UPCEAS13_MASK 		= 0x00000800;    	
    	public static final int UPCAAS13_MASK 		= 0x00080000;
    	public static final int UPCEASA_MASK 		= 0x00000200; 
        public static final int EAN13C_MASK 		= 0x80000000;
        public static final int EAN8C_MASK 			= 0x40000000;
        public static final int UPCAC_MASK 			= 0x20000000;
        public static final int UPCEC_MASK 			= 0x10000000;
        public static final int CODE39V_MASK 		= 0x00800000;
        public static final int CODE39R_MASK 		= 0x08000000;
        public static final int I2OF5V_MASK 		= 0x00400000;
        public static final int I2OF5R_MASK 		= 0x04000000;
        public static final int CODABARSS_MASK 		= 0x00000001;
    //HHP
        public static final int	CODABAR_OPTION_MASK			= 0x0000007F;
        	public static final int	CB_TXSTARTSTOP_MASK		= 0x00000001;
        	public static final int	CB_CHKDGT_SHIFT			= 1;
        	public static final int	CB_CHKDGT_MASK			= 0x00000006;
        	public static final int	CB_CONCATENATE_SHIFT 	= 3;
        	public static final int	CB_CONCATENATE_MASK		= 0x00000018;
        public static final int	CODE39_OPTION_MASK			= 0x00003F00;
        	public static final int	C39_TXSTARTSTOP_MASK	= 0x00000100;
        	public static final int	C39_CHKDGT_SHIFT		= 9;
        	public static final int	C39_CHKDGT_MASK			= 0x00000600;
        	public static final int	C39_APPEND_MASK			= 0x00000800;
        	public static final int	C39_FULLASCII_MASK		= 0x00001000;
        	public static final int	I2OF5_OPTION_SHIFT		= 16;
        	public static final int	I2OF5_OPTION_MASK		= 0x00030000;
        	public static final int	CODE11_OPTION_MASK		= 0x00100000;
        	public static final int	CODE11_CHKDT_SHIFT		= 20;
        	public static final int	CODE128_OPTION_MASK		= 0x00200000;
        	public static final int	TELEPEN_OPTION_SHIFT	= 22;
        	public static final int	TELEPEN_OPTION_MASK		= 0x00400000;
        	public static final int	POSICODE_OPTION_SHIFT	= 30;
        	public static final int	POSICODE_OPTION_MASK	= 0xC0000000;
   	public static int  Symbologies;
   		public static final int SYMBOLOGIES_MASK	= 0xFFFE0000;
   		public static final int EAN13_MASK 			= 0x00000001;
   		public static final int EAN8_MASK 			= 0x00000002;
   		public static final int UPCA_MASK 			= 0x00000004;
   		public static final int UPCE_MASK 			= 0x00000008;
   		public static final int Code39_MASK 		= 0x00000010;
   		public static final int ITF14_MASK 			= 0x00000020;
   		public static final int Code128_MASK 		= 0x00000040;
   		public static final int I2of5_MASK 			= 0x00000080;
   		public static final int Codabar_MASK 		= 0x00000100;
   		public static final int GS1128_MASK 		= 0x00000200;
   		public static final int Code93_MASK 		= 0x00000400;
   		public static final int Code35_MASK 		= 0x00000800;
   		public static final int BooklandEAN_MASK 	= 0x00001000;
   		public static final int EAN13withAddon_MASK = 0x00002000;
   		public static final int EAN8withAddon_MASK 	= 0x00004000;
   		public static final int UPCAwithAddon_MASK 	= 0x00008000;
   		public static final int UPCEwithAddon_MASK 	= 0x00010000;
	//HHP
   		public static final int	ONED_SYMBOLOGY_MASK		= 0x01FFFFFF;
	   		public static final int	ONED_CODABAR_MASK	= 0x00000001;
	   		public static final int	ONED_CODE11_MASK	= 0x00000002;
	   		public static final int	ONED_CODE32_MASK	= 0x00000004;
	   		public static final int	ONED_CODE39_MASK	= 0x00000008;
	   		public static final int	ONED_CODE93_MASK	= 0x00000010;
	   		public static final int	ONED_CODE128_MASK	= 0x00000020;
	   		public static final int	ONED_EAN8_MASK		= 0x00000040;
	   		public static final int	ONED_EAN13_MASK		= 0x00000080;
	   		public static final int	ONED_EANUCC_MASK	= 0x00000100;
	   		public static final int	ONED_I2OF5_MASK		= 0x00000200;
	   		public static final int	ONED_MATRIX2OF5_MASK	= 0x00000400;
	   		public static final int	ONED_MSI_MASK		= 0x00000800;
	   		public static final int	ONED_PLESSEY_MASK	= 0x00001000;
	   		public static final int	ONED_POSICODE_MASK	= 0x00002000;
	   		public static final int	ONED_RSS14_MASK		= 0x00004000;
	   		public static final int	ONED_RSSLIMIT_MASK	= 0x00008000;
	   		public static final int	ONED_RSSEXPAND_MASK	= 0x00010000;
	   		public static final int	ONED_S2OF5ID_MASK	= 0x00020000;
	   		public static final int	ONED_S2OF5IA_MASK	= 0x00040000;
	   		public static final int	ONED_TLC39_MASK		= 0x00080000;
	   		public static final int	ONED_TELEPEN_MASK	= 0x00100000;
	   		public static final int	ONED_TRIOPTIC_MASK	= 0x00200000;
	   		public static final int	ONED_UPCA_MASK		= 0x00400000;
	   		public static final int	ONED_UPCE0_MASK		= 0x00800000;
	   		public static final int	ONED_UPCE1_MASK		= 0x01000000;   		
    public static int	OptionsBackup;
    public static int	OptionsExBackup;
		public static final int	WIDE_ANGLE_MASK 	= 0x00004000;
		public static final int HIGH_FILTER_MASK	= 0x00008000;
	public static int	SymbologiesBackup;
	public static int	SymbologiesExBackup;	
    public  static int OptionsEx;
    	public static final int	UPCA_OPTION_MASK			= 0x0000007F;
    		public static final int	UPCA_VERIFYCHKDGT_MASK	= 0x00000001;
    		public static final int	UPCA_NUMBERSYS_MASK		= 0x00000002;
    		public static final int	UPCA_2ADDENDA_MASK		= 0x00000004;
    		public static final int	UPCA_5ADDENDA_MASK		= 0x00000008;
    		public static final int	UPCA_REQADDENDA_MASK	= 0x00000010;
    		public static final int	UPCA_ADDENDASEP_MASK	= 0x00000020;
    		public static final int	UPCA_COUPONCODE_MASK	= 0x00000040;
    	public static final int	UPCE_OPTION_MASK			= 0x00003F80;
    		public static final int	UPCE_EXPAND_MASK		= 0x00000080;
    		public static final int	UPCE_REQADDENDA_MASK	= 0x00000100;
    		public static final int	UPCE_ADDENDASEP_MASK	= 0x00000200;
    		public static final int	UPCE_CHECKDGT_MASK		= 0x00000400;
    		public static final int	UPCE_NUMBERSYS_MASK		= 0x00000800;
    		public static final int	UPCE_2ADDENDA_MASK		= 0x00001000;
    		public static final int	UPCE_5ADDENDA_MASK		= 0x00002000; 
    	public static final int	EAN8_OPTION_MASK			= 0x07C00000;
    		public static final int	E8_VERIFYCHKDGT_MASK	= 0x00400000;
    		public static final int	E8_2ADDENDA_MASK		= 0x00800000;
    		public static final int	E8_5ADDENDA_MASK		= 0x01000000;
    		public static final int	E8_REQADDENDA_MASK		= 0x02000000;
    		public static final int	E8_ADDENDASEP_MASK		= 0x04000000;
    	public static final int	EAN13_OPTION_MASK			= 0x000FC000;
    		public static final int	E13_VERIFYCHKDGT_MASK	= 0x00004000;
    		public static final int	E13_2ADDENDA_MASK		= 0x00008000;
    		public static final int	E13_5ADDENDA_MASK		= 0x00010000;
    		public static final int	E13_REQADDENDA_MASK		= 0x00020000;
    		public static final int	E13_ADDENDASEP_MASK		= 0x00040000;
    		public static final int	E13_ISBNTRANS_MASK		= 0x00080000;     	
    		
    		public static final int	UPC_EAN_VERSION_MASK	= 0x10000000;
    		public static final int	EANUCC_EMUL_SHIFT		= 29;
    		public static final int	EANUCC_EMUL_MASK		= 0x60000000;
    		
    		public static final int	MSI_OPTION_MASK			= 0x08000000;
    		public static final int	POSTNET_OPTION_MASK		= 0x00100000;
    		public static final int	PLANET_OPTION_MASK		= 0x00200000;
    public  static int SymbologiesEx;	
	    public static final int	TWOD_SYMBOLOGY_MASK			= 0x000007FF;
		    public static final int	TWOD_AZTECCODE_MASK		= 0x00000001;
		    public static final int	TWOD_AZTECRUNES_MASK	= 0x00000002;
		    public static final int	TWOD_CODABLOCKF_MASK	= 0x00000004;
		    public static final int	TWOD_CODE16K_MASK		= 0x00000008;
		    public static final int	TWOD_CODE49_MASK		= 0x00000010;
		    public static final int	TWOD_DATAMATRIX_MASK	= 0x00000020;
		    public static final int	TWOD_MAXICODE_MASK		= 0x00000040;
		    public static final int	TWOD_MICROPDF_MASK		= 0x00000080;
		    public static final int	TWOD_PDF417_MASK		= 0x00000100;
		    public static final int	TWOD_QRCODE_MASK		= 0x00000200;
		    public static final int TWOD_HANSIN_MASK        = 0x00000400;
	
	    public static final int	POSTALCODE_MASK				= 0x001FF000;	
		    public static final int	POS_POSTNET_MASK		= 0x00001000;
		    public static final int	POS_PLANETCODE_MASK		= 0x00002000;
		    public static final int	POS_UKPOST_MASK			= 0x00004000;
		    public static final int	POS_CANADAPOST_MASK		= 0x00008000;
		    public static final int	POS_KIXPOST_MASK		= 0x00010000;
		    public static final int	POS_AUSPOST_MASK		= 0x00020000;
		    public static final int	POS_JAPANPOST_MASK		= 0x00040000;
		    public static final int	POS_CHINAPOST_MASK		= 0x00080000;
		    public static final int	POS_KOREAPOST_MASK		= 0x00100000; 
		    
		    public static final int	OCR_SHIFT				= 24;
		    public static final int	OCR_MASK				= 0x07000000;	    
    public  static int Timeout = 2000;
    public  static int Security = 1;
    public  static int Minlength = 2;
    public  static int WedgeMode = 1;
    //
    public	static int BatteryValue = 100;
//    
    public static int SyncDestination = 2;
    public static boolean AttachType = false;
    public static boolean AttachTimestamp = false;
    public static boolean AttachSerialNumber = false;
    public static boolean AutoConnect = false;
    
    public static int DataOrder = 0;
    public static int DataDelimiter = 0;
    public static int RecordDelimiter = 0;
    
    public static int Destination = 1;
    public static boolean AttachLocation = false;

    public static boolean SyncNonCompliant = true;
    public static boolean AttachQuantity = false; 
    
    public static boolean EraseMemory = false;
    
    public static boolean bIsGPSSupported = false;
    
    public static boolean bIsExternalStorageAvailable = false;
    public static boolean bIsExternalStorageWriteable = false;
    
    public static String filename;   
    
    public static int BufferRead, BufferWrite;
    
    public static long[] OptionMask = new long[] {
        0x00000001,
        0x00000200,
        0x00000400,
        0x00000800,
        0x00001000,
        0x00002000,
        0x00080000,
        0x00400000,
        0x00800000,
        0x04000000,
        0x08000000,
        0x10000000,
        0x20000000,
        0x40000000,
        0x80000000
    };
        public static String[] BarcodeTypeName = new String[] 
        {
            "EAN 13",
            "EAN 8",
            "UPCA",
            "UPCE",
            "Code 39",
            "ITF-14",                               // Unused
            "Code 128",
            "I2of5",
            "CodaBar",
            "UCC/EAN-128",
            "Code 93",
            "Code 35",
            "Unknown", 
            "Unknown", 
            "Bookland EAN",
            "Unknown",
        };
        public static String[] BarcodeType300 = new String[] 
        {
            "Code 32",      //0 0x00000001
            "Trioptic",       //1 0x00000002
            "Korea Post",     //2 0x00000004
            "Aus. Post",      //3 0x00000008
            "British Post",   //4 0x00000010                                
            "Canada Post",    //5 0x00000020
            "EAN-8",          //6 0x00000040
            "UPC-E",          //7 0x00000080
            "UCC/EAN-128",    //8 0x00000100
            "Japan Post",     //9 0x00000200
            "KIX Post",       //10 0x00000400
            "Planet Code",    //11 0x00000800
            "OCR",            //12 0x00001000
            "Postnet",        //13 0x00002000                          
            "China Post",     //14 0x00004000
            "Micro PDF417",   //15 0x00008000
            "TLC 39",         //16 0x00010000
            "PosiCode",       //17 0x00020000
            "Codabar",        //18 0x00040000
            "Code 39",        //19 0x00080000
            "UPC-A",          //20 0x00100000
            "EAN-13",         //21 0x00200000
            "I2of5",          //22 0x00400000                        
            "IATA",           //23 0x00800000
            "MSI",            //24 0x01000000
            "Code 11",        //25 0x02000000
            "Code 93",        //26 0x04000000
            "Code 128",       //27 0x08000000
            "Code 49",        //28 0x10000000
            "Matrix2of5",     //29 0x20000000
            "Plessey",        //30 0x40000000
            "Code 16K",       //31 0x80000000
////////////////////////////////////////////////////////////////////////                                
            "Codablock F",    //32 0x00000001
            "PDF417",         //33 0x00000002
            "QR/Micro QR",    //34 0x00000004
            "Telepen",        //35 0x00000008
            "VeriCode",       //36 0x00000010
            "Data Matrix",    //37 0x00000020
            "MaxiCode",       //38 0x00000040                         
            "EAN/UCC",        //39 0x00000080
            "RSS",            //40 0x00000100
            "Aztec Code",     //41 0x00000200
            "No Read",        //42
            "HanXin Code",    //43
            "Unknown"         //44                                
        };    
//
    	public static int	StoredBarcode = 0;
    	public static int	MemoryLeft = 0;        

    	public static int	SleepTimeout = 2;
//
    	public static int	MSRFormat = 0;
    	public static int	TrackTerminator = 6;
    	public static int	AESKeyLength = 0;
    	public static String AESKey;
    	public static int	CardType = 0;
    	public static int	MSRAttachSentinel = 0;
    	public static int	MSRPartialDataStartPosition = 0;
    	public static int	MSRPartialDataLength = 0;
    	public static int	MSRPartialDataAction = 0;
//
    	public static int	ConnectDevice = 0;
    	//public static int 	BluetoothOptions = 0;
    	public static int	AutoReconnection =0;
    	public static int	PowerOnTime = 0;
    	public static int	PowerOffTime = 5;
    	
    	public static int   AutoLock = 0;
    	public static int   Keyboard = 0;
    	public static int   InitDelay = 0;
    	public static int   CharDelay = 0;
    	public static int   CtrlChar = 0;

    	public static int   WedgeStore = 1;
    	public static int   BarcodeFormat = 0;
    	public static int	Terminator = 5;
    	public static int	AIM_ID = 0;
    	public static int	StartPosition = 1;
    	public static int   NoOfChars = 0;
    	public static int   Action = 1;
    	public static String	Prefix;
    	public static String	Suffix;
 
    	public static int	RereadDelay;
    	
    	public static int MFiMode = 0;
    	
    	public static int   KDCSettingsBackup = 0;
    	public static int	KDCSettings = 0;
    			public static final int SYSTEM_MASK			= 0xFF000000;
 
    				public static final int BEEPSOUND_MASK 		= 0x80000000;
    				public static final int BEEPVOLUME_MASK 	= 0x40000000;
    				//public static final int MENUBARCODE_MASK 	= 0x20000000;
    				public static final int BEEPONSCAN_MASK 	= 0x20000000;
    				public static final int AUTOERASE_MASK 		= 0x10000000;
    				public static final int KEYPAD_MASK			= 0x08000000;
    				public static final int VIBRATOR_MASK		= 0x04000000;    				
    				public static final int CONNECTALERT_MASK	= 0x02000000;
       				public static final int MFIMODE_MASK 		= 0x01000000;
/*    			
    			public static final int MSR_MASK			= 0x0000FFF0;	
    				public static final int TRACKS_MASK			= 0x00000070;
    					public static final int TRACK1_MASK					= 0x00000010;
    					public static final int TRACK2_MASK					= 0x00000020;
    					public static final int TRACK3_MASK					= 0x00000040;
    				public static final int ENCRYPT_MASK 					= 0x00000080;
    				public static final int BEEPONERROR_MASK 				= 0x00000100;
    				public static final int AES_KEY_LENGTH_MASK 			= 0x00000200;
    				public static final int AES_KEY_MASK 					= 0x00000400;
    				public static final int CARD_TYPE_MASK 					= 0x00000800;
    				public static final int MSR_ATTACH_SENTINEL_MASK 		= 0x00001000;
    				public static final int MSR_PARTIAL_DATA_START_POS_MASK = 0x00002000;
    				public static final int MSR_PARTIAL_DATA_LENGTH_MASK 	= 0x00004000;
*/
    			public static final int BLUETOOTH_MASK		= 0x000001FD;
    				//public static final int BT_TOGGLE		= 0x00000001;
    				//public static final int BT_POWER_MSG	= 0x00000004;
    				//public static final int AUTO_CONNECT	= 0x00000008;
    				//public static final int AUTO_POWER_ON	= 0x00000010;
    				//public static final int AUTO_POWER_OFF	= 0x00000020;
    				//public static final int BEEP_WARNING	= 0x00000040;
    				//public static final int WAKEUP_NULLS	= 0x00000080;
    				//public static final int AUTO_RECONNECTION= 0x0000100;
    				public static final int CONNECT_ALERT_MASK	= 0x0000100;
    			public static final int	DUPLICATED_MASK		= 0000000002;
    			public static final int AUTO_TRIGGER_MASK	= 0x00001000;

    	    	public static String BluetoothName;
    	    	public static String BluetoothNameTemp;
    			public static String BluetoothAddress;
    			public static boolean	bIsPairingScreen;
    				
} 
