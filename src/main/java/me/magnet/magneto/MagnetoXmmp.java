package me.magnet.magneto;

import javax.inject.Inject;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.hipchat.HipChatApi;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.ChatManagerListener;

/**
 * The Xmmp version of Magneto. Requires a configuration.properties to be on the classpath.
 */
@Slf4j
public class MagnetoXmmp extends Magneto {

    private final RequestRouter router;
    private final Settings settings;
    private final HipChatApi hipChatApi;
    private final Set<String> joinedRooms = Sets.newHashSet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public MagnetoXmmp(RequestRouter router, Settings settings, HipChatApi hipChatApi) {
        super(router, settings.getUserMention());
        this.router = router;
        this.settings = settings;
        this.hipChatApi = hipChatApi;
    }

    @Override
    public void start() throws Exception {
        SASLAuthentication.supportSASLMechanism("PLAIN", 0);

        String host = settings.getChatServerHost();
        int port = settings.getChatServerPort();
        String serviceName = settings.getServiceName();
        ConnectionConfiguration config;

        if (Strings.isNullOrEmpty(serviceName)){
            config = new ConnectionConfiguration(host, port);
        }else{
            config = new ConnectionConfiguration(host, port, serviceName);
        }

        final XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        String username = settings.getUserName();
        String password = settings.getUserPassword();
        connection.login(username, password);

        //Only listen to rooms if the service supports it.
        if (!Strings.isNullOrEmpty(settings.getConferenceServerHost())) {
            listenToChatRooms(connection);
            log.info("Starting to look for rooms");
        }
        listenToDirectChats(connection);
        log.info("Started listening to direct chats..");

        awaitShutdown(connection);
    }

    /**
     * Listen for chats initiated by the user.
     *
     * @param connection
     */
    private void listenToDirectChats(XMPPConnection connection) {
        ChatManager chatmanager;
        chatmanager = connection.getChatManager();

        chatmanager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        startNewChat(chat, createdLocally);
                    };
                });
    }

    /**
     * Start a new chat when it is initiated by the user.
     *
     * @param chat
     * @param createdLocally
     */
    private void startNewChat(Chat chat, boolean createdLocally) {
        final XmmpDirectChatRelay relay = new XmmpDirectChatRelay(chat, hipChatApi);
        final Magneto $this = this;

        //Only process if it is not local. this prevents the bot from talking to itself.
        if (createdLocally) {
            return;
        }

        chat.addMessageListener(new MessageListener() {

            @Override
            public void processMessage(Chat chat, Message message) {

                try {
                    //In direct chat the usermention can be neglected, so respond to any message
                    if( !Strings.isNullOrEmpty(message.getBody()) && !accepts(message)){
                        String oldBody = message.getBody();
                        String newBody = settings.getUserMention() + " " + oldBody;

                        //Loop through all the bodies in the message and remove them.
                        Collection<Message.Body> bodies = message.getBodies();
                        for(Message.Body body :bodies){
                            Boolean success=message.removeBody(body);
                        }

                        //So it can be replaced with the newbody
                        message.setBody(newBody);
                    }
                    $this.processMessage(relay, (Message) message);
                } catch (Exception e) {
                    log.error("Cannot process message: {}", e.getMessage(), e);
                }
            }

        });

    }


    private void listenToChatRooms(final XMPPConnection connection) throws XMPPException {
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    checkForNewRooms(connection);
                } catch (XMPPException | RuntimeException e) {
                    log.error("Could not listen to room.", e);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

    private void checkForNewRooms(XMPPConnection connection) throws XMPPException {

        Collection<HostedRoom> hostedRooms =
                MultiUserChat.getHostedRooms(connection, settings.getConferenceServerHost());
        Set<String> encountered = Sets.newHashSet();
        boolean changeFound = false;
        synchronized (joinedRooms) {
            for (final HostedRoom room : hostedRooms) {
                encountered.add(room.getJid());
                if (joinedRooms.add(room.getJid())) {
                    listenToRoom(connection, settings, room);
                    changeFound = true;
                }
            }
            if (joinedRooms.retainAll(encountered)) {
                log.info("One or more rooms were deleted");
                changeFound = true;
            }
            if (changeFound) {
                hipChatApi.refreshRoomIds();
            }
        }

    }

    private void listenToRoom(XMPPConnection connection, Settings settings, final HostedRoom room)
            throws XMPPException {
        final MultiUserChat chat = new MultiUserChat(connection, room.getJid());
        DiscussionHistory discussionHistory = new DiscussionHistory();
        discussionHistory.setMaxChars(0);
        discussionHistory.setMaxStanzas(0);
        discussionHistory.setSeconds(0);

        chat.join(settings.getUserDisplayName(), null, discussionHistory, 30000);
        log.info("Joined room: " + room.getJid());

        final XmmpChatRoomRelay relay = new XmmpChatRoomRelay(chat, hipChatApi);
        chat.addMessageListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                if (packet instanceof Message) {
                    try {
                        processMessage(relay, (Message) packet);
                    } catch (Exception e) {
                        log.error("Cannot process message: {}", e.getMessage(), e);
                    }
                }
            }
        });
    }

    private void awaitShutdown(final XMPPConnection connection) {
        final CountDownLatch waitForDisconnect = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
                waitForDisconnect.countDown();
            }
        }));
        try {
            waitForDisconnect.await();
            log.info("Disconnecting");
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Exiting before disconnect completed");
        }
        log.info("Shutdown complete");
    }


}
