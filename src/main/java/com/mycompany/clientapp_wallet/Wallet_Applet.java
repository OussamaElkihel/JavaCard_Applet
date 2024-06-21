package com.mycompany.clientapp_wallet;

import javacard.framework.*;

/**
 * Wallet Java Card Applet.
 * This Applet performs basic operations: Get balance, Credit, Debit and PIN verification.
 */
public class Wallet_Applet extends Applet {

    // Class code
    private static final byte WAL_CLA = (byte) 0xB0;

    // Instruction code for the commands
    private static final byte GET_BAL = (byte) 0x00;
    private static final byte INC_BAL = (byte) 0x01;
    private static final byte DEC_BAL = (byte) 0x02;
    private static final byte VER_PIN = (byte) 0x03;

    short balance = 100;

    OwnerPIN pin;
    private static final byte MAX_TRY = (byte) 0x03;
    private static final byte MAX_SIZE = (byte) 0x03;
    private byte[] pinCode = {1, 2, 3};

    // signal the PIN validation is required
    // for a credit or a debit transaction
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;

    /**
     * Installs this applet.
     *
     * @param bArray the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Wallet_Applet(bArray, bOffset, bLength);
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected Wallet_Applet(byte[] bArray, short bOffset, byte bLength) {
        pin = new OwnerPIN(MAX_TRY, MAX_SIZE);
        pin.update(pinCode, (short) 0, MAX_SIZE);
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

        switch (buffer[ISO7816.OFFSET_INS]) {
            case GET_BAL:
                get_balance(apdu);
                return;
            case INC_BAL:
                inc_balance(apdu);
                return;
            case DEC_BAL:
                dec_balance(apdu);
                return;
            case VER_PIN:
                verifyPin(apdu);
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    }

    private void get_balance(APDU apdu) {
        //get buffer
        byte[] buffer = apdu.getBuffer();
        //verify pin
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }
        //set the data transfer direction to outbound
        apdu.setOutgoing();
        apdu.setOutgoingLength((byte) 2);
        //prepare buffer
        buffer[0] = (byte) (balance >> 8);
        buffer[1] = (byte) (balance);
        //send values
        apdu.sendBytes((short) 0, (short) 2);
    }

    private void inc_balance(APDU apdu) {
        //get buffer
        byte[] buffer = apdu.getBuffer();
        //verify pin
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }
        //get the number of sent bytes
        byte numBytes = (byte) (buffer[ISO7816.OFFSET_LC]);
        //get the number of bytes read
        byte byteRead = (byte)(apdu.setIncomingAndReceive());
        //if byteRead != numBytes throw Exception
        if (byteRead != numBytes) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        //read the value from buffer
        byte inc_value = buffer[ISO7816.OFFSET_CDATA];
        //increment balance
        balance = (short) (balance + inc_value);
    }

    //same logic as inc_balance()
    private void dec_balance(APDU apdu){
        byte[] buffer = apdu.getBuffer();
        if(!pin.isValidated()) ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        byte numBytes = (byte) (buffer[ISO7816.OFFSET_LC]);
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (byteRead != numBytes) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        byte dec_value = buffer[ISO7816.OFFSET_CDATA];
        balance = (short) (balance - dec_value);
        }
 
    //verify pin
    private void verifyPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, (byte) 3)) {
            // PIN verified successfully
            return;
        } else {
            // PIN verification failed
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }
    }

}
