/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.clientapp_wallet;

import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author Benlakhal
 */
public class Client_Wallet{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException{
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

        //Send APDU
        CommandAPDU commandAPDU = new CommandAPDU(0xB0, 0x00, 0x00, 0x00);
        ResponseAPDU response = simulator.transmitCommand(commandAPDU);

        //Check response status word
        if (response.getSW() == 0x9000) {
            short balance = (short) ((response.getData()[0] << 8) | (response.getData()[1] & 0xFF));
            System.out.println("Balance: " + balance);
        } else {
            System.out.println("Balance inquiry failed. SW: " + Integer.toHexString(response.getSW()));
        }
    }

}
