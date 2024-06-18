package com.mycompany.clientapp_wallet;

import javacard.framework.*;

/**
 * ClassicApplet3 Java Card Applet.
 * 
 * This applet echoes back incoming APDU data.
 * 
 * Author: Benlakhal
 */

public class Wallet_Applet extends Applet {

    // Instruction code for the echo command
    private static final byte ECHO_INS = (byte) 0x00;

    /**
     * Installs this applet.
     * 
     * @param bArray the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Wallet_Applet();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected Wallet_Applet() {
        register();
    }

    /**
     * Processes an incoming APDU.
     * 
     * @param apdu the incoming APDU
     */
    public void process(APDU apdu) {
        // Get the APDU buffer
        byte[] buffer = apdu.getBuffer();

        // Check if the applet is selected
        if (selectingApplet()) {
            return;
        }

        // Get the instruction byte
        byte ins = buffer[ISO7816.OFFSET_INS];

        // Check if the instruction is the ECHO command
        if (ins == ECHO_INS) {
            // Get the data length
            short dataLen = apdu.setIncomingAndReceive();
            
            // Echo back the incoming data
            apdu.setOutgoing();
            apdu.setOutgoingLength(dataLen);
            apdu.sendBytes(ISO7816.OFFSET_CDATA, dataLen);
        } else {
            // Unsupported instruction
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
