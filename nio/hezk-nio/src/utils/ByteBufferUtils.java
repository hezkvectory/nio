package utils;

import java.nio.ByteBuffer;
/**
 * ByteBuffer工具类
 * @author hezhengkui
 *
 */
public class ByteBufferUtils {

	/**
	 * 合并byteBuffer
	 * 
	 * @param byteBuffer1
	 * @param byteBuffer2
	 * @return
	 * 
	 */
	public static ByteBuffer composite(ByteBuffer byteBuffer1,
			ByteBuffer byteBuffer2) {
		int capacity = (byteBuffer1.limit() - byteBuffer1.position())
				+ (byteBuffer2.limit() - byteBuffer2.position());
		ByteBuffer ret = ByteBuffer.allocate(capacity);

		ret.put(byteBuffer1);
		ret.put(byteBuffer2);

		ret.position(0);
		ret.limit(ret.capacity());
		return ret;
	}

	/**
	 * 
	 * @param src
	 * @param startindex
	 *            从0开始
	 * @param endindex
	 * @return
	 * 
	 * @author: hezhengkui
	 * 
	 */
	public static ByteBuffer copy(ByteBuffer src, int startindex, int endindex) {
		int size = endindex - startindex;
		ByteBuffer ret = ByteBuffer.allocate(size);
		src.position(startindex);
		for (int i = 0; i < size; i++) {
			ret.put(src.get());
		}
		return ret;
	}

}
