package org.hy.common.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import org.hy.common.Help;





/**
 * 发送邮件的主人的账户信息
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-07-17
 *           V2.0  2016-07-28  添加：支持SSL验证
 *                             添加：实现 Comparable 等比较方法
 *                             修正：实现发送者（或接收者）属性参数只生成一只，不用重复生成
 */
public class MailOwnerInfo extends Authenticator implements java.lang.Comparable<MailOwnerInfo>
{

    /** 发送邮件的服务器的IP。如：smtp.163.com */
    private String                 sendHost;

    /** 发送邮件的服务器的端口 */
    private int                    sendPort;
    
    /** 发送邮件的协议 */
    private String                 sendProtocol;
    
    /** 接收邮件的服务器的IP。如：pop3.163.com */
    private String                 reciveHost;
    
    /** 接收邮件的服务器的端口 */
    private int                    recivePort;
    
    /** 接收邮件的协议 */
    private String                 reciveProtocol;
    
    /** 登陆邮件发送服务器的用户名 */
    private String                 userName;

    /** 登陆邮件发送服务器的密码 */
    private String                 password;
    
    /** 用户与密码是否改变过 */
    private boolean                isNamePasswdChange;
    
    /** 重写Authenticator对象的getPasswordAuthentication()方法 */
    private PasswordAuthentication passwdAuthentication;
    
    /** 是否需要身份验证 */
    private boolean                isValidate;
    
    /** 是否需求SSL验证 */
    private boolean                isValidateSSL;
    
    /** 邮件EMail地址对象 */
    private InternetAddress        emailAddress;
    
    /** 是否启用代理 */
    private boolean                isProxySet;
    
    /** 代理服务器的IP */
    private String                 proxyHost;
    
    /** 代理服务器的端口 */
    private int                    proxyPort;
    
    /** 发送者的属性 */
    private Properties             sendProperties;
    
    /** 接收者的属性 */
    private Properties             reciveProperties;
    

    
    public MailOwnerInfo()
    {
        this.sendPort           = 25;
        this.sendProtocol       = "smtp";
        this.recivePort         = 110;
        this.reciveProtocol     = "pop3";
        this.isValidate         = false;
        this.isNamePasswdChange = false;
        this.isProxySet         = false;
    }
    
    
    
    /**
     * 用于接收邮件的参数对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-11-29
     * @version     v1.0
     *
     * @return
     */
    public URLName getReciveURLName()
    {
        return new URLName(this.reciveProtocol ,this.reciveHost ,this.recivePort ,null ,this.getEmail() ,this.password);
    }
    
    

    /**
     * 获得发送邮件会话属性
     */
    public synchronized Properties getSendProperties()
    {
        if ( this.sendProperties == null )
        {
            this.sendProperties = new Properties();
    
            if ( this.isProxySet )
            {
                this.sendProperties.put("mail.smtp.socks.host" ,this.proxyHost);
                this.sendProperties.put("mail.smtp.socks.port" ,String.valueOf(this.proxyPort));
                
    //            v_Props.put("mail.smtp.auth.login.disable" ,"true");
    //            v_Props.put("mail.smtp.auth.plain.disable" ,"true");
                
    //            Properties v_SYS_Props = System.getProperties();
    //            v_SYS_Props.setProperty("proxySet"       ,"true");
    //            
    //            v_SYS_Props.setProperty("ProxyHost"      ,this.proxyHost);
    //            v_SYS_Props.setProperty("ProxyPort"      ,String.valueOf(this.proxyPort));
    //            
    //            v_SYS_Props.setProperty("http.proxyHost" ,this.proxyHost);
    //            v_SYS_Props.setProperty("http.proxyPort" ,String.valueOf(this.proxyPort));
                
    //            v_SYS_Props.setProperty("socksProxyHost" ,this.proxyHost);
    //            v_SYS_Props.setProperty("socksProxyPort" ,String.valueOf(this.proxyPort));
            }
            
            this.sendProperties.put("mail.smtp.host"             ,this.sendHost);
            this.sendProperties.put("mail.smtp.port"             ,String.valueOf(this.sendPort));
            this.sendProperties.put("mail.smtp.auth"             ,this.isValidate ? "true" : "false");
            if ( this.isValidate )
            {
                this.sendProperties.put("username"               ,this.userName);
                this.sendProperties.put("password"               ,this.password);
            }
            this.sendProperties.put("mail.transport.protocol"    ,this.sendProtocol);
            
            
            if ( this.isValidateSSL() )
            {
                this.sendProperties.setProperty("mail.smtp.socketFactory.class"    ,"javax.net.ssl.SSLSocketFactory");
                this.sendProperties.setProperty("mail.smtp.socketFactory.fallback" ,"false") ;
                this.sendProperties.setProperty("mail.smtp.socketFactory.port"     ,String.valueOf(this.sendPort)) ;
                this.sendProperties.put("mail.smtp.starttls.enable"                ,"true");  // 是否启用TLS加密传输，需要服务器支持
            }
            
            // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
            // 用（你可以在控制台（console)上看到发送邮件的过程）
            this.sendProperties.put("mail.debug"                 ,"false");
        
        }
        
        return this.sendProperties;
    }
    
    
    
    /**
     * 获得接收邮件会话属性
     */
    public synchronized Properties getReciveProperties()
    {
        if ( this.reciveProperties == null )
        {
            this.reciveProperties = new Properties();
            
            if ( this.isProxySet )
            {
                this.reciveProperties.put("mail.smtp.socks.host" ,this.proxyHost);
                this.reciveProperties.put("mail.smtp.socks.port" ,String.valueOf(this.proxyPort));
            }
            
            this.reciveProperties.put("mail.smtp.host"             ,this.reciveHost);
            this.reciveProperties.put("mail.smtp.port"             ,String.valueOf(this.recivePort));
            this.reciveProperties.put("mail.smtp.auth"             ,this.isValidate ? "true" : "false");
            this.reciveProperties.put("mail.transport.protocol"    ,this.reciveProtocol);
            
            // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
            // 用（你可以在控制台（console)上看到发送邮件的过程）
            this.reciveProperties.put("mail.debug"                 ,"false");
        }
        
        return this.reciveProperties;
    }
    
    
    
    /**
     * 如果需要身份认证，则创建一个密码验证器
     */
    protected PasswordAuthentication getPasswordAuthentication() 
    {
        if ( this.isValidate )
        {
            if ( this.passwdAuthentication == null )
            {
                this.passwdAuthentication = new PasswordAuthentication(this.userName, this.password);
            }
            else if ( this.isNamePasswdChange )
            {
                this.passwdAuthentication = new PasswordAuthentication(this.userName, this.password);
                this.isNamePasswdChange   = false;
            }
            
            return this.passwdAuthentication;
        }
        else
        {
            // 当不需要身份认证时
            return null;
        }
    }



    public String getEmail()
    {
        return this.emailAddress.getAddress();
    }
    
    
    
    public InternetAddress getEmailAddress()
    {
        return this.emailAddress;
    }


    
    public void setEmail(String i_Email)
    {
        try
        {
            this.emailAddress = new InternetAddress(i_Email);
            
            if ( this.userName == null )
            {
                this.setUserName(this.emailAddress.getAddress());
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
    }


    
    public boolean isValidate()
    {
        return isValidate;
    }



    public void setValidate(boolean isValidate)
    {
        this.isValidate = isValidate;
    }


    
    public String getPassword()
    {
        return password;
    }



    public void setPassword(String password)
    {
        this.password           = password;
        this.isNamePasswdChange = true;
    }


    
    public String getUserName()
    {
        return userName;
    }


    
    public void setUserName(String userName)
    {
        this.userName           = userName;
        this.isNamePasswdChange = true;
        
        if ( this.emailAddress == null )
        {
            this.setEmail(userName);
        }
    }



    public String getSendHost()
    {
        return sendHost;
    }


    
    public void setSendHost(String i_SendHost)
    {
        this.sendHost = i_SendHost;
    }


    
    public int getSendPort()
    {
        return this.sendPort;
    }


    
    public void setSendPort(int i_SendPort)
    {
        this.sendPort = i_SendPort;
    }
    
    
    
    /**
     * 获取：发送邮件的协议
     */
    public String getSendProtocol()
    {
        return sendProtocol;
    }
    
    
    
    /**
     * 设置：发送邮件的协议
     * 
     * @param sendProtocol 
     */
    public void setSendProtocol(String sendProtocol)
    {
        this.sendProtocol = sendProtocol;
    }



    /**
     * 获取：接收邮件的服务器的IP。如：pop3.163.com
     */
    public String getReciveHost()
    {
        return reciveHost;
    }


    
    /**
     * 设置：接收邮件的服务器的IP。如：pop3.163.com
     * 
     * @param reciveHost 
     */
    public void setReciveHost(String reciveHost)
    {
        this.reciveHost = reciveHost;
    }



    /**
     * 获取：接收邮件的服务器的端口
     */
    public int getRecivePort()
    {
        return recivePort;
    }


    
    /**
     * 设置：接收邮件的服务器的端口
     * 
     * @param receivePort 
     */
    public void setRecivePort(int receivePort)
    {
        this.recivePort = receivePort;
    }

    
    
    /**
     * 获取：接收邮件的协议
     */
    public String getReciveProtocol()
    {
        return reciveProtocol;
    }

    
    
    /**
     * 设置：接收邮件的协议
     * 
     * @param reciveProtocol 
     */
    public void setReciveProtocol(String reciveProtocol)
    {
        this.reciveProtocol = reciveProtocol;
    }



    public boolean isProxySet()
    {
        return isProxySet;
    }


    
    public void setProxySet(boolean isProxySet)
    {
        this.isProxySet = isProxySet;
    }


    
    public String getProxyHost()
    {
        return proxyHost;
    }


    
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
        
        if ( !Help.isNull(this.proxyHost) && 0 < this.proxyPort && this.proxyPort < 65535 )
        {
            this.isProxySet = true;
        }
        else
        {
            this.isProxySet = false;
        }
    }


    
    public int getProxyPort()
    {
        return proxyPort;
    }


    
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
        
        if ( !Help.isNull(this.proxyHost) && 0 < this.proxyPort && this.proxyPort < 65535 )
        {
            this.isProxySet = true;
        }
        else
        {
            this.isProxySet = false;
        }
    }


    
    /**
     * 获取：是否需求SSL验证
     */
    public boolean isValidateSSL()
    {
        return isValidateSSL;
    }


    
    /**
     * 设置：是否需求SSL验证
     * 
     * @param isValidateSSL 
     */
    public void setValidateSSL(boolean isValidateSSL)
    {
        this.isValidateSSL = isValidateSSL;
    }



    @Override
    public int hashCode()
    {
        if ( Help.isNull(this.userName) )
        {
            return Help.random(Integer.MAX_VALUE);
        }
        
        return this.userName.hashCode();
    }



    @Override
    public boolean equals(Object i_Other)
    {
        if ( i_Other == null )
        {
            return false;
        }
        else if ( this == i_Other )
        {
            return true;
        }
        else if ( i_Other instanceof MailOwnerInfo )
        {
            if ( Help.isNull(this.userName) )
            {
                return false;
            }
            
            return this.userName.equals(((MailOwnerInfo)i_Other).userName);
        }
        else
        {
            return false;
        }
    }



    @Override
    public int compareTo(MailOwnerInfo i_Other)
    {
        if ( i_Other == null )
        {
            return 1;
        }
        else if ( this == i_Other )
        {
            return 0;
        }
        else
        {
            if ( Help.isNull(this.userName) )
            {
                return -1;
            }
            
            return this.userName.compareTo(i_Other.userName);
        }
    }
    
}
