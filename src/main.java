import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main {
    public static void main(String[] args) throws IOException {
        Callable<Void> callableGetOn = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                getOn();
                return null;
            }
        };

        Callable<Void> callableWriteTemp = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                writeTemp();
                return null;
            }
        };

        List<Callable<Void>> taskList = new ArrayList<Callable<Void>>();
        taskList.add(callableWriteTemp);
        taskList.add(callableGetOn);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        SerialPort[] ports = SerialPort.getCommPorts();

        SerialPort comPort = ports[0];
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        InputStream in = comPort.getInputStream();
        try
        {
            for (;;){
                System.out.print((char)in.read());
               /*try
                {
                    //start the threads and wait for them to finish
                    executor.invokeAll(taskList);
                }
                catch (InterruptedException ie)
                {
                    //do something if you care about interruption;
                }*/
            }

        } catch (Exception e) { e.printStackTrace(); }

    }

    private static void writeTemp() throws IOException {
        Random r = new Random();
            double min = -50;
            double max = 100;
            double randomValue = min + (max - min) * r.nextDouble();
            String stringDouble = String.valueOf(randomValue);
            URL url = new URL("https://temperature-detector-36e8f-default-rtdb.firebaseio.com/PIC_DEVICE/temp.json");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("PUT");
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write("{\"data\": \"" + stringDouble + "\"}");
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();
            InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream();

    }

    private static void getOn() throws IOException {
            URL url = new URL("https://temperature-detector-36e8f-default-rtdb.firebaseio.com/PIC_DEVICE/on/data.json");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            System.out.println(response);

    }
}
