package me.magnet.magneto;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.hipchat.HipChatApi;
import me.magnet.magneto.hipchat.HipChatNotification;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Value
@Slf4j
class XmmpDirectChatRelay implements ChatRoom {

	private Chat chat;
	private HipChatApi hipChatApi;

	@Override
	public void sendMessage(String message) {
		try {
			chat.sendMessage(message);
		}
		catch (XMPPException e) {
			throw new DeliveryException(message, e);
		}
	}

	@Override
	public void sendHtml(HipChatNotification message) {
        //Facebook does not support html.
        //So convert any html back to plaintext to still have all the functionality of plugins
        String messageText = message.toString();//TODO get rid of html tags.
        String plainText = stripHtml(messageText);
        sendMessage(plainText);
        log.info("Sending html in plaintext");
	}

	@Override
	public void sendHtml(String message) {
        //Facebook does not support html
        //So convert any html back to plaintext to still have all the functionality of plugins
        log.info("Sending html in plaintext");

        String plainText = stripHtml(message);

        sendMessage(plainText);
	}

    private String stripHtml(String message) {
        Document doc = Jsoup.parse(message);

        //Convert body to plain text.
        String plainText = doc.body().text();

        //Make a list of included images.
        Elements images = doc.select("img");
        plainText += "\n These images where included:\n";
        for(Element image : images){
            plainText += image.attr("src").toString()+"\n";
        }

        return plainText;
    }


    @Override
	public String getRoom() {
		return "Direct_"+chat.getParticipant();
	}
}
