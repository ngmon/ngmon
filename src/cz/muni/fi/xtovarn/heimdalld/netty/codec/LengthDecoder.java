package cz.muni.fi.xtovarn.heimdalld.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class LengthDecoder extends FrameDecoder {
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		// Make sure if the length field was received.
		if (buffer.readableBytes() < 2) {
			// The length field was not received yet - return null.
			// This method will be invoked again when more packets are
			// received and appended to the buffer.
			return null;
		}

		// The length field is in the buffer.

		// Mark the current buffer position before reading the length field
		// because the whole frame might not be in the buffer yet.
		// We will reset the buffer position to the marked position if
		// there's not enough bytes in the buffer.
		buffer.markReaderIndex();

		// Read the length field.
		int length = buffer.readUnsignedShort();

		// Make sure if there's enough bytes in the buffer.
		if (buffer.readableBytes() < length) {
			// The whole bytes were not received yet - return null.
			// This method will be invoked again when more packets are
			// received and appended to the buffer.

			// Reset to the marked position to read the length field again
			// next time.
			buffer.resetReaderIndex();

			return null;
		}

		// There's enough bytes in the buffer. Pass it to the next handler.
		System.out.println(buffer.readerIndex());
		return buffer;
	}
}