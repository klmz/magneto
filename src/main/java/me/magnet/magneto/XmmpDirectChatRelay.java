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


    //HTML message are not yet supported by the hipchat api implementation so they need to be convert to plain text.
	@Override
	public void sendHtml(HipChatNotification message) {
            String plainText = stripHtml(message.toString());
            sendMessage(plainText);
	}

	@Override
	public void sendHtml(String message) {
            String plainText = stripHtml(message);
            sendMessage(plainText);

	}

    /**
     * If html is not supported convert html needs to be converted plain text.
     * @param message
     * @return
     */
    private String stripHtml(String message) {
        Document doc = Jsoup.parse(message);

        //Convert body to plain text.
        String plainText = doc.body().text();

        //Make a list of included images if there are any.
        Elements images = doc.select("img");
        if(images.size()>0){
            plainText += "\n These images were included:\n";
            for(Element image : images){
                plainText += image.attr("src").toString()+"\n";
            }
        }

        return plainText;
    }


    @Override
	public String getRoom() {
		return "Direct_"+chat.getParticipant();
	}
}
