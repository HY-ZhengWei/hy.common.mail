package org.hy.common.mail.junit;

import java.util.ArrayList;
import java.util.List;

import org.hy.common.Date;
import org.hy.common.mail.MailOwnerInfo;
import org.hy.common.mail.MailSendInfo;
import org.hy.common.mail.SimpleMail;
import org.hy.common.xml.XJava;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：通过代理发邮件 
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-12-27
 * @version     v1.0
 */
@Xjava(value=XType.XML)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_Mail_Proxy
{
    private static boolean $isInit = false;
    
    
    
    public JU_Mail_Proxy() throws Exception
    {
        if ( !$isInit )
        {
            $isInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    @Test
    public void test_Mail()
    {
        List<String> v_Recivers = new ArrayList<String>();
        v_Recivers.add("HY.ZhengWei@qq.com");
        
        
        MailSendInfo v_SendInfo = new MailSendInfo();
        v_SendInfo.addEmail(v_Recivers.toArray(new String[]{}));    // 设置多个邮件接收人
        v_SendInfo.setSubject("邮件标题：测试" + Date.getNowTime().getFull());
        v_SendInfo.setContent("邮件内容：Hello World!");
        
        
        MailOwnerInfo v_MailOwner = (MailOwnerInfo)XJava.getObject("MailSender");
        boolean v_Ret = SimpleMail.sendHtmlMail(v_MailOwner ,v_SendInfo);
        
        if ( v_Ret )
        {
            System.out.println("邮件发送成功");
        }
        else
        {
            System.err.println("邮件发送出现异常");
        }
    }
    
}
