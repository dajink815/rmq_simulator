package com.uangel.rmq.module.transport;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import com.uangel.rmq.module.RmqRecoveryListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RmqTransport {

    private final String host;
    private final String queueName;
    private final String userName;
    private final String password;
    private final int port;

    private Connection connection;
    private Channel channel;


    /**
     * @param host      RabbitMQ host
     * @param userName  RabbitMQ user
     * @param password  RabbitMQ password(암호화)
     * @param port      RabbitMQ post
     * @param queueName RabbitMQ a2s queueName
     */
    public RmqTransport(String host, String userName, String password, int port, String queueName) {
        this.host = host;
        this.queueName = queueName;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

    protected Channel getChannel() {
        return this.channel;
    }

    protected String getQueueName() {
        return this.queueName;
    }

    /**
     * RabbitMQ Connection, Channel
     *
     * @return Connection, Channel 생성 결과
     */
    public boolean connect() {
        if (!makeConnection()) {
            return false;
        }

        if (!makeChannel()) {
            closeConnection();
            return false;
        }

        return true;
    }

    public void close() {
        closeChannel();
        closeConnection();
    }

    /**
     * RabbitMQ connection try
     * RabbitMQ Local server recovery 및 connection 시도
     *
     * @return RabbitMQ Connection 생성 결과
     */
    private boolean makeConnection() {
        if (this.channel != null && this.channel.isOpen()) {
            return true;
        }

        boolean result = false;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setUsername(this.userName);
        factory.setPassword(this.password);
        factory.setPort(this.port);
        // Add Automatic Recovery 기능 추가
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(1000);
        factory.setRequestedHeartbeat(5);
        // 2020.11.24 RMQ Connection Timeout
        factory.setConnectionTimeout(2000);
        factory.setExceptionHandler(new DefaultExceptionHandler() {
            @Override
            public void handleUnexpectedConnectionDriverException(Connection con, Throwable exception) {
                log.error("handleUnexpectedConnectionDriverException [{}] Queue Connect Fail", queueName);
            }

            @Override
            public void handleConnectionRecoveryException(Connection conn, Throwable exception) {
                // Connection Recovery 실패시 발생.
                log.error("handleConnectionRecoveryException [{}] Queue Connect Fail", queueName);
            }

            @Override
            public void handleChannelRecoveryException(Channel ch, Throwable exception) {
                // Channel Recovery 실패시 발생.
                log.error("handleChannelRecoveryException [{}] Queue Connect Fail", queueName);
            }
        });

        try {
            //서버가 지원하는 경우 클라이언트가 제공한 ConnectionName 이 관리 UI에 표시된다.
            this.connection = factory.newConnection("a2s_" + this.queueName);
            this.connection.addBlockedListener(new BlockedListener() {
                @Override
                public void handleBlocked(String reason) {
                    log.error("handleBlocked [{}] Queue", queueName);
                }
                @Override
                public void handleUnblocked() {
                    log.error("handleUnblocked [{}] Queue", queueName);
                }
            });
            result = true;
        } catch (Exception e) {
            log.error("RmqTransport.makeConnection.Exception, [{}@{}:{}-{}] ", this.userName, this.host, this.port, this.queueName, e);
        }

        return result;
    }

    private void closeConnection() {
        try {
            this.connection.close();
        } catch (Exception e) {
            log.error("RmqTransport.closeConnection.Exception ", e);
        }
    }

    private boolean makeChannel() {
        if (this.channel != null && this.channel.isOpen()) {
            return true;
        }

        boolean result = false;
        try {
            this.channel = this.connection.createChannel();
            RecoveryListener recoveryListener = new RmqRecoveryListener(queueName);
            ((Recoverable) this.channel).addRecoveryListener(recoveryListener);
            this.channel.queueDeclare(this.queueName, false, false, false, null);
            result = true;
        } catch (Exception e) {
            log.error("RmqTransport.makeChannel.Exception, [{}@{}:{}-{}] ", this.userName, this.host, this.port, this.queueName, e);
        }
        return result;
    }

    public void deleteQueue(String queueName) throws IOException {
        this.channel.queueDelete(queueName);
    }

    private void closeChannel() {
        try {
            this.channel.close();
        } catch (Exception e) {
            log.error("RmqTransport.closeChannel.Exception ", e);
        }
    }

}
