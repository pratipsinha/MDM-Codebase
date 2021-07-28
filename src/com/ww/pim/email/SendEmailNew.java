package com.ww.pim.email;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmailNew 
{

	public static void main(String args[])
	{
		String toAddress = "mdrouson.sarwar@wattswater.com";
		String fromAddress = "mdrouson.sarwar@wattswater.com";
		String mailSubject = "Test:Email Attachment";
		String mailBody = "Hi, \n\n This is a test Mail. \n\n Thanks,\n\n Rouson";
		String filePath = "C:/Users/sarwarm/Downloads/Mass Update Import_2018_04_03_07_13_35_999_W2UAT.txt";
		String fileName = "Mass Update Import_2018_04_03_07_13_35_999_W2UAT.txt";
		new SendEmailNew().sendEmailToUser(toAddress, fromAddress, mailBody, mailSubject, filePath, fileName);
	}
	
	public boolean sendEmailToUser(String toAddress,String fromAddress,String mailBody,String mailSubject,String filePath,String fileName)
	{

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
			
			//message.setContent(mailBody,"text/html;charset=utf-8");
			
			Multipart multipart = new MimeMultipart();
			
			MimeBodyPart attachMentPart = new MimeBodyPart();
		    DataSource source = new FileDataSource(filePath);
		    attachMentPart.setDataHandler(new DataHandler(source));
		    attachMentPart.setFileName(fileName);
		    multipart.addBodyPart(attachMentPart);
		    
		    MimeBodyPart htmlPart = new MimeBodyPart();
		    //htmlPart.setContent(mailBody, "text/html;charset=utf-8");
		    htmlPart.setContent(mailBody, "text/html;charset=utf-8");
		    multipart.addBodyPart(htmlPart);
		    
		    message.setContent(multipart);
			
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
			e.printStackTrace();
		}
		System.out.println("end getToAddressList : "+recipientAddressList);
		return recipientAddressList;
	}
	
}
