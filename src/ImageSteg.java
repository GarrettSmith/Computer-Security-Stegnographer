
import java.util.BitSet;


/**
 * 
 */

/**
 * @author Garrett Smith, 301Byte.Size390
 *
 */
public abstract class ImageSteg {

  public static int maximumChars(int n, byte[] imageBytes) {
    int bits = imageBytes.length * n;
    return bits / Character.SIZE;
  }

  public static byte[] encode(int n, String msg, byte[] imageBytes) {
    // ensure 0 < n <= Byte.Size
    if (0 >= n || n > Byte.SIZE) {
      return imageBytes;
    }
    // trim the message to the largest possible length
    msg = msg.substring(0, Math.min(msg.length(), maximumChars(n, imageBytes)));
    // convert the message into a set of bits
    byte[] msgBytes = msg.getBytes();
    BitSet msgBits = bytesToBitSet(msgBytes);
    // convert the image into a set of bits
    BitSet imageBits = bytesToBitSet(imageBytes);
    // space out the message bits so they only occupy the lowest n bits of each byte
    msgBits = expandBits(n, msgBits);
    // clear the lower n bits in the image bytes
    imageBits = clearBits(n, imageBits);
    // or the message with the image to get the final result
    imageBits.or(msgBits);
    // convert the bits back to bytes    
    return bitsToBytes(imageBits);
  }
  
  public static String decode(int n, byte[] imageBytes) {
    // convert the image bytes into bits
    BitSet imageBits = bytesToBitSet(imageBytes);
    // grab the lower n bits from each byte
    imageBits = collapseBits(n, imageBits);
    // convert the bits into bytes
    byte[] bytes = bitsToBytes(imageBits);
    // convert the bytes into a string    
    return new String(bytes);
  }
  
  /**
   * Converts an array of bytes into a BitSet.
   * @param bytes
   * @return
   */
  private static BitSet bytesToBitSet(byte[] bytes) {
    BitSet bits = new BitSet(bytes.length * Byte.SIZE);
    // for each byte in the array
    for (int i = 0; i < bytes.length; i++) {
      // go over each bit in the byte
      for (int j = 0; j < Byte.SIZE; j++) {
        // check if the current bit is set
        boolean set = (bytes[i] >> j & 1) == 1;
        // set the appropriate bit
        bits.set(i * Byte.SIZE + (Byte.SIZE - (j + 1)), set);
      }
    }
    return bits;
  }
  
  /**
   * Converts a BitSet into an array of bytes.
   * @param bits
   * @return
   */
  private static byte[] bitsToBytes(BitSet bits) {
    int size = bits.length() / Byte.SIZE;
    // check for trailing bits
    if (bits.length() % 8 != 0) {
      size++;
    }
    byte[] bytes = new byte[size];
    for (int i = 0; i < size; i++) {
      byte b = 0;
      for (int j =0; j < Byte.SIZE; j++) {
        if(bits.get(i * Byte.SIZE + j)) {
          b |= (1 << (Byte.SIZE - (j + 1)));
        }
      }
      bytes[i] = b;
    }
    return bytes;
  }
  
  /**
   * Spaces out the bits so when seen as an array of bytes only the lower n bits
   * are set.
   * @param n
   * @param source
   * @return
   */
  private static BitSet expandBits(int n, BitSet source) {
    int segments = source.length() / n;
    int offset = (Byte.SIZE - n);
    BitSet result = new BitSet(segments * Byte.SIZE);
    // loop over each segment of bits
    for (int i = 0; i < segments; i++) {
      // for each bit
      for (int j = 0; j < n; j++) {
        result.set(i * Byte.SIZE + j + offset, source.get(i * n + j));
      }
    }
    return result;
  }
  
  /**
   * Collapses the bits so only the lower n bits of each represented byte are retained.
   * @param n
   * @param source
   * @return
   */
  private static BitSet collapseBits(int n, BitSet source) {
    int segments = source.length() / Byte.SIZE;
    int offset = Byte.SIZE - n;
    BitSet result = new BitSet(segments * n);
    for (int i = 0; i < segments; i++) {
      for (int j = 0; j < n; j++) {
        result.set(i * n + j, source.get(i * Byte.SIZE + offset + j));
      }
    }
    return result;
  }
  
  /**
   * Clears the lower n bits of each byte represented by the BitSet.
   * @param n
   * @param source
   * @return
   */
  private static BitSet clearBits(int n, BitSet source) {
    int sections = source.length() / Byte.SIZE;
    int offset = Byte.SIZE - n;
    BitSet result = (BitSet) source.clone();
    for (int i = 0; i < sections; i++) {
      for (int j = 0; j < n; j++) {
        result.clear(i * Byte.SIZE + offset + j);
      }
    }
    return result;
  }
  
  /**
   * Helper method to print out the bits of a BitSet to be easily read.
   * @param bits
   * @return
   */
  public static String bitsToString(BitSet bits) {
    StringBuilder bld = new StringBuilder();
    for (int i = 0; i < bits.length(); i++) {
      char c;
      if (bits.get(i)) {
        c = '1'; 
      }
      else {
        c = '0';
      }
      if (i % Byte.SIZE == 0 && i != 0) {
        bld.append(' ');
      }
      bld.append(c);
    }
    return bld.toString();
  }
}
