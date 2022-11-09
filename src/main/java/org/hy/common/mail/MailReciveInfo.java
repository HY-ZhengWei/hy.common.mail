package org.hy.common.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.hy.common.Date;
import org.hy.common.Help;





/**
 * 接收邮件的信息
 *
 * @author      ZhengWei(HY)
 * @createDate  2015-11-29
 * @version     v1.0
 */
public class MailReciveInfo implements Comparable<MailReciveInfo>
{

    private MimeMessage            mimeMessage;
    
    /** 发送人的邮件地址 */
    private String                 sendMail;
    
    /** 发送人名称(有可能为空) */
    private String                 sender;
    
    /** 发送邮件的时间 */
    private Date                   sendTime;
    
    /**
     * 所有收件人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    private Map<String ,String>    reciverTO;
    
    /**
     * 所有抄送人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    private Map<String ,String>    reciverCC;
    
    /**
     * 所有密送人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    private Map<String ,String>    reciverBCC;
    
    /** 邮件主题 */
    private String                 subject;
    
    /** 邮件Html格式的内容 */
    private StringBuilder          contentHtml;
    
    /** 邮件文本格式的内容 */
    private StringBuilder          contentText;
    
    /** 
     * 邮件是否已读
     * 
     * 由于POP3协议是不支持该该功能的，POP3只支持Flags.Flag.DELETE。
     * POP3没有状态，只能读出和删除。
     * 
     * 但IMAP协议是支持的
     */
    private boolean                isReaded;
    
    /** 
     * 邮件附件的文件信息
     * Map.key    附件的文件名称
     * Map.value  附件下载(保存时)到本地，文件流的父对象信息
     */
    private Map<String ,BodyPart>  attachFiles;
    
    
    
    public MailReciveInfo(MimeMessage i_MimeMessage) throws MessagingException, IOException
    {
        this.mimeMessage = i_MimeMessage;
        this.contentHtml = new StringBuilder();
        this.contentText = new StringBuilder();
        this.attachFiles = new LinkedHashMap<String ,BodyPart>();
        
        this.parser();
    }

    
    
    /**
     * 解析收到的邮件信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-29
     * @version     v1.0
     *
     * @throws MessagingException
     * @throws IOException
     */
    public void parser() throws MessagingException, IOException
    {
        InternetAddress v_Address[]   = (InternetAddress []) this.mimeMessage.getFrom();
        Flags           v_Flags       = ((Message)this.mimeMessage).getFlags();
        Flags.Flag[]    v_SystemFlags = v_Flags.getSystemFlags();
        Part            v_Part        = (Part) this.mimeMessage;
        
        this.sendMail   = Help.NVL(v_Address[0].getAddress());
        this.sender     = Help.NVL(v_Address[0].getPersonal());
        this.sendTime   = new Date(this.mimeMessage.getSentDate());
        this.subject    = MimeUtility.decodeText(this.mimeMessage.getSubject());
        this.reciverTO  = this.parserAddress(RecipientType.TO);
        this.reciverCC  = this.parserAddress(RecipientType.CC);
        this.reciverBCC = this.parserAddress(RecipientType.BCC);
        this.isReaded   = false;
        for (int i=0; i<v_SystemFlags.length; i++)
        {
            if ( v_SystemFlags[i] == Flags.Flag.SEEN )
            {
                this.isReaded = true;
                break;
            }
        }
        
        this.parserContent(   v_Part);
        this.parserAttachFile(v_Part);
    }
    
    
    
    /**
     * 获得邮件的收件人，抄送和密送人的地址和姓名
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-29
     * @version     v1.0
     *
     * @param i_RecipientType  邮件接收人的分类
     *                         RecipientType.TO   收件人
     *                         RecipientType.CC   抄送人
     *                         RecipientType.BCC  密送人
     * @return Map.key         邮件人的地址                 
     *         Map.value       邮件人的姓名
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private Map<String ,String> parserAddress(RecipientType i_RecipientType) throws MessagingException, UnsupportedEncodingException
    {
        Map<String ,String> v_Ret     = new LinkedHashMap<String ,String>();
        InternetAddress []  v_Address = (InternetAddress []) mimeMessage.getRecipients(i_RecipientType);
        
        if ( !Help.isNull(v_Address) )
        {
            for (int i=0; i<v_Address.length; i++)
            {
                String v_Email = v_Address[i].getAddress();
                if ( v_Email == null )
                {
                    v_Email = "";
                }
                else
                {
                    v_Email = MimeUtility.decodeText(v_Email);
                }
                
                String v_Personal = v_Address[i].getPersonal();
                if ( v_Personal == null )
                {
                    v_Personal = "";
                }
                else
                {
                    v_Personal = MimeUtility.decodeText(v_Personal);
                }
                
                if ( !Help.isNull(v_Email) )
                {
                    v_Ret.put(v_Email ,v_Personal);
                }
            }
        }
    
        return v_Ret;
    }
    
    
    
    /**
     * 解析邮件，把得到的邮件内容保存到一个StringBuilder对象中解析邮件。
     * 根据MimeType类型的不同执行不同的操作，递归解析
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-29
     * @version     v1.0
     *
     * @param i_Part
     * @throws MessagingException
     * @throws IOException
     */
    private void parserContent(Part i_Part) throws MessagingException, IOException
    {
        String  v_ContentType = i_Part.getContentType();
        int     v_NameIndex   = v_ContentType.indexOf("name");
        boolean v_Conname     = v_NameIndex != -1;
        
        if ( i_Part.isMimeType("text/plain") && !v_Conname )
        {
            this.contentText.append((String) i_Part.getContent());
        }
        else if ( i_Part.isMimeType("text/html") && !v_Conname )
        {
            this.contentHtml.append((String) i_Part.getContent());
        }
        else if ( i_Part.isMimeType("multipart/*") )
        {
            Multipart v_Multipart = (Multipart) i_Part.getContent();
            int       v_Counts    = v_Multipart.getCount();
            
            for (int i=0; i<v_Counts; i++)
            {
                this.parserContent(v_Multipart.getBodyPart(i));
            }
        }
        else if ( i_Part.isMimeType("message/rfc822") )
        {
            this.parserContent((Part) i_Part.getContent());
        }
    }
    
    
    
    /**
     * 解析邮件中的附件信息
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-29
     * @version     v1.0
     *
     * @param i_Part
     * @throws MessagingException
     * @throws IOException
     */
    private void parserAttachFile(Part i_Part) throws MessagingException, IOException
    {
        String v_FileName = "";
        if ( i_Part.isMimeType("multipart/*") )
        {
            Multipart v_Multipart = (Multipart) i_Part.getContent();
            for (int i = 0; i<v_Multipart.getCount(); i++)
            {
                BodyPart v_BodyPart    = v_Multipart.getBodyPart(i);
                String   v_Disposition = v_BodyPart.getDisposition();
                
                if ( v_Disposition != null && (v_Disposition.equals(Part.ATTACHMENT) || v_Disposition.equals(Part.INLINE)) )
                {
                    v_FileName = v_BodyPart.getFileName();
                    if ( v_FileName.toLowerCase().indexOf("gb2312") != -1
                      || v_FileName.toLowerCase().indexOf("gbk")    != -1
                      || v_FileName.toLowerCase().indexOf("utf")    != -1 )
                    {
                        v_FileName = MimeUtility.decodeText(v_FileName);
                    }
                    this.attachFiles.put(v_FileName ,v_BodyPart);
                }
                else if ( v_BodyPart.isMimeType("multipart/*") )
                {
                    this.parserAttachFile(v_BodyPart);
                }
                else
                {
                    v_FileName = v_BodyPart.getFileName();
                    if (  v_FileName != null
                      && (v_FileName.toLowerCase().indexOf("gb2312") != -1
                       || v_FileName.toLowerCase().indexOf("gbk")    != -1
                       || v_FileName.toLowerCase().indexOf("utf")    != -1 ) )
                    {
                        v_FileName = MimeUtility.decodeText(v_FileName);
                        this.attachFiles.put(v_FileName ,v_BodyPart);
                    }
                }
            }
        }
        else if ( i_Part.isMimeType("message/rfc822") )
        {
            this.parserAttachFile((Part) i_Part.getContent());
        }
    }
    
    

    /**
     * 获得此邮件的MessageID
     */
    public String getMessageID()
    {
        try
        {
            return this.mimeMessage.getMessageID();
        }
        catch (MessagingException exce)
        {
            return "";
        }
    }
    
    
    
    /**
     * 获得此邮件的MessageNO
     */
    public int getMessageNo()
    {
        return this.mimeMessage.getMessageNumber();
    }
    
    
    
    /**
     * 获取：发送人的邮件地址
     */
    public String getSendMail()
    {
        return sendMail;
    }

    
    
    /**
     * 获取：发送人名称(有可能为空)
     */
    public String getSender()
    {
        return sender;
    }


    
    /**
     * 获取：发送邮件的时间
     */
    public Date getSendTime()
    {
        return sendTime;
    }

    
    
    /**
     * 获取：所有收件人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    public Map<String ,String> getReciverTO()
    {
        return reciverTO;
    }


    
    /**
     * 获取：所有抄送人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    public Map<String ,String> getReciverCC()
    {
        return reciverCC;
    }


    
    /**
     * 获取：所有密送人信息
     * Map.key         邮件人的地址                 
     * Map.value       邮件人的姓名(有可能为空)
     */
    public Map<String ,String> getReciverBCC()
    {
        return reciverBCC;
    }



    /**
     * 获取：邮件主题
     */
    public String getSubject()
    {
        return subject;
    }



    /**
     * 邮件是否需要回执，如果需要回执返回"true",否则返回"false"
     */
    public boolean isReply()
    {
        try
        {
            return Help.isNull(this.mimeMessage.getHeader("Disposition-Notification-To"));
        }
        catch (MessagingException exce)
        {
            return false;
        }
    }
    
    
    
    /**
     * 获取邮件内容的类型
     */
    public String getContentType()
    {
        try
        {
            return ((Part)this.mimeMessage).getContentType();
        }
        catch (MessagingException exce)
        {
            return "";
        }
    }
    


    /**
     * 获得邮件正文内容，默认先取文本格式的，后取Html格式的
     */
    public String getContent()
    {
        String v_Info = this.contentText.toString();
        if ( v_Info != null && !"".equals(v_Info) )
        {
            return v_Info;
        }
        else
        {
            return this.contentHtml.toString();
        }
    }
    
    
    
    /**
     * 获得邮件Html格式的正文内容
     */
    public String getContentHtml()
    {
        return this.contentHtml.toString();
    }
    
    
    
    /**
     * 获得邮件文本格式的正文内容
     */
    public String getContentText()
    {
        return this.contentText.toString();
    }
    
    
    
    /**
     * 获取：邮件是否已读
     * 
     * 由于POP3协议是不支持该该功能的，POP3只支持Flags.Flag.DELETE。
     * POP3没有状态，只能读出和删除。
     * 
     * 但IMAP协议是支持的
     */
    public boolean isReaded()
    {
        return isReaded;
    }



    /**
     * 获取附件文件的个数
     */
    public int getAttachFileCount()
    {
        return this.attachFiles.size();
    }
    
    
    
    /**
     * 获取附件文件名称
     */
    public List<String> getAttachFileNames()
    {
        return Help.toListKeys(this.attachFiles);
    }
    
    
    
    /**
     * 获取附件文件名称和对应文件的大小
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @return  Map.key    文件名称
     *          Map.value  文件大小
     * @throws MessagingException
     */
    public Map<String ,Integer> getAttachFileSizes() throws MessagingException
    {
        Map<String ,Integer> v_Ret = new LinkedHashMap<String ,Integer>();
        
        for (String v_FileName : this.attachFiles.keySet())
        {
            v_Ret.put(v_FileName ,Integer.valueOf(this.attachFiles.get(v_FileName).getSize()));
        }
        
        return v_Ret;
    }
    
    
    
    /**
     * 获取具体某一附件文件的数据流。用于下载保存文件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_FileName
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public InputStream getAttachFile(String i_FileName) throws IOException, MessagingException
    {
        if ( Help.isNull(this.attachFiles) )
        {
            return null;
        }
        
        if ( !this.attachFiles.containsKey(i_FileName) )
        {
            return null;
        }
        
        return this.attachFiles.get(i_FileName).getInputStream();
    }
    
    
    
    /**
     * 按发送时间和消息ID比较
     *
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param i_Other
     * @return
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MailReciveInfo i_Other)
    {
        if ( this == i_Other )
        {
            return 0;
        }
        else if ( i_Other == null )
        {
            return -1;
        }
        else
        {
            if ( this.sendTime == i_Other.sendTime )
            {
                if ( this.getMessageID() == null )
                {
                    return -1;
                }
                else
                {
                    return this.getMessageID().compareTo(i_Other.getMessageID());
                }
            }
            else if ( this.sendTime == null )
            {
                return -1;
            }
            else if ( i_Other.sendTime == null )
            {
                return 1;
            }
            else
            {
                int v_Ret = this.sendTime.compareTo(i_Other.sendTime);
                if ( v_Ret == 0 )
                {
                    if ( this.getMessageID() == null )
                    {
                        return -1;
                    }
                    else
                    {
                        return this.getMessageID().compareTo(i_Other.getMessageID());
                    }
                }
                else
                {
                    return v_Ret;
                }
            }
        }
    }



    @Override
    public int hashCode()
    {
        if ( this.sendTime != null && !Help.isNull(this.getMessageID()) )
        {
            return Integer.parseInt(this.sendTime.hashCode() + "" + this.getMessageID().hashCode());
        }
        else if ( this.sendTime != null )
        {
            return this.sendTime.hashCode();
        }
        else if ( !Help.isNull(this.getMessageID()) )
        {
            return this.getMessageID().hashCode();
        }
        else
        {
            return this.getMessageNo();
        }
    }



    /**
     * 按发送时间和消息ID，比较比较是否相等。
     *
     * @author      ZhengWei(HY)
     * @createDate  2015-11-30
     * @version     v1.0
     *
     * @param obj
     * @return
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object i_Other)
    {
        if ( this == i_Other )
        {
            return true;
        }
        else if ( i_Other == null )
        {
            return false;
        }
        else if ( i_Other instanceof MailReciveInfo )
        {
            return this.compareTo((MailReciveInfo)i_Other) == 0;
        }
        else
        {
            return false;
        }
    }
    
}
