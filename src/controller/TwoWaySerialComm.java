package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import model.AssistentFile;
import model.ServerInfo;

public class TwoWaySerialComm
{
    public TwoWaySerialComm(String com)
    {
        super();
        
        while (true) {
        	try {
            	connect(com);
            	System.out.println("Conexão iniciada com o Arduino na "+com);
            	break;
    		} catch (Exception e) {
    			System.out.println("Falha ao conectar com microcontrolador, verifique se a porta está correta.");
    			try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e1) {
					System.out.println("Erro ao dar delay");
				}
    			System.out.println("Tentando novamente...");
    		}
        }
        
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2345);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

            }
        }     
    }
    
    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    //System.out.print(new String(buffer,0,len));
                	String value = new String(buffer,0,len);
                	if (!value.isEmpty() && !value.equals("0") && !value.equals("1")) {
                		String valor = value + " " +  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                		ServerInfo.setCurrentInfo(valor);
                		
                		AssistentFile f = new AssistentFile("C:/Arquivos", "log.txt");
                		f.write(valor);
                	}
                		
                }
            }
            catch ( IOException e )
            {
                System.out.println("Conexão com a placa perdida!");
                Thread.currentThread().interrupt();
                return;
            }            
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }                
            }
            catch ( IOException e )
            {
                System.out.println("Conexão com a placa perdida!");
                Thread.currentThread().interrupt();
                return;
            }            
        }
    }
}