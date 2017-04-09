/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rfidsystem;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import ufrsimplest.UfrCoder;
import static ufrsimplest.UfrCoder.GetLibFullPath;
import ufrsimplest.UfrCoder.uFrFunctions;
import java.lang.System;
import java.lang.*; 
import java.sql.*;  // packages containing the JDBC classes need for  database programming 
import java.util.ArrayList;
import java.util.List;
import java.io.*; 
import java.net.*;




/**
 *
 * @author fansua
 */
public class RFID implements Runnable 
{
    
    final static byte DL_MIFARE_CLASSIC_1K = 0x21; 
    boolean deviceConn = false;
    boolean systemStart = false; 
    int cardResult = 0;
    int readerResult= 0; 
    int [] readerType = new int[1]; 
    byte [] cardUID = new byte[9]; 
    private boolean loopStart; 
    ByteByReference dlCardType;
    IntByReference cardSerial = new IntByReference();
    ByteByReference cardUIDSize = new ByteByReference();
    ByteByReference cardType = new ByteByReference();
    String[] errorCodeList = new String[200];
    UfrCoder.ERRORCODES EC;
    UfrCoder.ERRORCODES[] ERRCODES = UfrCoder.ERRORCODES.values();
    final static int FUNCT_LIGHT_OK = 4;
    final static int FUNCT_SOUND_OK = 0; //4 Tripple sound
    final static int FUNCT_LIGHT_ERROR = 2;
    final static int FUNCT_SOUND_ERROR = 0;//2 Long sound
    final static int DL_OK = 0;
    final static byte MIFARE_AUTHENT1A = 0x60;
    final static byte   KEY_INDEX = 0;
   // final static byte   MAX_BLOCK = 16;
    final static int SLEEP_VALUE = 500;// was 500 
     final static int SLEEP_VAL = 30000;// was 500 
    final static int SLEEP_VALUE1 = 3;
    static final String JDBCDriver = "org.mariadb.jdbc.Driver";
    static final String JDBUrl = "jdbc:mariadb://localhost:3306/";
    static final String userNameDB = "root";
    static final String passDB = "password321";
    String devComConn = "";
    String errorExplainToSend="";
    String errorCodeToSend="";
    String cType="";
    String cSize="";
    String cNum="";
    int writeCount =0; 
    boolean linearAddressStatus = false; 
    int wLinearAddress  = 0;
    boolean tableUpdate = false; 
    private  String dataString="";
    
    uFrFunctions rfidObj; 
    
    
   
    public RFID()
    {
        this.dlCardType = new ByteByReference(); 
         FillEcArray();  // Fill error Code array 
        connCommandDB();  // should change this to initilize all the databases
       // logDB();
        try
        {
             rfidObj = (uFrFunctions) Native.loadLibrary(GetLibFullPath(false), uFrFunctions.class);
        } 
        catch (UnsatisfiedLinkError e)
        {
            System.out.println("Unable to load libarary for path: " + GetLibFullPath(false));
        }
        
        
    }
    private void FillEcArray()
    {
        for (UfrCoder.ERRORCODES ECC : EC.values()) 
            errorCodeList[ECC.getValue()] = ECC.name();    
    }
    private void connCommandDB()
    {
        Connection connDB = null; 
        Statement statementDB = null; 
        try{
            
            //System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
           // System.out.println("Connection successfully");
         
           // System.out.println("Attempting to create commandDB database");
            statementDB = connDB.createStatement();
            String sql = "CREATE DATABASE IF NOT EXISTS commandDB";
            statementDB.executeUpdate(sql);
           // System.out.println("Database,  commandDB was created successfully");
           // System.out.println("Atempting to create  table instructions in commandDB");
             sql = "CREATE TABLE IF NOT EXISTS commandDB.instructions "
                //+ "(id INTEGER not NULL, "  //change back later 
                + " (ReaderStatus VARCHAR(255), "
                + " ErrorCode VARCHAR(255), "
                + " ErrorExplain VARCHAR(255), "
                + " CardID VARCHAR(255),"
                + " CardType VARCHAR(255),"
                + " CardSize VARCHAR(255),"
                + " PageNumber VARCHAR(255),"
                + " AccessStatus VARCHAR(255),"
                + " Temperature VARCHAR(255))";
              //  + " PRIMARY KEY ( ConnOrNot ))";   //change back to id
           statementDB.executeUpdate(sql);
           
            //System.out.println("Created instructions Table in commandDB database");
            
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }//catch(Exception e ){
            //  e.printStackTrace();  // handles error for Class.forName  
       // }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
        //System.out.println("Moving On!");
    }
   /* private void logDB()
    {
        Connection connDB = null; 
        Statement statementDB = null; 
        try{ 
          //  System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
           // System.out.println("Connection successfully");
         
            //System.out.println("Attempting to create logDB database");
            statementDB = connDB.createStatement();
            String sql = "CREATE DATABASE IF NOT EXISTS logDB";
            statementDB.executeUpdate(sql);
           // System.out.println("Database,  logDB was created successfully");
          //  System.out.println("Atempting to create  table instructions in commandDB");
             sql = "CREATE TABLE IF NOT EXISTS logDB.logInformation "
                + "(id INTEGER not NULL, "
                + " first VARCHAR(255), "
                + " last VARCHAR(255), "
                + " age INTEGER, "
                + " PRIMARY KEY ( id ))";
           statementDB.executeUpdate(sql);
           
            //System.out.println("Created instructions Table in commandDB database");
            
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }//catch(Exception e ){
            //  e.printStackTrace();  // handles error for Class.forName  
       // }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
        //System.out.println("Goodbye!");
    }
    */
     public boolean GetFunct()
    {
        return systemStart;
    }
     
     public void SetLoop(boolean bValue) 
    {
        this.loopStart = bValue;
    }

    public boolean GetLoop()
    {
        return loopStart;
    }
           
    private void printStatus(int iResultValue) 
    {

        System.out.println("Error Code: " + "0x" + Integer.toHexString(iResultValue).toUpperCase());
        System.out.println("Error Explanation: " + errorCodeList[iResultValue]);
        errorCodeToSend = Integer.toHexString(iResultValue).toUpperCase();
        errorExplainToSend = errorCodeList[iResultValue] ;
        
    }
    private void createSendStatus(String devCon,int iResultValue,String cType,String cSize,String cNum,boolean status, boolean aStatus)
    {
        String cardType = "RF_NULL";
        String cardSize ="RF_NULL";
        String cardNum ="RF_NULL";
        if(status)
        {
            cardType = cType;
            cardSize = cSize; 
            cardNum = cNum; 
            
        }
      
        errorCodeToSend = Integer.toHexString(iResultValue).toUpperCase(); // have to send these two 
        errorExplainToSend = errorCodeList[iResultValue] ;
     
        
        //System.out.println("Sending commands to the database");
        //sendTodata for insertion and updating
        sendToDatabase(devCon,errorCodeToSend,errorExplainToSend,cardNum,cardType,cardSize,aStatus);
    }
    private void sendToDatabase(String devCon,String errorCode,String errorExplain,String cardNum,String cardType,String cardSize,boolean aStatus)
    {
        String sql ="";
        
  
        
        Connection connDB = null; 
        Statement statementDB = null; 
        try{
            //System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
            //System.out.println("Connection successfully");
         
            //System.out.println("Attempting to insert into the database");
            statementDB = connDB.createStatement();
           if(!tableUpdate)
           {
                sql = String.format("INSERT INTO commandDB.instructions (ReaderStatus,ErrorCode,ErrorExplain,CardID,CardType,CardSize,AccessStatus) Values('%s','%s','%s','%s','%s','%s','%s')",devCon,errorCode,errorExplain,cardNum,cardType,cardSize,aStatus);
                tableUpdate = true; 
           }
           else
           {
                sql = String.format("UPDATE commandDB.instructions SET ReaderStatus='%s',Errorcode='%s',ErrorExplain='%s',CardID='%s',CardType='%s',CardSize='%s',AccessStatus='%s'",devCon,errorCode,errorExplain,cardNum,cardType,cardSize,aStatus);
           }
            statementDB.executeUpdate(sql);
          //  System.out.println(" Command Infomation was added to the commandDB succesfully");
         
            
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
       // System.out.println("Commands has been sent");
        
    }
    

    
  private String getTemperature()
  {
      String sql ="";
        String temp ="";
        Connection connDB = null; 
        Statement statementDB = null; 
        try{
            //System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
            //System.out.println("Connection successfully");
         
            //System.out.println("Attempting to insert into the database");
            statementDB = connDB.createStatement();
           
             sql = "SELECT Temperature from commandDB.instructions";  
       
           ResultSet rs = statementDB.executeQuery(sql);
          //  System.out.println(" Command Infomation was added to the commandDB succesfully");
         try
            {
                while(rs.next())
                {
                    int numCol = rs.getMetaData().getColumnCount(); 
                    for(int i =1; i<= numCol; i++)
                    {
                       //idList.add(rs.getString(i)); //getInt
                    temp = rs.getString(i);
                        
                   }
                }
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch(SQLException rse)
                {
                }
                
            }
            
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
       // System.out.println("Commands has been sent");
       return temp; 
        
      
  }
  
  
   private void writeOnTag()
    { 
       
        try{
       
                // data area to  write
                
                
               dataString =  getTemperature();
               if(dataString != null)
               {
                    System.out.println("When I get here the temp is "+ dataString);
                    int dataStringLen = dataString.length(); 
                    System.out.println(dataStringLen);
                    if(!linearAddressStatus)
                    {
                        wLinearAddress = 0; 
                        linearAddressStatus = true;
                    }
            
                    int result = 0; 
                    ShortByReference shBytesWritten = new ShortByReference(); 
            
                    byte[] dataToWrite = new byte[dataStringLen];
                    dataToWrite = dataString.getBytes(); 
                    result = rfidObj.LinearWrite(dataToWrite, wLinearAddress, dataStringLen, shBytesWritten, MIFARE_AUTHENT1A, KEY_INDEX);
                    if (result == DL_OK) 
                    {
                        rfidObj.ReaderUISignal(FUNCT_LIGHT_OK, FUNCT_SOUND_OK);
                        writeCount +=1;
                    }
                    else 
                    {
                        rfidObj.ReaderUISignal(FUNCT_LIGHT_ERROR, FUNCT_SOUND_ERROR);
                    }
                    readFromTag(); 
                    wLinearAddress +=  dataStringLen;
                    //if(writeCount == 20){}
                    // check if the count ==  14  then erase the card 

                }   
            }
        finally
        {
        }
    }
    private void readFromTag() 
    {
        try {

     

            byte[] dataToRead = new byte[752];
            int rLinearAddress = 0;
            int dataToReadLen = dataToRead.length;
            int iFResult = 0;
            ShortByReference shBytesRet = new ShortByReference();
            iFResult = rfidObj.LinearRead(dataToRead, rLinearAddress, dataToReadLen, shBytesRet, MIFARE_AUTHENT1A, KEY_INDEX);
            if (iFResult == DL_OK) {
                //txtLinearRead.setText(new String(baReadData));
                System.out.println("This is the data on the card" + new String(dataToRead));
                rfidObj.ReaderUISignal(FUNCT_LIGHT_OK, FUNCT_SOUND_OK);
 
            } else {
                rfidObj.ReaderUISignal(FUNCT_LIGHT_ERROR, FUNCT_SOUND_ERROR);
 
            }

        } finally {

        }
        
    }
 
    
    
    private String getPhpPage()
    {
        String sql ="";
        String pageNum ="";
        Connection connDB = null; 
        Statement statementDB = null; 
        try{
            //System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
            //System.out.println("Connection successfully");
         
            //System.out.println("Attempting to insert into the database");
            statementDB = connDB.createStatement();
           
             sql = "SELECT PageNumber from commandDB.instructions";  
       
           ResultSet rs = statementDB.executeQuery(sql);
          //  System.out.println(" Command Infomation was added to the commandDB succesfully");
         try
            {
                while(rs.next())
                {
                    int numCol = rs.getMetaData().getColumnCount(); 
                    for(int i =1; i<= numCol; i++)
                    {
                       //idList.add(rs.getString(i)); //getInt
                    pageNum = rs.getString(i);
                        
                   }
                }
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch(SQLException rse)
                {
                }
                
            }
            
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
       // System.out.println("Commands has been sent");
       return pageNum; 
        
    }
    private void validateUserAccess(String id)
    {
          // query database return a list of ID;
        List<String> idLst; 
        idLst  = getIdList(); 
         boolean idFound = false; 
         boolean faceFound = false; 
         boolean faceConn = false; 
        byte Val; 
        for(String val: idLst)
        {
            if(id.equals(val))
            {
               
                idFound = true; 
               Val  = checkFace('T');
              if(Val == 84)  //  ascii for T
              {
                System.out.println("Face was found");
               // writeOnTag();
                createSendStatus(devComConn,cardResult,cType,cSize,cNum,true,true);
                // send status here 
                faceFound = true; 
                faceConn = true; 
                break;
              }
              else if (Val == 70)  // ascii for  F
              {
                  //System.out.println("Face was NOT found"); //VAL  IS 70  UNSSIGNED  R
                  faceFound = false;  // tag in  system  but no face match 
                  faceConn = true;
                  break; 
                  //notFound= true;  
              }
              else 
              {
                  faceConn = false;
                  faceFound = false;
           
              }
                //unlockDoor(); 
                //unlockDoor("True"); // this is the one i run 
               // unlockDoor("False");
                
                
            }
            else
            {
                 //print that person does not exist 
                // check for false entry {check sensor /trigger alarm}
                // temporarily will take a picture
               // System.out.println("The person does not exist");
                idFound = false;    // this means the  tag  is not in the system
                faceConn = false; 
                faceFound = false; 
               // triggerAlarm(); 
                // check for false entry 
                //unlockDoor("False"); 
                continue;
            }
            
        }
        if(!idFound)
        {
            System.out.println("LOWKEY INTRUDER!! .. YOU DONT WORK HERE");
            createSendStatus(devComConn,cardResult,cType,cSize,cNum,true,false);
        }
        if(idFound && faceConn && !faceFound)
        {
            System.out.println("BLOODY THIEF!!...STOP STEALING ID'S");
            createSendStatus(devComConn,cardResult,cType,cSize,cNum,true,false);
        }
        if(idFound && !faceConn && !faceFound)
        {
             System.out.println("UNABLE TO VERIFY FACE.. PLEASE START FACIAL RECOG SOFTWARE!!");
             createSendStatus(devComConn,cardResult,cType,cSize,cNum,true,false);
        }
        
    }
    private byte checkFace(char c)
    {
          byte haveAccess = 0;
        try
        {
            System.out.println("Establishing connection to host");
            Socket connSoc =  new Socket("192.168.2.120",5070);
            System.out.println("Connection Successful");
            //Sending the message  to the  server 
	    DataOutputStream dataSend = new DataOutputStream(connSoc.getOutputStream()); 
	    dataSend.writeByte(c);
            dataSend.flush(); 
           DataInputStream dataReceive = new DataInputStream(connSoc.getInputStream());
           haveAccess =  dataReceive.readByte();
           System.out.println(haveAccess);
           dataSend.close();
	   dataReceive.close(); 
	    connSoc.close();
    
        }
        catch(Exception e)
        {
            System.out.println("Please start facial Recognition software");
        }
        return haveAccess; 
    }
    
    private List<String> getIdList()
    {
        Connection connDB = null; 
        Statement statementDB = null; 
        List<String> idList = new ArrayList<String>();
        try
        {
            
            //System.out.println("Attempting to  establishah connection to the host");
            connDB = DriverManager.getConnection(JDBUrl,userNameDB,passDB); // opening a connection
            //System.out.println("Connection successfully");
         
           // System.out.println("Attempting to query the database");
            statementDB = connDB.createStatement();
            String sql = "SELECT EmployeeID from EMPLOYEEDB.EMPLOYEEINFO";   
            ResultSet rs = statementDB.executeQuery(sql);
            //System.out.println("Fetching the info was success");
            try
            {
                while(rs.next())
                {
                    int numCol = rs.getMetaData().getColumnCount(); 
                    for(int i =1; i<= numCol; i++)
                    {
                        idList.add(rs.getString(i)); //getInt
                        
                    }
                }
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch(SQLException rse)
                {
                }
                
            }
            
         
            
        }catch(SQLException se){ 
              se.printStackTrace();  // handles error for jdbc 
        }//catch(Exception e ){
            //  e.printStackTrace();  // handles error for Class.forName  
       // }
        finally
        {
            try{
                if(statementDB != null)
                    statementDB.close();
            }catch(SQLException se){
           
            }
            try{
                if(connDB != null)
                    connDB.close(); 
            }catch(SQLException se){
                 // se.printStackTrace();
            }
        }
        //System.out.println("Moving On!");
        return idList;
            
    }
     private synchronized void MainLoop() 
    {
        
        String sBuffer = "";
        String  scannedID = "";
        String pageNumber ="";
        byte bUidSize;
        SetLoop(true);
        if (!deviceConn) 
        {
            readerResult = rfidObj.ReaderOpen();
            if (readerResult == 0) 
            {
                deviceConn = true;
                devComConn= "Connected";
                printStatus(readerResult);
                
            } 
            else 
            {
              //  System.out.println("The device is not Connected");
                devComConn ="Not Connected";
            }

        }
        sBuffer = "";
        if (deviceConn) 
        {
            readerResult = rfidObj.GetReaderType(readerType);  // manipulated a ointer with 4bytes & retuns status 
            if (readerResult == DL_OK)
            {
                cardResult = rfidObj.GetDlogicCardType(dlCardType);

                if (cardResult == DL_OK) 
                {
                    cardResult = rfidObj.GetCardIdEx(cardType, cardUID, cardUIDSize); // place the card info in each parameter & return whether it was successful
                    if (cardResult == DL_OK) 
                    {
   
                        bUidSize = cardUIDSize.getValue();
                        for (byte bCount = 0; bCount < bUidSize; bCount++)
                        {
                            sBuffer += Integer.toHexString((((char) cardUID[bCount] & 0xFF)));
                        }
                    }
                     cType= Integer.toHexString(dlCardType.getValue()).toUpperCase();
                    cSize= Integer.toHexString(cardUIDSize.getValue()).toUpperCase();
                    cNum= sBuffer.toUpperCase().toUpperCase();
                    printStatus(cardResult);
                    createSendStatus(devComConn,cardResult,cType,cSize,cNum,true,false); // send connection status & card info to DB 
                 
                     pageNumber = getPhpPage();
                      writeOnTag();
                     if(pageNumber != null)
                        if(pageNumber.equals("2"))
                            validateUserAccess(cNum);
                    
                } 
                else 
                {
                    printStatus(cardResult);
                    createSendStatus(devComConn,cardResult,cType,cSize,cNum,false,false);
                }
            } 
            else 
            {
                //System.out.println("This fucking connection is "+devComConn);
                deviceConn = false;
                //createSendStatus(devComConn,readerResult,cType,cSize,cNum,false);
                rfidObj.ReaderClose();
                printStatus(readerResult);
                
              
            } 
        } // end if device is connected 
       else  // DEVICE IS NOT CONNECTED 
        {
            System.out.println("RFR said that the device is:"+devComConn);
            createSendStatus(devComConn,readerResult,cType,cSize,cNum,false,false);  // false card info doesnt  exist 
            
        } 

        SetLoop(false);

    }
     

        public void run() 
        {
           
            try 
            {

                while (!Thread.interrupted()) 
                {
                    if (!GetFunct()) 
                    {
                        MainLoop();
                        Thread.yield();
                    }

                    TimeUnit.MILLISECONDS.sleep(SLEEP_VALUE);
                }
            } catch (InterruptedException ex) 
            {
                Logger.getLogger(RFID.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
     
    public static void main(String args[])
    {
               RFID rfid  = new RFID(); 
              Thread rfidThread = new Thread(rfid);
               rfidThread.start(); 
               
               
    }
    
}
