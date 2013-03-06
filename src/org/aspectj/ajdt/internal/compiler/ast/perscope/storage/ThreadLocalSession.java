package org.aspectj.ajdt.internal.compiler.ast.perscope.storage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ThreadLocalSession {
	private static ThreadLocal<ThreadLocalSession> threadLocal = new ThreadLocal<ThreadLocalSession>();
	private HttpServletRequest request;	 
	private static ThreadLocalSession getThreadLocal() {
		if (threadLocal.get()==null) {
			threadLocal.set(new ThreadLocalSession());
		}
		return threadLocal.get();
	}
	public static HttpServletRequest getRequest() {
		return getThreadLocal().request;
	}
	public static void setRequest(HttpServletRequest request) {
		getThreadLocal().request = request;
	}
	
	public static HttpSession getSession() {
		HttpSession answer = null;
		if(getThreadLocal().request != null) {
			answer = getThreadLocal().request.getSession();
		}
		return answer;
	}
	
	public static void clean() {
		setRequest(null);
		threadLocal.set(null);
		threadLocal.remove();
    }
	


}
