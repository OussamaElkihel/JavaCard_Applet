package com.mycompany.clientapp_wallet;

import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import java.util.Scanner;
import javacard.framework.AID;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Main application that tests the Wallet_Applet
 */
public class Client_Wallet {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException {
        CardSimulator simulator = new CardSimulator();
        AID appletAID = AIDUtil.create("F000000001");
        simulator.installApplet(appletAID, Wallet_Applet.class);

        //Create Terminal
        CardTerminal terminal = CardTerminalSimulator.terminal(simulator);

        //Connect to Card
        Card card = terminal.connect("T=1");
        CardChannel channel = card.getBasicChannel();

        //Select applet
        CommandAPDU selectCommand = new CommandAPDU(AIDUtil.select(appletAID));
        channel.transmit(selectCommand);

        //Start with PIN verification
        System.out.println("Enter PIN: ");
        Scanner scanner = new Scanner(System.in);
        String inputPin = scanner.nextLine();
        // Convert the input PIN to a byte array
        byte[] pinArray = new byte[inputPin.length()];
        for (int i = 0; i < inputPin.length(); i++) {
            pinArray[i] = (byte) (inputPin.charAt(i) - '0');
        }
        System.out.println("Pin Verification: \n");
        //Prepare and send APDU command
        CommandAPDU pin_apdu = new CommandAPDU(0xB0, 0x03, 0x00, 0x00, pinArray);
        ResponseAPDU response_pin = simulator.transmitCommand(pin_apdu);
        if (response_pin.getSW() == 0x9000) {
            System.out.println("Pin Verified Successfully \n");
        } else {
            System.out.println("Error: " + Integer.toHexString(response_pin.getSW()));
        }

        //Get the balance from the card
        System.out.println("Get Balance: \n");
        CommandAPDU bal_apdu = new CommandAPDU(0xB0, 0x00, 0x00, 0x00);
        ResponseAPDU response_bal = simulator.transmitCommand(bal_apdu);
        if (response_bal.getSW() == 0x9000) {
            short balance = (short) ((response_bal.getData()[0] << 8) | (response_bal.getData()[1] & 0xFF));
            System.out.println("Balance: " + balance);
        } else {
            System.out.println("Balance inquiry failed. SW: " + Integer.toHexString(response_bal.getSW()));
        }

        //Credit operation
        System.out.println("Inc Balance: \n");
        //Get increment value and convert it to byte
        System.out.println("Enter value: ");
        byte inputvalue = scanner.nextByte();
        byte[] array_inputvalue = new byte[1];
        array_inputvalue[0] = inputvalue;
        //Apdu command
        CommandAPDU inc_apdu = new CommandAPDU(0xB0, 0x02, 0x00, 0x00, array_inputvalue);
        ResponseAPDU response_inc = simulator.transmitCommand(inc_apdu);
        if (response_inc.getSW() == 0x9000) {
            System.out.println("Success ");
        } else {
            System.out.println("Failed. SW: " + Integer.toHexString(response_inc.getSW()));
        }
        
        //Get balance again to see the results
        System.out.println("Get Balance: \n");
        CommandAPDU bal0_apdu = new CommandAPDU(0xB0, 0x00, 0x00, 0x00);
        ResponseAPDU response_bal0 = simulator.transmitCommand(bal0_apdu);
        if (response_bal0.getSW() == 0x9000) {
            short balance = (short) ((response_bal0.getData()[0] << 8) | (response_bal0.getData()[1] & 0xFF));
            System.out.println("Balance: " + balance);
        } else {
            System.out.println("Balance inquiry failed. SW: " + Integer.toHexString(response_bal0.getSW()));
        }
    }

}
