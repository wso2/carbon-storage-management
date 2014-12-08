/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.data.mgt;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

public class Util {

	 /**
	   * Create a subcontext including any intermediate contexts.
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which value will be bound
	   * @param name
	   *          the name relative to ctx of the subcontext.
	   * @return The new or existing JNDI subcontext
	   * @throws javax.naming.NamingException
	   *           on any JNDI failure
	   */
	  public static Context createSubcontext(Context ctx, String name) throws NamingException {
	    Name n = ctx.getNameParser("").parse(name);
	    return createSubcontext(ctx, n);
	  }
	  
	  /**
	   * Create a subcontext including any intermediate contexts.
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which value will be bound
	   * @param name
	   *          the name relative to ctx of the subcontext.
	   * @return The new or existing JNDI subcontext
	   * @throws NamingException
	   *           on any JNDI failure
	   */
	  public static Context createSubcontext(Context ctx, Name name) throws NamingException {
	    Context subctx = ctx;
	    for (int pos = 0; pos < name.size(); pos++) {
	      String ctxName = name.get(pos);
	      try {
	        subctx = (Context) ctx.lookup(ctxName);
	      } catch (NameNotFoundException e) {
	        subctx = ctx.createSubcontext(ctxName);
	      }
	      // The current subctx will be the ctx for the next name component
	      ctx = subctx;
	    }
	    return subctx;
	  }
	  
	  /**
	   * Bind val to name in ctx, and make sure that all intermediate contexts exist
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which value will be bound
	   * @param name
	   *          the name relative to ctx where value will be bound
	   * @param value
	   *          the value to bind.
	   * @throws NamingException
	   *           for any error
	   */
	  public static void bind(Context ctx, String name, Object value) throws NamingException {
	    Name n = ctx.getNameParser("").parse(name);
	    bind(ctx, n, value);
	  }

	  /**
	   * Bind val to name in ctx, and make sure that all intermediate contexts exist
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which value will be bound
	   * @param name
	   *          the name relative to ctx where value will be bound
	   * @param value
	   *          the value to bind.
	   * @throws NamingException
	   *           for any error
	   */
	  public static void bind(Context ctx, Name name, Object value) throws NamingException {
	    int size = name.size();
	    String atom = name.get(size - 1);
	    Context parentCtx = createSubcontext(ctx, name.getPrefix(size - 1));
	    parentCtx.bind(atom, value);
	  }
	  
	  /**
	   * Unbinds a name from ctx, and removes parents if they are empty
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which the name will be unbound
	   * @param name
	   *          The name to unbind
	   * @throws NamingException
	   *           for any error
	   */
	  public static void unbind(Context ctx, String name) throws NamingException {
	    unbind(ctx, ctx.getNameParser("").parse(name));
	  }

	  /**
	   * Unbinds a name from ctx, and removes parents if they are empty
	   * 
	   * @param ctx
	   *          the parent JNDI Context under which the name will be unbound
	   * @param name
	   *          The name to unbind
	   * @throws NamingException
	   *           for any error
	   */
	  public static void unbind(Context ctx, Name name) throws NamingException {
	    ctx.unbind(name); // unbind the end node in the name
	    int sz = name.size();
	    // walk the tree backwards, stopping at the domain
	    while (--sz > 0) {
	      Name pname = name.getPrefix(sz);
	      try {
	        ctx.destroySubcontext(pname);
	      } catch (NamingException e) {
	        System.out.println("Unable to remove context " + pname + e);
	        break;
	      }
	    }
	  }
	  
	  /**
	   * Lookup an object in the default initial context
	   * 
	   * @param name
	   *          the name to lookup
	   * @param clazz
	   *          the expected type
	   * @return the object
	   * @throws Exception
	   *           for any error
	   */
	  public static Object lookup(String name, Class<?> clazz) throws Exception {
	    InitialContext ctx = new InitialContext();
	    try {
	      return lookup(ctx, name, clazz);
	    } finally {
	      ctx.close();
	    }
	  }

	  /**
	   * Lookup an object in the default initial context
	   * 
	   * @param name
	   *          the name to lookup
	   * @param clazz
	   *          the expected type
	   * @return the object
	   * @throws Exception
	   *           for any error
	   */
	  public static Object lookup(Name name, Class<?> clazz) throws Exception {
	    InitialContext ctx = new InitialContext();
	    try {
	      return lookup(ctx, name, clazz);
	    } finally {
	      ctx.close();
	    }
	  }

	  /**
	   * Lookup an object in the given context
	   * 
	   * @param context
	   *          the context
	   * @param name
	   *          the name to lookup
	   * @param clazz
	   *          the expected type
	   * @return the object
	   * @throws Exception
	   *           for any error
	   */
	  public static Object lookup(Context context, String name, Class clazz) throws Exception {
	    Object result = context.lookup(name);
	    checkObject(context, name, result, clazz);
	    return result;
	  }

	  /**
	   * Lookup an object in the given context
	   * 
	   * @param context
	   *          the context
	   * @param name
	   *          the name to lookup
	   * @param clazz
	   *          the expected type
	   * @return the object
	   * @throws Exception
	   *           for any error
	   */
	  public static Object lookup(Context context, Name name, Class clazz) throws Exception {
	    Object result = context.lookup(name);
	    checkObject(context, name.toString(), result, clazz);
	    return result;
	  }
	  
	  /**
	   * Checks an object implements the given class
	   * 
	   * @param context
	   *          the context
	   * @param name
	   *          the name to lookup
	   * @param object
	   *          the object
	   * @param clazz
	   *          the expected type
	   * @throws Exception
	   *           for any error
	   */
	  protected static void checkObject(Context context, String name, Object object, Class clazz)
	      throws Exception {
	    Class objectClass = object.getClass();
	    if (clazz.isAssignableFrom(objectClass) == false) {
	      StringBuffer buffer = new StringBuffer(100);
	      buffer.append("Object at '").append(name);
	      buffer.append("' in context ").append(context.getEnvironment());
	      buffer.append(" is not an instance of ");
	      appendClassInfo(buffer, clazz);
	      buffer.append(" object class is ");
	      appendClassInfo(buffer, object.getClass());
	      throw new ClassCastException(buffer.toString());
	    }
	  }
	  
	  /**
	   * Append Class Info
	   * 
	   * @param buffer
	   *          the buffer to append to
	   * @param clazz
	   *          the class to describe
	   */
	  protected static void appendClassInfo(StringBuffer buffer, Class clazz) {
	    buffer.append("[class=").append(clazz.getName());
	    buffer.append(" classloader=").append(clazz.getClassLoader());
	    buffer.append(" interfaces={");
	    Class[] interfaces = clazz.getInterfaces();
	    for (int i = 0; i < interfaces.length; ++i) {
	      if (i > 0)
	        buffer.append(", ");
	      buffer.append("interface=").append(interfaces[i].getName());
	      buffer.append(" classloader=").append(interfaces[i].getClassLoader());
	    }
	    buffer.append("}]");
	  }
	  
	  public static void deleteSubContext(Context context, String name) throws NamingException{
		  context.destroySubcontext(name);
	  }
}
