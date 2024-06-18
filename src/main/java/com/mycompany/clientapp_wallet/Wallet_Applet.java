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

    // Class code
    private static final byte WAL_CLA = (byte) 0xB0;

    // Instruction code for the commands
    private static final byte GET_BAL = (byte) 0x00;
    private static final byte INC_BAL = (byte) 0x01;
    private static final byte DEC_BAL = (byte) 0x02;
    private static final byte VER_PIN = (byte) 0x03;

    private short balance = 500;

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

        // Check if the applet is selected
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] != WAL_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        byte ins = buffer[ISO7816.OFFSET_INS];

        switch (ins)  {
            case GET_BAL:
                apdu.setOutgoing();
                apdu.setOutgoingLength((byte) 2);
                buffer[0] = (byte) (balance >> 8);
                buffer[1] = (byte) (balance);
                apdu.sendBytes((short) 0, (short) 2);
                return;
            default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
