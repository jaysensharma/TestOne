package client;

import java.util.Hashtable;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TopicReceiver implements MessageListener {
    public final static String JNDI_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    public final static String JMS_FACTORY = "jms/RemoteConnectionFactory";
    public final static String TOPIC = "TestTopic";
    private TopicConnectionFactory topicConFactory;
    private TopicConnection topicCon;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private Topic topic;
    private boolean quit = false;

    public void onMessage(Message msg) {
        try {
            String msgText;
            if (msg instanceof TextMessage) {
                msgText = ((TextMessage) msg).getText();
            } else {
                msgText = msg.toString();
            }
            System.out.println("\n\t " + msgText);
            if (msgText.equalsIgnoreCase("quit")) {
                synchronized (this) {
                    quit = true;
                    this.notifyAll(); 
                }
            }
        } catch (JMSException jmse) {
            jmse.printStackTrace();
        }
    }

    public void init(Context ctx, String topicName) throws NamingException, JMSException {
        topicConFactory = (TopicConnectionFactory) ctx.lookup(JMS_FACTORY);
        topicCon = topicConFactory.createTopicConnection("jmsuser", "jmsuser@123"); //"testuser", "Passw0rd!"
        topicCon.setClientID("durable");
        topicSession = topicCon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = (Topic) ctx.lookup(topicName);
        topicSubscriber = topicSession.createDurableSubscriber(topic,"Subs-A");
        topicSubscriber.setMessageListener(this);
        topicCon.start();
    }

    public void close() throws JMSException {
        topicSubscriber.close();
        topicSession.close();
        topicCon.close();
    }

    public static void main(String[] args) throws Exception {
        InitialContext ic = getInitialContext("remote://localhost:4447");
        TopicReceiver topicReceiver = new TopicReceiver();
        topicReceiver.init(ic, TOPIC);
        System.out.println("*******************JMS Ready To Receive Messages (To quit, send a \"quit\" message from TopicPublisher.class).");
        synchronized (topicReceiver) {
            while (!topicReceiver.quit) {
                try {
                    topicReceiver.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        topicReceiver.close();
    }

    private static InitialContext getInitialContext(String url) throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_PRINCIPAL, "jmsuser");
        env.put(Context.SECURITY_CREDENTIALS, "jmsuser@123");     
        return new InitialContext(env);
    }
}
