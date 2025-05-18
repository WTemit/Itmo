package client.network;

import client.util.SerializationUtil;
import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class ClientNetworkIO {
    private static final Logger logger = LogManager.getLogger(ClientNetworkIO.class);
    private static final int BUFFER_SIZE = 8192; // Adjust as needed
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 1000; // 1 second delay

    private final String serverHost;
    private final int serverPort;
    private DatagramChannel channel;
    private SocketAddress serverAddress;
    private Selector selector;

    public ClientNetworkIO(String serverHost, int serverPort) throws IOException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        setupConnection();
    }

    private void setupConnection() throws IOException {
        try {
            serverAddress = new InetSocketAddress(serverHost, serverPort);
            channel = DatagramChannel.open();
            channel.configureBlocking(false); // Non-blocking mode
            // We don't necessarily need to connect for UDP, but it simplifies sending/receiving
            // channel.connect(serverAddress); // Optional: connect
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            logger.info("DatagramChannel opened and configured for non-blocking I/O. Server target: {}", serverAddress);
        } catch (IOException e) {
            logger.error("Failed to setup network channel or selector: {}", e.getMessage(), e);
            throw e; // Propagate exception
        }
    }

    public Response sendRequestAndReceiveResponse(Request request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Send Request
                ByteBuffer sendBuffer = SerializationUtil.serializeRequest(request);
                logger.debug("Attempt {}/{}: Sending request {} to {}", attempt, MAX_RETRIES, request.getCommandName(), serverAddress);
                channel.send(sendBuffer, serverAddress); // Use send with address if not connected

                // Receive Response with timeout
                ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                logger.debug("Waiting for response (max {} ms)...", RETRY_DELAY_MS * 2); // Timeout slightly longer than retry delay

                // Use Selector for non-blocking read with timeout
                int readyChannels = selector.select(RETRY_DELAY_MS * 2); // Timeout for select

                if (readyChannels == 0) {
                    logger.warn("Attempt {}/{}: No response received within timeout.", attempt, MAX_RETRIES);
                    continue; // Go to next retry attempt
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        DatagramChannel readableChannel = (DatagramChannel) key.channel();
                        SocketAddress senderAddress = readableChannel.receive(receiveBuffer);

                        if (senderAddress == null) {
                            logger.warn("Attempt {}/{}: Received null sender address (spurious wakeup?). Retrying.", attempt, MAX_RETRIES);
                            keyIterator.remove();
                            continue; // Should not happen often
                        }

                        // Optional: Check if response is from the expected server
                        // if (!serverAddress.equals(senderAddress)) {
                        //     logger.warn("Received packet from unexpected source: {}", senderAddress);
                        //     keyIterator.remove(); // Consume event
                        //     receiveBuffer.clear(); // Clear buffer for next potential read
                        //     continue; // Ignore packet and wait again (or retry)
                        // }

                        receiveBuffer.flip(); // Prepare buffer for reading
                        logger.debug("Attempt {}/{}: Received {} bytes from {}", attempt, MAX_RETRIES, receiveBuffer.limit(), senderAddress);

                        try {
                            Response response = SerializationUtil.deserializeResponse(receiveBuffer);
                            keyIterator.remove(); // Processed the key
                            return response; // Success!
                        } catch (IOException | ClassNotFoundException | ClassCastException e) {
                            logger.error("Attempt {}/{}: Failed to deserialize response: {}", attempt, MAX_RETRIES, e.getMessage(), e);
                            // Don't retry on deserialization error, likely corrupted data
                            keyIterator.remove(); // Processed the key
                            return null; // Indicate error
                        }
                    }
                    keyIterator.remove(); // Remove even if not readable (shouldn't happen here)
                }
                // If loop finishes without returning, something went wrong with selection keys
                logger.warn("Selector loop finished without processing readable key (Attempt {}).", attempt);

            } catch (SocketTimeoutException e) {
                logger.warn("Attempt {}/{}: Socket timed out waiting for response.", attempt, MAX_RETRIES);
            } catch (IOException e) {
                logger.error("Attempt {}/{}: Network I/O error: {}", attempt, MAX_RETRIES, e.getMessage(), e);
                // Consider if retry is appropriate for this type of IOException
                // For now, we break and return null on general IO errors
                return null;
            }

            // Wait before retrying
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Retry delay interrupted.");
                    return null; // Stop if interrupted
                }
            }
        }

        logger.error("Failed to receive response from server after {} attempts.", MAX_RETRIES);
        return null; // Failed after all retries
    }

    public void close() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
                logger.info("Selector closed.");
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
                logger.info("DatagramChannel closed.");
            }
        } catch (IOException e) {
            logger.error("Error closing network resources: {}", e.getMessage(), e);
        }
    }
}