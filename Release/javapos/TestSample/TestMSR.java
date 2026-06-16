import jpos.*;
import jpos.events.*;


public class TestMSR implements DataListener {

     private MSR msr;
     public  int iSwipes=0;
     public  boolean running=false;

     public TestMSR(String sDeviceName) 
     {
          msr=new MSR(); // Create a new instance of the MSR

          try{
               // 
               msr.open(sDeviceName); 

               // the data through dataOccurred() function
               msr.addDataListener(this);
               
               System.out.println("Device Opened!");

               String ver = new Integer(msr.getDeviceControlVersion()).toString();
               System.out.println("Control: " + msr.getDeviceControlDescription() + " Version: v" + new Integer(ver.substring(0,1)) + "." + new Integer(ver.substring(1,4)) + "." + new Integer(ver.substring(4,7)) );
               
               ver = new Integer(msr.getDeviceServiceVersion()).toString();
               System.out.println("Service: " + msr.getDeviceServiceDescription() + " Version: v" + new Integer(ver.substring(0,1)) + "." + new Integer(ver.substring(1,4)) + "." + new Integer(ver.substring(4,7)));

              
               // claim the device with a 1 second timeout
               msr.claim(1000);
               System.out.println("Device Claimed!");
               
               // set the number of tracks to read
               msr.setTracksToRead(0x07);//Track1,2,3
 
               // Set the dataEventEnabled
               msr.setDataEventEnabled(true);
                
               // Set the deviceEnabled
               msr.setDeviceEnabled(true);
               
               msr.setAutoDisable(false);
                
               // set the running flag
               running = true;

          }catch(JposException e){
               System.err.print(e);
          }
      }

     public void closeMSR() {
          try{
                msr.release();
                System.out.println("Device Released!");

          }catch(JposException e){
               System.err.print(e);
          }
          /**
               Do something here.
           */
          try{
            msr.close();
            System.out.println("Device Closed!");
            // set the running to false
            running = false;
          }catch(JposException e){
               System.err.print(e);
          }
     }

     public void dataOccurred(DataEvent de)
     {
         try
         {
                  System.out.println("\n Data Event(" + (iSwipes+1) + ") =>");

                  // Get the length of the tracks read from the DataEvent
                  int t1Length = de.getStatus() & 0xFF;
                  int t2Length = (de.getStatus() & 0xFF00) >> 8;
                  int t3Length = (de.getStatus() & 0xFF0000) >> 16;
                  //int t4Length = (de.getStatus() & 0xFF000000) >> 32;
	          		   
                  String msrTrack1 = new String(msr.getTrack1Data());
                  String msrTrack2 = new String(msr.getTrack2Data());
                  String msrTrack3 = new String(msr.getTrack3Data());
                  //String msrTrack4 = new String(msr.getTrack4Data());
	                                   
	
                  // Output the tracks themselves
                  System.out.println("Track1(" + t1Length+")="+ msrTrack1 );
                  System.out.println("Track2(" + t2Length+")="+ msrTrack2 );
                  System.out.println("Track3(" + t3Length+")="+ msrTrack3 );
                  //System.out.println("Track4(" + t4Length+")="+ msrTrack4 );
                  
                  if(msr.getParseDecodeData()==true) {
	                  System.out.println("accountNumber: " + msr.getAccountNumber());
	                  System.out.println("expirationDate: " + msr.getExpirationDate());
	                  System.out.println("firstName: " + msr.getFirstName());
	                  System.out.println("middleInitial: " + msr.getMiddleInitial());
	                  System.out.println("surname: " + msr.getSurname());
	                  System.out.println("serviceCode: " + msr.getServiceCode());
	                  
	                  String t1DiscData = new String(msr.getTrack1DiscretionaryData());
	                  System.out.println("t1DiscData: " + t1DiscData);
	                  String t2DiscData = new String(msr.getTrack2DiscretionaryData());
	                  System.out.println("t2DiscData: " + t2DiscData);
                  }

                  // increment the number of swipes
                  iSwipes++;
                  
                  msr.setDataEventEnabled(true);
                  
         }
         catch(JposException je)
         {
                  // dump that we got an exception
                  System.err.println("MSR: Jpos Exception");
         }

     }
     
     public static void main(String [] args){

	TestMSR test;
	// Create a new instance of the TestMSR
 
	test = new TestMSR("lpu237MSR");
			
	// If the MSR is running then let the user know to swipe some 
	// cards
	if (test.running)
		System.out.println("Swipe a card, up to 5 times for program to exit");

	while ((test.running) && (test.iSwipes < 5) ) {
		try {
			for (int i = 0; i < 10; i++) {
				// sleep for 100ms
                			Thread.sleep(100);
	         		}
		} catch (InterruptedException e) {
			System.err.print(e);
		}

	}
			
		// If the test is still running then close it out
	if (test.running) 
		test.closeMSR();
	System.exit(0);
     }
}
	
