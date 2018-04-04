package org.hy.common.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.hy.common.Help;





/**
 * 发送邮件的信息
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-07-22
 *           V2.0  2018-04-04  添加：抄送者们、暗送者们的功能
 */
public class MailSendInfo
{
    
    /** 邮件接收者的地址对象 */
    private List<InternetAddress>  emailAddressList;
    
    /** 邮件抄送者的地址对象  */
    private List<InternetAddress>  emailCCAddressList;
    
    /** 邮件密送者的地址对象  */
    private List<InternetAddress>  emailBCCAddressList;
    
    /** 邮件主题 */
    private String                 subject;

    /** 邮件的文本内容 */
    private String                 content;

    /** 邮件附件的文件名 */
    private List<String>           attachFileNames;
    
    
    
    public MailSendInfo()
    {
        this.emailAddressList    = new ArrayList<InternetAddress>();
        this.emailCCAddressList  = new ArrayList<InternetAddress>();
        this.emailBCCAddressList = new ArrayList<InternetAddress>();
    }


    
    public List<String> getAttachFileNames()
    {
        return attachFileNames;
    }


    
    public void setAttachFileNames(List<String> i_AttachFileNames)
    {
        this.attachFileNames = i_AttachFileNames;
    }
    
    
    
    public void setAttachFile(String i_FileName)
    {
        this.addAttachFile(i_FileName);
    }
    
    
    
    public void setAttachFile(File i_File)
    {
        this.addAttachFile(i_File);
    }
    
    
    
    /**
     * 添加附件信息
     * 
     * @param i_FileName  文件的全路径信息
     */
    public synchronized void addAttachFile(String i_FileName)
    {
        if ( this.attachFileNames == null )
        {
            this.attachFileNames = new ArrayList<String>();
        }
        
        if ( Help.isNull(i_FileName) )
        {
            throw new NullPointerException("File name is null");
        }
        
        this.addAttachFile(new File(i_FileName));
    }
    
    
    
    /**
     * 添加附件信息
     * 
     * @param i_File
     */
    public synchronized void addAttachFile(File i_File)
    {
        if ( this.attachFileNames == null )
        {
            this.attachFileNames = new ArrayList<String>();
        }
        
        if ( !i_File.isFile() )
        {
            throw new NullPointerException(i_File.getAbsolutePath() + " is not file type.");
        }
        
        if ( !i_File.canRead() )
        {
            throw new NullPointerException(i_File.getAbsolutePath() + " can't read.");
        }
        
        this.attachFileNames.add(i_File.getAbsolutePath());
    }


    
    public String getContent()
    {
        return content;
    }


    
    public void setContent(String content)
    {
        this.content = content;
    }


    
    public String getSubject()
    {
        return subject;
    }


    
    public void setSubject(String subject)
    {
        this.subject = subject;
    }



    public void setEmail(String i_Email)
    {
        this.addEmail(i_Email);
    }
    
    
    
    public void setEmailCC(String i_Email)
    {
        this.addEmailCC(i_Email);
    }
    
    
    
    public void setEmailBCC(String i_Email)
    {
        this.addEmailBCC(i_Email);
    }
    
    
    
    public synchronized void addEmail(String [] i_EMail)
    {
        for (String v_EMail : i_EMail)
        {
            this.addEmail(v_EMail);
        }
    }
    
    
    
    public synchronized void addEmailCC(String [] i_EMail)
    {
        for (String v_EMail : i_EMail)
        {
            this.addEmailCC(v_EMail);
        }
    }
    
    
    
    public synchronized void addEmailBCC(String [] i_EMail)
    {
        for (String v_EMail : i_EMail)
        {
            this.addEmailBCC(v_EMail);
        }
    }
    
    
    
    public synchronized void addEmail(String i_EMail)
    {
        try
        {
            this.emailAddressList.add(new InternetAddress(i_EMail));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    public synchronized void addEmailCC(String i_EMail)
    {
        try
        {
            this.emailCCAddressList.add(new InternetAddress(i_EMail));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    public synchronized void addEmailBCC(String i_EMail)
    {
        try
        {
            this.emailBCCAddressList.add(new InternetAddress(i_EMail));
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }
    
    
    
    public InternetAddress [] getEmailAddressList()
    {
        return this.emailAddressList.toArray(new InternetAddress[]{});
    }
    
    
    
    public InternetAddress [] getEmailCCAddressList()
    {
        return this.emailCCAddressList.toArray(new InternetAddress[]{});
    }
    
    
    
    public InternetAddress [] getEmailBCCAddressList()
    {
        return this.emailBCCAddressList.toArray(new InternetAddress[]{});
    }
    
    
    
    public synchronized void clearEmail()
    {
        this.emailAddressList   .clear();
        this.emailCCAddressList .clear();
        this.emailBCCAddressList.clear();
    }
    
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    protected void finalize() throws Throwable
    {
        this.emailAddressList.clear();
        this.emailAddressList = null;
        
        super.finalize();
    }
    */

}
