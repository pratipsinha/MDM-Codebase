package com.ww.pim.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmail {
public static void main(String args[]) {
	SendEmail smail = new SendEmail();
	smail.sendMailCommon("ghosha@watts.com", "pim@watts.com", "Hello", "test");
}
	
	public boolean sendMailCommon(String toAddress,String fromAddress,String mailBody,String mailSubject){

		System.out.println("***Send mail Called***");

		final String username = ""; //watts to provide
		final String password = ""; //watts to provide


		InternetAddress []recipientAddressList = null;
		Properties props = new Properties();
		boolean emailFlag = false;

		//props.put("mail.smtp.auth", "true");
		//props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.watts.com");
		//props.put("mail.smtp.port", "25");
		Session session = Session.getDefaultInstance(props);
		/*Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				System.out.println("***Inside Authentication***");
				return new PasswordAuthentication(username, password);
			}
		});*/

		try {
			recipientAddressList = getToAddressList(toAddress);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromAddress));
			message.setRecipients(Message.RecipientType.TO,recipientAddressList);
			message.setSubject(mailSubject);

			message.setContent(mailBody,"text/html");
			Transport.send(message);
			emailFlag = true;
			System.out.println("Done and Sent");


		} catch (MessagingException me) {
			me.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return emailFlag;
	}
	
	private InternetAddress [] getToAddressList(String strReceivedAddressList)throws Exception
	{

		System.out.println("getToAddressList(..) called");
		String recipient = strReceivedAddressList;
		int count =0;

		InternetAddress []recipientAddressList = null;
		try{
			if(strReceivedAddressList.contains(";"))
			{
				String [] recipientList = recipient.split(";");	
				recipientAddressList = new InternetAddress[recipientList.length];
				for(String recp :recipientList)
				{
					recipientAddressList[count] = new InternetAddress(recp.trim());
					System.out.println("*********** recipientAddressList[count] value "+recipientAddressList[count]);
					count++;

				}
			}else
			{
				recipientAddressList = new InternetAddress[1];
				recipientAddressList[0] = new InternetAddress(recipient.trim());
				System.out.println("*********** recipientAddressList.[0] value "+recipientAddressList[0]);
			}

		}catch(Exception e)
		{

		}
		System.out.println("end getToAddressList : "+recipientAddressList);
		return recipientAddressList;
	}
}
