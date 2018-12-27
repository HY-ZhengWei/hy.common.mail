package org.hy.common.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.hy.common.Help;





/**
 * 发送邮件的主人的账户信息
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2013-07-17
 *           V2.0  2016-07-28  添加：支持SSL验证
 *                             添加：实现 Comparable 等比较方法
 *                             修正：实现发送者（或接收者）属性参数只生成一只，不用重复生成
 *           V3.0  2018-11-23  添加：发件人昵称的功能（建议人：杨东）
 *           V4.0  2018-12-27  优化：通过代理服务发送邮件的功能
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
    
    /** 邮件发件人的昵称 */
    private String                 nickName;
    
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
    
    /** 邮件EMail地址（原始的邮件地址，未添加昵称等信息的纯邮件地址） */
    private String                 email;
    
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
                // 属性参数来源详见：SocketFetcher.createSocket()。注：SocketFetcher类中还有更多其它属性参数
                // com.sun.mail.util.SocketFetcher
                this.sendProperties.put("mail.smtp.proxy.host" ,this.proxyHost);
                this.sendProperties.put("mail.smtp.proxy.port" ,String.valueOf(this.proxyPort));
                
                /*
                // 下面的设置是不生效的
                this.sendProperties.put("proxySet", true); 
                this.sendProperties.put("http.proxyHost", this.proxyHost); 
                this.sendProperties.put("http.proxyPort", this.proxyPort); 
                this.sendProperties.put("socksProxySet", true); 
                this.sendProperties.put("socksProxyHost", this.proxyHost); 
                this.sendProperties.put("socksProxyPort", this.proxyPort);
                this.sendProperties.put("proxyHost", this.proxyHost); 
                this.sendProperties.put("proxyPort", this.proxyPort);
                */ 
                
                /*
                // 下面为的设置可以生效，可通过代理发送。这也是网上很人文章说的方案
                // 但也会改变整个Java运行环境，不建议采用
                System.getProperties().put("proxySet", true); 
                System.getProperties().put("http.proxyHost", this.proxyHost); 
                System.getProperties().put("http.proxyPort", this.proxyPort); 
                System.getProperties().put("socksProxySet", true); 
                System.getProperties().put("socksProxyHost", this.proxyHost); 
                System.getProperties().put("socksProxyPort", this.proxyPort);
                System.getProperties().put("proxyHost", this.proxyHost); 
                System.getProperties().put("proxyPort", this.proxyPort); 
                */
                
                /*
                this.sendProperties.put("mail.smtp.auth.login.disable" ,"true");
                this.sendProperties.put("mail.smtp.auth.plain.disable" ,"true");
                */
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
            this.sendProperties.put("mail.debug" ,"false");
        
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



    public synchronized String getEmail()
    {
        return this.email;
    }
    
    
    
    public InternetAddress getEmailAddress()
    {
        return this.emailAddress;
    }


    
    public synchronized void setEmail(String i_Email)
    {
        this.email = i_Email;
        try
        {
            if ( Help.isNull(this.nickName) ) 
            {
                this.emailAddress = new InternetAddress(this.email);
            }
            else
            {
                String v_NickName = this.nickName;  
                try 
                {  
                    v_NickName = MimeUtility.encodeText(v_NickName);  
                } 
                catch (Exception exce) 
                {  
                    exce.printStackTrace();  
                }
                
                this.emailAddress = new InternetAddress(v_NickName + " <" + this.email + ">");
            }
            
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


    
    /**
     * 获取：是否需要身份验证
     */
    public boolean isValidate()
    {
        return isValidate;
    }


    
    /**
     * 设置：是否需要身份验证
     * 
     * @param isValidate 
     */
    public void setValidate(boolean isValidate)
    {
        this.isValidate = isValidate;
    }



    /**
     * 获取：登陆邮件发送服务器的密码
     */
    public String getPassword()
    {
        return password;
    }


    
    /**
     * 设置：登陆邮件发送服务器的密码
     * 
     * @param password 
     */
    public void setPassword(String password)
    {
        this.password           = password;
        this.isNamePasswdChange = true;
    }



    /**
     * 获取：邮件发件人的昵称
     */
    public String getNickName()
    {
        return nickName;
    }


    
    /**
     * 设置：邮件发件人的昵称
     * 
     * @param nickName 
     */
    public void setNickName(String nickName)
    {
        this.nickName = nickName;
        
        if ( !Help.isNull(this.email) )
        {
            this.setEmail(this.email);
        }
    }



    /**
     * 获取：登陆邮件发送服务器的用户名
     */
    public String getUserName()
    {
        return userName;
    }


    
    /**
     * 设置：登陆邮件发送服务器的用户名
     * 
     * @param nickName 
     */
    public void setUserName(String userName)
    {
        this.userName           = userName;
        this.isNamePasswdChange = true;
        
        if ( this.emailAddress == null )
        {
            this.setEmail(userName);
        }
    }


    
    /**
     * 获取：发送邮件的服务器的IP。如：smtp.163.com
     */
    public String getSendHost()
    {
        return sendHost;
    }


    
    /**
     * 设置：发送邮件的服务器的IP。如：smtp.163.com
     * 
     * @param sendHost 
     */
    public void setSendHost(String sendHost)
    {
        this.sendHost = sendHost;
    }



    /**
     * 获取：发送邮件的服务器的端口
     */
    public int getSendPort()
    {
        return sendPort;
    }


    
    /**
     * 设置：发送邮件的服务器的端口
     * 
     * @param sendPort 
     */
    public void setSendPort(int sendPort)
    {
        this.sendPort = sendPort;
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
