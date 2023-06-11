import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class client {
    public static String buildMessage(String senderID, String receiverID, String cont, String name){
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
        String start ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <message ";
        String type = String.format("type=\"%s\" ","send_message");
        String sender = String.format("sender=\"%s\" ",senderID);
        String receiver = String.format("receiver=\"%s\" ",receiverID);
        String senderName = String.format("sender_name=\"%s\" ",name);
        String content = String.format("content=\"%s\" ",cont);
        String time = String.format("time=\"%s\" ",formattedTime);
        String end = "></message>";
        String result = start + type + sender + receiver + senderName + content + time + end;
        System.out.println(result);
        return result;
    }
    public static String buildAddDialog(String senderID, String receiverID, String name, String mes_type){
        if (!mes_type.equals("dialog_accept") && !mes_type.equals("dialog_request")) return "";
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
        String start ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <message ";
        String type = String.format("type=\"%s\" ",mes_type);
        String sender = String.format("sender=\"%s\" ",senderID);
        String receiver = String.format("receiver=\"%s\" ",receiverID);
        String senderName = String.format("sender_name=\"%s\" ",name);
        String time = String.format("time=\"%s\" ",formattedTime);
        String end = "></message>";
        String result = start + type + sender + receiver + senderName + time + end;
        System.out.println(result);
        return result;
    }
    public static void handleMessage(String receivedMessage, MutableString uniqueIdString, PrintWriter out){
        try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                SAXPars saxp = new SAXPars();
                saxp.setAttributeListener(map -> {
                    switch (map.get("type")){
                        case "ID_message":
                            uniqueIdString.setValue(map.get("receiver"));
                            System.out.println("Updated ID: " + uniqueIdString.getValue());
                            break;
                        case "send_message":
                            System.out.println("Send message");
                        default:
                            System.out.println("Unrecognized message");
                    }
                });
                InputStream is = new ByteArrayInputStream(receivedMessage.getBytes());
                parser.parse(is, saxp);
                System.out.println("Echoing: " + receivedMessage);
        }
        catch (IOException e) {
            System.err.println("IO Exception");
        }
        catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        Map<String, String> ContactNames = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        MutableString uniqueIdString = new MutableString("0");
        System.out.println("Enter your Name: ");
        MutableString ClientName = new MutableString(scanner.nextLine());
        final int PORT = 8080;
        InetAddress addr = InetAddress.getByName("localhost");
        System.out.println("addr = " + addr);
        System.out.println("uniqueId = " + uniqueIdString.getValue());
        Socket socket = new Socket(addr, PORT);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

        try {
            // Create a separate thread for receiving messages
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread receiveThread = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
                        if (receivedMessage.equals("END"))
                            break;
                        handleMessage(receivedMessage,uniqueIdString,out);
                    }
                }
                catch (IOException e) {
                    System.err.println("IO Exception");
                }
            });
            receiveThread.start();

            // Send messages
            String input_text;
            String input_ID;
            while (true) {
                input_ID = scanner.nextLine();
                input_text = scanner.nextLine();
                if (input_text.equalsIgnoreCase("END"))
                    break;

                String sanitizedInput = input_text.replace("\"", "&quot;");
                String message = buildMessage(uniqueIdString.getValue(), input_ID, sanitizedInput, ClientName.getValue());
                if (!message.equals("")) out.println(message);
            }
        }
//        try {
//            System.out.println("socket = " + socket);
//            //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String buf = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <message type=\"send_message\" sender=\"00\" receiver=\"01\" content=\"Test\"></message>";
//            out.println(buf);
//            System.out.println(buf);
//            Scanner scanner = new Scanner(System.in);
//            String input = scanner.nextLine();
//            String sanitizedInput = input.replace("\"", "&quot;");
//            String message = build_message(uniqueIdString,"01",sanitizedInput);
//            out.println(message);
//            out.println("END");
//        }
//
        finally {
            System.out.println("closing...");
            socket.close();
        }



    }
}
class MutableString {
    private String value;
    public MutableString(String value) { this.value = value; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

}