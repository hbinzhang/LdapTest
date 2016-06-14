package com.zhb.ldap.test;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class Test {

	/*
	public static void main(String[] args) {
		Test LDAPTest1 = new Test();
		String root = "dc=hebqts,dc=gov.cn"; // 根
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://10.3.10.106/" + root); // ip地址本机就是localhost
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=Manager,dc=hebqts,dc=gov.cn"); // 管理员
		env.put(Context.SECURITY_CREDENTIALS, "secret"); // 管理员 密码
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			System.out.println("认证成功");
		}
		catch (javax.naming.AuthenticationException e) {
			e.printStackTrace();
			System.out.println("认证失败");
		}
		catch (Exception e) {
			System.out.println("认证出错：");
			e.printStackTrace();
		}
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (NamingException e) {

			}
		}
	}
	*/
	
	public static void main(String[] args) {  
	    String url = "ldap://10.3.10.106:389/";  
	    String domain = "dc=hebqts,dc=gov.cn";  
	    String user = "cn=Manager";  
	    String password = "secret";  
	    Hashtable<String, String> env = new Hashtable<String, String>();  
	    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); // LDAP 工厂  
	    env.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别  
	    env.put(Context.PROVIDER_URL, url);  
	    env.put(Context.SECURITY_PRINCIPAL, user+","+domain); //  填DN  
	    env.put(Context.SECURITY_CREDENTIALS, password); // AD Password  
	    env.put("java.naming.ldap.attributes.binary", "objectSid objectGUID");  
	    LdapContext ldapCtx = null;  
	    try {  
	        ldapCtx = new InitialLdapContext(env , null);  
	        queryGroup(ldapCtx);  
	        //testAdd(ldapCtx);
	        //testModify(ldapCtx);
	        testRemove(ldapCtx);         
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    } finally {  
	        if(ldapCtx != null) {  
	            try {  
	                ldapCtx.close();  
	            } catch (NamingException e) {  
	            }  
	        }  
	    }  
	}  
	  
	public static void testAdd(LdapContext ctx) throws Exception { 
		Attributes attrs = new BasicAttributes(true); 
		Attribute objclass = new BasicAttribute("objectclass"); 
		// 添加ObjectClass 
		String[] attrObjectClassPerson = { "inetOrgPerson", "organizationalPerson", "person", "top" }; 
		Arrays.sort(attrObjectClassPerson); 
		for (String ocp : attrObjectClassPerson) { 
			objclass.add(ocp); 
		} 
		attrs.put(objclass); 
		String uid = "Hai Bin Zhang"; 
		// cn=Hai Bin Zhang,ou=Admin,ou=Corporate,dc=hebqts,dc=gov.cn
		String userDN = "cn=Hai Bin Zhang,ou=Admin,ou=Corporate,dc=hebqts,dc=gov.cn"; 
		// 密码处理 
		// attrs.put("uid", uid); 
		attrs.put("cn", uid); 
		attrs.put("sn", uid); 
		attrs.put("displayName", "张三"); 
		attrs.put("mail", "abc@163.com"); 
		//attrs.put("description", ""); 
		attrs.put("userPassword", "Passw0rd".getBytes("UTF-8")); 
		ctx.createSubcontext(userDN, attrs); 
		
		 System.out.println("===============Add succesful================");  
	} 
	
	public static boolean testModify(LdapContext ctx) throws Exception { 
		boolean result = true; 
		String uid = "Hai Bin Zhang"; 
		String userDN = "cn=Hai Bin Zhang,ou=Admin,ou=Corporate,dc=hebqts,dc=gov.cn"; 
		Attributes attrs = new BasicAttributes(true); 
		attrs.put("mail", "zhanghbmodified@163.com"); 
		ctx.modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, attrs); 
		
		System.out.println("===============Modify succesful================");  
		return result; 
	}  

	public static void testRemove(LdapContext ctx) throws Exception { 
		String uid = "Hai Bin Zhang"; 
		String userDN = "cn=Hai Bin Zhang,ou=Admin,ou=Corporate,dc=hebqts,dc=gov.cn"; 
		ctx.destroySubcontext(userDN); 
		System.out.println("===============Remove succesful================");  
	}  

	private static void queryGroup(LdapContext ldapCtx) throws NamingException {  
	    SearchControls searchCtls = new SearchControls();  
	    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);  
	    String searchFilter = "objectClass=dcObject";  
	    String searchBase = "dc=hebqts,dc=gov.cn";  
	    String returnedAtts[] = {"o", "objectClass", "dc"};  
	    searchCtls.setReturningAttributes(returnedAtts);  
	    NamingEnumeration<SearchResult> answer = ldapCtx.search(searchBase, searchFilter, searchCtls);  
	    while (answer.hasMoreElements()) {
	        SearchResult sr = answer.next();  
	        Attributes Attrs = sr.getAttributes();  
	        if (Attrs != null) {  
	            NamingEnumeration<?> ne = Attrs.getAll();  
	            while(ne.hasMore()) {  
	                Attribute Attr = (Attribute)ne.next();  
	                String name = Attr.getID();  
	                Enumeration<?> values = Attr.getAll();  
	                if (values != null) { // 迭代  
	                    while (values.hasMoreElements()) {  
	                        String value = "";  
	                        if("objectGUID".equals(name)) {  
	                            value = UUID.nameUUIDFromBytes((byte[]) values.nextElement()).toString();  
	                        } else {  
	                            value = (String)values.nextElement();  
	                        }  
	                        System.out.println(name + " " + value);  
	                    }  
	                }  
	            }  
	            System.out.println("==========Query succesful==========");  
	        }  
	    }  
	}  

}