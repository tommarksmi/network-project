/* CRC16.java */

package stopwaitsend;

import java.math.BigInteger;
import java.util.Arrays;

public class CRC16 {

    private static void printThis(BigInteger b) {
        String s = "";

        for (int i = b.bitLength() - 1; i >= 0; i--) {
            if (b.testBit(i)) {
                s += "1";
            } else {
                s += "0";
            }
        }
        System.out.println(s);
    }

    /* CRC-16 IBM generator polynomial : x^16 + x^15 + x^2 + 1;
     * irreducible in GF(2); appears here in standard form */
    private static BigInteger polynomial
            = new BigInteger(new byte[]{(byte) 0x80, (byte) 0x05});

    
    
    /* return the last two elements of a byte array */
    private static byte[] bottom16(byte[] b) {
        return Arrays.copyOfRange(b, b.length - 2, b.length);
    }

    /**
     * Compute the CRC16 of some given data
     *
     * @param data the given data in a byte array of arbitrary size
     * @return the 16-bit checksum in a 2-element byte array
     */
    public static byte[] go(byte[] data) {

        /* This is a straightforward implementation of CRC-16 IBM
         * using arbitrary precision Java API BigIntegers; see
         * http://en.wikipedia.org/wiki/Computation_of_CRC */
        BigInteger bitstring
                = new BigInteger(data);

        BigInteger remainder
                = new BigInteger(new byte[]{(byte) 1, (byte) 0, (byte) 0});

        for (int i = bitstring.bitLength() - 1; i >= 0; i--) {
            if (remainder.testBit(15) ^ bitstring.testBit(i)) {
                remainder = remainder.shiftLeft(1).xor(polynomial);
            } else {
                remainder = remainder.shiftLeft(1);
            }
        }
        return bottom16(remainder.toByteArray());
    }

    /**
     * Check the CRC16 of some given data
     *
     * @param data the given data in an arbitrary-length byte array
     * @return true if the checksum is valid, false otherwise
     */
    public static boolean valid(byte[] data) {
        if (new BigInteger(go(data)).intValue() == 0) {
            return true;
        }
        return false;
    }
}
