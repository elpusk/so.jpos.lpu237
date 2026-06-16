package kr.co.elpusk.javapos.msr;

import jpos.JposConst;
import jpos.JposException;
import jpos.MSR;
import jpos.MSRConst;
import jpos.events.DataEvent;
import jpos.loader.JposServiceInstance;
import jpos.services.EventCallbacks;
import jpos.services.MSRService111;
import jpos.util.tracing.Tracer;
import jpos.util.tracing.TracerFactory;

import java.util.LinkedList;
import java.util.Queue;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import kr.co.elpusk.javapos.msr.Lpu237MSRService;

public class Lpu237MSRService implements JposServiceInstance, MSRService111, Runnable
{
	/**
	 * constant properties
	 */
	private enum command{
		cmd_none, cmd_start_wait
	}	
	
	private static final long serialVersionUID = 2026061600;
	private static int n_uid = 82300;
	
	private static final int iso_buffer_size = 120;
	private static final byte iso1_stx = '%';
	private static final byte iso1_etx = '?';

	private static final byte iso2_3_stx = ';';
	private static final byte iso2_3_etx = '?';
	
	private static final long join_wait_time_mm = 3000;
	
	/**
	 * static properties none spec
	 */	
	private static Lpu237MSRService self_cur = null; 

	//private static final Log logger = LogFactory.getLog(Lpu237MSRService.class);
	//private final Log logger = LogFactory.getLog(getClass());
	private static Tracer tracer = TracerFactory.getInstance().createTracer( "Lpu237MSRService" );
	
	private static byte[] out_size;//
	private static byte[] out_iso1;//
	private static byte[] out_iso2;//
	private static byte[] out_iso3;//	
	
	private static volatile boolean shutdown = false;//
	
	private static Thread worker = null;//
	
	private static Queue<command> q_cmd = null;//
	
	private static boolean isLoadLibrary = false;
	
	/**
	 * instance property
	 *  none specification.
	 */
	private EventCallbacks callbacks;

	/**
	 * ******************************************************
	 * specification common properties
	 * ******************************************************
	 */
	private boolean autoDisable;//RW
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getAutoDisable()
	 */
    //@Override
	public boolean getAutoDisable() throws JposException 
    {
    	checkIfOpen();
        return this.autoDisable;
    }	
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setAutoDisable(boolean)
	 */
    //@Override
	public void setAutoDisable(boolean b_autoDisable) throws JposException 
    {
    	checkIfOpen();
        this.autoDisable = b_autoDisable;
    }
	
    private static final boolean CapCompareFirmwareVersion =false;//R Revised in Release 1.14
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapCompareFirmwareVersion()
	 */
    //@Override
	public boolean getCapCompareFirmwareVersion() throws JposException 
    {
    	//throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support CompareFirmwareVersion.");
    	checkIfOpen();
    	return	CapCompareFirmwareVersion; 
    }
    
    private static final int CapPowerReporting = 0;//R Updated in Release 1.11
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapPowerReporting()
	 */
    //@Override
	public int getCapPowerReporting() throws JposException
    {
    	checkIfOpen();
    	return JposConst.JPOS_PR_NONE;
    }
    
    private static final boolean CapStatisticsReporting =false;//R Added in Release 1.8
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapStatisticsReporting()
	 */
    //@Override
	public boolean getCapStatisticsReporting() throws JposException 
    {
    	//throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support StatisticsReporting.");
    	checkIfOpen();
    	return CapStatisticsReporting;
    }
    
    private static final boolean CapUpdateFirmware =false;//R Updated in Release 1.14
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapUpdateFirmware()
	 */
    //@Override
	public boolean getCapUpdateFirmware() throws JposException 
    {
    	//throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support UpdateFirmware.");
    	checkIfOpen();
    	return 	CapUpdateFirmware;
    }
    
    private static final boolean CapUpdateStatistics =false;//R Added in Release 1.8
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapUpdateStatistics()
	 */
    //@Override
	public boolean getCapUpdateStatistics() throws JposException 
    {
    	//throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support UpdateStatistics.");
    	checkIfOpen();
    	return CapUpdateStatistics;
    }
    
    private String checkHealthText = "";
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCheckHealthText()
	 */
    //@Override
	public String  getCheckHealthText() throws JposException
    {
    	checkIfOpen();
    	return this.checkHealthText;
    }
    private static volatile boolean claimed = false;//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getClaimed()
	 */
    //@Override
	public boolean getClaimed() throws JposException
    {
    	checkIfOpen();
    	return Lpu237MSRService.claimed;
    }
    
    //Holds the number of enqueued DataEvents. but has not yet been delivered
    private int dataCount = 0;//R, Used only with Devices that have Event Driven Input
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDataCount()
	 */
    //@Override
	public int getDataCount() throws JposException
    {
    	checkIfOpen();
    	return dataCount;
    }
    
    private boolean dataEventEnabled;//RW, Used only with Devices that have Event Driven Input
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDataEventEnabled()
	 */
    //@Override
	public boolean getDataEventEnabled() throws JposException
    {
    	checkIfOpen();
    	return this.dataEventEnabled;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setDataEventEnabled(boolean)
	 */
    //@Override
	public void setDataEventEnabled(boolean dataEventEnabled) throws JposException 
    {
    	checkIfOpen();
    	this.dataEventEnabled = dataEventEnabled;
        
        if((this.dataEventEnabled)&&(this.dataCount!=0))
        	lpu237DataRead();
    }
   
    private boolean deviceEnabled;//RW
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDeviceEnabled()
	 */
    //@Override
	public boolean getDeviceEnabled() throws JposException
    {
    	checkIfOpen();
    	//checkIfClaimed();
    	return this.deviceEnabled;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setDeviceEnabled(boolean)
	 */
    //@Override
	public void setDeviceEnabled(boolean enabled) throws JposException 
    {
		//System.out.println("Lpu237MSR enable("+enabled+") \n");
        checkIfOpen();
        checkIfClaimed();
        //
        this.deviceEnabled = enabled;
        
        if( enabled )
        	tracer.println("setDeviceEnabled - enable");
        else
        	tracer.println("setDeviceEnabled - disable");
    }
    

    private boolean freezeEvents;//RW Updated in Release 1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getFreezeEvents()
	 */
    //@Override
	public boolean getFreezeEvents() throws JposException
    {
    	checkIfOpen();
        return freezeEvents;
    }    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setFreezeEvents(boolean)
	 */
    //@Override
	public void setFreezeEvents(boolean freezeEvents) throws JposException
    {
        checkIfOpen();
        this.freezeEvents = freezeEvents;
        
        if(!this.freezeEvents)
        	lpu237DataRead();
    }
    
    private int outputID = 0;//R, Used only with Asynchronous Output Devices.
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getOutputID()
	 */
    
	public int getOutputID() throws JposException
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support OutputID.");
    }
    
    private int powerNotify;//RW
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getPowerNotify()
	 */
    //@Override
	public int getPowerNotify() throws JposException
    {
    	checkIfOpen();
        return this.powerNotify;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setPowerNotify(int)
	 */
    //@Override
	public void setPowerNotify(int powerNotify)  throws JposException
    {
    	checkIfOpen();
    	
    	if( powerNotify == JposConst.JPOS_PN_ENABLED ){
   			throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support PowerNotify.");
    	}
        this.powerNotify = powerNotify;
    }
    
    private int powerState = JposConst.JPOS_PS_UNKNOWN;//R Updated in Release 1.11
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getPowerState()
	 */
    //@Override
	public int getPowerState() throws JposException
    {
    	checkIfOpen();
    	return powerState;
    }
    
    private int state = JposConst.JPOS_S_CLOSED;//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getState()
	 */
    //@Override
	public int getState()
    {
        return this.state;
    }
    
    //private static final String deviceControlDescription = "POS MSR JavaPOS Control, (C) 2026 Elpusk.Co.,Ltd.";//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDeviceControlDescription()
	 */
/*	 
    	public String getDeviceControlDescription()
    {
    	return deviceControlDescription;
    }
*/    
    //private static final int deviceControlVersion = 1000000;//R current v1.0.0( ex 1002038 -> 1.2.38 )
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDeviceControlVersion()
	 */
/*	 
    	public int getDeviceControlVersion()
    {
    	return deviceControlVersion;
    }
*/    
    private static final String deviceServiceDescription = "LPU237 MSR JPOS Service Driver, (C) 2026 Elpusk.Co.,Ltd.";//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDeviceServiceDescription()
	 */
    //@Override
	public String getDeviceServiceDescription() throws JposException
    {
    	checkIfOpen();
    	return deviceServiceDescription;
    }
    
    private static final int deviceServiceVersion = 1011000; // 1.11.0;//R current v1.1.1
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDeviceServiceVersion()
	 */
    //@Override
	public int getDeviceServiceVersion() throws JposException
    {
    	checkIfOpen();
    	return deviceServiceVersion;
    }
    
    private static final String physicalDeviceDescription = "MSR LPU237 made in korea.";//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getPhysicalDeviceDescription()
	 */
    //@Override
	public String getPhysicalDeviceDescription() throws JposException
    {
    	checkIfOpen();
    	return physicalDeviceDescription;  	
    }
    
    private static final String physicalDeviceName = "LPU237";//R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getPhysicalDeviceName()
	 */
    //@Override
	public String getPhysicalDeviceName() throws JposException
    {
    	checkIfOpen();
    	return physicalDeviceName;  	
    }
   
    
	/**
	 * ******************************************************
	 * specification common methods
	 * ******************************************************
	 */
    
    private boolean open;
	
    //Updated in Release 1.7
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#open(java.lang.String, jpos.services.EventCallbacks)
	 */
    //@Override
	public void open(String s, EventCallbacks eventcallbacks) throws JposException 
    {
		//System.out.println("Lpu237MSRService open \n");
    	tracer.println("Lpu237MSRService : open . \n");
        if (this.open) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Service is already open.");
        }
        
        this.open = true;
        this.state = JposConst.JPOS_S_IDLE;
        this.callbacks = eventcallbacks;

        // ini by specification
        this.autoDisable = false;
        this.dataCount = 0;
        this.dataEventEnabled = false;
        this.deviceEnabled = false;
        this.freezeEvents = false;
        this.powerNotify = JposConst.JPOS_PN_DISABLED;
        this.powerState = JposConst.JPOS_PS_UNKNOWN;
        this.tracksToRead = MSRConst.MSR_TR_1_2_3;
        this.tracksToWrite = MSRConst.MSR_TR_NONE;
        this.transmitSentinels = false;
        //
        this.decodeData = true;
    }
	
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#close()
	 */
    //@Override
	public void close() throws JposException 
    {
        checkIfOpen();
        
        if( worker != null ){
        	kill();
        	worker = null;
        }
        
        if(this.getClaimed() ){
            reset();

            lpu237_cancel_wait();
            lpu237_close();
        }
        
        this.open = false;
        this.state = JposConst.JPOS_S_CLOSED;

        // Also need to reset all the member variables
        callbacks = null;
        deviceEnabled = false;
        freezeEvents = false;
        synchronized( this ){
        	Lpu237MSRService.claimed = false;
        }
        
        
        tracer.println("Lpu237MSRService : close . \n");
    }
    
    //Updated in Release 1.11
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#claim(int)
	 */
    //@Override
	public void claim(int timeOut) throws JposException 
    {
		//System.out.println("Lpu237MSRService claim("+timeOut+") \n");
        checkIfOpen();
        
        synchronized(this){
        	if( Lpu237MSRService.claimed == true ){
        		throw new JposException(JposConst.JPOS_E_TIMEOUT, "Device is claimed already.");
        	}
        	//Lpu237MSRService.claimed = true;
        }
        
        if( !lpu237_open() ){
        	throw new JposException(JposConst.JPOS_E_NOEXIST, "No device.");
        }else {
        
	        Lpu237MSRService.claimed = true;
	        lpu237_enable_read(true);
	        
	        if( worker == null ){
	        	worker = new Thread( this,"lpu237_java_worker" );
	        	worker.setDaemon(true);
	        	//
	        	ClearQ();
	        	worker.start();
	        }
	        
	        EnQ(command.cmd_start_wait);
        }
    }   

    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#release()
	 */
    //@Override
	public void release() throws JposException
    {
        checkIfOpen();
        checkIfClaimed();
        
        synchronized(this){
        	Lpu237MSRService.claimed = false;
        }
        this.deviceEnabled = false;
        this.state = JposConst.JPOS_S_IDLE;
        
        if( worker != null ){
        	kill();
        	worker = null;
        }

        reset();

        lpu237_cancel_wait();
        lpu237_close();
    }
	   
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#checkHealth(int)
	 */
    //@Override
	public void checkHealth(int n_mode ) throws JposException 
    {
   		throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support your health mode.");
   		//use after open,claim,enable
    }
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#clearInput()
	 */
    //@Override
	public void clearInput() throws JposException 
    {
    	checkIfOpen();
    	checkIfClaimed();
    	tracer.println("Lpu237MSRService : clearInput . \n");
/*
    	track1Data = "";
        track2Data = "";
        track3Data = "";
        
        setIso_data1(new byte[0],0);
        setIso_data2(new byte[0],0);
        setIso_data3(new byte[0],0);
  */      
        dataCount=0;
    }
    
    //Added in Release 1.10
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#clearInputProperties()
	 */
    //@Override
	public void clearInputProperties() throws JposException 
    {
    	checkIfOpen();
    	checkIfClaimed();
    	//Sets all data properties that were populated as a result of firing a DataEventor 
    	// ErrorEventback to their default values. This does not reset the DataCountor 
    	// State properties.
    	accountNumber = "";
        track1Data = "";
        track2Data = "";
        track3Data = "";
        expirationDate = "";
        title = "";
        firstName = "";
        middleInitial = "";
        surName = "";
        suffix = "";
        serviceCode = "";
        track1DiscretionaryData = "";
        track2DiscretionaryData = "";
        
        setIso_data1(new byte[0],0);
        setIso_data2(new byte[0],0);
        setIso_data3(new byte[0],0);
        
        tracer.println("Lpu237MSRService : clearInputProperties . \n");
    }
    
    //Updated in Release 1.7
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#clearOuput()
	 */
    	public void clearOuput() throws JposException 
    {
    	//checkIfOpen();
    	tracer.println("Lpu237MSRService : clearOuput . \n");
    }
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#directIO(int, int[], java.lang.Object)
	 */
    //@Override
	public void directIO(int n_command, int[] inout_data, Object obj) throws JposException 
    {
    	checkIfOpen();
    	tracer.println("Lpu237MSRService : directIO . \n");
    }
    
    //Added in Release 1.9
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#compareFirmwareVersion(java.lang.String, int[])
	 */
    //@Override
	public void compareFirmwareVersion(String arg0, int[] arg1) throws JposException 
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support comparefirmware.");
    	//use after open,claim,enable
	}
    
    //Updated in Release 1.10
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#resetStatistics(java.lang.String)
	 */
    //@Override
	public void resetStatistics(String arg0) throws JposException 
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support statistics operation.");
    	//use after open,claim,enable
    }
    
    //Added in Release 1.8
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#retrieveStatistics(java.lang.String[])
	 */
    //@Override
	public void retrieveStatistics(String[] arg0) throws JposException 
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support statistics operation.");
    	//use after open,claim,enable
    }
    
    //Added in Release 1.9
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#updateFirmware(java.lang.String)
	 */
    //@Override
	public void updateFirmware(String arg0) throws JposException 
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support update-firmware.");
    	//use after open,claim,enable
    }

    //Updated in Release 1.10
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#updateStatistics(java.lang.String)
	 */
    //@Override
	public void updateStatistics(String arg0) throws JposException 
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support statistics operation.");
    	//use after open,claim,enable
    }
    
    /**
     * specification MSR properties for v1.11
     */
    private String accountNumber="";//Updated in Release 1.13, R - no need
	/* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getAccountNumber()
	 */
	//@Override
	public String getAccountNumber() throws JposException 
	{
		checkIfOpen();
		return accountNumber;
	}
       
    //private byte[] additionalSecurityInformation = null;//Added in Release 1.12, R - no need
    //private String CapCardAuthentication = "";//Added in Release 1.12, R - no need
    //private int capDataEncryption = 0;//Added in Release 1.12, R - no need
    //CapDeviceAuthentication// Property Added in Release 1.12
    private final static boolean capIso  = true;// R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapISO()
	 */
    //@Override
	public boolean getCapISO() throws JposException
    {
    	checkIfOpen();
    	return capIso;
    }
    
    private final static boolean  capJISOne = false;// R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapJISOne()
	 */
    //@Override
	public boolean getCapJISOne() throws JposException
    {
    	checkIfOpen();
    	return capJISOne;
    }
    
    private final static boolean  capJISTwo = false;// R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapJISTwo()
	 */
    //@Override
	public boolean getCapJISTwo() throws JposException 
    {
    	checkIfOpen();
        return capJISTwo;
    }
    
    //CapTrackDataMasking Property Updated in Release 1.13
    
    private boolean capTransmitSentinels = true; //false; //Added in Release 1.5 , R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapTransmitSentinels()
	 */
    //@Override
	public boolean getCapTransmitSentinels() throws JposException 
    {
    	checkIfOpen();
    	return capTransmitSentinels;
    }
   
    private final static int capWritableTracks = MSRConst.MSR_TR_NONE;//Added in Release 1.10
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCapWritableTracks()
	 */
    //@Override
	public int getCapWritableTracks() throws JposException 
    {
    	checkIfOpen();
    	return capWritableTracks;
    }
    
    //CardAuthenticationData Property Added in Release 1.12
    //CardAuthenticationDataLength Property Updated in Release 1.13
    //CardPropertyList Property Added in Release 1.12
    //CardType Property Added in Release 1.12
    //CardTypeList Property Added in Release 1.12
    //DataEncryptionAlgorithm Property Added in Release 1.12
    
    //RW DecodeData Property  Updated in Release 1.13
    private boolean decodeData;//v1.13 false-raw data format. , true- decoded ASCII
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getDecodeData()
	 */
    //@Override
	public boolean getDecodeData() throws JposException 
    {
    	checkIfOpen();
    	return this.decodeData;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setDecodeData(boolean)
	 */
    //@Override
	public void setDecodeData(boolean b_decodeData) throws JposException 
    {
    	checkIfOpen();
    	this.decodeData = b_decodeData;
        //Setting this property to false automatically sets ParseDecodeData to false.
        if( !b_decodeData ){
        	this.parseDecodeData = b_decodeData;
        }
    }
    
    //DeviceAuthenticated Property Added in Release 1.12
    //DeviceAuthenticationProtocol Property Added in Release 1.12
    private final static int encodingMaxLength = 0;//Added in Release 1.10, R
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getEncodingMaxLength()
	 */
    //@Override
	public int getEncodingMaxLength() throws JposException 
    {
    	checkIfOpen();
    	//checkIfClaimed();
    	//if( !getDeviceEnabled() ){//JPOS_E_DISABLED
    	//	throw new JposException(JposConst.JPOS_E_DISABLED, "enable first. getEncodingMaxLength.");
    	//}
    	return encodingMaxLength;
    }
    
    //RW ErrorReportingType Property Updated in Release 1.13
    private int errorReportingType;	//v1.13 
	//MSR_ERT_CARD Report errors at a card level.
	//MSR_ERT_TRACK Report errors at the track level
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getErrorReportingType()
	 */
    //@Override
	public int getErrorReportingType() throws JposException 
    {
    	checkIfOpen();
    	return this.errorReportingType;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setErrorReportingType(int)
	 */
    //@Override
	public void setErrorReportingType(int n_errorReportingType) throws JposException {
    	checkIfOpen();
    	this.errorReportingType = n_errorReportingType;
    }
    
    //R ExpirationDate Property Updated in Release 1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getExpirationDate()
	 */
    private String expirationDate;//v.1.12
    //@Override
	public String getExpirationDate() throws JposException
    {
    	checkIfOpen();
    	return expirationDate;
    }
    
    //R FirstName Property Updated in Release 1.12
    private String firstName;//v.1.12
    /* (non-Javadoc)
 	 * @see kr.co.elpusk.javapos.msr.MSRService#getFirstName()
 	 */
    //@Override
 	public String getFirstName() throws JposException 
     {
    	 checkIfOpen();
    	 return firstName;
     }
     
    //R MiddleInitial Property  Updated in Release 1.12
    private String middleInitial;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getMiddleInitial()
	 */
    //@Override
	public String getMiddleInitial() throws JposException 
    {
    	checkIfOpen();
    	return middleInitial;
    }
    
    private boolean parseDecodeData = true;//v.1.12
    //RW ParseDecodeData Property  Updated in Release 1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getParseDecodeData()
	 */
    //@Override
	public boolean getParseDecodeData() throws JposException 
    {
    	checkIfOpen();
    	return this.parseDecodeData;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setParseDecodeData(boolean)
	 */
    //@Override
	public void setParseDecodeData(boolean b_parseDecodeData) throws JposException 
    {
    	checkIfOpen();
    	this.parseDecodeData = b_parseDecodeData;
        if( b_parseDecodeData )
        	this.decodeData = true;
    }
    
    private String serviceCode;//v.1.12
    //R ServiceCode Property  Updated in Release 1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getServiceCode()
	 */
    //@Override
	public String getServiceCode() throws JposException 
    {
    	checkIfOpen();
    	return serviceCode;
    }
    
    //R Suffix Property Updated in Release 1.12
    private String suffix;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getSuffix()
	 */
    //@Override
	public String getSuffix() throws JposException 
    {
    	checkIfOpen();
    	return suffix;
    }
    
    //R Surname Property  Updated in Release 1.12
    private String surName;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getSurname()
	 */
    //@Override
	public String getSurname() throws JposException 
    {
    	checkIfOpen();
    	return surName;
    }
    
    //R Title Property Updated in Release 1.12
    private String title;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTitle()
	 */
    //@Override
	public String getTitle() throws JposException 
    {
    	checkIfOpen();
    	return title;
    }
    
    //R Track1Data Property  Updated in Release 1.12
    private String track1Data;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack1Data()
	 */
    //@Override
	public byte[] getTrack1Data() throws JposException {
    	
    	checkIfOpen();
    	if( this.decodeData ){
    		if( this.transmitSentinels ){
    			String data = String.format("%c%s%c", Lpu237MSRService.iso1_stx,track1Data,Lpu237MSRService.iso1_etx );
    			return data.getBytes();
    		}
    		else{
    			return track1Data.getBytes();
    		}
    	}
    	else{
    		int i = 0;
    		byte[] srcData = getIso_data1();
    		byte[] rawData;
    		
    		if( this.transmitSentinels ){
    			rawData= new byte[srcData.length+2];
    			rawData[0] = Lpu237MSRService.iso1_stx-0x20;
    			
				for( i=0; i<srcData.length; i++ ){
					rawData[i+1] = (byte)(srcData[i]-0x20);
				}//end for
				
				rawData[i+1] = Lpu237MSRService.iso1_etx-0x20;
    		}
    		else{
    			rawData= new byte[srcData.length];

    			for( i=0; i<srcData.length; i++ ){
					rawData[i] = (byte)(srcData[i]-0x20);
				}//end for
    		}
    		
    		return rawData;
    	}
    }
    
    //R Track1DiscretionaryData Property Updated in Release 1.12
    private String track1DiscretionaryData;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack1DiscretionaryData()
	 */
    //@Override
	public byte[] getTrack1DiscretionaryData() throws JposException
    {
    	checkIfOpen();
    	return track1DiscretionaryData.getBytes();
    }
    
    //Track1EncryptedData Property Added in Release 1.12
    //Track1EncryptedDataLength Property Updated in Release 1.13
    //R Track2Data Property  Updated in Release 1.12
    private String track2Data;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack2Data()
	 */
    //@Override
	public byte[] getTrack2Data() throws JposException
    {
    	checkIfOpen();
    	if( this.decodeData ){
    		if( this.transmitSentinels ){
    			String data = String.format("%c%s%c", Lpu237MSRService.iso2_3_stx,track2Data,Lpu237MSRService.iso2_3_etx );
    			return data.getBytes();
    		}
    		else{
    			return track2Data.getBytes();
    		}
    	}
    	else{
    		int i = 0;
    		byte[] srcData = getIso_data2();
    		byte[] rawData;
    		
    		if( this.transmitSentinels ){
    			rawData= new byte[srcData.length+2];
    			rawData[0] = Lpu237MSRService.iso2_3_stx-0x30;
    			
				for( i=0; i<srcData.length; i++ ){
					rawData[i+1] = (byte)(srcData[i]-0x30);
				}//end for
				
				rawData[i+1] = Lpu237MSRService.iso2_3_etx-0x30;
    		}
    		else{
    			rawData= new byte[srcData.length];

    			for( i=0; i<srcData.length; i++ ){
					rawData[i] = (byte)(srcData[i]-0x30);
				}//end for
    		}
    		
    		return rawData;
    	}
    }
    
    //R Track2DiscretionaryData Property Updated in Release 1.12
    private String track2DiscretionaryData;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack2DiscretionaryData()
	 */
    //@Override
	public byte[] getTrack2DiscretionaryData() throws JposException
    {
    	checkIfOpen();
    	return track2DiscretionaryData.getBytes();
    }
    
    //Track2EncryptedData Property Added in Release 1.12
    //Track2EncryptedDataLength Property Updated in Release 1.13
    //R Track3Data Property  Updated in Release 1.12
    private String track3Data;//v.1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack3Data()
	 */
    //@Override
	public byte[] getTrack3Data() throws JposException 
    {
    	checkIfOpen();
    	if( this.decodeData ){
    		if( this.transmitSentinels ){
    			String data = String.format("%c%s%c", Lpu237MSRService.iso2_3_stx,track3Data,Lpu237MSRService.iso2_3_etx );
    			return data.getBytes();
    		}
    		else{
    			return track3Data.getBytes();
    		}
    	}
    	else{
    		int i = 0;
    		byte[] srcData = getIso_data3();
    		byte[] rawData;
    		
    		if( this.transmitSentinels ){
    			rawData= new byte[srcData.length+2];
    			rawData[0] = Lpu237MSRService.iso2_3_stx-0x30;
    			
				for( i=0; i<srcData.length; i++ ){
					rawData[i+1] = (byte)(srcData[i]-0x30);
				}//end for
				
				rawData[i+1] = Lpu237MSRService.iso2_3_etx-0x30;
    		}
    		else{
    			rawData= new byte[srcData.length];

    			for( i=0; i<srcData.length; i++ ){
					rawData[i] = (byte)(srcData[i]-0x30);
				}//end for
    		}
    		
    		return rawData;
    	}
    }
    
    //Track3EncryptedData Property Added in Release 1.12
    //Track3EncryptedDataLength Property Updated in Release 1.13
    //Track4Data Property Updated in Release 1.12
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTrack4Data()
	 */
    //@Override
	public byte[] getTrack4Data() throws JposException 
    {
    	checkIfOpen();
    	return new byte[0];//A zero length array indicates that the track was not accessible.
    }
    
    //Track4EncryptedData Property Added in Release 1.12
    //Track4EncryptedDataLength Property Updated in Release 1.13
    
    private int tracksToRead = MSRConst.MSR_TR_1_2_3;//Updated in Release 1.5 RW
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTracksToRead()
	 */
    //@Override
	public int getTracksToRead() throws JposException
    {
    	checkIfOpen();
    	tracer.println("Lpu237MSRService : getTracksToRead . \n");
    	return tracksToRead;
    }  
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setTracksToRead(int)
	 */
    //@Override
	public void setTracksToRead( int tracksToRead ) throws JposException
    {
    	checkIfOpen();
    	this.tracksToRead = tracksToRead;
    	tracer.println("Lpu237MSRService : setTracksToRead . \n");
    	//System.out.println("tracksToRead="+tracksToRead+"\n");
    }  
    
    private int tracksToWrite = MSRConst.MSR_TR_NONE;//Added in Release 1.10, RW
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTracksToWrite()
	 */
    //@Override
	public int getTracksToWrite() throws JposException
    {
    	checkIfOpen();
    	//checkIfClaimed();
    	//use after open,claim,enable
		return tracksToWrite;
    }  
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setTracksToWrite(int)
	 */
    //@Override
	public void setTracksToWrite( int tracksToWrite) throws JposException
    {
    	throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support set tracksToWrite.");
    	//use after open,claim,enable
    }  
 
    private boolean transmitSentinels=false;//Added in Release 1.5, RW This property is initialized to false by the open method
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getTransmitSentinels()
	 */
    //@Override
	public boolean getTransmitSentinels() throws JposException
    {
    	checkIfOpen();
        return this.transmitSentinels;
    }
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setTransmitSentinels(boolean)
	 */
    //@Override
	public void setTransmitSentinels(boolean b_transmitSentinels) throws JposException 
    {
    	checkIfOpen();
        this.transmitSentinels = b_transmitSentinels;
    }

    //WriteCardType Property Added in Release 1.12
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#writeTracks(byte[][], int)
	 */
    //authenticateDevice Method Added in Release 1.12
    //deauthenticateDevice Method Added in Release 1.12
    //retrieveCardProperty Method Updated in Release 1.13
    //retrieveDeviceAuthenticationData Method Added in Release 1.12
    //updateKey Method Added in Release 1.12
    //writeTracks Method Updated in Release 1.12
	//@Override
	public void writeTracks(byte[][] data, int timeout) throws JposException {
		checkIfOpen();
		checkIfClaimed();
		
		if( !getDeviceEnabled() )
			throw new JposException(JposConst.JPOS_E_DISABLED, "Device is disabled.");
		
		throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device is not support writeTracks.");
	}
    
    
    
    /**
	 * ***************************************************************
	 * not spec method
	 * ***************************************************************
	 */
	
    protected void invoke(Runnable runnable) throws JposException {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE);
        }
    }

    protected void checkIfOpen() throws JposException {
        if (!open)
            throw new JposException(JposConst.JPOS_E_CLOSED, "Service is not open.");
    }

    protected synchronized void checkIfClaimed() throws JposException {
        if (!Lpu237MSRService.claimed)
            throw new JposException(JposConst.JPOS_E_NOTCLAIMED, "Device is not claimed.");
    }


    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#getCallbacks()
	 */
	public EventCallbacks getCallbacks() 
    {
        return callbacks;
    }
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#setCallbacks(jpos.services.EventCallbacks)
	 */
	public void setCallbacks(EventCallbacks callbacks) 
    {
        this.callbacks = callbacks;
    }
    
    

    ///////////////////////////////////////////////////
    // spec' methods
	
	/**
	 * Instance properties
	 */
	private byte[] iso_data1;
	private byte[] iso_data2;
	private byte[] iso_data3;
	
	/**
	 * native function prototype
	 */
	
	private static native void initIDs();
	
	/**
	 * lpu237_cancel_wait
	 * cancel waiting-status.
	 */
	native void lpu237_cancel_wait();
	
	/**
	 * lpu237_wait_read
	 * start waits reading
	 */
	native void lpu237_wait_read();
	
	/**
	 * lpu237_enable_read
	 * enable / disable magnetic card reading.
	 * native function ....... defined in JNI dll.
	 * @param b_enable
	 */
	native void lpu237_enable_read( boolean b_enable );

	/**
	 * lpu237_open
	 *  native function ....... defined in JNI dll.
	 * @return
	 */
	native boolean lpu237_open();
	/**
	 * lpu237_close
	 *  native function ....... defined in JNI dll.
	 * @return
	 */
	native boolean lpu237_close();
	
	
	/**
	 * lpu237_ini
	 *  native function ....... defined in JNI dll.
	 * @return
	 */
	native boolean lpu237_ini();

	/**
	 * lpu237_end
	 *  native function ....... defined in JNI dll.
	 * @return
	 */
	native boolean lpu237_end();
	
	static{
		/*
		String property = System.getProperty("java.library.path");
		StringTokenizer parser = new StringTokenizer(property, ";");
		while (parser.hasMoreTokens()) {
		    System.err.println(parser.nextToken());
		}
		
		if (System.getProperty("sun.arch.data.model").equals("32")) {
		    // 32-bit JVM
		} else {
		    // 64-bit JVM
		}
		//System.load(f.getAbsolutePath());
		*/

		q_cmd = new LinkedList<command>();
		
		out_size = new byte[3];
		out_iso1 = new byte[Lpu237MSRService.iso_buffer_size];
		out_iso2 = new byte[Lpu237MSRService.iso_buffer_size];
		out_iso3 = new byte[Lpu237MSRService.iso_buffer_size];
		
		loadLibrary();
	}

	static synchronized void loadLibrary()
	{
		try{
			if( !isLoadLibrary ){
				System.loadLibrary("tg_lpu237_jni");//load tg_lpu237_jni.dll
				tracer.println("loadLibrary : OK . \n");
				isLoadLibrary = true;
				//System.out.println("loadLibrary : OK .\n");
			}
			
		}
		catch(UnsatisfiedLinkError e) {
		      System.err.println("Native code library failed to load.\n" + e);
		      //System.out.println("Native code library failed to load.\n");
		      System.exit(1);
		}
	}
	
	private static synchronized void unLoadLibrary()
	{
		isLoadLibrary = false;
	}
	
	/**
	 * lpu237CallbackReadDone - callback for native.
	 * this method will be called by C++.(JNI DLL)
	 */
 	static public void lpu237CallbackReadDone()
	{
		// this method is called by JNI layer for callback handling.
		do{
			tracer.println("lpu237CallbackReadDone . \n");
			//System.out.println("lpu237CallbackReadDone .\n");
			
			//
			int[] n_size =  new int[3];
			for( int i =0; i<n_size.length; i++ ){
				if(Lpu237MSRService.out_size[i] > 0 )
					n_size[i] = (int)Lpu237MSRService.out_size[i];
				else
					n_size[i] = 0;
			}//end for
			
			if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_1) !=  MSRConst.MSR_TR_1)
				n_size[0]=0;
			if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_2) !=  MSRConst.MSR_TR_2)
				n_size[1]=0;
			if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_3) !=  MSRConst.MSR_TR_3)
				n_size[2]=0;
			
			if( Lpu237MSRService.self_cur == null )
				continue;
			
			if((n_size[0]==0)&&(n_size[1]==0)&&(n_size[2]==0))
				continue;
			
			//out_iso1 contains ISO1 track data.
			Lpu237MSRService.self_cur.setIso_data1(Lpu237MSRService.out_iso1,n_size[0]);
			//out_iso2 contains ISO1 track data.
			Lpu237MSRService.self_cur.setIso_data2(Lpu237MSRService.out_iso2,n_size[1]);
			//out_iso3 contains ISO1 track data.
			Lpu237MSRService.self_cur.setIso_data3(Lpu237MSRService.out_iso3,n_size[2]);
			//
			if( !Lpu237MSRService.self_cur.open )
				continue;
			if( !Lpu237MSRService.claimed )
				continue;
			if( !Lpu237MSRService.self_cur.deviceEnabled )
				continue;
			
			Lpu237MSRService.self_cur.dataCount=1;
			
			if( Lpu237MSRService.self_cur.freezeEvents )
				continue;
			if( !Lpu237MSRService.self_cur.dataEventEnabled )
				continue;
			
			
			Lpu237MSRService.self_cur.dataCount=0;
			//
			Lpu237MSRService.self_cur.set_all();

			if( Lpu237MSRService.self_cur.dataEventEnabled ){
				Lpu237MSRService.self_cur.dataEventEnabled = false;
			}
			if( Lpu237MSRService.self_cur.autoDisable ){
				Lpu237MSRService.self_cur.deviceEnabled = false;
			}
			
			if (Lpu237MSRService.self_cur.getCallbacks() != null){
				int evt_status = (n_size[0]) | (n_size[1]<<8) | (n_size[2]<<16); 
	            MSR msr = new MSR();
	            DataEvent evt = new DataEvent(msr, evt_status);
	            evt.getSource();
				
	            Lpu237MSRService.self_cur.getCallbacks().fireDataEvent(new DataEvent(evt,evt_status));
			}
			/*
			if( Lpu237MSRService.self_cur.dataEventEnabled ){
				Lpu237MSRService.self_cur.dataEventEnabled = false;
			}
			if( Lpu237MSRService.self_cur.autoDisable ){
				Lpu237MSRService.self_cur.deviceEnabled = false;
			}
			*/
		}while(false);
		
		if( Lpu237MSRService.self_cur.open )
			Lpu237MSRService.self_cur.EnQ(command.cmd_start_wait);
	}
 	
 	static public void lpu237DataRead()
 	{
 		
 		if( Lpu237MSRService.self_cur == null )
			return;
 		if( !Lpu237MSRService.self_cur.open )
			return;
		if( !Lpu237MSRService.claimed )
			return;
		if( !Lpu237MSRService.self_cur.deviceEnabled )
			return;
		
		if( Lpu237MSRService.self_cur.freezeEvents )
			return;
		if( !Lpu237MSRService.self_cur.dataEventEnabled )
			return;
		if(Lpu237MSRService.self_cur.dataCount==0)
			return;
 		
 		tracer.println("lpu237DataRead. \n");


/* 		 		
 		int evt_erCode = JposConst.JPOS_E_EXTENDED; 
 		int evt_erCodeExt = JposConst.JPOSERREXT+4;//LRC_ERROR;1START,2END,3PARITY,4LRC 
 		int evt_erLocus = JposConst.JPOS_EL_INPUT; //JposConst.JPOS_EL_INPUT_DATA
 		int evt_erResponse = 0; 
 		
        MSR msr = new MSR();
        ErrorEvent evt = new ErrorEvent(msr, evt_erCode,evt_erCodeExt,evt_erLocus,evt_erResponse);
        evt.getSource();
		
        Lpu237MSRService.self_cur.getCallbacks().fireErrorEvent(new ErrorEvent(evt,evt_erCode,evt_erCodeExt,evt_erLocus,evt_erResponse));
        
        Lpu237MSRService.self_cur.dataCount=0;
        Lpu237MSRService.self_cur.dataEventEnabled = false;
        return;
*/ 		
		//
        
		int[] n_size =  new int[3];
		for( int i =0; i<n_size.length; i++ ){
			if(Lpu237MSRService.out_size[i] > 0 )
				n_size[i] = (int)Lpu237MSRService.out_size[i];
			else
				n_size[i] = 0;
		}//end for
		if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_1) !=  MSRConst.MSR_TR_1)
			n_size[0]=0;
		if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_2) !=  MSRConst.MSR_TR_2)
			n_size[1]=0;
		if( (Lpu237MSRService.self_cur.tracksToRead & MSRConst.MSR_TR_3) !=  MSRConst.MSR_TR_3)
			n_size[2]=0;
		
 		Lpu237MSRService.self_cur.dataCount=0;
		//
		Lpu237MSRService.self_cur.set_all();
		
		if (Lpu237MSRService.self_cur.getCallbacks() != null){
			int evt_status = (n_size[0]) | (n_size[1]<<8) | (n_size[2]<<16); 
            MSR msr = new MSR();
            DataEvent evt = new DataEvent(msr, evt_status);
			evt.getSource();
			
            Lpu237MSRService.self_cur.getCallbacks().fireDataEvent(new DataEvent(evt,evt_status));
		}
		if( Lpu237MSRService.self_cur.dataEventEnabled ){
			Lpu237MSRService.self_cur.dataEventEnabled = false;
		}
		if( Lpu237MSRService.self_cur.autoDisable ){
			Lpu237MSRService.self_cur.deviceEnabled = false;
		}
 		
 	}
	   
	@Override
	protected void finalize() throws Throwable
	{
		tracer.println("Lpu237MSRService : finalize . \n");
		
		try {
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
	         super.finalize();
	    }
	}
	
	private synchronized void EnQ( command cmd )
	{
		switch( cmd ){
		case cmd_start_wait:
			Lpu237MSRService.self_cur = this;
			q_cmd.add(cmd);
			break;
		default:
			break;
		}//end switch
	}
	
	private synchronized command DeQ()
	{
		command cmd =command.cmd_none;
		if( !q_cmd.isEmpty())
			cmd = q_cmd.remove();
		return cmd;
	}
	
	private synchronized boolean IsEmptyQ()
	{
		boolean bResult = true;
		
		bResult = q_cmd.isEmpty();
		
		return bResult;
	}

	private synchronized void ClearQ()
	{
		q_cmd.clear();
	}
	
    public Lpu237MSRService() {
    	tracer.println("Lpu237MSRService : constructor . \n");
    	//loadLibrary();
    	
    	if( lpu237_ini() ){
    		tracer.println("Lpu237MSRService - success. \n");
    	}
    	else{
    		tracer.println("Lpu237MSRService - failure. \n");
    	}
    }
    
	
	/**
	 * 
	 * @return real copy iso1 data
	 */
	private synchronized byte[] getIso_data1() 
	{
		byte[] data = new byte[iso_data1.length];
		System.arraycopy(iso_data1, 0, data, 0, iso_data1.length);
		return data;
	}
	
	private synchronized void setIso_data1(byte[] iso_data1, int n_iso_data1) 
	{
		if( n_iso_data1 > 0 ){
			this.iso_data1 = new byte[n_iso_data1];
			System.arraycopy( iso_data1, 0, this.iso_data1, 0, n_iso_data1);
		}
		else{
			this.iso_data1 = new byte[0];
		}
	}
	
	private synchronized byte[] getIso_data2() 
	{
		byte[] data = new byte[iso_data2.length];
		System.arraycopy(iso_data2, 0, data, 0, iso_data2.length);
		return data;
	}
	
	private synchronized void setIso_data2(byte[] iso_data2, int n_iso_data2) 
	{
		if( n_iso_data2 >  0 ){
			this.iso_data2 = new byte[n_iso_data2];
			System.arraycopy( iso_data2, 0, this.iso_data2, 0, n_iso_data2);
		}
		else{
			this.iso_data2 = new byte[0];
		}
	}
	
	private synchronized byte[] getIso_data3() 
	{
		byte[] data = new byte[iso_data3.length];
		System.arraycopy(iso_data3, 0, data, 0, iso_data3.length);
		return data;
	}
	
	private synchronized void setIso_data3(byte[] iso_data3, int n_iso_data3) 
	{
		if( n_iso_data3 > 0){
			this.iso_data3 = new byte[n_iso_data3];
			System.arraycopy( iso_data3, 0, this.iso_data3, 0, n_iso_data3);
		}
		else{
			this.iso_data3 = new byte[0];
		}
	}


	///////////////////////////////////////////
	/*
	 * analysis card data.
	 */

	private final static int MAX_SIZE_CARD_PAN = 19;
	private final static int MAX_SIZE_CARD_HOLDER_NAME = 57;
	private final static int SIZE_CARD_EXPIRATION_DATA = 4;
	private final static int SIZE_CARD_SERVICE_CODE_DATA = 3;
	private final static int MAX_SIZE_DISCRETIONARYDATA = 110;
	
	private enum type_card{
		MST_UNDEFINED,
		MST_CREDIT_INTERNATIONAL,
		MST_AMERICAN_EXPREEE,
		MST_DINERS_CLUB,
		MST_JCB,
		MST_VISA,
		MST_MASTER_CARD,
		MST_CHINA_UNION_PAY
	};	


	private static String byteArrayToString( byte[] b)
	{
		StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length && b[i] != 0 ; ++i) {
            sb.append((char)b[i]);
        }
        return sb.toString();
	}
	
	void analysisCardData()
	{
		byte cTrack = 0;
		byte cData;
		int i = 0;
		int nBuf = 0;
		int nRaw[] = {
				this.iso_data1.length,
				this.iso_data2.length,
				this.iso_data3.length
		};
		boolean bError = false;
		
		//card data
		byte card_cFormatCode = 0;
		int card_nPan = 0;
		byte[] card_sPan = new byte[Lpu237MSRService.MAX_SIZE_CARD_PAN];
		int card_nName = 0;
		byte[] card_sName = new byte[Lpu237MSRService.MAX_SIZE_CARD_HOLDER_NAME];
		byte[] card_sExpDate = new byte[Lpu237MSRService.SIZE_CARD_EXPIRATION_DATA];
		byte[] card_sServiceCode = new byte[Lpu237MSRService.SIZE_CARD_SERVICE_CODE_DATA];
		type_card card_MsCardType = type_card.MST_UNDEFINED;
		byte[] card_track1DiscretionaryData = new byte[Lpu237MSRService.MAX_SIZE_DISCRETIONARYDATA];
		byte[] card_track2DiscretionaryData = new byte[Lpu237MSRService.MAX_SIZE_DISCRETIONARYDATA];
		
		do{
			// analysis iso1 track.
			if( nRaw[cTrack] > 0 ){
				cData = this.iso_data1[0];
				if( cData != 'B' ){	i=nRaw[cTrack];	}//exit for
				else{	i=1;	card_cFormatCode = cData;	}
	
				//PAD
				for( ; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data1[i];
					if( cData == '^' ){	i++;	break;	}//exit for
					else{
						if( card_nPan< Lpu237MSRService.MAX_SIZE_CARD_PAN )	card_sPan[card_nPan] = cData;
						else{	i=nRaw[cTrack];	}
					}
				}//end for
	
				//Card Holder Name
				for( ; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data1[i];
					if( cData == '^' ){	i++;	card_sName[card_nName] = 0x00;	break;	}//make zero-string
					else{
						if( card_nName< Lpu237MSRService.MAX_SIZE_CARD_HOLDER_NAME )	card_sName[card_nName++] = cData;
						else{	i=nRaw[cTrack];	}
					}
				}//end for
	
				//Expiration data
				for( ; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data1[i];
					if( cData == '^' ){	i++;	break;	}//exit for
					if( nBuf < Lpu237MSRService.SIZE_CARD_EXPIRATION_DATA )	card_sExpDate[nBuf++] = cData;
					else 	break;
				}// end for
				
				//SERVICE CODE 
				nBuf = 0;
				for( ; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data1[i];
					if( cData == '^' ){	i++;	break;	}//exit for
					if( nBuf < Lpu237MSRService.SIZE_CARD_SERVICE_CODE_DATA )	card_sServiceCode[nBuf++] = cData;
					else 	break;
				}// end for
				
				nBuf = 0;
				for( ; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data1[i];
					// here Optional Discretionary data
					// TODO
					card_track1DiscretionaryData[nBuf++] = cData;
				}//end for
			}
	
			// analysis iso2 track.
			cTrack++;
			if( nRaw[cTrack] > 0 ){
				nBuf = 0;
				for( i=0; i<nRaw[cTrack]; i++  ){
					cData = this.iso_data2[i];
					if( cData == '=' ){	i++;	break;	}//exit for
					else{
						if( nBuf < Lpu237MSRService.MAX_SIZE_CARD_PAN ){
							if( card_nPan>0 ){
								if( cData != card_sPan[nBuf++] ){	nBuf = 0; 	i = nRaw[cTrack];	}//Error : trac1 PAN is different from track2 PAN
							}
							else{	card_sPan[nBuf++] = cData;	}
						}
						else{	nBuf = 0;	i = nRaw[cTrack];	}	//error
					}
				}// end for
	
				if( nBuf > 0 ){
					card_nPan = nBuf;
					//Expiration data
					for( nBuf = 0; i<nRaw[cTrack]; i++  ){
						cData = this.iso_data2[i];
						if( cData == '=' ){	i++;	break;	}//exit for
						if( nBuf < Lpu237MSRService.SIZE_CARD_EXPIRATION_DATA ){
							if( card_cFormatCode == 'B' ){
								if( card_sExpDate[nBuf] != cData ){
									nBuf = 0;	i = nRaw[cTrack];	//error
								}
							}
							else	card_sExpDate[nBuf] = cData;
	
							nBuf++;
						}
						else 	break;
					}// end for
					//Service code
					for( nBuf = 0; i<nRaw[cTrack]; i++  ){
						cData = this.iso_data2[i];
						if( cData == '=' ){	i++;	break;	}//exit for
						if( nBuf < Lpu237MSRService.SIZE_CARD_SERVICE_CODE_DATA ){
							if( card_cFormatCode == 'B' ){
								if( card_sServiceCode[nBuf] != cData ){
									nBuf = 0;	i = nRaw[cTrack];	//error
								}
							}
							else	card_sServiceCode[nBuf] = cData;
	
							nBuf++;
						}
						else 	break;
					}// end for
	
					nBuf = 0;
					for( ; i<nRaw[cTrack]; i++  ){
						cData = this.iso_data2[i];
						// here Optional Discretionary data
						card_track2DiscretionaryData[nBuf++] = cData;
					}//end for
				}
			}
	
			if( card_nPan < 12 ){
				//the current card isn't a credit card.
				card_cFormatCode = 0;
				card_nPan = card_nName = 0;
				continue;
			}

			for( i=0; i<card_nPan; i++ ){
				if( card_sPan[i] == ' ' ){//remove space
					card_nPan = i;	break;	//exit for
				}
				else if( card_sPan[i]<'0' || card_sPan[i]>'9'){
					//the current card isn't a credit card.
					card_cFormatCode = 0;
					card_nPan = card_nName = 0;
					bError = true;
					break;//exit for
				}
			}//end for
			
			if( bError){
				//the current card isn't a credit card.
				continue;
			}

			//
			String s_pan = byteArrayToString( card_sPan );
			card_MsCardType = type_card.MST_UNDEFINED;
			
			if( (card_nPan == 14 || card_nPan==15) && card_sPan[0]=='3' ){
				card_MsCardType = type_card.MST_AMERICAN_EXPREEE;
			}
			else if( s_pan.indexOf("51") == 0){
				card_MsCardType = type_card.MST_MASTER_CARD;
			}
			else if(  s_pan.indexOf("52") == 0){
				card_MsCardType = type_card.MST_MASTER_CARD;
			}
			else if( s_pan.indexOf("53") == 0 ){
				card_MsCardType = type_card.MST_MASTER_CARD;
			}
			else if( s_pan.indexOf("54") == 0 ){
				card_MsCardType = type_card.MST_MASTER_CARD;
			}
			else if( s_pan.indexOf("55") == 0 ){
				card_MsCardType = type_card.MST_MASTER_CARD;
			}
			else if( card_nPan == 16 && card_sPan[0]=='3' ){
				card_MsCardType = type_card.MST_JCB;
			}
			else if( s_pan.indexOf("3616")==0 && card_nPan == 15 ){
				card_MsCardType = type_card.MST_DINERS_CLUB;
			}
			else if( card_sPan[0]=='4' && card_nPan == 16 ){
				card_MsCardType = type_card.MST_VISA;
			}
			else if( s_pan.indexOf("621" )==0 && card_nPan == 16 ){
				card_MsCardType = type_card.MST_CHINA_UNION_PAY;
			}
			else if( s_pan.indexOf( "622" )==0 && card_nPan == 16 ){
				card_MsCardType = type_card.MST_CHINA_UNION_PAY;
			}
		}while(false);
		
		/*
		 * 		byte card_cFormatCode = 0;
		int card_nPan = 0;
		byte[] card_sPan = new byte[Lpu237MSRService.MAX_SIZE_CARD_PAN];
		int card_nName = 0;
		byte[] card_sName = new byte[Lpu237MSRService.MAX_SIZE_CARD_HOLDER_NAME];
		byte[] card_sExpDate = new byte[Lpu237MSRService.SIZE_CARD_EXPIRATION_DATA];

		 */
		if( !bError ){
			if( card_nPan > 0){
				this.accountNumber = byteArrayToString( card_sPan );
			}
			if( card_sExpDate[0] != 0 ){
				this.expirationDate = byteArrayToString( card_sExpDate );
			}
			if( card_sServiceCode[0] != 0 ){
				this.serviceCode = byteArrayToString( card_sServiceCode );
			}
			
			if( card_track1DiscretionaryData[0] != 0 ){
				this.track1DiscretionaryData = byteArrayToString( card_track1DiscretionaryData );
			}
			if( card_track2DiscretionaryData[0] != 0 ){
				this.track2DiscretionaryData = byteArrayToString( card_track2DiscretionaryData );
			}
			
			String account_name;
			String result[];
			if( card_sName[0] != 0 ){
				//this.firstName = byteArrayToString( card_sName );
				account_name = byteArrayToString( card_sName ).trim();
				//account_name ="HOGAN/PAUL A.DR ";
				//account_name = "HOGAN/PAUL A    ";
				result = account_name.split("/", 2);
				if(result.length==2){	// exist '/'
					this.surName = result[0];
					this.firstName = result[1];
				}else{	// '/' none
					result = account_name.split(" ", 2);
					if(result.length==2){// ' ' exist
						this.surName = result[0];
						this.firstName = result[1];
					}else{	// ' ' none
						this.surName = account_name;
					}
				}
				//first name ->
				result = this.firstName.split(" ", 2);
				if(result.length==2){// ' ' exist
					this.firstName = result[0];
					this.middleInitial = result[1];
					result = this.middleInitial.split(" ", 2);
					if(result.length==2){// ' ' exist
						this.middleInitial = result[0];
					}
					result = this.middleInitial.split("\\.", 2);
					if(result.length==2){// '.' exist
						this.middleInitial = result[0];
						this.title = result[1];
					}
				}
				
				
				
/*				
				int nameDelim = account_name.indexOf("/");
				if( nameDelim != -1 ){
			            this.surName = account_name.substring(0, nameDelim);
			            //this.firstName = account_name.substring(nameDelim+1);
			            account_name = account_name.substring(nameDelim+1);
			            nameDelim = account_name.indexOf(".");
			            if( nameDelim != -1 ){
			            	this.firstName = account_name.substring(0, nameDelim);
			            	this.title = account_name.substring(nameDelim+1);
			            }else{
				           nameDelim = account_name.indexOf(" ");
				           if( nameDelim != -1 ){
				            	this.firstName = account_name.substring(0, nameDelim);
				            	this.middleInitial = account_name.substring(nameDelim+1);
				            }else{
				            	this.firstName = account_name;
				            }
			            }
			        }else{
			        	nameDelim = account_name.indexOf(" ");
			        	if( nameDelim != -1 ){
			            	this.surName = account_name.substring(0, nameDelim);
			            	this.firstName = account_name.substring(nameDelim+1);
			        	}else{
			        		this.firstName = account_name;
			        	}
			        }
        				
    */    			
    				//this.suffix = "suffix?";	
			}
		}
	}
	
	/**
	 * set all parameter with iso_data1, iso_data2, iso_data3
	 */
	private void set_all()
	{
		int n_start = 0;
		int n_stop = 0;
		int i = 0;
		
		accountNumber = "";
        track1Data = "";
        track2Data = "";
        track3Data = "";
        expirationDate = "";
        title = "";
        firstName = "";
        middleInitial = "";
        surName = "";
        suffix = "";
        serviceCode = "";
        track1DiscretionaryData = "";
        track2DiscretionaryData = "";

        
		// setting ISO1 track
		do{
			this.track1Data = "";
			
			if( this.iso_data1.length <= 0 )
				continue;
			if( (this.tracksToRead & MSRConst.MSR_TR_1) !=  MSRConst.MSR_TR_1)
				continue;
			//
			n_start = 0;
			n_stop = this.iso_data1.length;
			
			for( i=n_start; i<n_stop; i++ ){
				this.track1Data += (char)this.iso_data1[i];
			}//end for
			//
		}while(false);
		
		//setting ISO2 track
		do{
			this.track2Data = "";
			
			if( this.iso_data2.length <= 0 )
				continue;
			if( (this.tracksToRead & MSRConst.MSR_TR_2) !=  MSRConst.MSR_TR_2)
				continue;
			//
			n_start = 0;
			n_stop = this.iso_data2.length;
			
			for( i=n_start; i<n_stop; i++ ){
				this.track2Data += (char)this.iso_data2[i];
			}//end for
		}while(false);

		//setting ISO3 track
		do{
			this.track3Data = "";
			
			if( this.iso_data3.length <= 0 )
				continue;
			if( (this.tracksToRead & MSRConst.MSR_TR_3) !=  MSRConst.MSR_TR_3)
				continue;
			//
			n_start = 0;
			n_stop = this.iso_data3.length;
			
			for( i=n_start; i<n_stop; i++ ){
				this.track3Data += (char)this.iso_data3[i];
			}//end for
		}while(false);
		
		//
		do{
			if( !this.parseDecodeData ){
				this.accountNumber = "";
				this.expirationDate = "";
				this.firstName = "";
				this.middleInitial = "";
				this.serviceCode = "";
				this.suffix = "";
				this.surName = "";
				this.title = "";
				this.track1DiscretionaryData = "";
				this.track2DiscretionaryData = "";
				continue;
			}
			// parsing card data.
			analysisCardData();
			
		}while(false);
		
		
	}
	
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#kill()
	 */
	public void kill() {
		synchronized (this) {
			Lpu237MSRService.shutdown = true;
		}
		
    	try {
			worker.join(Lpu237MSRService.join_wait_time_mm);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	synchronized (this) {
    		Lpu237MSRService.shutdown = false;
    	}
	}    
	
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#deleteInstance()
	 */
    //@Override
	public void deleteInstance() throws JposException {
    	try{
    		lpu237_end();
    		//unLoadLibrary();
    		checkIfOpen();
    	}
    	catch(JposException e){
    	}
    }
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#reset()
	 */
	public void reset() {
    }
    
    /* (non-Javadoc)
	 * @see kr.co.elpusk.javapos.msr.MSRService#run()
	 */
//    @Override
	public void run() {
    	
    	tracer.println("worker : start . \n");
    	//System.out.println("worker : start .\n");
        while (true) {
            try {
                Thread.sleep(10);

            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            
            //
            if( !IsEmptyQ() ){
            	command cmd = DeQ();
            	
            	switch( cmd ){
            	case cmd_start_wait:
            		lpu237_wait_read();
            		//System.out.println("lpu237_wait_read()\n");
            		break;
            	default:
            		break;
            	}//end switch
            }
            
			if(Thread.interrupted()){  //Thread.interrupted()
				break;
			}
			
			synchronized(this) {
				if(Lpu237MSRService.shutdown) {
					break;
				}
			}			
        }//end while
        
        tracer.println("worker : stop . \n");
        //System.out.println("\"worker : stop .\n");
    }    

}
