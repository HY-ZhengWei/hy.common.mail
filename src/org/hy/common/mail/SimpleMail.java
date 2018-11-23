package org.hy.common.mail;
      
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.hy.common.Date;
import org.hy.common.Help;





/**
 * 简单邮件发送器
 * 
 * 1. 支持单发和群发
 * 2. 支持文本格式发送(可带附件)
 * 3. 支持HTML格式发送(可带附件)
 * 4. 支持代理配置(只支持Socket代理，不支持Http代理)
 * 5. 支持接收邮件
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-07-22
 *           V2.0  2015-11-30  添加接收邮件功能
 *           V2.1  2016-07-29  修正：当有多个发送者，发送多个邮件时，会出现验证不通过的问题。
 *                             将Session.getDefaultInstance()方法改为Session.getInstance()的解决。
 *           v2.2  2018-04-04  发送邮件功能添加抄送者们、暗送者们的配置属性
 */
public final class SimpleMail
{    
    
    private SimpleMail()
    {
        
    }
    
    
    
    /**
     * 以文本格式发送邮件
     * 
     * @param i_Owner
     * @param i_SendInfo
     * @return
     */
    public synchronized static boolean sendTextMail(MailOwnerInfo i_Owner ,MailSendInfo i_SendInfo) 
    {    
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session v_SendMailSession = Session.getInstance(i_Owner.getSendProperties() ,i_Owner);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        v_SendMailSession.setDebug(false);
        v_SendMailSession.setDebugOut(null);
        
        try
        {
            Message v_MailMessage = new MimeMessage(v_SendMailSession);
            
            v_MailMessage.setFrom(i_Owner.getEmailAddress());
            v_MailMessage.setRecipients(Message.RecipientType.TO ,(Address [])i_SendInfo.getEmailAddressList());
            if ( !Help.isNull(i_SendInfo.getEmailCCAddressList()) )
            {
                v_MailMessage.setRecipients(Message.RecipientType.CC  ,(Address [])i_SendInfo.getEmailCCAddressList());
            }
            if ( !Help.isNull(i_SendInfo.getEmailBCCAddressList()) )
            {
                v_MailMessage.setRecipients(Message.RecipientType.BCC ,(Address [])i_SendInfo.getEmailBCCAddressList());
            }
            v_MailMessage.setSubject(   i_SendInfo.getSubject());
            v_MailMessage.setText(      i_SendInfo.getContent());
            v_MailMessage.setSentDate(  new Date());
            
            // 附件处理
            if ( !Help.isNull(i_SendInfo.getAttachFileNames()) )
            {
                Multipart v_Multipart = new MimeMultipart();
                
                for (int i=0; i<i_SendInfo.getAttachFileNames().size(); i++)
                {
                    BodyPart       v_AttachFile = new MimeBodyPart();
                    FileDataSource v_FileDS     = new FileDataSource(i_SendInfo.getAttachFileNames().get(i));
                    v_AttachFile.setDataHandler(new DataHandler(v_FileDS));
                    v_AttachFile.setFileName(v_FileDS.getName());
                    
                    v_Multipart.addBodyPart(v_AttachFile);
                }
                
                v_MailMessage.setContent(v_Multipart);
            }
            
            Transport.send(v_MailMessage);
            
            return true;
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        
        return false;    
    }    
    
    
    
    /**
     * 以HTML格式发送邮件
     * 
     * @param i_Owner
     * @param i_SendInfo
     * @return
     */
    public synchronized static boolean sendHtmlMail(MailOwnerInfo i_Owner ,MailSendInfo i_SendInfo)
    {    
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session v_SendMailSession = Session.getInstance(i_Owner.getSendProperties() ,i_Owner);
        
        try
        {
            // 根据session创建一个邮件消息
            Message v_MailMessage = new MimeMessage(v_SendMailSession);

            v_MailMessage.setFrom(i_Owner.getEmailAddress());
            v_MailMessage.setRecipients(Message.RecipientType.TO ,(Address [])i_SendInfo.getEmailAddressList());
            if ( !Help.isNull(i_SendInfo.getEmailCCAddressList()) )
            {
                v_MailMessage.setRecipients(Message.RecipientType.CC  ,(Address [])i_SendInfo.getEmailCCAddressList());
            }
            if ( !Help.isNull(i_SendInfo.getEmailBCCAddressList()) )
            {
                v_MailMessage.setRecipients(Message.RecipientType.BCC ,(Address [])i_SendInfo.getEmailBCCAddressList());
            }
            v_MailMessage.setSubject(i_SendInfo.getSubject());
            v_MailMessage.setSentDate(new Date());
            
            Multipart v_Multipart = new MimeMultipart();                            // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
            BodyPart  v_Html      = new MimeBodyPart();
            v_Html.setContent(i_SendInfo.getContent() ,"text/html; charset=utf-8"); // 创建一个包含HTML内容的MimeBodyPart
            v_Multipart.addBodyPart(v_Html);                                        // 设置HTML内容
            v_MailMessage.setContent(v_Multipart);                                  // 将MiniMultipart对象设置为邮件内容
            
            // 附件处理
            if ( !Help.isNull(i_SendInfo.getAttachFileNames()) )
            {
                for (int i=0; i<i_SendInfo.getAttachFileNames().size(); i++)
                {
                    BodyPart       v_AttachFile = new MimeBodyPart();
                    FileDataSource v_FileDS     = new FileDataSource(i_SendInfo.getAttachFileNames().get(i));
                    v_AttachFile.setDataHandler(new DataHandler(v_FileDS));
                    v_AttachFile.setFileName(v_FileDS.getName());
                    
                    v_Multipart.addBodyPart(v_AttachFile);
                }
            }
            
            Transport.send(v_MailMessage);
            
            return true;
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        
        return false;    
    }
    
    
    
    /**
     * 接收最新的一封邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @return       
     */
    public static MailReciveInfo reciveLast(MailOwnerInfo i_Owner)
    {
        List<MailReciveInfo> v_Ret = recives(i_Owner ,true ,null ,null ,null ,null);
        
        if ( Help.isNull(v_Ret) )
        {
            return null;
        }
        else
        {
            return v_Ret.get(0);
        }
    }
    
    
    
    /**
     * 接收指定具体编号的邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_MessageNo   接收指定具体编号的邮件
     * @return       
     */
    public static MailReciveInfo recive(MailOwnerInfo i_Owner ,int i_MessageNo)
    {
        List<MailReciveInfo> v_Ret = recives(i_Owner ,false ,null ,i_MessageNo ,null ,null);
        
        if ( Help.isNull(v_Ret) )
        {
            return null;
        }
        else
        {
            return v_Ret.get(0);
        }
    }
    
    
    
    /**
     * 接收最新的前几个邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_Top         接收最新的前几个邮件
     * @return              按时间和消息ID倒序排序的
     */
    public static List<MailReciveInfo> recivesTop(MailOwnerInfo i_Owner ,int i_Top)
    {
        return recives(i_Owner ,false ,i_Top ,null ,null ,null);
    }
    
    
    
    /**
     * 接收指定范围编号的邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_StartNo     接收指定范围编号的邮件，此为开始编号
     * @param i_EndNo       接收指定范围编号的邮件，此为结束编号。为空时，自动取最大邮件数
     * @return              按时间和消息ID倒序排序的
     */
    public static List<MailReciveInfo> recives(MailOwnerInfo i_Owner ,Integer i_StartNo ,Integer i_EndNo)
    {
        return recives(i_Owner ,false ,null ,null ,i_StartNo ,i_EndNo);
    }
    
    
    
    /**
     * 接收所有的邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @return              按时间和消息ID倒序排序的
     */
    public static List<MailReciveInfo> recives(MailOwnerInfo i_Owner)
    {
        return recives(i_Owner ,false ,null ,null ,null ,null);
    }
    
    
    
    /**
     * 接收邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_GetLast     接收最新的一封邮件
     * @param i_Top         接收最新的前几个邮件
     * @param i_MessageNo   接收指定具体编号的邮件
     * @param i_StartNo     接收指定范围编号的邮件，此为开始编号
     * @param i_EndNo       接收指定范围编号的邮件，此为结束编号。为空时，自动取最大邮件数
     * @return              按时间和消息ID倒序排序的
     */
    private static List<MailReciveInfo> recives(MailOwnerInfo i_Owner ,boolean i_GetLast ,Integer i_Top ,Integer i_MessageNo ,Integer i_StartNo ,Integer i_EndNo)
    {
        List<MailReciveInfo> v_Ret = new ArrayList<MailReciveInfo>();
        // 根据邮件会话属性和密码验证器构造一个接收邮件的session
        Session v_ReceiverSession = Session.getDefaultInstance(i_Owner.getReciveProperties() ,i_Owner);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        v_ReceiverSession.setDebug(false);
        v_ReceiverSession.setDebugOut(null);
        
        Store      v_Store   = null;
        Folder     v_Folder  = null;
        Message [] v_Message = null;
        try
        {
            v_Store = v_ReceiverSession.getStore(i_Owner.getReciveURLName());
            v_Store.connect();
            v_Folder = v_Store.getFolder("INBOX");
            v_Folder.open(Folder.READ_ONLY);
            
            // 接收最新的一封邮件
            if ( i_GetLast )
            {
                int v_Count  = v_Folder.getMessageCount();
                v_Message    = new Message[1];
                v_Message[0] = v_Folder.getMessage(v_Count);
            }
            // 接收最新的前几个邮件
            else if ( i_Top != null && i_Top > 0 )
            {
                int v_Count = v_Folder.getMessageCount();
                if ( v_Count <= i_Top  )
                {
                    v_Message = v_Folder.getMessages(); 
                }
                else
                {
                    v_Message = v_Folder.getMessages(v_Count - i_Top + 1 ,v_Count); 
                }
            }
            // 接收指定具体编号的邮件
            else if ( i_MessageNo != null && i_MessageNo > 0 )
            {
                int v_Count = v_Folder.getMessageCount();
                v_Message = new Message[1];
                
                if ( i_MessageNo >= v_Count )
                {
                    v_Message[0] = v_Folder.getMessage(v_Count);
                }
                else
                {
                    v_Message[0] = v_Folder.getMessage(i_MessageNo.intValue());
                }
            }
            // 接收指定范围编号的邮件
            else if ( i_StartNo != null && i_StartNo > 0 )
            {
                int v_Count = v_Folder.getMessageCount();
                if ( v_Count <= i_StartNo  )
                {
                    v_Message    = new Message[1];
                    v_Message[0] = v_Folder.getMessage(v_Count);
                }
                else if ( i_EndNo == null || i_EndNo <= 0 || v_Count <= i_EndNo )
                {
                    v_Message = v_Folder.getMessages(i_StartNo.intValue() ,v_Count); 
                }
                else
                {
                    v_Message = v_Folder.getMessages(i_StartNo.intValue() ,i_EndNo.intValue()); 
                }
            }
            // 接收所有的邮件
            else
            {
                v_Message = v_Folder.getMessages();
            }
            
            for (int i=v_Message.length-1; i>=0; i--)
            {
                v_Ret.add(new MailReciveInfo((MimeMessage) v_Message[i]));
            }
        }
        catch (NoSuchProviderException ex)
        {
            ex.printStackTrace();
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取邮件的总个数
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @return       
     */
    public static int recivesCount(MailOwnerInfo i_Owner)
    {
        // 根据邮件会话属性和密码验证器构造一个接收邮件的session
        Session v_ReceiverSession = Session.getDefaultInstance(i_Owner.getReciveProperties() ,i_Owner);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        v_ReceiverSession.setDebug(false);
        v_ReceiverSession.setDebugOut(null);
        
        Store      v_Store   = null;
        Folder     v_Folder  = null;
        try
        {
            v_Store = v_ReceiverSession.getStore(i_Owner.getReciveURLName());
            v_Store.connect();
            v_Folder = v_Store.getFolder("INBOX");
            v_Folder.open(Folder.READ_ONLY);
            
            return v_Folder.getMessageCount();
        }
        catch (NoSuchProviderException ex)
        {
            ex.printStackTrace();
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if ( v_Folder != null )
                {
                    v_Folder.close(false);
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
            
            try
            {
                if ( v_Store != null )
                {
                    v_Store.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
        }
        
        return 0;
    }
    
    
    
    /**
     * 标记邮件已读。
     * 
     * 由于POP3协议是不支持该该功能的，POP3只支持Flags.Flag.DELETE。
     * POP3没有状态，只能读出和删除。
     * 
     * 但IMAP协议是支持的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_MailReciveInfo
     * @throws MessagingException
     */
    public static void readedByIMAP(MailOwnerInfo i_Owner ,MailReciveInfo i_MailReciveInfo) throws MessagingException
    {
        List<MailReciveInfo> v_Param = new ArrayList<MailReciveInfo>(1);
        v_Param.add(i_MailReciveInfo);
        
        readedsByIMAP(i_Owner ,v_Param);
    }
    
    
    
    /**
     * 标记一批邮件已读
     * 
     * 由于POP3协议是不支持该该功能的，POP3只支持Flags.Flag.DELETE。
     * POP3没有状态，只能读出和删除。
     * 
     * 但IMAP协议是支持的
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_MailReciveInfos
     * @throws MessagingException
     */
    public static void readedsByIMAP(MailOwnerInfo i_Owner ,List<MailReciveInfo> i_MailReciveInfos) throws MessagingException
    {
        if ( Help.isNull(i_MailReciveInfos) )
        {
            return;
        }
        
        Collections.reverse(i_MailReciveInfos);
        int v_StartNo = i_MailReciveInfos.get(0)                           .getMessageNo();
        int v_EndNo   = i_MailReciveInfos.get(i_MailReciveInfos.size() - 1).getMessageNo();
        
        // 根据邮件会话属性和密码验证器构造一个接收邮件的session
        Session v_ReceiverSession = Session.getDefaultInstance(i_Owner.getReciveProperties() ,i_Owner);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        v_ReceiverSession.setDebug(false);
        v_ReceiverSession.setDebugOut(null);
        
        Store      v_Store   = null;
        Folder     v_Folder  = null;
        Message [] v_Message = null;
        try
        {
            v_Store = v_ReceiverSession.getStore(i_Owner.getReciveURLName());
            v_Store.connect();
            v_Folder = v_Store.getFolder("INBOX");
            v_Folder.open(Folder.READ_WRITE);      // 删除时标记为可写模式
            v_Message = v_Folder.getMessages(v_StartNo ,v_EndNo);
            
            for (int i=v_Message.length-1; i>=0; i--)
            {
                MailReciveInfo v_New = new MailReciveInfo((MimeMessage) v_Message[i]);
                
                for (MailReciveInfo v_Old : i_MailReciveInfos)
                {
                    if ( v_Old.equals(v_New) )
                    {
                        v_Message[i].setFlag(Flag.SEEN ,true);
                        v_Message[i].saveChanges();
                        break;
                    }
                }
            }
        }
        catch (NoSuchProviderException ex)
        {
            ex.printStackTrace();
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if ( v_Folder != null )
                {
                    // 执行删除
                    v_Folder.close(true);
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            try
            {
                if ( v_Store != null )
                {
                    v_Store.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
        }
    }
    
    
    
    /**
     * 删除邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_MailReciveInfo
     * @throws MessagingException
     */
    public static void delete(MailOwnerInfo i_Owner ,MailReciveInfo i_MailReciveInfo) throws MessagingException
    {
        List<MailReciveInfo> v_Param = new ArrayList<MailReciveInfo>(1);
        v_Param.add(i_MailReciveInfo);
        
        deletes(i_Owner ,v_Param);
    }
    
    
    
    /**
     * 删除一批邮件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Owner
     * @param i_MailReciveInfos
     * @throws MessagingException
     */
    public static void deletes(MailOwnerInfo i_Owner ,List<MailReciveInfo> i_MailReciveInfos) throws MessagingException
    {
        if ( Help.isNull(i_MailReciveInfos) )
        {
            return;
        }
        
        Collections.reverse(i_MailReciveInfos);
        int v_StartNo = i_MailReciveInfos.get(0)                           .getMessageNo();
        int v_EndNo   = i_MailReciveInfos.get(i_MailReciveInfos.size() - 1).getMessageNo();
        
        // 根据邮件会话属性和密码验证器构造一个接收邮件的session
        Session v_ReceiverSession = Session.getDefaultInstance(i_Owner.getReciveProperties() ,i_Owner);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        v_ReceiverSession.setDebug(false);
        v_ReceiverSession.setDebugOut(null);
        
        Store      v_Store   = null;
        Folder     v_Folder  = null;
        Message [] v_Message = null;
        try
        {
            v_Store = v_ReceiverSession.getStore(i_Owner.getReciveURLName());
            v_Store.connect();
            v_Folder = v_Store.getFolder("INBOX");
            v_Folder.open(Folder.READ_WRITE);      // 删除时标记为可写模式
            v_Message = v_Folder.getMessages(v_StartNo ,v_EndNo);
            
            for (int i=v_Message.length-1; i>=0; i--)
            {
                MailReciveInfo v_New = new MailReciveInfo((MimeMessage) v_Message[i]);
                
                for (MailReciveInfo v_Old : i_MailReciveInfos)
                {
                    if ( v_Old.equals(v_New) )
                    {
                        v_Message[i].setFlag(Flag.DELETED ,true);
                        break;
                    }
                }
            }
        }
        catch (NoSuchProviderException ex)
        {
            ex.printStackTrace();
        }
        catch (MessagingException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if ( v_Folder != null )
                {
                    // 执行删除
                    v_Folder.close(true);
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
            }
            
            try
            {
                if ( v_Store != null )
                {
                    v_Store.close();
                }
            }
            catch (Exception exce)
            {
                // Nothing.
            }
        }
    }
    
}   
